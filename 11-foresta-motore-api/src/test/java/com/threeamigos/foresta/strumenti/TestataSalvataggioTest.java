package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestataSalvataggioTest {

    @Test
    void portaIDatiCheLeSonoStatiDati() {
        TestataSalvataggio testata = new TestataSalvataggio(Comando.SI, "Partita", null);
        assertSame(Comando.SI, testata.getId());
        assertEquals("Partita", testata.getDescrizione());
        assertNull(testata.getGruppoGiocatore());
    }
}
