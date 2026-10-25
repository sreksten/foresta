package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoVenditaArtefatto;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * La bottega di un commerciante di artefatti (armaiolo o venditore di pergamene): a sinistra l'inventario
 * del gruppo, a destra il magazzino del negozio. Una sola schermata per tutti i negozi, che cambia nome e
 * immagine secondo il negozio aperto.
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasCommerciante extends DisplayableCanvasScambiatoreArtefatti {

    private TipoNegozio negozio = TipoNegozio.ARMAIOLO;

    public DisplayableCanvasCommerciante(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita);
        BusEventi.iscriviti(NotificaRifiutoVenditaArtefatto.class, this::gestisciEventoRifiutoVenditaArtefatto);
    }

    private void gestisciEventoRifiutoVenditaArtefatto(NotificaRifiutoVenditaArtefatto notificaRifiutoVenditaArtefatto) {
        String frase = negozio == TipoNegozio.VENDITORE_DI_PERGAMENE
                ? "Mi dispiace, io tratto solo pergamene, gemme, monili, gingilli e sigilli."
                : "Mi dispiace, la roba magica la vende il venditore di pergamene.";
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(frase, getCoordinateFumetto()));
    }

    /**
     * Il negoziante dice subito, a bottega aperta, che cosa farà della merce: la smaltisce poi all'uscita dalla città
     * (vedi RegistroArtefatti.smaltisciVenduti), quando la bottega non si vede più.
     */
    @Override
    String fraseDopoLaVendita() {
        return negozio == TipoNegozio.VENDITORE_DI_PERGAMENE
                ? "Ottime pergamene: le rivenderò a qualche mago di passaggio!"
                : "Con il materiale che mi hai fornito, farò altre meravigliose creazioni!";
    }

    /**
     * Imposta il tipo di negozio, che decide il negoziante mostrato.
     */
    public void impostaNegozio(TipoNegozio negozio) {
        this.negozio = negozio;
    }

    @Override
    public TipoPersonaggio getMestiereAlCentro() {
        return negozio == TipoNegozio.VENDITORE_DI_PERGAMENE ? TipoPersonaggio.VENDITORE_DI_PERGAMENE : TipoPersonaggio.ARMAIOLO;
    }

    @Override
    public String getEtichettaMestiere() {
        return negozio.getNome();
    }

    @Override
    protected String aiutoDoppioClickSinistra() {
        return "Doppio click: vendi";
    }

    @Override
    protected String aiutoDoppioClickDestra() {
        return "Doppio click: compra";
    }

    @Override
    public void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, "Inventario gruppo",
                negozio == TipoNegozio.VENDITORE_DI_PERGAMENE ? "Inventario venditore" : "Inventario armaiolo");
    }

    /**
     * Disegna la colonna centrale con il personaggio scelto.
     */
    public void disegnaColonnaPersonaggio(Graphics2D graphics) {

        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        // Nome del negoziante (e sotto il mestiere)
        y = disegnaIntestazioneCentrale(graphics, y, coloreTestata) + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Immagine personaggio
        BufferedImage immaginePersonaggio = negozio == TipoNegozio.VENDITORE_DI_PERGAMENE
                ? MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.VENDITORE_DI_PERGAMENE)
                : MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.ARMAIOLO);

        // Per tenere i personaggi sullo stesso livello (se si passa da un personaggio all'altro)
        // ed evitare sfarfallamenti, scegliamo il ladro come personaggio "base" per calcolare l'altezza a cui disegnare.
        y += ALTEZZA_LADRO;
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth()) / 2, y - immaginePersonaggio.getHeight(), null);
        y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        Image i = ImageCache.get("Monete", coloreTestata);
        graphics.drawImage(i, xMinimaZonaCentrale, y, null);
        i = ImageCache.get(vistaPartita.getCassa().getMonete(), font, coloreTestata);
        graphics.drawImage(i, xMassimaZonaCentrale - i.getWidth(null), y, null);

        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        BufferedImage separatore = ImageCache.separatore;
        graphics.drawImage(separatore, (width - separatore.getWidth()) / 2, y, null);
    }

    @Override
    protected boolean processaClickPersonaggio(int x, int y, Tasto tasto) {
        return false;
    }

    @Override
    protected boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto) {
        return false;
    }

}
