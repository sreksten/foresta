package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegoleIncantatura;
import com.threeamigos.foresta.oggetti.Artefatto;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;

/**
 * La bottega dell'incantatore: a sinistra l'inventario del gruppo, a destra il banco di lavoro.
 * Nella colonna centrale le monete, il costo della fusione e i posti dell'artefatto sul banco.
 */
public class DisplayableCanvasIncantatore extends DisplayableCanvasScambiatoreArtefatti {

    DisplayableCanvasIncantatore(int width, int height) {
        super(width, height);
        BusEventi.iscriviti(NotificaRifiutoIncantatura.class, this::onEventoRifiutoIncantatura);
        BusEventi.iscriviti(NotificaApprovazioneIncantatura.class, this::onEventoApprovazioneIncantatura);
    }

    private void onEventoRifiutoIncantatura(NotificaRifiutoIncantatura evento) {
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(evento.getMotivo().getFrase(), getCoordinateFumetto()));
    }

    private void onEventoApprovazioneIncantatura(NotificaApprovazioneIncantatura evento) {
        String nome = evento.getArtefatto().getModelloDati().getNomeBreve();
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo("Ecco fatto! " + nome.substring(0, 1).toUpperCase()
                + nome.substring(1) + " è pronto.", getCoordinateFumetto()));
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

        y = disegnaValore(graphics, "Monete", String.valueOf(GruppoGiocatore.getIstanza().getMonete()), y, coloreTestata);

        Collection<Artefatto> banco = automa.getParteRemota().getInventario();
        boolean conPergamene = banco.stream().anyMatch(RegoleIncantatura::isPergamena);
        if (conPergamene) {
            y = disegnaValore(graphics, "Costo fusione", String.valueOf(GruppoGiocatore.getIstanza().costoFusione(banco)), y, coloreTestata);
        }
        Optional<Artefatto> artefatto = RegoleIncantatura.artefattoSulBanco(banco);
        if (artefatto.isPresent()) {
            int effetti = RegoleIncantatura.effetti(artefatto.get());
            int daAggiungere = banco.stream().filter(RegoleIncantatura::isPergamena).mapToInt(RegoleIncantatura::effetti).sum();
            y = disegnaValore(graphics, "Effetti", (effetti + daAggiungere) + "/" + artefatto.get().getEffettiMassimi(), y, coloreTestata);
        }

        BufferedImage separatore = ImageCache.separatore;
        graphics.drawImage(separatore, (width - separatore.getWidth()) / 2, y, null);
    }

    private int disegnaValore(Graphics2D graphics, String etichetta, String valore, int y, DoomdarkColorModel.Color colore) {
        Image i = ImageCache.get(etichetta, colore);
        graphics.drawImage(i, xMinimaZonaCentrale, y, null);
        i = DoomdarkTextProducer.getImage(valore, font, colore);
        graphics.drawImage(i, xMassimaZonaCentrale - i.getWidth(null), y, null);
        return y + fontHeight + SPAZIATURA_TRA_PERSONAGGIO_E_ATTRIBUTI;
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
