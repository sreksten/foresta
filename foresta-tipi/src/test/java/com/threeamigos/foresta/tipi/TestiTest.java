package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
