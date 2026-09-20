package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class RichiestaSelezioneIncantesimoDaLanciare extends RichiestaConComandi {

    public RichiestaSelezioneIncantesimoDaLanciare(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_SELEZIONE_INCANTESIMO_DA_LANCIARE, possibilita);
    }
}
