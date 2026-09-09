package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
abstract class EventoPersonaggio extends EventoBase {

    protected final Personaggio personaggio;

    protected EventoPersonaggio(TipoEvento tipoEvento, Personaggio personaggio) {
        super(tipoEvento);
        this.personaggio = personaggio;
    }

    public Personaggio getPersonaggio() {
        return personaggio;
    }

}
