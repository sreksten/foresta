package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.ComandiAssociati;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoIcona;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni canto del Bardo ha il suo comando e la sua icona (vedi bardo.md, fase 3).
 */
class IconeDeiCantiTest {

	@Test
	void ognunoHaUnComandoDiversoEUnIconaChePuoEssereLetta() {
		Set<Comando> comandi = new HashSet<>();
		for (TipoCanto tipo : TipoCanto.values()) {
			Comando comando = ComandiAssociati.di(tipo);
			assertNotNull(comando, tipo.name());
			assertTrue(comandi.add(comando), "comando ripetuto: " + comando);
			assertSame(tipo, ComandiAssociati.canto(comando));
			assertNotNull(TipoIcona.ofComando(comando).getIcona(), comando.name());
		}
		assertEquals(4, comandi.size());
	}

	@Test
	void glAltriComandiNonSonoCanti() {
		assertNull(ComandiAssociati.canto(Comando.DARDO_ARCANO));
		assertNull(ComandiAssociati.canto(Comando.ALBA_SACRA));
		assertNull(ComandiAssociati.canto(Comando.ANNULLA));
	}
}
