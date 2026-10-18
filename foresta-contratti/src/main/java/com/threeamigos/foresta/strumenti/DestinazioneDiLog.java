package com.threeamigos.foresta.strumenti;

/**
 * Dove va a finire quel che il gioco scrive con {@link Logger}. Il desktop usa {@link DestinazioneConsoleEFile};
 * una piattaforma che ha un registro suo (Logcat su Android, os_log su iOS) ne installa una con
 * {@link Logger#impostaDestinazione(DestinazioneDiLog)}, prima che il gioco scriva il primo messaggio.
 */
public interface DestinazioneDiLog {

	/**
	 * La destinazione che non scrive niente, per un'app pubblicata in cui il registro non serve a nessuno: con questa,
	 * {@link Logger#log(java.util.function.Supplier)} non costruisce nemmeno i testi.
	 */
	DestinazioneDiLog NESSUNA = new DestinazioneDiLog() {
		@Override
		public void scrivi(String messaggio) {
		}

		@Override
		public void scrivi(Exception e) {
		}

		@Override
		public boolean isAttiva() {
			return false;
		}
	};

	/**
	 * Se la destinazione scrive davvero. Una che non scrive (vedi {@link #NESSUNA}) risponde false, e chi sta per costruire
	 * un testo solo per il registro può risparmiarselo.
	 */
	default boolean isAttiva() {
		return true;
	}

	/**
	 * Scrive un messaggio.
	 */
	void scrivi(String messaggio);

	/**
	 * Scrive un'eccezione con la sua traccia.
	 */
	void scrivi(Exception e);

}
