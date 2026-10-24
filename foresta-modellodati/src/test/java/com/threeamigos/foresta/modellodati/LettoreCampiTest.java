package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La rilettura di un personaggio salvato (il lettore dei campi è provato in foresta-serializzazione).
 */
class LettoreCampiTest {

    @Test
    void unPersonaggioSenzaNomeSiSalvaESiRilegge() throws IOException {
        // Given: un nome vuoto vale come nessun nome
        PersonaggioMD personaggio = new PersonaggioMD();
        personaggio.setClasse(TipoPersonaggio.GUERRIERO);
        personaggio.setNome("  ");
        personaggio.setVivo(true);
        assertNull(personaggio.getNome());
        StringWriter scritto = new StringWriter();
        PrintWriter writer = new PrintWriter(scritto);
        personaggio.salva(writer);
        writer.flush();
        // When
        PersonaggioMD riletto = new PersonaggioMD();
        riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));
        // Then
        assertNull(riletto.getNome());
        assertTrue(riletto.isVivo());
        assertEquals(TipoPersonaggio.GUERRIERO, riletto.getClasse());
    }
}
