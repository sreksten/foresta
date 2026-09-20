package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class EventoSelezioneIncantesimoDaLanciare extends EventoConComandi {

    public EventoSelezioneIncantesimoDaLanciare(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_SELEZIONE_INCANTESIMO_DA_LANCIARE, possibilita);
    }
}
