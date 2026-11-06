package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaAvvisoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.artefatti.VistaBancoDiLavoro;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.awt.*;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;
import com.threeamigos.foresta.testi.Lingue;

/**
 * La bottega dell'incantatore: a sinistra l'inventario del gruppo, a destra il banco di lavoro.
 * Nella colonna centrale le monete, il costo della fusione e i posti dell'artefatto sul banco.
 */
public class DisplayableCanvasIncantatore extends DisplayableCanvasScambiatoreArtefatti {

    // Lo stesso scambio della classe base, con quel che serve per mostrare la fusione
    private VistaBancoDiLavoro banco;

    public DisplayableCanvasIncantatore(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita);
        BusEventi.iscriviti(NotificaRifiutoIncantatura.class, this::onEventoRifiutoIncantatura);
        BusEventi.iscriviti(NotificaAvvisoIncantatura.class,
                evento -> BusEventi.pubblica(new InternoNotificaViaFumettoATempo(evento.getFrase(), getCoordinateFumetto())));
        BusEventi.iscriviti(NotificaApprovazioneIncantatura.class, this::onEventoApprovazioneIncantatura);
    }

    /**
     * Imposta il banco di lavoro (e lo scambio) che la schermata mostra.
     */
    public void impostaBanco(VistaBancoDiLavoro banco) {
        impostaScambio(banco);
        this.banco = banco;
    }

    private void onEventoRifiutoIncantatura(NotificaRifiutoIncantatura evento) {
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(evento.getMotivo().getFrase(), getCoordinateFumetto()));
    }

    private void onEventoApprovazioneIncantatura(NotificaApprovazioneIncantatura evento) {
        String nome = evento.getArtefatto().getNomeBreve();
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(TestiDellaUI.get("INCANTATORE_FATTO", Lingue.corrente().inizialeMaiuscola(nome)), getCoordinateFumetto()));
    }

    @Override
    protected String aiutoDoppioClickSinistra() {
        return TestiDellaUI.get("AIUTO_DOPPIO_CLICK_SUL_BANCO");
    }

    @Override
    protected String aiutoDoppioClickDestra() {
        return TestiDellaUI.get("AIUTO_DOPPIO_CLICK_DAL_BANCO");
    }

    @Override
    public void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, TestiDellaUI.get("INVENTARIO_GRUPPO"), TestiDellaUI.get("BANCO_DI_LAVORO"));
    }

    @Override
    public TipoPersonaggio getMestiereAlCentro() {
        return TipoPersonaggio.INCANTATORE;
    }

    @Override
    public String getEtichettaMestiere() {
        return TestiDellaUI.get("MESTIERE_INCANTATORE");
    }

    private final DisegnoDiContenutoAwt disegno = new DisegnoDiContenutoAwt();

    /**
     * Disegna la colonna centrale: l'incantatore, le monete e le righe della fusione (vedi RigheDellIncantatore, nel modulo comune).
     */
    public void disegnaColonnaPersonaggio(Graphics2D graphics) {
        disegno.disegna(graphics, colonnaDelNegoziante(getMestiereAlCentro(), RigheDellIncantatore.righe(banco)), 0, 0);
    }

    // I colori della fusione (effetti che escono e che entrano, posti liberi), nel modulo comune
    private final ColoriDellIncantatore colori = new ColoriDellIncantatore(() -> banco);

    @Override
    protected DoomdarkColorModel.Color coloreEffetto(VistaArtefatto artefatto, Object effetto, boolean parteAttiva,
                                                     DoomdarkColorModel.Color predefinito) {
        return colori.coloreEffetto(artefatto, effetto, parteAttiva, predefinito);
    }

    @Override
    protected DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
        return colori.coloreLivello(artefatto, parteAttiva);
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
