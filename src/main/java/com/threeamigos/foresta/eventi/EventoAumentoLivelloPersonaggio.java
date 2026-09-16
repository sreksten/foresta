package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoAumentoLivelloPersonaggio extends EventoPersonaggio {

    private final int livelloPrecedente;
    private final int livelloAttuale;

    public EventoAumentoLivelloPersonaggio(Personaggio personaggio, int livelloPrecedente, int livelloAttuale) {
        super(TipoEvento.PERSONAGGIO_AUMENTO_LIVELLO, personaggio);
        this.livelloPrecedente = livelloPrecedente;
        this.livelloAttuale = livelloAttuale;
    }

    public int getLivelloPrecedente() {
        return livelloPrecedente;
    }

    public int getLivelloAttuale() {
        return livelloAttuale;
    }
}
