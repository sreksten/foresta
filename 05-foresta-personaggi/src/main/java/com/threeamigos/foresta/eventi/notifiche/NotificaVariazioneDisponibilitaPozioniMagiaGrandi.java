package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Notifica un cambiamento nel totale delle pozioni di magia grandi disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaPozioniMagiaGrandi extends NotificaVariazioneDisponibilita {

    public NotificaVariazioneDisponibilitaPozioniMagiaGrandi(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_MAGIA_GRANDI, valorePrecedente, nuovoValore);
    }
}
