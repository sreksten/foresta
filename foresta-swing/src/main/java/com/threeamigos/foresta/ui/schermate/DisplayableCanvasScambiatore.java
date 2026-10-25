package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.barra.Cartiglio;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
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
public abstract class DisplayableCanvasScambiatore implements Finestra {

    protected static final int SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI = 20;

    /**
     * Fra una riga e l'altra del nome del negoziante, e fra il nome e il mestiere scritto in piccolo sotto
     */
    public static final int INTERLINEA_DEL_NOME = 2;
    public static final int SPAZIO_FRA_NOME_E_MESTIERE = 4;

    /**
     * Larghezza della cornice inventario
     */
    protected static final int corniceInventarioWidth = ImageCache.corniceInventario.getWidth(null);

    /**
     * La dimensione interna del bordo della cornice inventario
     */
    protected static final int DIMENSIONE_BORDO_INTERNO = 16;

    protected static final int ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO = ImageCache.corniceInventario.getHeight()
            - 2 * (DIMENSIONE_BORDO_INTERNO + ImageCache.SPACING);

    protected static final int LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO = ImageCache.corniceInventario.getWidth()
            - 2 * (DIMENSIONE_BORDO_INTERNO + ImageCache.SPACING);

    /**
     * Pixel di scorrimento per ogni scatto della rotella
     */
    protected static final int PASSO_SCORRIMENTO = 2;

    /**
     * Usiamo l'immagine del Ladro come riferimento per disegnare il personaggio centrale della finestra di scambio
     */
    protected static final int ALTEZZA_LADRO = MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.LADRO).getHeight(null);

    /**
     * Lo stato della partita, in sola lettura: monete, prezzi, personaggi e inventario del gruppo
     */
    protected final VistaPartita vistaPartita;

    /**
     * Larghezza globale della finestra di scambio
     */
    protected final int width;
    /**
     * Altezza globale della finestra di scambio
     */
    protected final int height;

    /**
     * Il font usato per disegnare il testo nella finestra di scambio
     */
    public final DoomdarkFont font = DoomdarkFontMedium.getInstance();
    /**
     * L'altezza del font usato per disegnare il testo nella finestra di scambio
     */
    public final int fontHeight = font.getHeight();
    /**
     * Il font piccolo usato per disegnare le descrizioni dei nodi nell'albero degli oggetti
     */
    public final DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();

    /**
     * Coordinata X iniziale della parte sinistra
     */
    protected final int xMinimaZonaSinistra;
    /**
     * Coordinata X finale della parte sinistra
     */
    protected final int xMassimaZonaSinistra;
    /**
     * Coordinata X iniziale della parte centrale
     */
    public final int xMinimaZonaCentrale;
    /**
     * Coordinata X finale della parte centrale
     */
    public final int xMassimaZonaCentrale;
    /**
     * Coordinata X iniziale della parte destra
     */
    protected final int xMinimaZonaDestra;
    /**
     * Coordinata X finale della parte destra
     */
    protected final int xMassimaZonaDestra;

    /**
     * Scroll verso l'alto o il basso per ciascuna zona
     */
    protected int offsetYZonaSinistra = 0;
    protected int offsetYZonaCentrale = 0;
    protected int offsetYZonaDestra = 0;

    /**
     * Posizione del mouse, per evidenziare in bianco l'artefatto sotto il cursore.
     * -1 significa "cursore fuori dalla finestra".
     */
    protected int mouseX = -1;
    protected int mouseY = -1;

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
    public void apri() {
        offsetYZonaSinistra = 0;
        offsetYZonaCentrale = 0;
        offsetYZonaDestra = 0;
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
     * Quota (in coordinate della finestra) a cui le sottoclassi disegnano la riga "Monete"
     * in disegnaColonnaPersonaggio, usata per ancorare gli sprite locali a quella riga.
     */
    public int yRigaMonete() {
        return 3 * SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + altezzaIntestazioneCentrale() + ALTEZZA_LADRO;
    }

    /**
     * Il mestiere del negoziante al centro della schermata (armaiolo, alchimista...): serve a cercarne il nome. Null se
     * al centro c'è un altro personaggio, che si intitola come prima.
     */
    public TipoPersonaggio getMestiereAlCentro() {
        return null;
    }

    /**
     * Il mestiere come si scrive sotto il nome del negoziante ("Armaiolo"), o al posto del nome se non c'è.
     */
    public String getEtichettaMestiere() {
        return null;
    }

    /**
     * Il nome del negoziante spezzato in righe che stiano fra le due cornici; vuota se non c'è un negoziante o un nome.
     */
    private List<String> righeDelNomeAlCentro() {
        TipoPersonaggio mestiere = getMestiereAlCentro();
        String nome = mestiere == null ? null : vistaPartita.getNomeDelPersonaggioDiCitta(mestiere);
        if (nome == null) {
            return new ArrayList<>();
        }
        return FontTool.split(font, nome, xMassimaZonaCentrale - xMinimaZonaCentrale);
    }

    /**
     * L'altezza dell'intestazione della colonna centrale: una riga per chi non è un negoziante, per il negoziante il nome
     * su una o più righe e sotto il mestiere in carattere piccolo.
     */
    public int altezzaIntestazioneCentrale() {
        List<String> righe = righeDelNomeAlCentro();
        if (righe.isEmpty()) {
            return fontHeight;
        }
        return righe.size() * (fontHeight + INTERLINEA_DEL_NOME) - INTERLINEA_DEL_NOME + SPAZIO_FRA_NOME_E_MESTIERE + fontSmall.getHeight();
    }

    /**
     * Disegna in alto nella colonna centrale l'intestazione del negoziante: il nome, centrato e spezzato su più righe se
     * non sta fra le cornici, e sotto il mestiere in carattere piccolo. Senza nome c'è solo il mestiere, come prima.
     *
     * @return la quota sotto l'intestazione
     */
    public int disegnaIntestazioneCentrale(Graphics2D graphics, int y, DoomdarkColorModel.Color colore) {
        List<String> righe = righeDelNomeAlCentro();
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
        this.vistaPartita = vistaPartita;
        // Imposta la dimensione della finestra e calcola l'ampiezza delle tre colonne sinistra, centrale, destra
        this.width = width;
        this.height = height;

        xMinimaZonaSinistra = SPACING + DIMENSIONE_BORDO_INTERNO;
        xMassimaZonaSinistra = xMinimaZonaSinistra + corniceInventarioWidth - DIMENSIONE_BORDO_INTERNO;

        xMinimaZonaCentrale = SPACING + corniceInventarioWidth + SPACING;
        xMassimaZonaCentrale = width - SPACING - corniceInventarioWidth - SPACING;

        xMinimaZonaDestra = width - SPACING - corniceInventarioWidth + DIMENSIONE_BORDO_INTERNO;
        xMassimaZonaDestra = width - SPACING - DIMENSIONE_BORDO_INTERNO;
    }

    /**
     * Restituisce le coordinate alle quali disegnare il fumetto
     */
    public CoordinateFumetto getCoordinateFumetto() {
        // L'intestazione cambia con il nome del negoziante (più righe se è lungo), quindi le coordinate non si ricordano
        Image ladro = MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.LADRO);
        int larghezzaImmagine = ladro.getWidth(null);
        int altezzaImmagine = ladro.getHeight(null);
        int yIntestazione = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + altezzaIntestazioneCentrale() + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        return new CoordinateFumetto(
                width / 2 + larghezzaImmagine + SPACING,
                yIntestazione + altezzaImmagine / 2,
                width / 2 + MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.ARMAIOLO).getWidth() / 3,
                yIntestazione + altezzaImmagine / 3
        );
    }

    /**
     * Disegna le due cornici cornice dell'inventario
     */
    protected void disegnaInventario(Graphics2D graphics) {
        graphics.drawImage(ImageCache.corniceInventario, SPACING, SPACING, null);
        graphics.drawImage(ImageCache.corniceInventario, width - SPACING - corniceInventarioWidth, SPACING, null);
        disegnaIntestazioniInventario(graphics);
    }

    /**
     * Demandato alle sottoclassi che sanno cosa rappresentano i due rettangoli e che chiamano la impl
     * passando le due etichette
     */
    public abstract void disegnaIntestazioniInventario(Graphics2D graphics);

    /**
     * Disegna le intestazioni dell'inventario
     * @param graphics il graphics2D su cui disegnare
     * @param intestazioneSinistra l'intestazione sinistra del riquadro oggetti presenti
     * @param intestazioneDestra l'intestazione destra del riquadro oggetti presenti
     */
    protected void disegnaIntestazioniInventarioImpl(Graphics2D graphics, String intestazioneSinistra, String intestazioneDestra) {
        Image image = ImageCache.get(intestazioneSinistra, DoomdarkColorModel.Color.BLACK);
        int x = SPACING + corniceInventarioWidth / 2 - image.getWidth(null) / 2;
        int y = SPACING;
        graphics.drawImage(image, x + 2, y + 2, null);
        image = ImageCache.get(intestazioneSinistra, DoomdarkColorModel.Color.LIGHT_GRAY);
        graphics.drawImage(image, x, y, null);

        image = ImageCache.get(intestazioneDestra, DoomdarkColorModel.Color.BLACK);
        x = width - SPACING - corniceInventarioWidth / 2 - image.getWidth(null) / 2;
        graphics.drawImage(image, x + 2, y + 2, null);
        image = ImageCache.get(intestazioneDestra, DoomdarkColorModel.Color.LIGHT_GRAY);
        graphics.drawImage(image, x, y, null);
    }

    /**
     * Se il punto (x, y) della finestra cade dentro l'elenco che comincia in boxX (la parte sinistra o la destra)
     */
    protected boolean dentroElenco(int boxX, int x, int y) {
        int xInterno = x - (boxX + SPACING);
        int yInterno = y - (DIMENSIONE_BORDO_INTERNO + 2 * SPACING);
        return xInterno >= 0 && xInterno < LARGHEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO
                && yInterno >= 0 && yInterno < ALTEZZA_DISPONIBILE_IN_RIQUADRO_INVENTARIO;
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

    @Override
    public boolean gestisceDoppioClick() {
        return true;
    }

    @Override
    public void processaMovimento(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    @Override
    public void processaUscita(int x, int y) {
        mouseX = -1;
        mouseY = -1;
    }

    @Override
    public int passiPerScattoDellaRotella() {
    	return PASSI_PER_SCATTO_DELLE_LISTE;
    }

    @Override
    public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
        if (movimentoRotella == MovimentoRotella.SU) {
            if (x >= xMinimaZonaSinistra && x < xMassimaZonaSinistra) {
                offsetYZonaSinistra = Math.max(0, offsetYZonaSinistra - numeroRotazioni * PASSO_SCORRIMENTO);
            } else if (x >= xMinimaZonaCentrale && x < xMassimaZonaCentrale) {
                offsetYZonaCentrale = Math.max(0, offsetYZonaCentrale - numeroRotazioni * PASSO_SCORRIMENTO);
            } else if (x >= xMinimaZonaDestra && x < xMassimaZonaDestra) {
                offsetYZonaDestra = Math.max(0, offsetYZonaDestra - numeroRotazioni * PASSO_SCORRIMENTO);
            }
        } else if (movimentoRotella == MovimentoRotella.GIU) {
            if (x >= xMinimaZonaSinistra && x < xMassimaZonaSinistra) {
                offsetYZonaSinistra += numeroRotazioni * PASSO_SCORRIMENTO;
            } else if (x >= xMinimaZonaCentrale && x < xMassimaZonaCentrale) {
                offsetYZonaCentrale += numeroRotazioni * PASSO_SCORRIMENTO;
            } else if (x >= xMinimaZonaDestra && x < xMassimaZonaDestra) {
                offsetYZonaDestra += numeroRotazioni * PASSO_SCORRIMENTO;
            }
        }
    }

}
