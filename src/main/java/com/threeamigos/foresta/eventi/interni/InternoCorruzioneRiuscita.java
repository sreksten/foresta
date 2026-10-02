package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.List;

/**
 * Il gruppo ha corrotto il gruppo avversario, ottenendo un passaggio sicuro.
 */
public class InternoCorruzioneRiuscita extends EventoBase {

    private final List<ClassePersonaggio> avversari;

    /**
     * @param avversari le classi degli avversari vivi che sono stati corrotti
     */
    public InternoCorruzioneRiuscita(List<ClassePersonaggio> avversari) {
        super(TipoEvento.INTERNO_CORRUZIONE_RIUSCITA);
        this.avversari = avversari;
    }

    public List<ClassePersonaggio> getAvversari() {
        return avversari;
    }
}
