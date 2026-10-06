package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Un insieme di personaggi
 */

public abstract class Gruppo implements VistaGruppo {
	
	// Caratteristiche del gruppo
	protected List<Personaggio> personaggi = new ArrayList<>();
	protected Personaggio capo;
	private int ultimoIndiceSelezionato;

	protected void reimposta() {
		personaggi.clear();
		capo = null;
		ultimoIndiceSelezionato = -1;
	}

	/**
	 * Seleziona in round-robin il prossimo personaggio idoneo o incapace a causa di stati alterati.
	 * Esclude i personaggi morti/sconfitti.
	 * @return Il Personaggio che deve agire in questo tick del timer, oppure null se sono tutti morti
	 */
	public Personaggio getProssimoAttaccante() {
		int dimensioneLista = personaggi.size();

		// Verifichiamo prima se c'è almeno un personaggio vivo nel gruppo per evitare loop infiniti
		boolean almenoUnoVivo = !getPersonaggiVivi().isEmpty();
		if (!almenoUnoVivo) {
			Logger.log("[ROUND-ROBIN] Tutti i personaggi del gruppo sono stati sconfitti.");
			return null;
		}

		// Cerchiamo il prossimo personaggio vivo facendo scorrere l'indice in cerchio (modulo)
		for (int i = 0; i < dimensioneLista; i++) {
			ultimoIndiceSelezionato = (ultimoIndiceSelezionato + 1) % dimensioneLista;
			Personaggio candidato = personaggi.get(ultimoIndiceSelezionato);

			// Se il personaggio è morto o in panchina, viene saltato istantaneamente e si passa al prossimo
			if (candidato.isFuoriCombattimento()) {
				continue;
			}

			// Se è vivo, è lui il personaggio designato dal round-robin per questo turno.
			// Il motore comunque deve tenere di conto degli effeti di stato.
			return candidato;
		}
		return null;
	}

	/**
	 * Riporta se il gruppo sia quello del giocatore o il gruppo avversario
	 */
	public abstract boolean isGruppoGiocatore();

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

	/**
	 * Riporta il numero di personaggi permanenti (esclusi quelli a tempo e gli ospiti di locazione).
	 */
	public final int getNumeroPersonaggiPermanenti() {
		return (int) personaggi.stream()
				.filter(p -> !p.isATempo() && !p.isOspiteDiLocazione())
				.count();
	}

	public final List<Personaggio> getPersonaggi() {
		return personaggi;
	}
	
	/**
	 * I personaggi vivi e in campo: chi è in panchina non conta (vedi Personaggio.isInPanchina).
	 */
	public final List<Personaggio> getPersonaggiVivi() {
		return personaggi.stream().filter(p -> !p.isFuoriCombattimento()).collect(Collectors.toList());
	}

	/**
	 * Rimette in campo chi era in panchina: a ogni nuova locazione.
	 */
	public final void svuotaPanchina() {
		personaggi.forEach(p -> p.setInPanchina(false));
	}

	/**
	 * Se qualcuno del gruppo va affrontato per forza (vedi Personaggio.isDaAffrontare).
	 */
	public final boolean isDaAffrontare() {
		return personaggi.stream().anyMatch(Personaggio::isDaAffrontare);
	}

	/**
	 * Se qualcuno del gruppo sfida a duello (vedi Personaggio.isSfidante).
	 */
	public final boolean isDuello() {
		return personaggi.stream().anyMatch(Personaggio::isSfidante);
	}

	/**
	 * Se qualcuno del gruppo combatte fino alla resa (vedi Personaggio.isFinoAllaResa).
	 */
	public final boolean isFinoAllaResa() {
		return personaggi.stream().anyMatch(Personaggio::isFinoAllaResa);
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

	/**
	 * Un ospite che un avversario attacca al posto di un personaggio del gruppo, se ce n'è uno (vedi
	 * GruppoGiocatore): per gli altri gruppi mai.
	 */
	public Optional<Personaggio> scegliOspiteBersaglio() {
		return Optional.empty();
	}

	public void rimuoviPersonaggi() {
		personaggi.clear();
		capo = null;
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
		return personaggi.size() > 1 ? "il gruppo" : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
	}

	public String chiMaiuscolo() {
		return personaggi.size() > 1 ? "Il gruppo" : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
	}
	
	@Override
	public boolean contiene(VistaPersonaggio personaggio) {
		return personaggi.contains(personaggio);
	}
}
