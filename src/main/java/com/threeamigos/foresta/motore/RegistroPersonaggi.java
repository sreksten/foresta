package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.motore.modellodati.RegistroPersonaggiMD;
import com.threeamigos.foresta.personaggi.*;

public class RegistroPersonaggi {

	private RegistroPersonaggi() {
	}
	
	private static final RegistroPersonaggiMD registroMD = ModelloDati.getIstanza().getRegistroPersonaggiMD();

	static void reimposta() {
		registroMD.reimposta();
		
		aggiungiPersonaggio(new Guerriero("Reginald"));
		aggiungiPersonaggio(new Guerriera("Elleran"));
		aggiungiPersonaggio(new Guerriero("Roderick"));
		aggiungiPersonaggio(new Guerriera("Wendy"));
		aggiungiPersonaggio(new Guerriero("Frederick"));

		aggiungiPersonaggio(new Ladro("Gabriel"));
		aggiungiPersonaggio(new Ladra("Filean"));
		aggiungiPersonaggio(new Ladro("Raven"));
		aggiungiPersonaggio(new Ladra("Wyan"));

		aggiungiPersonaggio(new Bardo("Samuel"));
		aggiungiPersonaggio(new Cantastorie("Gwendolyn"));

		aggiungiPersonaggio(new Elfo("Lamiel"));
		aggiungiPersonaggio(new Elfa("Yluviel"));

		aggiungiPersonaggio(new Mago("Merlin"));
		aggiungiPersonaggio(new Maga("LeFey"));
	}

	static Personaggio getPersonaggioDisponibile() {
		return costruisciPersonaggio(registroMD.getPersonaggioDisponibile());
	}

	static Personaggio getPersonaggioCasuale() {
		return costruisciPersonaggio(registroMD.getPersonaggioCasuale());
	}

	public static void addPersonaggioInLocazione(Personaggio personaggio, CoordinateMD coordinate) {
		registroMD.addPersonaggioInLocazione(personaggio.getModelloDati(), coordinate);
	}
	
	public static Personaggio getPersonaggioInLocazione(CoordinateMD coordinate) {
		return costruisciPersonaggio(registroMD.getPersonaggioInLocazione(coordinate));
	}

	public static void rimuoviPersonaggioInLocazione(CoordinateMD coordinate) {
		registroMD.rimuoviPersonaggioInLocazione(coordinate);
	}
	
	private static void aggiungiPersonaggio(Personaggio personaggio) {
		registroMD.aggiungiPersonaggio(personaggio.getModelloDati());
	}
	
	private static Personaggio costruisciPersonaggio(PersonaggioMD modelloDati) {
		if (modelloDati == null) {
			return null;
		}
		Personaggio personaggio = modelloDati.getClasse().getIstanza();
		personaggio.setModelloDati(modelloDati);
		return personaggio;
	}
}
