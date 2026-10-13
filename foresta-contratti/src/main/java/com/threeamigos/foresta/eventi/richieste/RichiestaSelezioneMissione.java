package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * Una missione chiede al giocatore una conferma (SI/NO) o una scelta fra 2 e 5 opzioni (NUMERO_1..NUMERO_5); il
 * testo della domanda e delle opzioni è già stato pubblicato nel riquadro del testo (vedi gestione_missioni.md, §4).
 */
public class RichiestaSelezioneMissione extends RichiestaConComandi {

    public RichiestaSelezioneMissione(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_SELEZIONE_MISSIONE, possibilita);
    }
}
