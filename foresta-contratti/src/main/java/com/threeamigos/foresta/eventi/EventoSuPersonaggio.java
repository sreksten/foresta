package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.interfacce.VistaPersonaggio;

/**
 * Classe base astratta per notificare eventi che accadono ad un Personaggio.
 *
 * @author Stefano Reksten
 */
public abstract class EventoSuPersonaggio extends EventoBase {

    protected final VistaPersonaggio personaggio;

    /**
     * @param tipoEvento il tipo dell'evento che si verifica
     * @param personaggio il Personaggio su cui si verifica l'evento
     */
    protected EventoSuPersonaggio(TipoEvento tipoEvento, VistaPersonaggio personaggio) {
        super(tipoEvento);
        this.personaggio = personaggio;
    }

    /**
     * @return il Personaggio su cui si verifica l'evento
     */
    public VistaPersonaggio getPersonaggio() {
        return personaggio;
    }

}
