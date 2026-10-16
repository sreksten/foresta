package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.ui.barra.ClasseIcona;

/**
 * Ogni canto del Bardo ha il suo comando e la sua icona (vedi bardo.md, fase 3).
 */
class IconeDeiCantiTest {

	@Test
	void ognunoHaUnComandoDiversoEUnIconaChePuoEssereLetta() {
		Set<Comando> comandi = new HashSet<>();
		for (TipoCanto tipo : TipoCanto.values()) {
			Comando comando = tipo.getComando();
			assertNotNull(comando, tipo.name());
			assertTrue(comandi.add(comando), "comando ripetuto: " + comando);
			assertSame(tipo, TipoCanto.ofComando(comando));
			assertNotNull(ClasseIcona.ofComando(comando).getIcona(), comando.name());
		}
		assertEquals(4, comandi.size());
	}

	@Test
	void glAltriComandiNonSonoCanti() {
		assertNull(TipoCanto.ofComando(Comando.DARDO_ARCANO));
		assertNull(TipoCanto.ofComando(Comando.ALBA_SACRA));
		assertNull(TipoCanto.ofComando(Comando.ANNULLA));
	}
}
