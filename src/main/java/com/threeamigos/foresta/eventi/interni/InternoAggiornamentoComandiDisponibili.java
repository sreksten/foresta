package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.Comando;

import java.util.Arrays;
import java.util.Collection;

/**
 * Dice quali siano i possibili comandi che l'automa di gioco in questo momento può accettare.
 *
 * @author Stefano Reksten
 */
public class InternoAggiornamentoComandiDisponibili extends RichiestaConComandi {

    public InternoAggiornamentoComandiDisponibili(Comando ... possibilita) {
        super(TipoEvento.INTERNO_AGGIORNAMENTO_COMANDI_DISPONIBILI, Arrays.asList(possibilita));
    }

    public InternoAggiornamentoComandiDisponibili(Collection<Comando> possibilita) {
        super(TipoEvento.INTERNO_AGGIORNAMENTO_COMANDI_DISPONIBILI, possibilita);
    }
}
