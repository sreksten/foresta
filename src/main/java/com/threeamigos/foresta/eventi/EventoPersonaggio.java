package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.PersonaggioBase;

/**
 *
 * @author Stefano Reksten
 */
public class EventoPersonaggio extends EventoBase {

    private final Personaggio personaggio;

    protected EventoPersonaggio(Personaggio personaggio) {
        this.personaggio = personaggio;
    }

    public Personaggio getPersonaggio() {
        return personaggio;
    }

}
