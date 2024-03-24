package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.motore.modellodati.RegistroPersonaggiMD;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Cantastorie;
import com.threeamigos.foresta.personaggi.Elfa;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.Guerriera;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladra;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Maga;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;

public class RegistroPersonaggi {

	private RegistroPersonaggi() {
	}
	
	private static RegistroPersonaggiMD registroMD = ModelloDati.getIstanza().getRegistroPersonaggiMD();

	static final void reimposta() {
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

	public static final void addPersonaggioInLocazione(Personaggio personaggio, CoordinateMD coordinate) {
		registroMD.addPersonaggioInLocazione(personaggio.getModelloDati(), coordinate);
	}
	
	public static final Personaggio getPersonaggioInLocazione(CoordinateMD coordinate) {
		return costruisciPersonaggio(registroMD.getPersonaggioInLocazione(coordinate));
	}

	public static final void rimuoviPersonaggioInLocazione(CoordinateMD coordinate) {
		registroMD.rimuoviPersonaggioInLocazione(coordinate);
	}
	
	private static final void aggiungiPersonaggio(Personaggio personaggio) {
		registroMD.aggiungiPersonaggio(personaggio.getModelloDati());
	}
	
	private static final Personaggio costruisciPersonaggio(PersonaggioMD modelloDati) {
		if (modelloDati == null) {
			return null;
		}
		Personaggio personaggio = modelloDati.getClasse().getIstanza();
		personaggio.setModelloDati(modelloDati);
		return personaggio;
	}
}
