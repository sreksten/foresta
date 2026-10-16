package com.threeamigos.foresta.strumenti;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
