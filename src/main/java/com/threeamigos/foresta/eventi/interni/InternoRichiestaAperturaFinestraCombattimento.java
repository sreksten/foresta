package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;

/**
 *
 * @author Stefano Reksten
 */
public class InternoRichiestaAperturaFinestraCombattimento extends EventoSuPersonaggio {

    private final VistaPersonaggio avversario;

    public InternoRichiestaAperturaFinestraCombattimento(VistaPersonaggio combattente, VistaPersonaggio avversario) {
        super(TipoEvento.INTERNO_STATO_FINESTRA_COMBATTIMENTO, combattente);
        this.avversario = avversario;
    }

    public VistaPersonaggio getAvversario() {
        return avversario;
    }
}
