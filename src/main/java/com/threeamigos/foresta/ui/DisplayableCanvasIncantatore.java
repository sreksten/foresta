package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaAvvisoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.interfacce.VistaArtefatto;
import com.threeamigos.foresta.interfacce.VistaBancoDiLavoro;
import com.threeamigos.foresta.interfacce.VistaPartita;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * La bottega dell'incantatore: a sinistra l'inventario del gruppo, a destra il banco di lavoro.
 * Nella colonna centrale le monete, il costo della fusione e i posti dell'artefatto sul banco.
 */
public class DisplayableCanvasIncantatore extends DisplayableCanvasScambiatoreArtefatti {

    // Lo stesso scambio della classe base, con quel che serve per mostrare la fusione
    private VistaBancoDiLavoro banco;

    DisplayableCanvasIncantatore(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita);
        BusEventi.iscriviti(NotificaRifiutoIncantatura.class, this::onEventoRifiutoIncantatura);
        BusEventi.iscriviti(NotificaAvvisoIncantatura.class,
                evento -> BusEventi.pubblica(new InternoNotificaViaFumettoATempo(evento.getFrase(), getCoordinateFumetto())));
        BusEventi.iscriviti(NotificaApprovazioneIncantatura.class, this::onEventoApprovazioneIncantatura);
    }

    void impostaBanco(VistaBancoDiLavoro banco) {
        impostaScambio(banco);
        this.banco = banco;
    }

    private void onEventoRifiutoIncantatura(NotificaRifiutoIncantatura evento) {
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(evento.getMotivo().getFrase(), getCoordinateFumetto()));
    }

    private void onEventoApprovazioneIncantatura(NotificaApprovazioneIncantatura evento) {
        String nome = evento.getArtefatto().getNomeBreve();
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Ecco fatto! " + nome.substring(0, 1).toUpperCase()
                + nome.substring(1) + " è pronto.", getCoordinateFumetto()));
    }

    @Override
    protected String aiutoDoppioClickSinistra() {
        return "Doppio click: metti sul banco di lavoro";
    }

    @Override
    protected String aiutoDoppioClickDestra() {
        return "Doppio click: rimuovi dal banco di lavoro";
    }

    @Override
    void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, "Inventario gruppo", "Banco di lavoro");
    }

    void disegnaColonnaPersonaggio(Graphics2D graphics) {

        Image doomdark;
        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        doomdark = ImageCache.get("Incantatore", coloreTestata);
        graphics.drawImage(doomdark, (width - doomdark.getWidth(null)) / 2, y, null);
        y += fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        BufferedImage immaginePersonaggio = ImageCache.incantatore;

        // Come per l'armaiolo, il ladro fa da altezza di riferimento per non far sfarfallare l'immagine
        y += ALTEZZA_LADRO;
        graphics.drawImage(immaginePersonaggio, (width - immaginePersonaggio.getWidth()) / 2, y - immaginePersonaggio.getHeight(), null);
        y += SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        y = disegnaValore(graphics, "Monete", String.valueOf(vistaPartita.getGruppoGiocatore().getMonete()), y, coloreTestata);

        boolean conIngredienti = banco.getInventarioParteRemota().stream().anyMatch(a -> a.getTipo().isIngrediente());
        if (conIngredienti) {
            y = disegnaValore(graphics, "Costo fusione", String.valueOf(banco.getCostoFusione()), y, coloreTestata);
        }
        Optional<? extends VistaArtefatto> artefatto = banco.getArtefattoSulBanco();
        if (artefatto.isPresent()) {
            int effetti = artefatto.get().getNumeroEffetti();
            int daAggiungere = banco.getEffettiDaTrasferire();
            y = disegnaValore(graphics, "Effetti", (effetti + daAggiungere) + "/" + artefatto.get().getEffettiMassimi(), y, coloreTestata);
        }

        BufferedImage separatore = ImageCache.separatore;
        graphics.drawImage(separatore, (width - separatore.getWidth()) / 2, y, null);
    }

    private int disegnaValore(Graphics2D graphics, String etichetta, String valore, int y, DoomdarkColorModel.Color colore) {
        Image i = ImageCache.get(etichetta, colore);
        graphics.drawImage(i, xMinimaZonaCentrale, y, null);
        i = ImageCache.get(valore, font, colore);
        graphics.drawImage(i, xMassimaZonaCentrale - i.getWidth(null), y, null);
        return y + fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
    }

    /**
     * Negli artefatti incantabili il livello dice se hanno ancora posti per gli effetti degli ingredienti (verde)
     * o no (rosso); gli altri (accessori, ingredienti) restano grigi.
     */
    @Override
    protected DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
        if (!artefatto.isIncantabile()) {
            return super.coloreLivello(artefatto, parteAttiva);
        }
        return artefatto.getNumeroEffetti() < artefatto.getEffettiMassimi()
                ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
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
