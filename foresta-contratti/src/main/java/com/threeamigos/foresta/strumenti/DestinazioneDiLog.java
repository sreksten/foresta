package com.threeamigos.foresta.strumenti;

/**
 * Dove va a finire quel che il gioco scrive con {@link Logger}. Il desktop usa {@link DestinazioneConsoleEFile};
 * una piattaforma che ha un registro suo (Logcat su Android, os_log su iOS) ne installa una con
 * {@link Logger#impostaDestinazione(DestinazioneDiLog)}, prima che il gioco scriva il primo messaggio.
 */
public interface DestinazioneDiLog {

	/**
	 * Scrive un messaggio.
	 */
	void scrivi(String messaggio);

	/**
	 * Scrive un'eccezione con la sua traccia.
	 */
	void scrivi(Exception e);
}
