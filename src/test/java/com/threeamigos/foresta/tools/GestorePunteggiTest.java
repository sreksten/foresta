package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.modellodati.LettoreCampi;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GestorePunteggiTest {

    @Test
    void ilPipeSparisceDaiNomi() {
        assertEquals("Pippo", GestorePunteggiBase.pulisciNome("Pip|po"));
        assertEquals("Pippo #1", GestorePunteggiBase.pulisciNome("Pippo #1"));
        assertEquals("nessun nome", GestorePunteggiBase.pulisciNome("  "));
        assertEquals("nessun nome", GestorePunteggiBase.pulisciNome("|"));
        assertEquals("nessun nome", GestorePunteggiBase.pulisciNome(null));
    }

    @Test
    void unaRigaDellaClassificaSiLegge() throws IOException {
        LettoreCampi campi = new LettoreCampi("Pippo|1234");
        assertEquals("Pippo", campi.testo());
        assertEquals(1234, campi.intero());
    }
}
