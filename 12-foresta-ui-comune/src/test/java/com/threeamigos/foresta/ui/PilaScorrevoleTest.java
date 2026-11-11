package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La pila del telefono che scorre di lato: segue il dito, e lasciata non si ferma mai a metà.
 */
class PilaScorrevoleTest {

	private final PilaScorrevole pila = new PilaScorrevole(100);

	/** Abbastanza tempo per arrivare da un'estremità all'altra */
	private void finisce() {
		pila.avanza(1);
	}

	@Test
	void compattataSeNeVedeIlVentiPerCentoDiSinistra() {
		assertEquals(80, pila.getScostamentoMassimo());
		assertEquals(0, pila.getScostamento(), "all'inizio è espansa");
		assertTrue(pila.isVersoEspansa(), "la freccia dice che si può compattare");
	}

	@Test
	void trascinataSegueIlDitoSenzaUscireDalleEstremita() {
		pila.prendi(500);
		pila.trascina(530);
		assertEquals(30, pila.getScostamento());
		pila.trascina(700);
		assertEquals(80, pila.getScostamento());
		pila.trascina(400);
		assertEquals(0, pila.getScostamento());
	}

	@Test
	void lasciataVaFinoInFondoNelVersoDellUltimoMovimentoAnchePocoDopoLaPresa() {
		pila.prendi(500);
		pila.trascina(510);
		pila.lascia();
		assertTrue(pila.isInMovimento());
		assertFalse(pila.isVersoEspansa(), "va verso compattata: la freccia punta già a sinistra");
		finisce();
		assertEquals(80, pila.getScostamento());
		assertFalse(pila.isInMovimento());

		// Da compattata: portata un po' a sinistra e poi di nuovo un poco a destra, conta l'ultimo movimento
		pila.prendi(500);
		pila.trascina(440);
		pila.trascina(445);
		pila.lascia();
		finisce();
		assertEquals(80, pila.getScostamento());
		pila.prendi(500);
		pila.trascina(470);
		pila.lascia();
		finisce();
		assertEquals(0, pila.getScostamento());
	}

	@Test
	void scorreDaSolaInUnTempoCortoNonDiColpo() {
		pila.commuta();
		pila.avanza(PilaScorrevole.SECONDI_DELLO_SCORRIMENTO / 2);
		assertEquals(40, pila.getScostamento(), "a metà del tempo è a metà strada");
		pila.avanza(PilaScorrevole.SECONDI_DELLO_SCORRIMENTO);
		assertEquals(80, pila.getScostamento());
		assertFalse(pila.isInMovimento());
	}

	@Test
	void laFrecciaLaCommutaELEspandiLaRiporta() {
		pila.commuta();
		finisce();
		assertEquals(80, pila.getScostamento());
		assertFalse(pila.isVersoEspansa());
		pila.commuta();
		finisce();
		assertEquals(0, pila.getScostamento());
		pila.commuta();
		finisce();
		pila.espandi();
		assertTrue(pila.isVersoEspansa());
		finisce();
		assertEquals(0, pila.getScostamento());
		// Già espansa, espandi non la muove
		pila.espandi();
		assertFalse(pila.isInMovimento());
	}

	@Test
	void ilTutorialChiedeDiEspandereAncheMentreLaSiTrascina() {
		pila.prendi(500);
		pila.trascina(560);
		pila.espandi();
		assertFalse(pila.isTrascinata());
		pila.trascina(600);
		assertEquals(60, pila.getScostamento(), "il dito non la tiene più");
		finisce();
		assertEquals(0, pila.getScostamento());
	}
}
