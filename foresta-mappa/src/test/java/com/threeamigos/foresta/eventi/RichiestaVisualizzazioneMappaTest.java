package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.richieste.RichiestaVisualizzazioneMappa;
import com.threeamigos.foresta.tipi.TipoEvento;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RichiestaVisualizzazioneMappaTest {

    @Test
    void dichiaraIlSuoTipo() {
        assertEquals(TipoEvento.RICHIESTA_VISUALIZZAZIONE_MAPPA, new RichiestaVisualizzazioneMappa().getTipoEvento());
    }
}
