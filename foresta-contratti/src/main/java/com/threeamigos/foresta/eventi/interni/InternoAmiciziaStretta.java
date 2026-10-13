package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.List;

/**
 * Un personaggio del gruppo ha stretto amicizia con il gruppo avversario.
 */
public class InternoAmiciziaStretta extends EventoBase {

    private final List<TipoPersonaggio> avversari;

    /**
     * @param avversari le classi degli avversari vivi con cui si è stretta amicizia
     */
    public InternoAmiciziaStretta(List<TipoPersonaggio> avversari) {
        super(TipoEvento.INTERNO_AMICIZIA_STRETTA);
        this.avversari = avversari;
    }

    public List<TipoPersonaggio> getAvversari() {
        return avversari;
    }
}
