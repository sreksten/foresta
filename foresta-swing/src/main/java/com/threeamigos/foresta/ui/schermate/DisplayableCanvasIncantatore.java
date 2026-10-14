package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
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
import com.threeamigos.foresta.ui.immagini.ClassePersonaggioImmagine;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

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
    public void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, "Inventario gruppo", "Banco di lavoro");
    }

    @Override
    public TipoPersonaggio getMestiereAlCentro() {
        return TipoPersonaggio.INCANTATORE;
    }

    @Override
    public String getEtichettaMestiere() {
        return "Incantatore";
    }

    /**
     * Disegna la colonna centrale con il personaggio scelto.
     */
    public void disegnaColonnaPersonaggio(Graphics2D graphics) {

        int y = SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
        DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

        // Nome dell'incantatore (e sotto il mestiere)
        y = disegnaIntestazioneCentrale(graphics, y, coloreTestata) + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;

        BufferedImage immaginePersonaggio = ClassePersonaggioImmagine.getImmagine(TipoPersonaggio.INCANTATORE);

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
            y = disegnaValore(graphics, "Effetti", banco.getEffettiDopoLaFusione() + "/" + artefatto.get().getEffettiMassimi(), y, coloreTestata);
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
     * Nel banco di lavoro gli effetti dell'artefatto che la fusione scalzerebbe sono rossi, quelli degli ingredienti
     * che entrerebbero verdi.
     */
    @Override
    protected DoomdarkColorModel.Color coloreEffetto(VistaArtefatto artefatto, Object effetto, boolean parteAttiva,
                                                     DoomdarkColorModel.Color predefinito) {
        if (parteAttiva || banco == null) {
            return predefinito;
        }
        if (banco.isEffettoInUscita(effetto)) {
            return DoomdarkColorModel.Color.RED;
        }
        return banco.isEffettoInEntrata(effetto) ? DoomdarkColorModel.Color.GREEN : predefinito;
    }

    /**
     * Negli artefatti incantabili il livello dice se hanno tutti i posti presi (verde) o se ne hanno ancora di
     * liberi (arancione: non sono sfruttati appieno); gli altri (accessori, ingredienti) restano grigi.
     */
    @Override
    protected DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
        if (!artefatto.isIncantabile()) {
            return super.coloreLivello(artefatto, parteAttiva);
        }
        return artefatto.getNumeroEffetti() < artefatto.getEffettiMassimi()
                ? DoomdarkColorModel.Color.ORANGE : DoomdarkColorModel.Color.GREEN;
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
