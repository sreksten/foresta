package com.threeamigos.foresta.strumenti.logger;

import java.util.function.Supplier;

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

	/**
	 * Cambia la destinazione dei messaggi, da fare prima del primo messaggio (una destinazione già creata, per esempio
	 * quella su file, resta aperta). Con null si torna a quella del desktop.
	 */
	public static void impostaDestinazione(DestinazioneDiLog nuovaDestinazione) {
		destinazione = nuovaDestinazione;
	}

	private static DestinazioneDiLog destinazione() {
		DestinazioneDiLog corrente = destinazione;
		return corrente != null ? corrente : creaLaDestinazionePredefinita();
	}

	// La destinazione del desktop, creata una volta sola alla prima necessità (e non all'avvio, perché una piattaforma ne può
	// installare una sua prima che serva): se due thread arrivano insieme, il secondo trova già quella del primo
	static synchronized DestinazioneDiLog creaLaDestinazionePredefinita() {
		if (destinazione == null) {
			destinazione = new DestinazioneConsoleEFile();
		}
		return destinazione;
	}

	/**
	 * Scrive un messaggio sulla destinazione di log. Per un testo che va costruito (una concatenazione, un
	 * {@code String.format}) si usa {@link #log(Supplier)}: questo costruisce il testo anche quando nessuno lo scrive.
	 */
	public static void log(String messaggio) {
		destinazione().scrivi(messaggio);
	}

	/**
	 * Scrive il messaggio che il fornitore costruisce, ma solo se la destinazione scrive davvero: con
	 * {@link DestinazioneDiLog#NESSUNA} il testo non viene nemmeno costruito, e il costo di una concatenazione o di un
	 * {@code String.format} (che nei calcoli del combattimento si paga a ogni colpo) sparisce. È la forma da usare per ogni
	 * testo costruito: {@code Logger.log(() -> "Colpo a " + nome)}. Le variabili usate nell'espressione devono essere
	 * {@code final} o non cambiare più.
	 */
	public static void log(Supplier<String> messaggio) {
		DestinazioneDiLog corrente = destinazione();
		if (corrente.isAttiva()) {
			corrente.scrivi(messaggio.get());
		}
	}

	/**
	 * Se il registro scrive davvero, per chi deve fare qualcosa di più di un testo per prepararlo.
	 */
	public static boolean isAttivo() {
		return destinazione().isAttiva();
	}

	/**
	 * Scrive un'eccezione con la sua traccia sulla destinazione di log.
	 */
	public static void log(Exception e) {
		destinazione().scrivi(e);
	}

	private Logger() {
	}

}
