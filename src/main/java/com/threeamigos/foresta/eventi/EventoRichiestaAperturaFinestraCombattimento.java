package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaAperturaFinestraCombattimento extends EventoPersonaggio {

    private final Personaggio avversario;

    public EventoRichiestaAperturaFinestraCombattimento(Personaggio combattente, Personaggio avversario) {
        super(TipoEvento.VISUALIZZA_FINESTRA_COMBATTIMENTO, combattente);
        this.avversario = avversario;
    }

    public Personaggio getAvversario() {
        return avversario;
    }
}
