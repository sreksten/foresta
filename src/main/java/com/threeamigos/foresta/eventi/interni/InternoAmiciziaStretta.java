package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.List;

/**
 * Un personaggio del gruppo ha stretto amicizia con il gruppo avversario.
 */
public class InternoAmiciziaStretta extends EventoBase {

    private final List<ClassePersonaggio> avversari;

    /**
     * @param avversari le classi degli avversari vivi con cui si è stretta amicizia
     */
    public InternoAmiciziaStretta(List<ClassePersonaggio> avversari) {
        super(TipoEvento.INTERNO_AMICIZIA_STRETTA);
        this.avversari = avversari;
    }

    public List<ClassePersonaggio> getAvversari() {
        return avversari;
    }
}
