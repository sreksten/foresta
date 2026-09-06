package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GruppoMD implements Serializzabile {

	protected List<PersonaggioMD> personaggiMD = new ArrayList<>();

	public void addPersonaggioMD(PersonaggioMD personaggioMD) {
		personaggiMD.add(personaggioMD);
	}

	public List<PersonaggioMD> getPersonaggiMD() {
		return personaggiMD;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(personaggiMD.size());
		for (PersonaggioMD personaggio : personaggiMD) {
			personaggio.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		int numeroPersonaggi = Integer.parseInt(line);
		for (int i = 0; i < numeroPersonaggi; i++) {
			PersonaggioMD personaggioMD = new PersonaggioMD();
			personaggioMD.leggi(stream);
			Personaggio personaggio = personaggioMD.getClasse().getIstanza(1);
			// Ci pensa poi la rilettura a sistemare il livello del personaggio
			personaggio.setModelloDati(personaggioMD);
			GruppoGiocatore.getIstanza().aggiungiPersonaggioSenzaNotificare(personaggio);
		}
	}
}
