package com.threeamigos.foresta.ui.componenti;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La cornice dell'inventario (364 righe, 728 a zoom 2) su uno schermo più basso si taglia nel mezzo: la parte di sopra e quella di sotto, attaccate.
 */
class CorniceTagliataTest {

	@Test
	void seCiStaSiDisegnaIntera() {
		List<CorniceTagliata.Fascia> fasce = CorniceTagliata.fasce(364, 2.0, 728);
		assertEquals(1, fasce.size());
		assertEquals(0, fasce.get(0).getYSorgente());
		assertEquals(364, fasce.get(0).getAltezzaSorgente());
		assertEquals(728, CorniceTagliata.altezza(fasce));
	}

	@Test
	void sulTelefonoSiPrendonoLaMetaDiSopraELaMetaDiSotto() {
		// 505 - 4 di spazio in cima: ci stanno 250 righe, 125 di sopra e 125 di sotto
		List<CorniceTagliata.Fascia> fasce = CorniceTagliata.fasce(364, 2.0, 501);
		assertEquals(2, fasce.size());
		CorniceTagliata.Fascia sopra = fasce.get(0);
		CorniceTagliata.Fascia sotto = fasce.get(1);
		assertEquals(0, sopra.getYSorgente());
		assertEquals(125, sopra.getAltezzaSorgente());
		assertEquals(0, sopra.getYDestinazione());
		assertEquals(250, sopra.getAltezzaDestinazione());
		assertEquals(364 - 125, sotto.getYSorgente(), "la parte di sotto finisce con l'ultima riga dell'immagine");
		assertEquals(125, sotto.getAltezzaSorgente());
		assertEquals(250, sotto.getYDestinazione(), "attaccata alla parte di sopra");
		assertEquals(500, CorniceTagliata.altezza(fasce));
		assertTrue(CorniceTagliata.altezza(fasce) <= 501);
	}

	@Test
	void conRigheDispariLaRigaInPiuVaSopra() {
		List<CorniceTagliata.Fascia> fasce = CorniceTagliata.fasce(364, 2.0, 503);
		assertEquals(126, fasce.get(0).getAltezzaSorgente());
		assertEquals(125, fasce.get(1).getAltezzaSorgente());
		assertEquals(502, CorniceTagliata.altezza(fasce));
	}
}
