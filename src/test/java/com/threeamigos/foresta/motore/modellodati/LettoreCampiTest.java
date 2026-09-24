package com.threeamigos.foresta.motore.modellodati;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LettoreCampiTest {

    @Test
    void conservaICampiVuoti() throws IOException {
        LettoreCampi campi = new LettoreCampi("a||3|");
        assertEquals("a", campi.testo());
        assertEquals("", campi.testo());
        assertEquals(3, campi.intero());
        assertEquals("", campi.testo());
        assertFalse(campi.haAltriCampi());
    }

    @Test
    void unaRigaVuotaNonHaCampi() throws IOException {
        assertFalse(new LettoreCampi("").haAltriCampi());
    }

    @Test
    void iCampiFacoltativiVuotiSonoNull() throws IOException {
        LettoreCampi campi = new LettoreCampi("|MANO_SECONDARIA|");
        assertNull(campi.testoFacoltativo());
        assertEquals(SlotArtefatto.MANO_SECONDARIA, campi.enumeratoFacoltativo(SlotArtefatto.class));
        assertNull(campi.enumeratoFacoltativo(SlotArtefatto.class));
        assertEquals("", Serializzabile.facoltativo(null));
        assertEquals("TESTA", Serializzabile.facoltativo(SlotArtefatto.TESTA));
    }

    @Test
    void campiMancantiOFineDelFileSonoErrori() throws IOException {
        LettoreCampi campi = new LettoreCampi("uno");
        campi.testo();
        assertThrows(IOException.class, campi::testo);
        assertThrows(IOException.class, () -> new LettoreCampi(null));
    }

    @Test
    void unPersonaggioSenzaNomeSiSalvaESiRilegge() throws IOException {
        // Given: un nome vuoto vale come nessun nome
        PersonaggioMD personaggio = new PersonaggioMD();
        personaggio.setClasse(com.threeamigos.foresta.personaggi.ClassePersonaggio.GUERRIERO);
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
        assertEquals(com.threeamigos.foresta.personaggi.ClassePersonaggio.GUERRIERO, riletto.getClasse());
    }
}
