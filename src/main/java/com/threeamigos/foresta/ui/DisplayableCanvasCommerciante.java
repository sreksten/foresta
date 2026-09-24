package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoVenditaArtefatto;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.TipoNegozio;

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

    DisplayableCanvasCommerciante(int width, int height) {
        super(width, height);
        BusEventi.iscriviti(NotificaRifiutoVenditaArtefatto.class, this::gestisciEventoRifiutoVenditaArtefatto);
    }

    private void gestisciEventoRifiutoVenditaArtefatto(NotificaRifiutoVenditaArtefatto notificaRifiutoVenditaArtefatto) {
        String frase = negozio == TipoNegozio.VENDITORE_DI_PERGAMENE
                ? "Mi dispiace, io tratto solo pergamene."
                : "Mi dispiace, le pergamene non le tratto.";
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(frase, getCoordinateFumetto()));
    }

    void impostaNegozio(TipoNegozio negozio) {
        this.negozio = negozio;
    }

    @Override
    void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, "Inventario gruppo",
                negozio == TipoNegozio.VENDITORE_DI_PERGAMENE ? "Inventario venditore" : "Inventario armaiolo");
    }

    void disegnaColonnaPersonaggio(Graphics2D graphics) {

        Image doomdark;
        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        // Nome personaggio
        doomdark = ImageCache.get(negozio.getNome(), coloreTestata);
        graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        // Immagine personaggio
        BufferedImage immaginePersonaggio = negozio == TipoNegozio.VENDITORE_DI_PERGAMENE
                ? ImageCache.venditoreDiPergamene
                : ImageCache.armaiolo;

        // Per tenere i personaggi sullo stesso livello (se si passa da un personaggio all'altro)
        // ed evitare sfarfallamenti, scegliamo il ladro come personaggio "base" per calcolare l'altezza a cui disegnare.
        y += ALTEZZA_LADRO;
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth()) / 2, y - immaginePersonaggio.getHeight(), null);
        y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        Image i = ImageCache.get("Monete", coloreTestata);
        graphics.drawImage(i, xMinimaZonaCentrale, y, null);
        i = DoomdarkTextProducer.getImage(GruppoGiocatore.getIstanza().getMonete(), font, coloreTestata);
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
