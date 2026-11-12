package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.barra.Cartiglio;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.componenti.CorniceTagliata;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.CoordinateFumetto;
import com.threeamigos.foresta.ui.sprite.SpriteInterface;
import com.threeamigos.foresta.ui.testo.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * La base per disegnare una interazione tra due elenchi di oggetti scambiabili:
 * <ul>
 *     <li>Personaggio e inventario del gruppo</li>
 *     <li>Inventario del gruppo e Armaiolo (che vende Artefatti)</li>
 *     <li>Inventario del gruppo e Alchimista (che vende Consumabili)</li>
 * </ul>
 * L'idea generale è che abbiamo due elenchi di oggetti scambiabili, uno a sinistra e uno a destra;
 * sulla colonna centrale un personaggio che può essere o il commerciante o il Personaggio attivo.
 * <p>
 * DisplayableCanvasScambiatoreArtefatti serve per le interazioni Personaggio - GruppoGiocatore - armaiolo.<br>
 * DisplayableCanvasScambiatoreConsumabili serve per le interazioni GruppoGiocatore - alchimista.
 *
 * @author Stefano Reksten
 */
public abstract class DisplayableCanvasScambiatore extends SchermataDiScambio {

    /**
     * Larghezza dell'immagine della cornice inventario: quella disegnata può essere minore (vedi getLarghezzaCornice)
     */
    protected static final int corniceInventarioWidth = ImageCache.corniceInventario.getWidth(null);

    /**
     * Usiamo l'immagine del Ladro come riferimento per disegnare il personaggio centrale della finestra di scambio
     */
    protected static final int ALTEZZA_LADRO = MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.LADRO).getHeight(null);

    /**
     * Sprite temporanei (es. monete spese/acquisite, incantesimo acquistato) disegnati
     * localmente da questa finestra, sempre attiva quando è in primo piano. A differenza
     * degli {@code SpriteATempo} pubblicati via {@code InternoCreazioneSpriteATempo}, non
     * dipendono dal ciclo di disegno di {@code DisplayableCanvas.inGioco()}.
     */
    private final List<SpriteInterface> spriteLocali = new ArrayList<>();

    /**
     * Chiamato quando la schermata si apre: le liste ripartono dall'alto e gli sprite della volta scorsa, anche se non finiti, non ricompaiono.
     */
    @Override
    public void apri() {
        super.apri();
        spriteLocali.clear();
    }

    /**
     * Aggiunge uno sprite che resta confinato in questa schermata.
     */
    protected void aggiungiSpriteLocale(SpriteInterface sprite) {
        spriteLocali.add(sprite);
    }

    /**
     * Anima gli sprite locali e toglie quelli esauriti.
     */
    protected void disegnaSpriteLocali(Graphics2D graphics) {
        List<SpriteInterface> copia = new ArrayList<>(spriteLocali);
        for (SpriteInterface sprite : copia) {
            sprite.anima(graphics);
            if (!sprite.isAttivo()) {
                spriteLocali.remove(sprite);
            }
        }
    }

    /**
     * Disegna in alto nella colonna centrale l'intestazione del negoziante: il nome, centrato e spezzato su più righe se
     * non sta fra le cornici, e sotto il mestiere in carattere piccolo. Senza nome c'è solo il mestiere, come prima.
     *
     * @return la quota sotto l'intestazione
     */
    public int disegnaIntestazioneCentrale(Graphics2D graphics, int y, DoomdarkColorModel.Color colore) {
        List<String> righe = getRigheDelNomeAlCentro();
        if (righe.isEmpty()) {
            Image mestiere = ImageCache.get(getEtichettaMestiere(), colore);
            graphics.drawImage(mestiere, (width - mestiere.getWidth(null)) / 2, y, null);
            return y + fontHeight;
        }
        for (String riga : righe) {
            Image nome = ImageCache.get(riga, font, colore);
            graphics.drawImage(nome, (width - nome.getWidth(null)) / 2, y, null);
            y += fontHeight + INTERLINEA_DEL_NOME;
        }
        y += SPAZIO_FRA_NOME_E_MESTIERE - INTERLINEA_DEL_NOME;
        Image mestiere = ImageCache.get(getEtichettaMestiere(), fontSmall, colore);
        graphics.drawImage(mestiere, (width - mestiere.getWidth(null)) / 2, y, null);
        return y + fontSmall.getHeight();
    }

    protected DisplayableCanvasScambiatore(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita, corniceInventarioWidth, ImageCache.corniceInventario.getHeight(), new MisureDelleImmaginiAwt());
    }

    /**
     * Disegna le due cornici cornice dell'inventario
     */
    protected void disegnaInventario(Graphics2D graphics) {
        disegnaCornice(graphics, SPACING);
        disegnaCornice(graphics, width - SPACING - getLarghezzaCornice());
        disegnaIntestazioniInventario(graphics);
    }

    /**
     * Una cornice dell'inventario: intera, o tagliata nel mezzo se lo schermo è più basso (la parte di sopra e quella di sotto) o se la colonna centrale
     * ha bisogno di spazio (la parte di sinistra e quella di destra): fino a quattro pezzi, vedi CorniceTagliata
     */
    private void disegnaCornice(Graphics2D graphics, int x) {
        double zoom = LivelloDiZoom.valore();
        for (CorniceTagliata.Fascia colonna : getColonneDellaCornice()) {
            int xSorgente = (int) Math.round(colonna.getYSorgente() * zoom);
            int xDestinazione = x + colonna.getYDestinazione();
            int larghezza = colonna.getAltezzaDestinazione();
            for (CorniceTagliata.Fascia fascia : getFasceDellaCornice()) {
                int ySorgente = (int) Math.round(fascia.getYSorgente() * zoom);
                int y = SPACING + fascia.getYDestinazione();
                graphics.drawImage(ImageCache.corniceInventario, xDestinazione, y, xDestinazione + larghezza, y + fascia.getAltezzaDestinazione(),
                        xSorgente, ySorgente, xSorgente + larghezza, ySorgente + fascia.getAltezzaDestinazione(), null);
            }
        }
    }

    /**
     * Demandato alle sottoclassi che sanno cosa rappresentano i due rettangoli e che chiamano la impl
     * passando le due etichette
     */
    public abstract void disegnaIntestazioniInventario(Graphics2D graphics);

    /**
     * Disegna le intestazioni dell'inventario
     * @param graphics il graphics2D su cui disegnare
     * @param chiaveSinistra la chiave del titolo della cornice di sinistra (corto se quello lungo non ci sta: vedi titoloDellaCornice)
     * @param chiaveDestra la chiave del titolo della cornice di destra
     */
    protected void disegnaIntestazioniInventarioImpl(Graphics2D graphics, String chiaveSinistra, String chiaveDestra) {
        String intestazioneSinistra = titoloDellaCornice(chiaveSinistra);
        String intestazioneDestra = titoloDellaCornice(chiaveDestra);
        Image image = ImageCache.get(intestazioneSinistra, DoomdarkColorModel.Color.BLACK);
        int x = SPACING + getLarghezzaCornice() / 2 - image.getWidth(null) / 2;
        int y = SPACING;
        graphics.drawImage(image, x + 2, y + 2, null);
        image = ImageCache.get(intestazioneSinistra, DoomdarkColorModel.Color.LIGHT_GRAY);
        graphics.drawImage(image, x, y, null);

        image = ImageCache.get(intestazioneDestra, DoomdarkColorModel.Color.BLACK);
        x = width - SPACING - getLarghezzaCornice() / 2 - image.getWidth(null) / 2;
        graphics.drawImage(image, x + 2, y + 2, null);
        image = ImageCache.get(intestazioneDestra, DoomdarkColorModel.Color.LIGHT_GRAY);
        graphics.drawImage(image, x, y, null);
    }

    /**
     * Ad aiuto acceso (vedi ModelloDati.isAiutoAbilitato), i cartigli con quelle righe accanto al mouse, dentro la
     * finestra: va chiamato per ultimo, sopra il resto della schermata
     */
    protected void disegnaAiuto(Graphics2D graphics, List<String> righe) {
        if (vistaPartita.isAiutoAbilitato() && mouseX >= 0) {
            Cartiglio.disegnaAccantoAlMouse(graphics, righe, mouseX, mouseY, width, height);
        }
    }

    /**
     * Il metodo che disegna la colonna centrale con il personaggio adatto (Personaggio, armaiolo, alchimista)
     */
    public abstract void disegnaColonnaPersonaggio(Graphics2D graphics);

}
