package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Classe astratta per modellare gil eventi che accadono ad un Personaggio.
 *
 * @author Stefano Reksten
 */
abstract class EventoPersonaggio extends EventoBase {

    protected final Personaggio personaggio;

    /**
     * @param tipoEvento il tipo dell'evento che si verifica
     * @param personaggio il Personaggio su cui si verifica l'evento
     */
    protected EventoPersonaggio(TipoEvento tipoEvento, Personaggio personaggio) {
        super(tipoEvento);
        this.personaggio = personaggio;
    }

    /**
     * @return il Personaggio su cui si verifica l'evento
     */
    public Personaggio getPersonaggio() {
        return personaggio;
    }

}
