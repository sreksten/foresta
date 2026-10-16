package com.threeamigos.foresta.strumenti;

/**
 * Il registro di log del gioco. Dove scrive lo decide la {@link DestinazioneDiLog}: se nessuno ne installa una, quella
 * del desktop ({@link DestinazioneConsoleEFile}: console e, con {@code -Dforesta.log=cartella}, un file per sessione),
 * creata al primo messaggio.
 */
public class Logger {

	/**
	 * La cartella dei file di log, se indicata con {@code -Dforesta.log=cartella}: senza, si scrive solo sulla console.
	 * Una sessione di gioco produce migliaia di righe, quindi conviene puntarla a un disco in RAM
	 * (vedi la nota su macOS in {@link DestinazioneConsoleEFile}) per non consumare il disco.
	 */
	public static final String PROPRIETA_CARTELLA_DI_LOG = "foresta.log";

	private static volatile DestinazioneDiLog destinazione;

	private Logger() {
	}

	/**
	 * Cambia la destinazione dei messaggi, da fare prima del primo messaggio (una destinazione già creata, per esempio
	 * quella su file, resta aperta). Con null si torna a quella del desktop.
	 */
	public static void impostaDestinazione(DestinazioneDiLog nuovaDestinazione) {
		destinazione = nuovaDestinazione;
	}

	private static DestinazioneDiLog destinazione() {
		DestinazioneDiLog corrente = destinazione;
		if (corrente == null) {
			synchronized (Logger.class) {
				corrente = destinazione;
				if (corrente == null) {
					corrente = new DestinazioneConsoleEFile();
					destinazione = corrente;
				}
			}
		}
		return corrente;
	}

	/**
	 * Scrive un messaggio sulla destinazione di log.
	 */
	public static void log(String messaggio) {
		destinazione().scrivi(messaggio);
	}

	/**
	 * Scrive un'eccezione con la sua traccia sulla destinazione di log.
	 */
	public static void log(Exception e) {
		destinazione().scrivi(e);
	}
}
