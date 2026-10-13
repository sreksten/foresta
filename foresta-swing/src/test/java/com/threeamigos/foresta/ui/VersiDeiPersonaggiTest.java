package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.intermezzi.Verso;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersiDeiPersonaggiTest {

	@Test
	void ogniPersonaggioHaUnVerso() {
		for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
			assertNotNull(VersiDeiPersonaggi.di(tipo), "manca il verso di " + tipo);
		}
	}

	@Test
	void siSpecchiaSoloSeIlVersoDellImmagineNonEQuelloVoluto() {
		Verso verso = VersiDeiPersonaggi.di(TipoPersonaggio.GUERRIERO);
		Verso opposto = verso == Verso.DESTRA ? Verso.SINISTRA : Verso.DESTRA;
		assertFalse(VersiDeiPersonaggi.serveSpecchiare(TipoPersonaggio.GUERRIERO, verso));
		assertTrue(VersiDeiPersonaggi.serveSpecchiare(TipoPersonaggio.GUERRIERO, opposto));
	}
}
