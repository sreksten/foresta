package com.threeamigos.foresta.motore.modellodati;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serializzazione e lettura di una mappa di proprietà stringa-stringa, condivisa da
 * {@link LocazioneMD} e {@link MissioneMD}. Ogni proprietà occupa un token della riga
 * (i token sono già separati da {@link Serializzabile#PIPE} dal chiamante) nella forma
 * "chiave{@value #SEPARATORE}valore".
 * <p>
 * Il separatore non è ':' perché quel carattere può comparire legittimamente nei valori
 * (ad esempio nella prosa generata da {@code GrammarBean}); '§' è stato scelto perché non
 * ricorre né nelle chiavi convenzionali del progetto né nei testi generati.
 */
final class MappaProprieta {

	static final String SEPARATORE = "§";

	private MappaProprieta() {
	}

	/**
	 * Serializza la mappa in una sequenza di token "chiave§valore" pronta per essere unita
	 * al resto della riga con {@link Serializzabile#PIPE}. Non emette nulla se la mappa è vuota.
	 */
	static String salva(Map<String, String> proprieta) {
		return proprieta.entrySet().stream()
				.map(e -> {
					validaChiave(e.getKey());
					return e.getKey() + SEPARATORE + e.getValue();
				})
				.collect(Collectors.joining(Serializzabile.PIPE));
	}

	/**
	 * Ricostruisce la mappa a partire dai token della riga già separati sul PIPE, a partire
	 * da {@code daIndice}. Sovrascrive il contenuto di {@code destinazione}, che viene svuotata
	 * per prima.
	 */
	static void leggi(String[] tokens, int daIndice, Map<String, String> destinazione) {
		destinazione.clear();
		for (int i = daIndice; i < tokens.length; i++) {
			String token = tokens[i];
			if (token.isEmpty()) {
				// La riga può essere del tutto vuota (nessuna proprietà): non è una proprietà con chiave vuota
				continue;
			}
			// Limite 2: il valore può contenere il separatore ed essere vuoto
			String[] proprietaCorrente = token.split(SEPARATORE, 2);
			destinazione.put(proprietaCorrente[0], proprietaCorrente.length > 1 ? proprietaCorrente[1] : "");
		}
	}

	private static void validaChiave(String chiave) {
		if (chiave.contains(SEPARATORE) || chiave.contains(Serializzabile.PIPE)) {
			throw new IllegalStateException("Chiave di proprietà non valida, contiene un carattere riservato: " + chiave);
		}
	}
}
