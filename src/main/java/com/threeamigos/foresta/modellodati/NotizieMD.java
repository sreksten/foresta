package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modello dati di sola memoria: gli ultimi messaggi mostrati al giocatore nel
 * pannello di testo (per poterlo ripopolare dopo un ricaricamento) e le ultime
 * notizie destinate alla schermata della mappa. La logica di aggiunta (testa
 * della lista, scarto delle più vecchie oltre il limite) vive in {@link
 * com.threeamigos.foresta.motore.Notizie}, non qui: questa classe tiene solo i dati.
 *
 * @author Stefano Reksten
 */
public class NotizieMD implements Serializzabile {

	/**
	 * Quanti messaggi si ricordano. Lo storico scorrevole del pannello di testo (ui.DoomdarkTextRectangle2x)
	 * ha lo stesso limite, così che dopo un ricaricamento il pannello possa essere ripopolato per intero.
	 */
	public static final int MASSIMO_MESSAGGI_RICORDATI = 100;

	// Le più recenti in testa, le più vecchie in coda.
	private final List<MessaggioMD> ultimiMessaggi = new ArrayList<>();
	private final List<Notizia> ultimeNotizie = new ArrayList<>();

	public List<MessaggioMD> getUltimiMessaggi() {
		return ultimiMessaggi;
	}

	public List<Notizia> getUltimeNotizie() {
		return ultimeNotizie;
	}

	public void reimposta() {
		ultimiMessaggi.clear();
		ultimeNotizie.clear();
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(ultimiMessaggi.size());
		for (MessaggioMD messaggioMD : ultimiMessaggi) {
			messaggioMD.salva(stream);
		}
		stream.println(ultimeNotizie.size());
		for (Notizia notizia : ultimeNotizie) {
			notizia.salva(stream);
		}
	}

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
			Notizia notizia = new Notizia();
			notizia.leggi(stream);
			ultimeNotizie.add(notizia);
		}
	}
}
