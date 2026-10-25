package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il gruppo ha vinto una sfida a carta, forbici e sasso (vedi carta_forbici_sasso.md): il suo sfidante non si uccide, quindi non è un avversario
 * sconfitto, ma per il bestiario conta come un incontro riuscito.
 */
public class InternoSfidaVinta extends EventoBase {

    private final TipoPersonaggio avversario;

    public InternoSfidaVinta(TipoPersonaggio avversario) {
        super(TipoEvento.INTERNO_SFIDA_VINTA);
        this.avversario = avversario;
    }

    /**
     * La classe di chi ha sfidato il gruppo
     */
    public TipoPersonaggio getAvversario() {
        return avversario;
    }

}
