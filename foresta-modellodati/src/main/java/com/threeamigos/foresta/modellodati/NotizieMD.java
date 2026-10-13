package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gli ultimi messaggi mostrati al giocatore nel pannello di testo (per poterlo ripopolare dopo un ricaricamento) e le
 * ultime notizie destinate alla schermata della mappa, salvati con la partita.
 * <p>
 * La logica di aggiunta (testa della lista, scarto delle più vecchie oltre il limite) vive in
 * {@code motore.Notizie}, non qui: questa classe tiene solo i dati.
 *
 * @author Stefano Reksten
 */
public class NotizieMD implements Serializzabile {

	/**
	 * Quanti messaggi si ricordano. Lo storico scorrevole del pannello di testo (ui.DoomdarkTextRectangle2x)
	 * ha lo stesso limite, così che dopo un ricaricamento il pannello possa essere ripopolato per intero.
	 */
	public static final int MASSIMO_MESSAGGI_RICORDATI = 100;

	/**
	 * Quante notizie delle locande si ricordano (e quante ne mostra il notiziario della mappa)
	 */
	public static final int MASSIMO_NOTIZIE_RICORDATE = 10;

	// Le più recenti in testa, le più vecchie in coda.
	private final List<MessaggioMD> ultimiMessaggi = new ArrayList<>();
	private final List<NotiziaMD> ultimeNotizie = new ArrayList<>();

	/**
	 * I messaggi ricordati, dal più recente al più vecchio, in numero non superiore a {@value #MASSIMO_MESSAGGI_RICORDATI}.
	 */
	public List<MessaggioMD> getUltimiMessaggi() {
		return ultimiMessaggi;
	}

	/**
	 * Le notizie ricordate, dalla più recente alla più vecchia, in numero non superiore a {@value #MASSIMO_NOTIZIE_RICORDATE}.
	 */
	public List<NotiziaMD> getUltimeNotizie() {
		return ultimeNotizie;
	}

	/**
	 * Dimentica messaggi e notizie.
	 */
	public void reimposta() {
		ultimiMessaggi.clear();
		ultimeNotizie.clear();
	}

	/**
	 * Scrive il numero dei messaggi e i messaggi, poi il numero delle notizie e le notizie.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(ultimiMessaggi.size());
		for (MessaggioMD messaggioMD : ultimiMessaggi) {
			messaggioMD.salva(stream);
		}
		stream.println(ultimeNotizie.size());
		for (NotiziaMD notizia : ultimeNotizie) {
			notizia.salva(stream);
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo messaggi e notizie ricordati finora.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		ultimiMessaggi.clear();
		int numeroMessaggi = Integer.parseInt(stream.readLine());
		for (int i = 0; i < numeroMessaggi; i++) {
			MessaggioMD messaggioMD = new MessaggioMD();
			messaggioMD.leggi(stream);
			ultimiMessaggi.add(messaggioMD);
		}
		ultimeNotizie.clear();
		int numeroNotizie = Integer.parseInt(stream.readLine());
		for (int i = 0; i < numeroNotizie; i++) {
			NotiziaMD notizia = new NotiziaMD();
			notizia.leggi(stream);
			ultimeNotizie.add(notizia);
		}
	}
}
