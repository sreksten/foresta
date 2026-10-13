package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class RegistroPersonaggiMD implements Serializzabile {

	private final List<PersonaggioMD> elencoIniziale = new ArrayList<>();

	private final Map<CoordinateMD, PersonaggioMD> personaggiInLocazione = new HashMap<>();

	// Chi ha un nome nel mondo senza stare in una locazione (vedi tipi.PersonaggioNoto), per chiave
	private final Map<String, ResidenteMD> residenti = new LinkedHashMap<>();

	public void reimposta() {
		elencoIniziale.clear();
		personaggiInLocazione.clear();
		residenti.clear();
	}

	public final ResidenteMD getResidente(String chiave) {
		return residenti.get(chiave);
	}

	public final void aggiungiResidente(String chiave, ResidenteMD residente) {
		residenti.put(chiave, residente);
	}

	public final Map<String, ResidenteMD> getResidenti() {
		return Collections.unmodifiableMap(residenti);
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
		stream.println(residenti.size());
		for (Map.Entry<String, ResidenteMD> entry : residenti.entrySet()) {
			entry.getValue().salva(stream, entry.getKey());
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
		residenti.clear();
		int numeroResidenti = Integer.parseInt(stream.readLine());
		for (int i = 0; i < numeroResidenti; i++) {
			LettoreCampi campi = new LettoreCampi(stream.readLine());
			String chiave = campi.testo();
			residenti.put(chiave, ResidenteMD.leggi(campi));
		}
	}
}
