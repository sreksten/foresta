package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il gioco ha finito e chiede alla piattaforma di terminare: o perché il giocatore ha confermato di voler uscire
 * (codice 0), o perché è successo un errore da cui non si può ripartire, come le grammatiche dei testi mancanti (codice
 * diverso da 0, con il motivo).
 * <p>
 * Il motore non chiude mai il processo: come si termina lo decide la piattaforma, iscrivendosi a questo evento. Sul
 * desktop {@code Main} chiama {@code System.exit(codice)}; su Android si chiude l'attività ({@code finish()}), perché un
 * processo che si ammazza da solo salta il ciclo di vita dell'app; su iOS un'app di norma non si chiude da sola, quindi per
 * un'uscita normale si torna alla schermata iniziale e per un errore fatale si mostra il motivo.
 *
 * @author Stefano Reksten
 */
public class NotificaUscitaDalGioco extends EventoBase {

    private final int codice;
    private final String motivo;

    /**
     * @param codice 0 per un'uscita voluta dal giocatore, altrimenti il codice dell'errore (come un codice d'uscita di un
     *               processo: chi lancia il gioco da uno script vede se è andata male)
     * @param motivo che cosa è andato storto, o null per un'uscita normale
     */
    public NotificaUscitaDalGioco(int codice, String motivo) {
        super(TipoEvento.NOTIFICA_USCITA_DAL_GIOCO);
        this.codice = codice;
        this.motivo = motivo;
    }

    public int getCodice() {
        return codice;
    }

    public String getMotivo() {
        return motivo;
    }

    /**
     * Se l'uscita è dovuta a un errore da cui non si può ripartire.
     */
    public boolean isErrore() {
        return codice != 0;
    }

}
