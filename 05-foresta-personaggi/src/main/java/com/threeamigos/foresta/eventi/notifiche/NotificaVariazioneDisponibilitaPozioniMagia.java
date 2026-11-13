package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Notifica un cambiamento nel totale delle pozioni di magia disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaPozioniMagia extends NotificaVariazioneDisponibilita {

    public NotificaVariazioneDisponibilitaPozioniMagia(int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_MAGIA, valorePrecedente, nuovoValore);
    }
}
