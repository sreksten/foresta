package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoMortePersonaggio extends EventoPersonaggio{

    private final String causaTrapasso;

    public EventoMortePersonaggio(Personaggio personaggio, String causaTrapasso) {
        super(personaggio);
        this.causaTrapasso = causaTrapasso;
    }

    public String getCausaTrapasso() {
        return causaTrapasso;
    }
}
