package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoTrofeoTest {

	@Test
	void perTipologiaRaggruppaITrofeiNellOrdineDeiSupertipi() {
		List<TipoTrofeo> trofei = TipoTrofeo.perTipologia();

		assertEquals(TipoTrofeo.values().length, trofei.size());
		for (int i = 1; i < trofei.size(); i++) {
			assertTrue(trofei.get(i - 1).getCategoriaTrofeo().compareTo(trofei.get(i).getCategoriaTrofeo()) <= 0,
					trofei.get(i - 1) + " viene prima di " + trofei.get(i));
		}
	}
}
