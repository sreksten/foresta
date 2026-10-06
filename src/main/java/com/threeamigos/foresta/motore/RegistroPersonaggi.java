package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.modellodati.RegistroPersonaggiMD;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;

import java.util.ArrayList;
import java.util.List;

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

	/**
	 * Uno a caso fra quelli ancora da sistemare nella foresta, tolto dall'elenco; null se sono finiti.
	 */
	static Personaggio getPersonaggioDisponibile() {
		int disponibili = getRegistroMD().getNumeroDisponibili();
		if (disponibili == 0) {
			return null;
		}
		return costruisciPersonaggio(getRegistroMD().rimuoviDisponibile(Dado.tiraAncheAUnaFaccia(disponibili) - 1));
	}

	static int getNumeroPersonaggiDisponibili() {
		return getRegistroMD().getNumeroDisponibili();
	}

	/**
	 * Uno a caso fra quelli che aspettano in una locazione, tolto dalla sua locazione; null se non ce ne sono.
	 */
	static Personaggio getPersonaggioCasuale() {
		List<CoordinateMD> ubicazioni = new ArrayList<>(getRegistroMD().getUbicazioniPersonaggi());
		if (ubicazioni.isEmpty()) {
			return null;
		}
		CoordinateMD coordinate = ubicazioni.get(Dado.tiraAncheAUnaFaccia(ubicazioni.size()) - 1);
		PersonaggioMD personaggio = getRegistroMD().getPersonaggioInLocazione(coordinate);
		getRegistroMD().rimuoviPersonaggioInLocazione(coordinate);
		return costruisciPersonaggio(personaggio);
	}

	/**
	 * Il compagno che entra nel gruppo. Quelli del registro nascono al livello 1 insieme al mondo: chi entra arriva
	 * al livello del mondo meno un numero a caso fra 0 e 2 (mai sotto 1), con caratteristiche tirate come per
	 * chiunque nasca a quel livello e il suo nome, e con l'equipaggiamento di base della sua classe.
	 */
	public static Personaggio preparaCompagno(Personaggio compagno) {
		int livello = EquipaggiamentoIniziale.livelloCasualeDalMondo();
		Personaggio pronto = compagno;
		if (livello > compagno.getLivello()) {
			pronto = FabbricaPersonaggi.crea(compagno.getClasse(), livello);
			pronto.getModelloDati().setNome(compagno.getNomeProprio().orElse(null));
		}
		EquipaggiamentoIniziale.equipaggia(pronto);
		return pronto;
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
		Personaggio personaggio = FabbricaPersonaggi.crea(modelloDati.getClasse(), 1);
		personaggio.setModelloDati(modelloDati);
		return personaggio;
	}
}
