package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La richiesta di scegliere un incantesimo, un canto o un'abilità: quali si vedono e quali sono in ricarica.
 */
class RichiestaSelezioneIncantesimoDaLanciareTest {

    @Test
    void laRichiestaSempliceMostraTuttiIComandi() {
        List<Comando> comandi = Arrays.asList(Comando.values()[0], Comando.values()[1]);
        RichiestaSelezioneIncantesimoDaLanciare richiesta = new RichiestaSelezioneIncantesimoDaLanciare(comandi);
        assertEquals(comandi, richiesta.getComandiVisibili());
        assertTrue(richiesta.getInRicarica().isEmpty());
    }

    @Test
    void laRichiestaConRicaricaDiceQuantiTurniMancano() {
        Comando in = Comando.values()[0];
        Comando fuori = Comando.values()[1];
        RicaricaDelComando ricarica = new RicaricaDelComando(2, 5);
        assertEquals(2, ricarica.getTurniRimasti());
        assertEquals(5, ricarica.getDurata());
        RichiestaSelezioneIncantesimoDaLanciare richiesta = new RichiestaSelezioneIncantesimoDaLanciare(Arrays.asList(in, fuori),
                Arrays.asList(in, fuori), Collections.singletonMap(fuori, ricarica));
        assertSame(ricarica, richiesta.getInRicarica().get(fuori));
        assertNull(richiesta.getInRicarica().get(in));
        assertEquals(2, richiesta.getComandiVisibili().size());
    }
}
