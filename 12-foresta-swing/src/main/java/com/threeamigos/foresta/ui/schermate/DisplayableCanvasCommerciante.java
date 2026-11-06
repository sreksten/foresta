package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoBottega;
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
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

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
                ? TestiDellaUI.get("COMMERCIANTE_RIFIUTO_PERGAMENE")
                : TestiDellaUI.get("COMMERCIANTE_RIFIUTO_ARMAIOLO");
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(frase, getCoordinateFumetto()));
    }

    /**
     * Il negoziante dice subito, a bottega aperta, che cosa farà della merce: la smaltisce poi all'uscita dalla città
     * (vedi RegistroArtefatti.smaltisciVenduti), quando la bottega non si vede più.
     */
    @Override
    String fraseDopoLaVendita() {
        return negozio == TipoNegozio.VENDITORE_DI_PERGAMENE
                ? TestiDellaUI.get("COMMERCIANTE_DOPO_VENDITA_PERGAMENE")
                : TestiDellaUI.get("COMMERCIANTE_DOPO_VENDITA_ARMAIOLO");
    }

    /**
     * Imposta il tipo di negozio, che decide il negoziante mostrato.
     */
    public void impostaNegozio(TipoNegozio negozio) {
        this.negozio = negozio;
    }

    @Override
    public TipoPersonaggio getMestiereAlCentro() {
        return TipoBottega.del(negozio).getMestiere();
    }

    @Override
    public String getEtichettaMestiere() {
        return TipoBottega.del(negozio).getEtichettaDelMestiere();
    }

    @Override
    protected String aiutoDoppioClickSinistra() {
        return TipoBottega.del(negozio).getAiutoDoppioClickSinistra();
    }

    @Override
    protected String aiutoDoppioClickDestra() {
        return TipoBottega.del(negozio).getAiutoDoppioClickDestra();
    }

    @Override
    public void disegnaIntestazioniInventario(Graphics2D graphics) {
        TipoBottega bottega = TipoBottega.del(negozio);
        disegnaIntestazioniInventarioImpl(graphics, bottega.getIntestazioneSinistra(), bottega.getIntestazioneDestra());
    }

    private final DisegnoDiContenutoAwt disegno = new DisegnoDiContenutoAwt();

    /**
     * Disegna la colonna centrale con il personaggio scelto (vedi SchermataDiScambio.colonnaDelNegoziante, nel modulo comune).
     */
    public void disegnaColonnaPersonaggio(Graphics2D graphics) {
        disegno.disegna(graphics, colonnaDelNegoziante(getMestiereAlCentro()), 0, 0);
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
