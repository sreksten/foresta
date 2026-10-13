package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Notifica un cambiamento nel totale dei punti fatti durante il corso del gioco. Molto old school.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazionePunteggio extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    public NotificaVariazionePunteggio(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_PUNTEGGIO);
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
