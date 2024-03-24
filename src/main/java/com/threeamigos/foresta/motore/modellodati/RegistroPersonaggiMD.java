package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.threeamigos.foresta.tools.Random;

public class RegistroPersonaggiMD implements Serializzabile {

	private List<PersonaggioMD> elencoIniziale = new ArrayList<>();

	private Map<CoordinateMD, PersonaggioMD> personaggiInLocazione = new HashMap<>();

	public void reimposta() {
		elencoIniziale.clear();
		personaggiInLocazione.clear();
	}

	public void aggiungiPersonaggio(PersonaggioMD personaggioMD) {
		elencoIniziale.add(personaggioMD);
	}

	public final PersonaggioMD getPersonaggioDisponibile() {
		PersonaggioMD personaggio = null;
		int size = elencoIniziale.size();
		if (size > 0) {
			int quale = Random.getInt(size);
			personaggio = elencoIniziale.get(quale);
			elencoIniziale.remove(quale);
		}
		return personaggio;
	}

	public final PersonaggioMD getPersonaggioCasuale() {
		if (!personaggiInLocazione.isEmpty()) {			
			ArrayList<CoordinateMD> elencoCoordinate = new ArrayList<>();
			elencoCoordinate.addAll(personaggiInLocazione.keySet());
			int indice = Random.getInt(elencoCoordinate.size());
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
