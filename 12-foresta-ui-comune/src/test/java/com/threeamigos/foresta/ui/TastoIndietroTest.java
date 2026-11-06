package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TastoIndietroTest {

	@Test
	void annullaChiudeLaFinestraAncheSeSiPuoSalvare() {
		assertEquals(Comando.ANNULLA, TastoIndietro.comando(Arrays.asList(Comando.FLOPPY_SALVA, Comando.ANNULLA, Comando.NORD)));
	}

	@Test
	void noRiportaAlGiocoDallaScelta_delloSlotEDallaConfermaDiUscita() {
		assertEquals(Comando.NO, TastoIndietro.comando(Arrays.asList(Comando.NUMERO_1, Comando.NUMERO_2, Comando.NO)));
		assertEquals(Comando.NO, TastoIndietro.comando(Arrays.asList(Comando.SI, Comando.NO)));
	}

	@Test
	void dallaSchermataDiGiocoSiApreIlSalvataggioCheProponeLUscita() {
		assertEquals(Comando.FLOPPY_SALVA, TastoIndietro.comando(Arrays.asList(Comando.NORD, Comando.SUD, Comando.FLOPPY_SALVA)));
	}

	@Test
	void senzaUnComandoPerTornareIndietroNonSiFaNiente() {
		assertNull(TastoIndietro.comando(Collections.singletonList(Comando.FLOPPY_CARICA)));
		assertNull(TastoIndietro.comando(Collections.emptyList()));
	}
}
