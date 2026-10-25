package com.threeamigos.foresta.gruppi;

import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.*;

/**
 * Il gruppo dei mostri che il gruppo del giocatore affronta in una locazione. Esiste una sola istanza, che si svuota a ogni locazione; tiene anche
 * chi è caduto ed è stato tolto dal gruppo prima di essere contato (vedi {@link #getCadutiRimossi()}).
 */
public class GruppoAvversario extends Gruppo {

	// Chi è caduto ed è stato tolto dal gruppo prima che lo scontro lo contasse (il minion della Mietitura, che muore e viene sostituito da uno
	// scheletro del giocatore): chi conta gli sconfitti lo trova qui (vedi getCadutiRimossi)
	private final List<Personaggio> cadutiRimossi = new ArrayList<>();

	private GruppoAvversario() {
		super();
	}
	
	private static GruppoAvversario gruppoAvversario;
	
	public static GruppoAvversario getIstanza() {
		if (gruppoAvversario == null) {
			gruppoAvversario = new GruppoAvversario();
		}
		return gruppoAvversario;
	}

	/**
	 * Dimentica il gruppo avversario corrente. Serve ai test (vedi GruppoGiocatore.azzeraIstanza).
	 */
	public static void azzeraIstanza() {
		gruppoAvversario = null;
	}

	@Override
	public void reimposta() {
		super.reimposta();
		// Le scorte che i mostri avevano con sé (vedi Gruppo.getGruppoMD) se ne vanno con loro
		getGruppoMD().reimposta();
		cadutiRimossi.clear();
	}

	/**
	 * Toglie il personaggio dal gruppo; se era già fuori combattimento lo ricorda fra {@link #getCadutiRimossi()}, così non sfugge al conteggio
	 * degli sconfitti.
	 */
	@Override
	public void rimuoviPersonaggio(Personaggio p) {
		if (p.isFuoriCombattimento() && personaggi.contains(p) && !cadutiRimossi.contains(p)) {
			cadutiRimossi.add(p);
		}
		super.rimuoviPersonaggio(p);
	}

	@Override
	public void rimuoviPersonaggi() {
		super.rimuoviPersonaggi();
		cadutiRimossi.clear();
	}

	/**
	 * Chi è caduto ed è stato tolto dal gruppo (un minion mietuto) da quando il gruppo si è formato.
	 */
	public List<Personaggio> getCadutiRimossi() {
		return Collections.unmodifiableList(cadutiRimossi);
	}

	@Override
	public boolean isGruppoGiocatore() {
		return false;
	}

	/**
	 * L'avversario prende come ordinale la sua posizione fra i mostri della sua specie nel gruppo (il primo scheletro, il secondo
	 * scheletro, il primo goblin, il terzo scheletro...), che serve a distinguerli (vedi PersonaggioBase.getNome): così il gruppo può
	 * essere eterogeneo, ogni specie conta per sé.
	 */
	@Override
	public void aggiungiPersonaggio(Personaggio p) {
		p.setOrdinale(contaDellaStessaSpecie(p) + 1);
		super.aggiungiPersonaggio(p);
	}

	/**
	 * Quanti personaggi del gruppo (anche caduti) hanno la stessa specie di {@code p}, lui compreso se è già nel gruppo.
	 */
	public int contaDellaStessaSpecie(Personaggio p) {
		String specie = p.getNomeSingolare();
		int quanti = 0;
		for (Personaggio altro : personaggi) {
			if (specie.equals(altro.getNomeSingolare())) {
				quanti++;
			}
		}
		return quanti;
	}

	public Personaggio getPersonaggioVivo() {
		for (Personaggio personaggio : personaggi) {
			if (!personaggio.isFuoriCombattimento()) {
				return personaggio;
			}
		}
		return null;
	}
}
