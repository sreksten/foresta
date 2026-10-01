package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gli identificativi degli intermezzi già mostrati nella partita corrente: un intermezzo
 * non ha progressi, quindi basta ricordare che è scattato. La logica vive in
 * {@link com.threeamigos.foresta.motore.RegistroIntermezzi}.
 *
 * @author Stefano Reksten
 */
public class IntermezziMD implements Serializzabile {

	private final Set<String> intermezziScattati = new LinkedHashSet<>();

	// Quante volte il gruppo si è accampato nella partita corrente: usato da
	// IntermezzoAccampamento per scattare una volta per accampamento (non di più in uno
	// stesso ciclo), fino a un massimo di occorrenze.
	private int numeroAccampamenti;

	public boolean isScattato(String id) {
		return intermezziScattati.contains(id);
	}

	public void aggiungiScattato(String id) {
		intermezziScattati.add(id);
	}

	public int getNumeroAccampamenti() {
		return numeroAccampamenti;
	}

	public void incrementaNumeroAccampamenti() {
		numeroAccampamenti++;
	}

	public void reimposta() {
		intermezziScattati.clear();
		numeroAccampamenti = 0;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(intermezziScattati.size());
		for (String id : intermezziScattati) {
			stream.println(id);
		}
		stream.println(numeroAccampamenti);
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		intermezziScattati.clear();
		// LettoreCampi lancia IOException su un file troncato (readLine nullo)
		int numero = new LettoreCampi(stream.readLine()).intero();
		for (int i = 0; i < numero; i++) {
			intermezziScattati.add(new LettoreCampi(stream.readLine()).testo());
		}
		numeroAccampamenti = new LettoreCampi(stream.readLine()).intero();
	}
}
