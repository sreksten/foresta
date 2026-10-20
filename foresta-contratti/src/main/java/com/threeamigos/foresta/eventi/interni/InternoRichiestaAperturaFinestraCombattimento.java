package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;

/**
 * La richiesta di aprire la finestra del combattimento fra un combattente e il suo avversario.
 *
 * @author Stefano Reksten
 */
public class InternoRichiestaAperturaFinestraCombattimento extends EventoSuPersonaggio {

    private final VistaPersonaggio avversario;

    public InternoRichiestaAperturaFinestraCombattimento(VistaPersonaggio combattente, VistaPersonaggio avversario) {
        super(TipoEvento.INTERNO_RICHIESTA_APERTURA_FINESTRA_COMBATTIMENTO, combattente);
        this.avversario = avversario;
    }

    public VistaPersonaggio getAvversario() {
        return avversario;
    }

}
