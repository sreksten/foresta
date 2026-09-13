package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoFumetto;
import com.threeamigos.foresta.eventi.EventoRifiutoAcquistoArtefatto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.awt.*;

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
 * DisplayableCanvasScambiatoreArtefatti serve per le interazioni Personaggio - GruppoGiocatore - armaiolo.<br/>
 * DisplayableCanvasScambiatoreConsumabili serve per le interazioni GruppoGiocatore - alchimista.
 *
 * @author Stefano Reksten
 */
abstract class DisplayableCanvasScambiatore implements Finestra {

    protected static final int SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI = 20;

    /**
     * Le coordinate del fumetto sono a comune perché tutte le sottoclassi disegnano un personaggio a metà schermo
     */
    protected static CoordinateFumetto COORDINATE_FUMETTO;

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
     * Usiamo l'immagine del Ladro come riferimento per disegnar eil personaggio centrale della finestra di scambio
     */
    protected static final int ALTEZZA_LADRO = ClassePersonaggioImmagine.getImmagine(ClassePersonaggio.LADRO).getHeight(null);

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
    protected final DoomdarkFont font = DoomdarkFontMedium.getInstance();
    /**
     * L'altezza del font usato per disegnare il testo nella finestra di scambio
     */
    protected final int fontHeight = font.getHeight();
    /**
     * Il font piccolo usato per disegnare le descrizioni dei nodi nell'albero degli oggetti
     */
    protected final DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();

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
    protected final int xMinimaZonaCentrale;
    /**
     * Coordinata X finale della parte centrale
     */
    protected final int xMassimaZonaCentrale;
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

    protected DisplayableCanvasScambiatore(int width, int height) {
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
    CoordinateFumetto getCoordinateFumetto() {
        if (COORDINATE_FUMETTO == null) {
            Image ladro = ClassePersonaggioImmagine.getImmagine(ClassePersonaggio.LADRO);
            int larghezzaImmagine = ladro.getWidth(null);
            int altezzaImmagine = ladro.getHeight(null);

            COORDINATE_FUMETTO = new CoordinateFumetto(
                    width / 2 + larghezzaImmagine + SPACING,
                    SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + altezzaImmagine / 2,
                    width / 2 + ImageCache.armaiolo.getWidth() / 3,
                    SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI + altezzaImmagine / 3
            );
        }
        return COORDINATE_FUMETTO;
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
    abstract void disegnaIntestazioniInventario(Graphics2D graphics);

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
     * Il metodo che disegna la colonna centrale con il personaggio adatto (Personaggio, armaiolo, alchimista)
     */
    abstract void disegnaColonnaPersonaggio(Graphics2D graphics);

    void onEventoRifiutoAcquisto(EventoRifiutoAcquistoArtefatto evento) {
        BusEventi.pubblica(new EventoFumetto("Non hai abbastanza monete per comprare questo oggetto.", getCoordinateFumetto()));
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
