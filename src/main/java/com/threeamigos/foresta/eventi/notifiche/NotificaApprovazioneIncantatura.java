package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaArtefatto;

/**
 * La fusione è riuscita: l'artefatto incantato è tornato nell'inventario del gruppo.
 */
public class NotificaApprovazioneIncantatura extends EventoBase {

    private final VistaArtefatto artefatto;
    private final int costo;

    public NotificaApprovazioneIncantatura(VistaArtefatto artefatto, int costo) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_INCANTATURA);
        this.artefatto = artefatto;
        this.costo = costo;
    }

    public VistaArtefatto getArtefatto() {
        return artefatto;
    }

    public int getCosto() {
        return costo;
    }
}
