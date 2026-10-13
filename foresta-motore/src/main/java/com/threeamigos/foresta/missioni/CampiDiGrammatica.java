package com.threeamigos.foresta.missioni;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * I campi di una riga di grammatica scritta come CHIAVE=valore separati da ";" (vedi missioni.txt): li legge e
 * controlla, così un campo sconosciuto, ripetuto o mancante si scopre subito.
 * <pre>
 * CHIAVE=TROLL_DEL_PONTE;NEMICO=TROLL;NUMERO=3;MONETE=30;TITOLO=Il ponte dei troll
 * </pre>
 */
final class CampiDiGrammatica {

	private static final String SEPARATORE = ";";
	private static final String UGUALE = "=";

	private final String riga;
	private final Map<String, String> campi = new HashMap<>();

	private CampiDiGrammatica(String riga, Set<String> chiaviAmmesse) {
		this.riga = riga;
		for (String campo : riga.split(SEPARATORE)) {
			if (campo.trim().isEmpty()) {
				continue;
			}
			int uguale = campo.indexOf(UGUALE);
			if (uguale < 0) {
				throw new IllegalArgumentException("Un campo è CHIAVE=valore: " + campo + " in " + riga);
			}
			String chiave = campo.substring(0, uguale).trim();
			if (!chiaviAmmesse.contains(chiave)) {
				throw new IllegalArgumentException("Campo sconosciuto: " + chiave + " in " + riga);
			}
			if (campi.put(chiave, campo.substring(uguale + 1).trim()) != null) {
				throw new IllegalArgumentException("Campo ripetuto: " + chiave + " in " + riga);
			}
		}
	}

	/**
	 * I campi della riga, che può avere solo quelle chiavi.
	 */
	static CampiDiGrammatica da(String riga, String... chiaviAmmesse) {
		return new CampiDiGrammatica(riga, new HashSet<>(Arrays.asList(chiaviAmmesse)));
	}

	String obbligatorio(String chiave) {
		String valore = campi.get(chiave);
		if (valore == null || valore.isEmpty()) {
			throw new IllegalArgumentException("Manca il campo " + chiave + " in " + riga);
		}
		return valore;
	}

	Optional<String> facoltativo(String chiave) {
		String valore = campi.get(chiave);
		return valore == null || valore.isEmpty() ? Optional.empty() : Optional.of(valore);
	}

	int intero(String chiave) {
		return Integer.parseInt(obbligatorio(chiave));
	}

	<E extends Enum<E>> E enumerato(String chiave, Class<E> classe) {
		return Enum.valueOf(classe, obbligatorio(chiave));
	}
}
