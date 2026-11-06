package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.Area;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiquadroMappaTest {

	@Test
	void laFinestraDiSetteStaCentrataSulGruppo() {
		assertEquals(7, RiquadroMappa.primaCasella(10, 30));
	}

	@Test
	void vicinoAiBordiLaFinestraNonEsceDallaForesta() {
		assertEquals(0, RiquadroMappa.primaCasella(1, 30));
		assertEquals(0, RiquadroMappa.primaCasella(0, 30));
		assertEquals(23, RiquadroMappa.primaCasella(29, 30));
		assertEquals(23, RiquadroMappa.primaCasella(27, 30));
	}

	@Test
	void leNuvoleSiVedonoSoloDentroLaCornice() {
		Area area = RiquadroMappa.areaDelleNuvole(100, 40, 224);
		assertEquals(100 + RiquadroMappa.BORDO_INTERNO, area.getX());
		assertEquals(40 + RiquadroMappa.BORDO_INTERNO, area.getY());
		assertEquals(224, area.getLarghezza());
		assertEquals(224, area.getAltezza());
	}
}
