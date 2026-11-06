package com.threeamigos.foresta.ui.testo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoricoDelTestoTest {

	private static final int LARGHEZZA = 300;
	private static final int ALTEZZA = 100;

	private static List<String> visibili(StoricoDelTesto storico) {
		List<String> righe = new ArrayList<>();
		storico.perOgniRigaVisibile((testo, quota) -> righe.add(testo));
		return righe;
	}

	@Test
	void unTestoCortoNonScorre() {
		StoricoDelTesto storico = new StoricoDelTesto(LARGHEZZA, ALTEZZA);
		storico.addString("Ciao");
		assertEquals(0, storico.getScorrimentoMassimo());
		assertFalse(storico.isScorribileSu());
		assertFalse(storico.isScorribileGiu());
		assertTrue(visibili(storico).contains("Ciao"));
	}

	@Test
	void laRigaPiuRecenteStaInFondoENelleAltreSalgono() {
		StoricoDelTesto storico = new StoricoDelTesto(LARGHEZZA, ALTEZZA);
		storico.addString("uno");
		storico.addString("due");
		List<Integer> quote = new ArrayList<>();
		List<String> testi = new ArrayList<>();
		storico.perOgniRigaVisibile((testo, quota) -> {
			testi.add(testo);
			quote.add(quota);
		});
		assertEquals("due", testi.get(0));
		assertEquals("uno", testi.get(1));
		int altezzaDellaRiga = DoomdarkFont.medio().getHeight();
		assertEquals(ALTEZZA - altezzaDellaRiga, (int) quote.get(0));
		assertEquals(ALTEZZA - altezzaDellaRiga - (altezzaDellaRiga + 1), (int) quote.get(1));
	}

	@Test
	void unTestoLungoVaACapoEsiPuoTornareIndietro() {
		StoricoDelTesto storico = new StoricoDelTesto(LARGHEZZA, ALTEZZA);
		for (int i = 0; i < 20; i++) {
			storico.addString("Messaggio numero " + i + " con parole a sufficienza per andare a capo entro la larghezza del riquadro");
		}
		assertTrue(storico.getScorrimentoMassimo() > 0);
		assertTrue(storico.isScorribileSu(), "c'è testo più vecchio");
		assertFalse(storico.isScorribileGiu(), "all'inizio si è in fondo");
		storico.scorri(3);
		assertTrue(storico.isScorribileSu());
		assertTrue(storico.isScorribileGiu());
		int versione = storico.getVersione();
		storico.scorri(0);
		assertEquals(versione, storico.getVersione(), "scorrere di zero non cambia nulla");
	}

	@Test
	void unNuovoMessaggioRiportaInFondoELoStoricoHaUnLimite() {
		StoricoDelTesto storico = new StoricoDelTesto(LARGHEZZA, ALTEZZA);
		for (int i = 0; i < 10; i++) {
			storico.addString("riga " + i);
		}
		storico.scorri(2);
		assertTrue(storico.isScorribileGiu());
		storico.addString("ultima");
		assertFalse(storico.isScorribileGiu());
		for (int i = 0; i < 1000; i++) {
			storico.addString("x" + i);
		}
		// Lo storico non cresce oltre il limite delle notizie ricordate: il massimo di scorrimento resta finito
		assertTrue(storico.getScorrimentoMassimo() < 1000 * 30);
	}

	@Test
	void clearSvuotaTutto() {
		StoricoDelTesto storico = new StoricoDelTesto(LARGHEZZA, ALTEZZA);
		storico.addString("uno");
		storico.clear();
		assertFalse(visibili(storico).contains("uno"));
		assertEquals(0, storico.getScorrimentoMassimo());
	}
}
