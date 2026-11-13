package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * Notifica un cambiamento nel totale degli incantesimi di un certo tipo disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaIncantesimi extends NotificaVariazioneDisponibilita {

    private final TipoIncantesimo tipoIncantesimo;

    public NotificaVariazioneDisponibilitaIncantesimi(TipoIncantesimo tipoIncantesimo, int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_INCANTESIMI, valorePrecedente, nuovoValore);
        this.tipoIncantesimo = tipoIncantesimo;
    }

    /**
     * L'incantesimo di cui è cambiato il numero di pergamene
     */
    public TipoIncantesimo getClasseIncantesimo() {
        return tipoIncantesimo;
    }
}
