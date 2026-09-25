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

	public boolean isScattato(String id) {
		return intermezziScattati.contains(id);
	}

	public void aggiungiScattato(String id) {
		intermezziScattati.add(id);
	}

	public void reimposta() {
		intermezziScattati.clear();
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(intermezziScattati.size());
		for (String id : intermezziScattati) {
			stream.println(id);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		intermezziScattati.clear();
		// LettoreCampi lancia IOException su un file troncato (readLine nullo)
		int numero = new LettoreCampi(stream.readLine()).intero();
		for (int i = 0; i < numero; i++) {
			intermezziScattati.add(new LettoreCampi(stream.readLine()).testo());
		}
	}
}
