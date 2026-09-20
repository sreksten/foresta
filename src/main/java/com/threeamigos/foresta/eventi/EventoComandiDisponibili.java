package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Arrays;
import java.util.Collection;

/**
 * Dice quali siano i possibili comandi che l'automa di gioco in questo momento può accettare.
 *
 * @author Stefano Reksten
 */
public class EventoComandiDisponibili extends EventoConComandi {

    public EventoComandiDisponibili(Comando ... possibilita) {
        super(TipoEvento.COMANDI_DISPONIBILI, Arrays.asList(possibilita));
    }

    public EventoComandiDisponibili(Collection<Comando> possibilita) {
        super(TipoEvento.COMANDI_DISPONIBILI, possibilita);
    }
}
