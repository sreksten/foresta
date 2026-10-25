package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * I testi per lingua applicati ai dati veri dei tipi (le prove del caricatore stanno in foresta-testi).
 */
class TestiTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void unaLinguaSenzaFileRipiegaSuItaliano() {
		String italiano = Testi.get("note_personaggi", "GOBLIN.CRITICO").get();

		Testi.impostaLingua("xx");

		assertEquals(italiano, Testi.get("note_personaggi", "GOBLIN.CRITICO").get());
		assertEquals(italiano, NoteDeiPersonaggi.nota(TipoPersonaggio.GOBLIN, TipoAttributo.CRITICO).get());
	}

	@Test
	void unaChiaveCheNonEsisteDaUnRisultatoVuoto() {
		assertFalse(Testi.get("note_personaggi", "NON.ESISTE").isPresent());
	}
}
