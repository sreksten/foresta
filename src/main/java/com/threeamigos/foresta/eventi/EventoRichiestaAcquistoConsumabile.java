package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.modellodati.TipoConsumabile;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Il giocatore chiede di acquistare un consumabile da un alchimista.
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaAcquistoConsumabile extends EventoBase {

    private final TipoConsumabile tipoConsumabile;
    private final ClasseIncantesimo classeIncantesimo;
    private final Personaggio personaggio;
    private final int prezzo;

    public EventoRichiestaAcquistoConsumabile(TipoConsumabile tipoConsumabile, ClasseIncantesimo classeIncantesimo,
                                              Personaggio personaggio, int prezzo) {
        super(TipoEvento.RICHIESTA_ACQUISTO_CONSUMABILE);
        this.tipoConsumabile = tipoConsumabile;
        this.classeIncantesimo = classeIncantesimo;
        this.personaggio = personaggio;
        this.prezzo = prezzo;
    }

    public TipoConsumabile getTipoConsumabile() {
        return tipoConsumabile;
    }

    public ClasseIncantesimo getClasseIncantesimo() {
        return classeIncantesimo;
    }

    public Personaggio getPersonaggio() {
    	return personaggio;
    }

    public int getPrezzo() {
        return prezzo;
    }
}
