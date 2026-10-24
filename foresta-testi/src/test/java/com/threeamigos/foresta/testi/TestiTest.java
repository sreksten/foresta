package com.threeamigos.foresta.testi;

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
	void unTestoSiLeggeNellaLinguaScelta() {
		assertEquals("Più giù", Testi.get("testi_di_prova", "SALUTO").get());
		Testi.impostaLingua("zz");
		assertEquals("Hello", Testi.get("testi_di_prova", "SALUTO").get());
	}

	@Test
	void unTestoCheMancaNellaLinguaSceltaSiCercaInItaliano() {
		Testi.impostaLingua("zz");
		assertEquals("Solo italiano", Testi.get("testi_di_prova", "SOLO_IN_ITALIANO").get());
	}

	@Test
	void unaLinguaSenzaFileRipiegaSuItaliano() {
		Testi.impostaLingua("xx");
		assertEquals("Più giù", Testi.get("testi_di_prova", "SALUTO").get());
	}

	@Test
	void unaChiaveOUnFileCheNonEsistonoDannoUnRisultatoVuoto() {
		assertFalse(Testi.get("testi_di_prova", "NON.ESISTE").isPresent());
		assertFalse(Testi.get("file_che_non_esiste", "QUALUNQUE").isPresent());
	}

	@Test
	void iTestiDiUnaCostanteSiLeggonoDalFileDellEnum() {
		assertEquals("Primo", Testi.testo(TipoDiProva.PRIMO, "NOME").get());
		assertEquals("Il primo", Testi.testo(TipoDiProva.PRIMO, "DESCRIZIONE").get());
		Testi.impostaLingua("zz");
		assertEquals("Premier", Testi.testo(TipoDiProva.PRIMO, "NOME").get());
	}

	@Test
	void unaVoceCheMancaNellaLinguaSceltaSiCercaInItaliano() {
		Testi.impostaLingua("zz");
		assertEquals("Il primo", Testi.testo(TipoDiProva.PRIMO, "DESCRIZIONE").get());
		assertEquals("Secondo", Testi.testo(TipoDiProva.SECONDO, "NOME").get());
		Testi.impostaLingua("xx");
		assertEquals("Primo", Testi.testo(TipoDiProva.PRIMO, "NOME").get());
	}

	@Test
	void unaVoceCheNonEsisteDaUnRisultatoVuotoOIlNomeDellaCostante() {
		assertFalse(Testi.testo(TipoDiProva.SECONDO, "DESCRIZIONE").isPresent());
		assertEquals("SECONDO", Testi.testoOCostante(TipoDiProva.SECONDO, "DESCRIZIONE"));
		assertEquals("Secondo", Testi.testoOCostante(TipoDiProva.SECONDO, "NOME"));
	}

	@Test
	void ilNomeDelFileESiDeriveDalNomeDellEnum() {
		assertEquals("tipo_di_prova", Testi.nomeDelFile(TipoDiProva.class));
		assertEquals("testi", Testi.nomeDelFile(Testi.class));
		assertEquals("a_b_c", Testi.nomeDelFile(ABC.class));
	}

	private enum ABC {
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
