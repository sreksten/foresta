package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class RegistroPersonaggiMD implements Serializzabile {

	private final List<PersonaggioMD> elencoIniziale = new ArrayList<>();

	private final Map<CoordinateMD, PersonaggioMD> personaggiInLocazione = new HashMap<>();

	public void reimposta() {
		elencoIniziale.clear();
		personaggiInLocazione.clear();
	}

	public void aggiungiPersonaggio(PersonaggioMD personaggioMD) {
		elencoIniziale.add(personaggioMD);
	}

	/**
	 * Toglie dall'elenco iniziale il personaggio in quella posizione (la sceglie a caso il motore).
	 */
	public final PersonaggioMD rimuoviDisponibile(int indice) {
		return elencoIniziale.remove(indice);
	}

	public final int getNumeroDisponibili() {
		return elencoIniziale.size();
	}

	public final Set<CoordinateMD> getUbicazioniPersonaggi() {
		return Collections.unmodifiableSet(personaggiInLocazione.keySet());
	}

	public final void addPersonaggioInLocazione(PersonaggioMD personaggio, CoordinateMD coordinate) {
		personaggiInLocazione.put(coordinate, personaggio);
	}

	public final PersonaggioMD getPersonaggioInLocazione(CoordinateMD coordinate) {
		return personaggiInLocazione.get(coordinate);
	}

	public final void rimuoviPersonaggioInLocazione(CoordinateMD coordinate) {
		personaggiInLocazione.remove(coordinate);
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(personaggiInLocazione.size());
		for (Map.Entry<CoordinateMD, PersonaggioMD> entry : personaggiInLocazione.entrySet()) {
			stream.print(entry.getKey().getX());
			stream.print(PIPE);
			stream.println(entry.getKey().getY());
			entry.getValue().salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		int numeroPersonaggi = Integer.parseInt(line);
		for (int i = 0; i < numeroPersonaggi; i++) {
			line = stream.readLine();
			LettoreCampi st = new LettoreCampi(line);
			CoordinateMD coordinate = new CoordinateMD(Integer.parseInt(st.testo()), Integer.parseInt(st.testo()));
			PersonaggioMD personaggio = new PersonaggioMD();
			personaggio.leggi(stream);
			personaggiInLocazione.put(coordinate, personaggio);
		}
	}
}
