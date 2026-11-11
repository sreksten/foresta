package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.Area;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

	@Test
	void nellaPilaLaMappaStaCentrataNellaCorniceGrandeESiSpostaConLei() {
		// Tessere da 32 (con lo zoom), cornice grande 368x288: la finestra di 224 ha 72 pixel ai lati e 32 sopra e sotto
		RiquadroMappa mappa = new RiquadroMappa(500, 4, 32, 32, null);
		assertEquals(500 + RiquadroMappa.BORDO_INTERNO, mappa.getAreaDellaMappa().getX(), "fuori dalla pila, dentro la sua cornice");
		mappa.nellaCorniceDellaPila(368, 288);
		assertTrue(mappa.isNellaPila());
		mappa.spostaInVerticale(110);
		Area area = mappa.getAreaDellaMappa();
		assertEquals(500 + 72, area.getX());
		assertEquals(110 + 32, area.getY());
		assertEquals(224, area.getLarghezza());
		assertEquals(224, area.getAltezza());
	}
}
