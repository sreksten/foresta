package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Notifica un cambiamento nel totale delle pozioni dell'aumento di livello disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaPozioniAumentoLivello extends NotificaVariazioneDisponibilita {

    public NotificaVariazioneDisponibilitaPozioniAumentoLivello(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_AUMENTO_LIVELLO, valorePrecedente, nuovoValore);
    }
}
