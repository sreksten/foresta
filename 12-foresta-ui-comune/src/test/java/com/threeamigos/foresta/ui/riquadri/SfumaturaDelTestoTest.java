package com.threeamigos.foresta.ui.riquadri;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Il testo più vecchio si spegne verso l'alto, ma la sfumatura non prende più del 10% del riquadro: sul telefono, dove il riquadro è basso, il
 * testo resta leggibile.
 */
class SfumaturaDelTestoTest {

	@Test
	void laSfumaturaEIlDieciPerCentoDelRiquadroMaiPiuDi255Pixel() {
		// Il riquadro di testo del telefono più piccolo (layout_telefono.md §4.1)
		assertEquals(17, new RiquadroTesto(0, 0, 390, 173).getAltezzaDellaSfumatura());
		assertEquals(40, new RiquadroTesto(0, 0, 600, 400).getAltezzaDellaSfumatura());
		assertEquals(RiquadroTesto.SFUMATURA_MASSIMA, new RiquadroTesto(0, 0, 600, 4000).getAltezzaDellaSfumatura());
		assertEquals(1, new RiquadroTesto(0, 0, 600, 3).getAltezzaDellaSfumatura(), "mai zero");
	}

	@Test
	void ilTestoENeroInCimaEBiancoDallaFineDellaSfumatura() {
		RiquadroTesto riquadro = new RiquadroTesto(0, 0, 390, 173);
		assertEquals(0f, riquadro.luminositaAllaQuota(0));
		assertEquals(0.5f, riquadro.luminositaAllaQuota(17) / 2, 0.0001f);
		assertEquals(1f, riquadro.luminositaAllaQuota(17));
		assertEquals(1f, riquadro.luminositaAllaQuota(100));
	}
}
