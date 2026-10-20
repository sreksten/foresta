package com.threeamigos.foresta.modellodati;

import java.io.IOException;
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
 *
 * @author Stefano Reksten
 */
final class MappaProprieta {

	static final String SEPARATORE = "§";

	private MappaProprieta() {
	}

	/**
	 * Il valore di una proprietà pronto per il salvataggio: senza i caratteri che spezzerebbero la riga (vedi
	 * {@link Serializzabile#pulisciCampo}). Un valore null si rifiuta: si salverebbe come la stringa "null", e riletto sarebbe un testo
	 * qualunque; per togliere una proprietà c'è {@code rimuoviProprieta}.
	 *
	 * @throws IllegalArgumentException se il valore è null
	 */
	static String pulisciValore(String nome, String valore) {
		if (valore == null) {
			throw new IllegalArgumentException("La proprietà " + nome + " non può avere un valore null: per toglierla si usa rimuoviProprieta");
		}
		return Serializzabile.pulisciCampo(valore);
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
	 * Ricostruisce la mappa a partire dai campi che restano nella riga. Sovrascrive il contenuto di
	 * {@code destinazione}, che viene svuotata per prima.
	 */
	static void leggi(LettoreCampi campi, Map<String, String> destinazione) throws IOException {
		destinazione.clear();
		while (campi.haAltriCampi()) {
			String token = campi.testo();
			if (token.isEmpty()) {
				// La riga può essere del tutto vuota (nessuna proprietà): non è una proprietà con chiave vuota
				continue;
			}
			// Limite 2: il valore può contenere il separatore ed essere vuoto
			String[] proprietaCorrente = token.split(SEPARATORE, 2);
			destinazione.put(proprietaCorrente[0], proprietaCorrente.length > 1 ? proprietaCorrente[1] : "");
		}
	}

	// Una chiave non passa dalla pulizia dei testi (vedi Serializzabile.pulisciCampo), perché è scritta dal codice e non
	// dal giocatore: se contiene un carattere che spezzerebbe la riga o i campi è un errore di programmazione, e si dice.
	private static void validaChiave(String chiave) {
		if (chiave.contains(SEPARATORE) || chiave.contains(Serializzabile.PIPE) || chiave.contains("\n") || chiave.contains("\r")) {
			throw new IllegalStateException("Chiave di proprietà non valida, contiene un carattere riservato: " + chiave);
		}
	}
}
