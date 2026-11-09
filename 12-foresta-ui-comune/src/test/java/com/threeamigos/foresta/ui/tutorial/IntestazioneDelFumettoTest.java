package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntestazioneDelFumettoTest {

	private static final int BORDO = DescrizioneFumetto.SMUSSAMENTO_BORDO;
	private static final int SPAZIO = IntestazioneDelFumetto.SPAZIO_FRA_ICONA_E_TITOLO;

	@Test
	void senzaIconeNeTitoloNonPrendePosto() {
		IntestazioneDelFumetto intestazione = new IntestazioneDelFumetto(new int[0], new int[0], false, 0, 0);
		assertFalse(intestazione.isPresente());
		assertEquals(0, intestazione.getLarghezza());
		assertEquals(0, intestazione.getAltezza());
	}

	@Test
	void leIconeStannoInFilaEIlTitoloDopoDiLoroCentratiSullaRiga() {
		IntestazioneDelFumetto intestazione = new IntestazioneDelFumetto(new int[] {20, 30}, new int[] {10, 24}, true, 50, 12);
		assertTrue(intestazione.isPresente());
		assertEquals(20 + SPAZIO + 30 + SPAZIO + 50, intestazione.getLarghezza());
		assertEquals(24 + IntestazioneDelFumetto.SPAZIO_SOTTO_L_INTESTAZIONE, intestazione.getAltezza());
		assertEquals(BORDO, intestazione.getXDellIcona(0));
		assertEquals(BORDO + 20 + SPAZIO, intestazione.getXDellIcona(1));
		assertEquals(BORDO + 20 + SPAZIO + 30 + SPAZIO, intestazione.getXDelTitolo());
		assertEquals(BORDO + (24 - 10) / 2, intestazione.getYDellIcona(0));
		assertEquals(BORDO, intestazione.getYDellIcona(1));
		assertEquals(BORDO + (24 - 12) / 2, intestazione.getYDelTitolo());
	}

	@Test
	void ilSoloTitoloStaInCimaASinistraEUnTitoloVuotoOccupaAlmenoUnPixel() {
		IntestazioneDelFumetto intestazione = new IntestazioneDelFumetto(new int[0], new int[0], true, 0, 12);
		assertTrue(intestazione.isPresente());
		assertEquals(1, intestazione.getLarghezza());
		assertEquals(12 + IntestazioneDelFumetto.SPAZIO_SOTTO_L_INTESTAZIONE, intestazione.getAltezza());
		assertEquals(BORDO, intestazione.getXDelTitolo());
		assertEquals(BORDO, intestazione.getYDelTitolo());
	}

	@Test
	void leSoleIconeNonLascianoSpazioPerIlTitolo() {
		IntestazioneDelFumetto intestazione = new IntestazioneDelFumetto(new int[] {20}, new int[] {16}, false, 99, 99);
		assertEquals(20, intestazione.getLarghezza());
		assertEquals(16 + IntestazioneDelFumetto.SPAZIO_SOTTO_L_INTESTAZIONE, intestazione.getAltezza());
	}

	@Test
	void larghezzeEAltezzeDevonoEssereTanteQuante() {
		assertThrows(IllegalArgumentException.class, () -> new IntestazioneDelFumetto(new int[] {1, 2}, new int[] {1}, false, 0, 0));
	}
}
