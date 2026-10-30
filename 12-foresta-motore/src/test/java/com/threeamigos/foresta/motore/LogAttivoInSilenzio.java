package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Prima di ogni test del modulo il registro di log è acceso ma non scrive da nessuna parte: i testi che il gioco
 * costruisce per il log (i fornitori di {@code Logger.log}) vengono costruiti sempre, anche se un test precedente
 * aveva spento il registro con {@code DestinazioneDiLog.NESSUNA}, e quindi la copertura non dipende dall'ordine dei test.
 * Si registra da sola (autorilevamento delle estensioni, vedi {@code junit-platform.properties}).
 */
public final class LogAttivoInSilenzio implements BeforeEachCallback {

	private static final DestinazioneDiLog SILENZIOSA = new DestinazioneDiLog() {
		@Override
		public void scrivi(String messaggio) {
		}

		@Override
		public void scrivi(Exception e) {
		}
	};

	@Override
	public void beforeEach(ExtensionContext contesto) {
		Logger.impostaDestinazione(SILENZIOSA);
	}
}
