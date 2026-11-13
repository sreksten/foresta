package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Notifica un cambiamento nel totale delle pozioni di salute disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaPozioniSalute extends NotificaVariazioneDisponibilita {

    public NotificaVariazioneDisponibilitaPozioniSalute(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_SALUTE, valorePrecedente, nuovoValore);
    }
}
