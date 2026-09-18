package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.motore.modellodati.RegistroPersonaggiMD;
import com.threeamigos.foresta.personaggi.*;

public class RegistroPersonaggi {

	private RegistroPersonaggi() {
	}

	private static RegistroPersonaggiMD getRegistroMD() {
		return ModelloDati.getIstanza().getRegistroPersonaggiMD();
	}

	static void reimposta() {
		getRegistroMD().reimposta();
		
		aggiungiPersonaggio(new Guerriero("Reginald", 1));
		aggiungiPersonaggio(new Guerriera("Elleran", 1));
		aggiungiPersonaggio(new Guerriero("Roderick", 1));
		aggiungiPersonaggio(new Guerriera("Wendy", 1));
		aggiungiPersonaggio(new Guerriero("Frederick", 1));

		aggiungiPersonaggio(new Ladro("Gabriel", 1));
		aggiungiPersonaggio(new Ladra("Filean", 1));
		aggiungiPersonaggio(new Ladro("Raven", 1));
		aggiungiPersonaggio(new Ladra("Wyan", 1));

		aggiungiPersonaggio(new Bardo("Samuel", 1));
		aggiungiPersonaggio(new Cantastorie("Gwendolyn", 1));

		aggiungiPersonaggio(new Elfo("Lamiel", 1));
		aggiungiPersonaggio(new Elfa("Yluviel", 1));

		aggiungiPersonaggio(new Mago("Merlin", 1));
		aggiungiPersonaggio(new Maga("LeFey", 1));
	}

	static Personaggio getPersonaggioDisponibile() {
		return costruisciPersonaggio(getRegistroMD().getPersonaggioDisponibile());
	}

	static int getNumeroPersonaggiDisponibili() {
		return getRegistroMD().getNumeroDisponibili();
	}

	static Personaggio getPersonaggioCasuale() {
		return costruisciPersonaggio(getRegistroMD().getPersonaggioCasuale());
	}

	public static void addPersonaggioInLocazione(Personaggio personaggio, CoordinateMD coordinate) {
		getRegistroMD().addPersonaggioInLocazione(personaggio.getModelloDati(), coordinate);
	}
	
	public static Personaggio getPersonaggioInLocazione(CoordinateMD coordinate) {
		return costruisciPersonaggio(getRegistroMD().getPersonaggioInLocazione(coordinate));
	}

	public static void rimuoviPersonaggioInLocazione(CoordinateMD coordinate) {
		getRegistroMD().rimuoviPersonaggioInLocazione(coordinate);
	}
	
	private static void aggiungiPersonaggio(Personaggio personaggio) {
		getRegistroMD().aggiungiPersonaggio(personaggio.getModelloDati());
	}
	
	private static Personaggio costruisciPersonaggio(PersonaggioMD modelloDati) {
		if (modelloDati == null) {
			return null;
		}
		Personaggio personaggio = modelloDati.getClasse().getIstanza(1);
		personaggio.setModelloDati(modelloDati);
		return personaggio;
	}
}
