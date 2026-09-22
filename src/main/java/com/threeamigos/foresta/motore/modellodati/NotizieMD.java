package com.threeamigos.foresta.motore.modellodati;

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

	// Le più recenti in testa, le più vecchie in coda.
	private final List<String> ultimiMessaggi = new ArrayList<>();
	private final List<Notizia> ultimeNotizie = new ArrayList<>();

	public List<String> getUltimiMessaggi() {
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
		for (String messaggio : ultimiMessaggi) {
			stream.println(messaggio);
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
			ultimiMessaggi.add(stream.readLine());
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
