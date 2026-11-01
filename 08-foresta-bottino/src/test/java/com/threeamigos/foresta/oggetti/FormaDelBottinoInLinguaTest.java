package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Nomi e articoli del bottino vengono dai testi di {@code TipoBottino} e dalle regole della lingua.
 */
class FormaDelBottinoInLinguaTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void italiano() {
		assertEquals("una ", FormaDelBottino.SPADA.getAIS());
		assertEquals("uno ", FormaDelBottino.SPADONE.getAIS());
		assertEquals("lo ", FormaDelBottino.SCUDO.getADS());
		assertEquals("un'", FormaDelBottino.ASCIA.getAIS());
		assertEquals("l'", FormaDelBottino.ELMO.getADS());
		assertEquals("alcuni ", FormaDelBottino.ELMO.getAIP());
		assertEquals("un paio di ", FormaDelBottino.SCHINIERI.getAIS());
		assertEquals("alcune paia di ", FormaDelBottino.SCHINIERI.getAIP());
		assertEquals("gli ", FormaDelBottino.SCHINIERI.getADS());
		assertEquals("bastoni magici", FormaDelBottino.BASTONE_MAGICO.getNomePlurale());
		assertEquals("la ", new Moneta().getADS());
		assertEquals("pietre preziose", new PietraPreziosa().getNomePlurale());
	}

	@Test
	void ingleseAmericanoEBritannico() {
		Testi.impostaLingua("en");
		assertEquals("a ", FormaDelBottino.SPADA.getAIS());
		assertEquals("a ", FormaDelBottino.ELMO.getAIS());
		assertEquals("an ", FormaDelBottino.ASCIA.getAIS());
		assertEquals("a pair of ", FormaDelBottino.SCHINIERI.getAIS());
		assertEquals("some pairs of ", FormaDelBottino.SCHINIERI.getAIP());
		assertEquals("the ", FormaDelBottino.SCUDO.getADS());
		Testi.impostaLingua("en-GB");
		assertEquals("suit of armour", FormaDelBottino.ARMATURA.getNomeSingolare());
		assertEquals("suits of armour", FormaDelBottino.ARMATURA.getNomePlurale());
	}
}
