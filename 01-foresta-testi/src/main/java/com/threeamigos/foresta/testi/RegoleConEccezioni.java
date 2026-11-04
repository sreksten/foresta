package com.threeamigos.foresta.testi;

import java.util.List;

/**
 * Le regole di una lingua, con gli articoli che un nome dichiara per sé (vedi {@link Nome#getArticolo}) davanti alla
 * regola: «un paio di schinieri» non si ricava dal genere. Tutto il resto lo fa la lingua.
 */
final class RegoleConEccezioni implements RegoleDellaLingua {

	private final RegoleDellaLingua lingua;

	RegoleConEccezioni(RegoleDellaLingua lingua) {
		this.lingua = lingua;
	}

	@Override
	public String articoloDeterminativo(Nome nome, boolean plurale) {
		return nome.getArticolo(true, plurale).orElseGet(() -> lingua.articoloDeterminativo(nome, plurale));
	}

	@Override
	public String articoloIndeterminativo(Nome nome, boolean plurale) {
		return nome.getArticolo(false, plurale).orElseGet(() -> lingua.articoloIndeterminativo(nome, plurale));
	}

	@Override
	public String preposizioneArticolata(Preposizione preposizione, Nome nome, boolean plurale) {
		if (!nome.getArticolo(true, plurale).isPresent()) {
			return lingua.preposizioneArticolata(preposizione, nome, plurale);
		}
		// Il nome ha un suo articolo: la lingua fonde la preposizione con quello
		return lingua.conPreposizione(preposizione, articoloDeterminativo(nome, plurale) + "x").replaceAll("x$", "");
	}

	@Override
	public String daAgente(Nome nome) {
		return lingua.daAgente(nome);
	}

	@Override
	public String conPreposizione(Preposizione preposizione, String nomeConArticolo) {
		return lingua.conPreposizione(preposizione, nomeConArticolo);
	}

	@Override
	public String senzaArticolo(String nomeConArticolo) {
		return lingua.senzaArticolo(nomeConArticolo);
	}

	@Override
	public String inizialeMaiuscola(String testo) {
		return lingua.inizialeMaiuscola(testo);
	}

	@Override
	public String cardinale(int numero, Genere genere) {
		return lingua.cardinale(numero, genere);
	}

	@Override
	public String ordinale(int numero, Genere genere, boolean conArticolo) {
		return lingua.ordinale(numero, genere, conArticolo);
	}

	@Override
	public String elenco(List<String> elementi) {
		return lingua.elenco(elementi);
	}

	@Override
	public Suono suonoIniziale(String testo) {
		return lingua.suonoIniziale(testo);
	}
}
