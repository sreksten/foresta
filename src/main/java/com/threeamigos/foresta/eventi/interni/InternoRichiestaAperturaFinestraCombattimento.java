package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class InternoRichiestaAperturaFinestraCombattimento extends EventoSuPersonaggio {

    private final Personaggio avversario;

    public InternoRichiestaAperturaFinestraCombattimento(Personaggio combattente, Personaggio avversario) {
        super(TipoEvento.INTERNO_STATO_FINESTRA_COMBATTIMENTO, combattente);
        this.avversario = avversario;
    }

    public Personaggio getAvversario() {
        return avversario;
    }
}
