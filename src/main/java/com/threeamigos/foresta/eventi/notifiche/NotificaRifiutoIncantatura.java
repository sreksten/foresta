package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.MotivoRifiutoIncantatura;

/**
 * L'incantatore rifiuta qualcosa sul banco di lavoro, o la fusione: il motivo dice perché.
 */
public class NotificaRifiutoIncantatura extends EventoBase {

    private final MotivoRifiutoIncantatura motivo;

    public NotificaRifiutoIncantatura(MotivoRifiutoIncantatura motivo) {
        super(TipoEvento.NOTIFICA_RIFIUTO_INCANTATURA);
        this.motivo = motivo;
    }

    public MotivoRifiutoIncantatura getMotivo() {
        return motivo;
    }
}
