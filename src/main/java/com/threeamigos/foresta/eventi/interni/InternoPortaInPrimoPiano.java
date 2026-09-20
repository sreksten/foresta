package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.ui.InterfacciaUtente;

/**
 * Porta in primo piano una certa Finestra (o Finestre) di gioco.
 *
 * @author Stefano Reksten
 */
public class InternoPortaInPrimoPiano extends EventoBase {

    private final InterfacciaUtente.Finestra[] finestre;

    public InternoPortaInPrimoPiano(InterfacciaUtente.Finestra ... finestre) {
        super(TipoEvento.INTERNO_PORTA_IN_PRIMO_PIANO);
        this.finestre = finestre;
    }

    public InterfacciaUtente.Finestra[] getFinestre() {
        return finestre;
    }
}
