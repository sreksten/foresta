package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.oggetti.Artefatto;

/**
 * La fusione è riuscita: l'artefatto incantato è tornato nell'inventario del gruppo.
 */
public class NotificaApprovazioneIncantatura extends EventoBase {

    private final Artefatto artefatto;
    private final int costo;

    public NotificaApprovazioneIncantatura(Artefatto artefatto, int costo) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_INCANTATURA);
        this.artefatto = artefatto;
        this.costo = costo;
    }

    public Artefatto getArtefatto() {
        return artefatto;
    }

    public int getCosto() {
        return costo;
    }
}
