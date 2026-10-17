package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gli identificativi degli intermezzi già mostrati nella partita corrente: un intermezzo
 * non ha progressi, quindi basta ricordare che è scattato. La logica vive in
 * {@code intermezzi.RegistroIntermezzi}.
 *
 * @author Stefano Reksten
 */
public class IntermezziMD implements Serializzabile {

	private final Set<String> intermezziScattati = new LinkedHashSet<>();

	// Quante volte il gruppo si è accampato nella partita corrente: usato da
	// IntermezzoAccampamento per scattare una volta per accampamento (non di più in uno
	// stesso ciclo), fino a un massimo di occorrenze.
	private int numeroAccampamenti;

	/**
	 * Quante volte il gruppo si è accampato nella partita corrente.
	 */
	public int getNumeroAccampamenti() {
		return numeroAccampamenti;
	}

	/**
	 * Dice se quell'intermezzo è già stato mostrato in questa partita.
	 */
	public boolean isScattato(String id) {
		return intermezziScattati.contains(id);
	}

	/**
	 * Ricorda che quell'intermezzo è stato mostrato.
	 */
	public void aggiungiScattato(String id) {
		intermezziScattati.add(id);
	}

	/**
	 * Conta un accampamento in più.
	 */
	public void incrementaNumeroAccampamenti() {
		numeroAccampamenti++;
	}

	/**
	 * Dimentica gli intermezzi scattati e gli accampamenti: si ricomincia da una partita nuova.
	 */
	public void reimposta() {
		intermezziScattati.clear();
		numeroAccampamenti = 0;
	}

	/**
	 * Scrive il numero degli intermezzi scattati, un identificativo per riga, e poi il numero degli accampamenti.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(intermezziScattati.size());
		for (String id : intermezziScattati) {
			stream.println(id);
		}
		stream.println(numeroAccampamenti);
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo gli intermezzi scattati ricordati finora.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		intermezziScattati.clear();
		// LettoreCampi lancia IOException su un file troncato (readLine nullo)
		int numero = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numero; i++) {
			intermezziScattati.add(new LettoreCampi(stream.readLine()).testo());
		}
		numeroAccampamenti = LettoreCampi.leggiIntero(stream);
	}

}
