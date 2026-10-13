package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;

/**
 * L'incantatore rifiuta qualcosa sul banco di lavoro, o la fusione: il motivo dice perché.
 */
public class NotificaRifiutoIncantatura extends EventoBase {

    private final TipoMotivoRifiutoIncantatura motivo;

    public NotificaRifiutoIncantatura(TipoMotivoRifiutoIncantatura motivo) {
        super(TipoEvento.NOTIFICA_RIFIUTO_INCANTATURA);
        this.motivo = motivo;
    }

    public TipoMotivoRifiutoIncantatura getMotivo() {
        return motivo;
    }
}
