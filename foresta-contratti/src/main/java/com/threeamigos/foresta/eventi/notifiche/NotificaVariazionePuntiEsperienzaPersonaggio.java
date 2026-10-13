package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Notifica un cambiamento nel totale dei punti esperienza del personaggio principale.
 * Agisce anche sul livello del mondo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazionePuntiEsperienzaPersonaggio extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public NotificaVariazionePuntiEsperienzaPersonaggio(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_PUNTI_ESPERIENZA_PERSONAGGIO);
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    public int getValorePrecedente() {
        return valorePrecedente;
    }

    public int getNuovoValore() {
        return nuovoValore;
    }
}
