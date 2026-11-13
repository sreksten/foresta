package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Notifica un cambiamento nel totale delle panacee disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaPozioniPanacea extends NotificaVariazioneDisponibilita {

    public NotificaVariazioneDisponibilitaPozioniPanacea(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_PANACEA, valorePrecedente, nuovoValore);
    }
}
