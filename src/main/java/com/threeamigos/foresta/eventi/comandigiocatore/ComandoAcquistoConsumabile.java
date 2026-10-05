package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoConsumabile;

/**
 * Il giocatore chiede di acquistare un Consumabile da un alchimista.
 * L'esito viene deciso dal costo del Consumabile.
 *
 * @author Stefano Reksten
 */
public class ComandoAcquistoConsumabile extends EventoBase {

    private final TipoConsumabile tipoConsumabile;
    private final ClasseIncantesimo classeIncantesimo;
    private final Personaggio personaggio;
    private final int prezzo;

    public ComandoAcquistoConsumabile(TipoConsumabile tipoConsumabile, ClasseIncantesimo classeIncantesimo,
                                      Personaggio personaggio, int prezzo) {
        super(TipoEvento.COMANDO_ACQUISTO_CONSUMABILE);
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
