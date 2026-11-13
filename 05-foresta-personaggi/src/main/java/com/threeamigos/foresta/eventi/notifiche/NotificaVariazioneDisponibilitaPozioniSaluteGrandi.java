package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Notifica un cambiamento nel totale delle pozioni di salute grandi disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaPozioniSaluteGrandi extends NotificaVariazioneDisponibilita {

    public NotificaVariazioneDisponibilitaPozioniSaluteGrandi(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_SALUTE_GRANDI, valorePrecedente, nuovoValore);
    }
}
