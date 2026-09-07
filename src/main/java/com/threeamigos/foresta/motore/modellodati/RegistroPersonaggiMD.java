package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.Dado;

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

	public final PersonaggioMD getPersonaggioDisponibile() {
		return Dado.selezionaCasualmente(elencoIniziale);
	}

	public final int getNumeroDisponibili() {
		return elencoIniziale.size();
	}

	public final PersonaggioMD getPersonaggioCasuale() {
		if (!personaggiInLocazione.isEmpty()) {
            ArrayList<CoordinateMD> elencoCoordinate = new ArrayList<>(personaggiInLocazione.keySet());
			int indice = Dado.tiraAncheAUnaFaccia(elencoCoordinate.size()) - 1;
			CoordinateMD coordinate = elencoCoordinate.get(indice);
			PersonaggioMD personaggio = personaggiInLocazione.get(coordinate);
			personaggiInLocazione.remove(coordinate);
			return personaggio;
		}
		return null;
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
			StringTokenizer st = new StringTokenizer(line, PIPE);
			CoordinateMD coordinate = new CoordinateMD(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
			PersonaggioMD personaggio = new PersonaggioMD();
			personaggio.leggi(stream);
			personaggiInLocazione.put(coordinate, personaggio);
		}
	}
}
