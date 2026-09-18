package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.InterfacciaUtente;

/**
 * Porta in primo piano una certa Finestra (o Finestre) di gioco.
 *
 * @author Stefano Reksten
 */
public class EventoMostraFinestra extends EventoBase {

    private final InterfacciaUtente.Finestra[] finestre;

    public EventoMostraFinestra(InterfacciaUtente.Finestra ... finestre) {
        super(TipoEvento.MOSTRA_FINESTRA);
        this.finestre = finestre;
    }

    public InterfacciaUtente.Finestra[] getFinestre() {
        return finestre;
    }
}
