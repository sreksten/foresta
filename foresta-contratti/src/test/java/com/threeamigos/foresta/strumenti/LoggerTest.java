package com.threeamigos.foresta.strumenti;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La destinazione del Logger si può cambiare, come fa una piattaforma che ha un registro suo.
 */
class LoggerTest {

	@AfterEach
	void tornaAlDesktop() {
		Logger.impostaDestinazione(null);
	}

	@Test
	void iMessaggiVannoAllaDestinazioneInstallata() {
		// Given
		List<String> scritto = new ArrayList<>();
		Logger.impostaDestinazione(new DestinazioneDiLog() {
			@Override
			public void scrivi(String messaggio) {
				scritto.add(messaggio);
			}

			@Override
			public void scrivi(Exception e) {
				scritto.add("eccezione: " + e.getMessage());
			}
		});

		// When
		Logger.log("ciao");
		Logger.log(new IllegalStateException("rotto"));

		// Then
		assertEquals(2, scritto.size());
		assertEquals("ciao", scritto.get(0));
		assertEquals("eccezione: rotto", scritto.get(1));
	}

	@Test
	void ilFornitoreDelTestoSiChiamaSoloSeLaDestinazioneScrive() {
		// Given
		List<String> scritto = new ArrayList<>();
		Logger.impostaDestinazione(new DestinazioneDiLog() {
			@Override
			public void scrivi(String messaggio) {
				scritto.add(messaggio);
			}

			@Override
			public void scrivi(Exception e) {
			}
		});
		AtomicInteger costruzioni = new AtomicInteger();

		// When
		Logger.log(() -> "costruito " + costruzioni.incrementAndGet());

		// Then
		assertTrue(Logger.isAttivo());
		assertEquals(1, costruzioni.get());
		assertEquals(Arrays.asList("costruito 1"), scritto);
	}

	@Test
	void conLaDestinazioneNessunaIlTestoNonVieneNemmenoCostruito() {
		// Given
		Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
		AtomicInteger costruzioni = new AtomicInteger();

		// When
		Logger.log(() -> "costruito " + costruzioni.incrementAndGet());
		Logger.log("anche un testo gia' pronto non va da nessuna parte");
		Logger.log(new IllegalStateException("rotto"));

		// Then
		assertFalse(Logger.isAttivo());
		assertEquals(0, costruzioni.get(), "il fornitore non e' stato chiamato");
	}
}
