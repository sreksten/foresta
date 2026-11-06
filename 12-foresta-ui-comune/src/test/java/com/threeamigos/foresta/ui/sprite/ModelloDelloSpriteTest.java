package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelloDelloSpriteTest {

	private ModelloDelloSprite modello(Dissolvenza dissolvenza) {
		return new ModelloDelloSprite(2f, 1f, dissolvenza, 10f, 100f, 30f, 60f, 1f, 3f);
	}

	@Test
	void alloInizioLoSpriteStaFermoEIntero() {
		ModelloDelloSprite m = modello(Dissolvenza.LINEARE_CON_SOGLIA);
		assertTrue(m.prepara());
		assertEquals(10f, m.getX(), 1e-6);
		assertEquals(100f, m.getY(), 1e-6);
		assertEquals(1f, m.getScala(), 1e-6);
		assertEquals(1f, m.getAlpha(), 1e-6);
	}

	@Test
	void aMetaDellaDurataPosizioneEScalaSonoAMetaStrada() {
		ModelloDelloSprite m = modello(Dissolvenza.LINEARE_CON_SOGLIA);
		m.avanzaDi(1f);
		assertEquals(20f, m.getX(), 1e-6);
		assertEquals(80f, m.getY(), 1e-6);
		assertEquals(2f, m.getScala(), 1e-6);
		assertEquals(1f, m.getAlpha(), 1e-6);
	}

	@Test
	void laDissolvenzaLineareArrivaAZeroAllaFine() {
		ModelloDelloSprite m = modello(Dissolvenza.LINEARE_CON_SOGLIA);
		m.avanzaDi(1.5f);
		assertEquals(0.5f, m.getAlpha(), 1e-6);
		m.avanzaDi(0.5f);
		assertEquals(0f, m.getAlpha(), 1e-6);
	}

	@Test
	void laDissolvenzaIperbolicaEInteraFinoAllaSogliaEPoiScende() {
		ModelloDelloSprite m = modello(Dissolvenza.IPERBOLICA_CON_SOGLIA);
		m.avanzaDi(1f);
		assertEquals(1f, m.getAlpha(), 1e-6);
		m.avanzaDi(0.5f);
		assertEquals(0.2f, m.getAlpha(), 1e-6);
	}

	@Test
	void laDissolvenzaDaSubitoParteIntera() {
		ModelloDelloSprite m = modello(Dissolvenza.DA_SUBITO);
		assertEquals(1f, m.getAlpha(), 1e-6);
		m.avanzaDi(0.1f);
		assertEquals(0.5f, m.getAlpha(), 1e-6);
	}

	@Test
	void finitaLaDurataLoSpriteNonEPiuInVita() {
		ModelloDelloSprite m = modello(Dissolvenza.LINEARE_CON_SOGLIA);
		m.avanzaDi(2f);
		assertFalse(m.prepara());
		assertFalse(m.isAttivo());
		assertFalse(m.prepara());
	}

	@Test
	void conLOrologioLaPrimaMisuraNonFaAvanzareLeSuccessiveSi() {
		ModelloDelloSprite m = modello(Dissolvenza.LINEARE_CON_SOGLIA);
		m.aggiornaConOrologio(5_000_000_000L);
		assertEquals(0f, m.getSecondiTrascorsi(), 1e-6);
		m.aggiornaConOrologio(5_500_000_000L);
		assertEquals(0.5f, m.getSecondiTrascorsi(), 1e-6);
	}

	@Test
	void resettandoIlTempoLaMisuraRipartedaCapo() {
		ModelloDelloSprite m = modello(Dissolvenza.LINEARE_CON_SOGLIA);
		m.aggiornaConOrologio(1_000_000_000L);
		m.aggiornaConOrologio(2_000_000_000L);
		m.resettaTempoTrascorso();
		m.aggiornaConOrologio(9_000_000_000L);
		assertEquals(0f, m.getSecondiTrascorsi(), 1e-6);
	}

	@Test
	void impostareXSpostaPartenzaEArrivo() {
		ModelloDelloSprite m = modello(Dissolvenza.LINEARE_CON_SOGLIA);
		m.impostaX(50f);
		assertEquals(50f, m.getX(), 1e-6);
		m.avanzaDi(1f);
		assertEquals(50f, m.getX(), 1e-6);
	}
}
