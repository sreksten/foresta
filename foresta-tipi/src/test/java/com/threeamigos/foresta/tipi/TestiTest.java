package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I testi per lingua: l'italiano è la lingua del gioco e quella a cui si ripiega.
 */
class TestiTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void laLinguaPredefinitaEItaliano() {
		assertEquals("it", Testi.getLingua());
		Testi.impostaLingua("EN ");
		assertEquals("en", Testi.getLingua());
		Testi.impostaLingua("");
		assertEquals("it", Testi.getLingua());
	}

	@Test
	void unaLinguaSenzaFileRipiegaSuItaliano() {
		String italiano = Testi.get("note_personaggi", "GOBLIN.CRITICO").get();

		Testi.impostaLingua("xx");

		assertEquals(italiano, Testi.get("note_personaggi", "GOBLIN.CRITICO").get());
		assertEquals(italiano, NoteDeiPersonaggi.nota(TipoPersonaggio.GOBLIN, TipoAttributo.CRITICO).get());
	}

	@Test
	void unaChiaveOUnFileCheNonEsistonoDannoUnRisultatoVuoto() {
		assertFalse(Testi.get("note_personaggi", "NON.ESISTE").isPresent());
		assertFalse(Testi.get("file_che_non_esiste", "QUALUNQUE").isPresent());
	}

	@Test
	void unFlussoSiLeggeInUtf8EUnoCheNonCeVuoto() {
		Properties testi = Testi.leggi(new ByteArrayInputStream("CHIAVE=Più giù\n".getBytes(StandardCharsets.UTF_8)), "prova");
		assertEquals("Più giù", testi.getProperty("CHIAVE"));
		assertTrue(Testi.leggi(null, "assente").isEmpty());
	}

	@Test
	void unFlussoIlleggibileEUnErroreCheDiceQualeFile() {
		InputStream rotto = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("disco rotto");
			}
		};
		IllegalStateException errore = assertThrows(IllegalStateException.class, () -> Testi.leggi(rotto, "note_rotte"));
		assertTrue(errore.getMessage().contains("note_rotte"), errore.getMessage());
		assertTrue(errore.getCause() instanceof IOException);
	}
}
