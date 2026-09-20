package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Classe base astratta per notificare eventi che accadono ad un Personaggio.
 *
 * @author Stefano Reksten
 */
public abstract class EventoSuPersonaggio extends EventoBase {

    protected final Personaggio personaggio;

    /**
     * @param tipoEvento il tipo dell'evento che si verifica
     * @param personaggio il Personaggio su cui si verifica l'evento
     */
    protected EventoSuPersonaggio(TipoEvento tipoEvento, Personaggio personaggio) {
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
