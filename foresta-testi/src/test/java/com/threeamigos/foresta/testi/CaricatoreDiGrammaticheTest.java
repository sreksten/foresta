package com.threeamigos.foresta.testi;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il caricamento delle grammatiche del gioco: una che manca o non è valida solleva un'eccezione che dice quale e perché, e ognuna si
 * carica una volta sola, la prima volta che serve.
 */
class CaricatoreDiGrammaticheTest {

	@Test
	void unaGrammaticaDeiTestiSiCaricaConEConLeSostituzioniDelleSuePreposizioni() {
		assertNotNull(CaricatoreDiGrammatiche.caricaDeiTesti("fiabe.txt", true));
		assertNotNull(CaricatoreDiGrammatiche.caricaDeiTesti("sfide.txt", false));
	}

	@Test
	void unaGrammaticaCheMancaSollevaUnaEccezioneCheDiceLaRisorsaEIlMotivo() {
		GrammaticaNonValidaException e = assertThrows(GrammaticaNonValidaException.class,
				() -> CaricatoreDiGrammatiche.caricaDeiTesti("non_esiste.txt", false));

		assertEquals(CaricatoreDiGrammatiche.CARTELLA_TESTI + "non_esiste.txt", e.getRisorsa());
		assertTrue(e.getMessage().contains("non_esiste.txt"), e.getMessage());
		assertTrue(e.getMessage().contains("Grammar resource not found"), "dice anche perché: " + e.getMessage());
		assertTrue(e.getCause() instanceof GrammarBean.InvalidGrammarException);
	}

	@Test
	void unaPostproduzioneCheMancaSollevaLaStessaEccezione() {
		GrammaticaNonValidaException e = assertThrows(GrammaticaNonValidaException.class,
				() -> CaricatoreDiGrammatiche.carica(CaricatoreDiGrammatiche.class, CaricatoreDiGrammatiche.CARTELLA_TESTI + "fiabe.txt", "/manca_pp.txt"));

		assertTrue(e.getMessage().contains("Post-production resource not found"), e.getMessage());
	}

	@Test
	void ilCicloDiVitaDeiProduttoriCaricaETuttoESiAzzera() {
		ProduttoriDiTesti.precarica();
		ProduttoriDiTesti.resetProduzioni();
	}

	@Test
	void unaGrammaticaPigraSiCaricaUnaVoltaSola() {
		AtomicInteger caricamenti = new AtomicInteger();
		GrammaticaPigra pigra = new GrammaticaPigra(() -> {
			caricamenti.incrementAndGet();
			return CaricatoreDiGrammatiche.caricaDeiTesti("sfide.txt", false);
		});
		assertEquals(0, caricamenti.get(), "non si carica finché non serve");

		GrammarBean prima = pigra.get();

		assertSame(prima, pigra.get());
		assertSame(prima, pigra.carica(), "se è già caricata, la riusa");
		assertEquals(1, caricamenti.get());
	}

	@Test
	void unaGrammaticaPigraCheNonSiCaricaRiprovaAllaRichiestaSuccessiva() {
		AtomicInteger tentativi = new AtomicInteger();
		GrammaticaPigra pigra = new GrammaticaPigra(() -> {
			if (tentativi.incrementAndGet() == 1) {
				return CaricatoreDiGrammatiche.caricaDeiTesti("non_esiste.txt", false);
			}
			return CaricatoreDiGrammatiche.caricaDeiTesti("sfide.txt", false);
		});

		assertThrows(GrammaticaNonValidaException.class, pigra::get);

		assertNotNull(pigra.get());
		assertEquals(2, tentativi.get());
	}
}
