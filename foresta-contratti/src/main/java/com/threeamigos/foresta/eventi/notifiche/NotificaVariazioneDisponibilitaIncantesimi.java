package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * Notifica un cambiamento nel totale degli incantesimi di un certo tipo disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaIncantesimi extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;
    private final TipoIncantesimo tipoIncantesimo;

    public NotificaVariazioneDisponibilitaIncantesimi(TipoIncantesimo tipoIncantesimo, int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_INCANTESIMI);
        this.tipoIncantesimo = tipoIncantesimo;
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    public TipoIncantesimo getClasseIncantesimo() {
        return tipoIncantesimo;
    }

    public int getValorePrecedente() {
        return valorePrecedente;
    }

    public int getNuovoValore() {
        return nuovoValore;
    }
}
