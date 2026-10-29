package com.threeamigos.foresta.effettidistato;

import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EffettoDiStatoMDTest {

    @Test
    void unEffettoDiStatoCambiaDurataDanniEIntensita() {
        EffettoDiStatoMD effetto = new EffettoDiStatoMD(TipoEffettoDiStato.AVVELENATO, 3, 5);
        assertEquals(0.0d, effetto.getIntensita(), "senza intensità");
        effetto.setDurata(2);
        effetto.setDanniNelTempo(4);
        effetto.setIntensita(0.5d);
        assertEquals(2, effetto.getDurata());
        assertEquals(4, effetto.getDanniNelTempo());
        assertEquals(0.5d, effetto.getIntensita());
    }

    @Test
    void unEffettoSiRileggeDalSuoCampo() throws IOException {
        EffettoDiStatoMD effetto = new EffettoDiStatoMD(TipoEffettoDiStato.BRUCIATO, 3, 4, 0.5d);

        EffettoDiStatoMD riletto = EffettoDiStatoMD.daCampo(new LettoreCampi(effetto.comeCampo()));

        assertEquals(TipoEffettoDiStato.BRUCIATO, riletto.getTipoEffettoDiStato());
        assertEquals(3, riletto.getDurata());
        assertEquals(4, riletto.getDanniNelTempo());
        assertEquals(0.5d, riletto.getIntensita());
    }
}
