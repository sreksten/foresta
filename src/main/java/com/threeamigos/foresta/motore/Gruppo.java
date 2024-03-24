package com.threeamigos.foresta.motore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un insieme di personaggi
 */

public abstract class Gruppo {
	
	// Caratteristiche del gruppo
	protected List<Personaggio> personaggi = new ArrayList<>();
	protected Personaggio capo; 

	protected void reimposta() {
		personaggi.clear();
		capo = null;
	}

	/**
	 * Riporta il numero di tutti i personaggi, compresi eventualmente
	 * quelli passati a miglior vita
	 */
	public final int getNumeroPersonaggi() {
		return personaggi.size();
	}

	public final int getNumeroPersonaggiVivi() {
		return getPersonaggiVivi().size();
	}

	public final List<Personaggio> getPersonaggi() {
		return personaggi;
	}
	
	public final List<Personaggio> getPersonaggiVivi() {
		return personaggi.stream().filter(Personaggio::isVivo).collect(Collectors.toList());
	}

	public void aggiungiPersonaggio(Personaggio p) {
		if (capo == null) {
			capo = p;
		}
		personaggi.add(p);
		
	}

	public final Personaggio getCapo() {
		return capo;
	}
	
	public final Personaggio getPersonaggio(int numero) {
		return personaggi.get(numero);
	}

	public final Personaggio getPersonaggio(Comando azione) {
		return personaggi.get(azione.ordinal() - Comando.PERSONAGGIO_1.ordinal());
	}

	public void rimuoviPersonaggio(Personaggio p) {
		if (p.equals(capo)) {
			capo = null;
		}
		personaggi.remove(p);
		if (!personaggi.isEmpty()) {
			capo = personaggi.get(0);
		}
	}

	public String chi() {
		return personaggi.size() > 1 ? "il gruppo" : capo.getNome();
	}

	public String chiMaiuscolo() {
		return personaggi.size() > 1 ? "Il gruppo" : capo.getNome();
	}
	
	public boolean contiene(Personaggio personaggio) {
		return personaggi.contains(personaggio);
	}
}
