package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Un insieme di personaggi
 */

public abstract class Gruppo {
	
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

			// Se il personaggio è morto (sconfitto), viene saltato istantaneamente e si passa al prossimo
			if (!candidato.isVivo()) {
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
		return personaggi.size() > 1 ? "il gruppo" : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
	}

	public String chiMaiuscolo() {
		return personaggi.size() > 1 ? "Il gruppo" : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
	}
	
	public boolean contiene(Personaggio personaggio) {
		return personaggi.contains(personaggio);
	}
}
