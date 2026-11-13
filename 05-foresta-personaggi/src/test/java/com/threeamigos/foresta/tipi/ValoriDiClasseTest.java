package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La tabella dei valori delle classi (CostantiPersonaggi.valoriDi): ogni classe ha la sua riga, e una riga a cui manca un valore non si
 * costruisce.
 */
class ValoriDiClasseTest {

	@Test
	void ogniClasseHaLaSuaRiga() {
		for (TipoPersonaggio classe : TipoPersonaggio.values()) {
			ValoriDiClasse valori = CostantiPersonaggi.valoriDi(classe);
			assertTrue(valori.getSaluteBase() > 0, classe + ": salute");
			assertTrue(valori.getQuantitaMassima() >= 1, classe + ": quanti");
		}
	}

	@Test
	void chiHaLeStesseCaratteristicheDiUnAltraClasseNeHaIValori() {
		assertSame(CostantiPersonaggi.valoriDi(TipoPersonaggio.GUERRIERO), CostantiPersonaggi.valoriDi(TipoPersonaggio.VICHINGO));
		assertSame(CostantiPersonaggi.valoriDi(TipoPersonaggio.BARDO), CostantiPersonaggi.valoriDi(TipoPersonaggio.BARDO_DELLE_LOCANDE));
		assertTrue(CostantiPersonaggi.valoriDi(TipoPersonaggio.DONNA).isFemmina());
		assertFalse(CostantiPersonaggi.valoriDi(TipoPersonaggio.UOMO).isFemmina());
	}

	@Test
	void leCaratteristicheVengonoDallaTabella() {
		ValoriDiClasse goblin = CostantiPersonaggi.valoriDi(TipoPersonaggio.GOBLIN);
		assertTrue(goblin.isCorrompibile());
		assertFalse(goblin.isAmichevole());
		assertFalse(goblin.isBoss());
		assertEquals(0.0d, CostantiPersonaggi.valoriDi(TipoPersonaggio.VIVERNA).getMoltiplicatoreContrattazione(), 1e-9,
				"la viverna non ha contrattazione: non tratta con i mercanti");
		assertTrue(CostantiPersonaggi.valoriDi(TipoPersonaggio.DRAGO).isBoss());
		assertTrue(CostantiPersonaggi.valoriDi(TipoPersonaggio.MAGA).isFemmina());
	}

	@Test
	void unaRigaACuiMancaUnValoreDaUnErroreCheLoDice() {
		IllegalStateException errore = assertThrows(IllegalStateException.class,
				() -> ValoriDiClasse.nuovi().salute(100).magia(0, 0).per(TipoPersonaggio.GOBLIN));
		assertTrue(errore.getMessage().contains("GOBLIN"), errore.getMessage());
		assertTrue(errore.getMessage().contains("esperienza"), errore.getMessage());
	}
}
