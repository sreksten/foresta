package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Arrays;
import java.util.Collection;

/**
 * Dice quali siano i possibili comandi che l'automa di gioco in questo momento può accettare.
 *
 * @author Stefano Reksten
 */
public class EventoComandiDisponibili extends EventoBase {

    private final Collection<Comando> comandiDisponibili;

    public EventoComandiDisponibili(Comando ... comandiDisponibili) {
        super(TipoEvento.COMANDI_DISPONIBILI);
        this.comandiDisponibili = Arrays.asList(comandiDisponibili);
    }

    public EventoComandiDisponibili(Collection<Comando> comandiDisponibili) {
        super(TipoEvento.COMANDI_DISPONIBILI);
        this.comandiDisponibili = comandiDisponibili;
    }

    public Collection<Comando> getComandiDisponibili() {
        return comandiDisponibili;
    }
}
