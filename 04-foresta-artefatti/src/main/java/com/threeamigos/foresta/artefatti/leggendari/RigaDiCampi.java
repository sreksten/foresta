package com.threeamigos.foresta.artefatti.leggendari;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Una riga dei file dei leggendari (leggendari.txt): campi {@code CHIAVE=valore} separati da {@code ;}, per esempio
 * {@code CHIAVE=FALCE;TIPO=FALCE;NOME=la falce del Tristo Mietitore;LIVELLO=5;...}. La leggono gli oggetti leggendari
 * ({@link OggettoLeggendario}), i set ({@link SetLeggendario}) e i libri tecnici ({@link LibroTecnico}), ognuno con i suoi
 * campi: alcuni compaiono al massimo una volta (i campi singoli), altri si possono ripetere (per esempio un MOD per ogni
 * modificatore).
 * <p>
 * Ogni errore (un campo sconosciuto o ripetuto, uno obbligatorio che manca, un numero o un tipo che non si leggono) è
 * un'{@link IllegalArgumentException} il cui messaggio finisce con la riga intera: così si trova subito nel file.
 */
final class RigaDiCampi {

	private static final String SEPARATORE = ";";
	private static final String UGUALE = "=";

	private final String riga;
	private final Map<String, String> singoli = new HashMap<>();
	private final Map<String, List<String>> ripetuti = new HashMap<>();

	/**
	 * Legge la riga.
	 *
	 * @param campiSingoli   i campi che possono comparire al massimo una volta
	 * @param campiRipetuti  i campi che possono comparire più volte (nessuno, se la riga non ne ha)
	 * @throws NullPointerException     se la riga è null
	 * @throws IllegalArgumentException se un campo non è {@code CHIAVE=valore}, non è fra quelli previsti o è ripetuto
	 */
	RigaDiCampi(String riga, Collection<String> campiSingoli, Collection<String> campiRipetuti) {
		this.riga = Objects.requireNonNull(riga, "Manca la riga");
		Set<String> singoliPrevisti = new HashSet<>(campiSingoli);
		Set<String> ripetutiPrevisti = new HashSet<>(campiRipetuti);
		for (String campo : riga.split(SEPARATORE)) {
			if (campo.trim().isEmpty()) {
				continue;
			}
			int uguale = campo.indexOf(UGUALE);
			if (uguale < 0) {
				throw new IllegalArgumentException(conLaRiga("Un campo è CHIAVE=valore: " + campo));
			}
			String chiave = campo.substring(0, uguale).trim();
			String valore = campo.substring(uguale + 1).trim();
			if (ripetutiPrevisti.contains(chiave)) {
				ripetuti.computeIfAbsent(chiave, k -> new ArrayList<>()).add(valore);
			} else if (!singoliPrevisti.contains(chiave)) {
				throw new IllegalArgumentException(conLaRiga("Campo sconosciuto: " + chiave));
			} else if (singoli.put(chiave, valore) != null) {
				throw new IllegalArgumentException(conLaRiga("Campo ripetuto: " + chiave));
			}
		}
	}

	/**
	 * La riga intera, così come è stata letta.
	 */
	String getRiga() {
		return riga;
	}

	/**
	 * Il valore di un campo che deve esserci e non essere vuoto.
	 *
	 * @throws IllegalArgumentException se manca
	 */
	String obbligatorio(String chiave) {
		String valore = singoli.get(chiave);
		if (valore == null || valore.isEmpty()) {
			throw new IllegalArgumentException(conLaRiga("Manca il campo " + chiave));
		}
		return valore;
	}

	/**
	 * Il valore di un campo che può mancare; null se manca.
	 */
	String facoltativo(String chiave) {
		return singoli.get(chiave);
	}

	/**
	 * Se la riga ha quel campo (anche vuoto: un campo come {@code PLURALE=} conta per il solo fatto di esserci).
	 */
	boolean ha(String chiave) {
		return singoli.containsKey(chiave);
	}

	/**
	 * I valori di un campo che si ripete, nell'ordine della riga; vuota se non ce n'è.
	 */
	List<String> ripetuti(String chiave) {
		return Collections.unmodifiableList(ripetuti.getOrDefault(chiave, Collections.emptyList()));
	}

	/**
	 * Un numero intero scritto nel testo di un campo.
	 *
	 * @throws IllegalArgumentException se non è un numero intero
	 */
	int intero(String chiave, String testo) {
		try {
			return Integer.parseInt(testo);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(conLaRiga("Il campo " + chiave + " non è un numero intero: " + testo));
		}
	}

	/**
	 * Un numero decimale scritto nel testo di un campo.
	 *
	 * @throws IllegalArgumentException se non è un numero
	 */
	double decimale(String chiave, String testo) {
		try {
			return Double.parseDouble(testo);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(conLaRiga("Il campo " + chiave + " non è un numero: " + testo));
		}
	}

	/**
	 * La costante dell'enum con quel nome, scritta nel testo di un campo.
	 *
	 * @throws IllegalArgumentException se non esiste
	 */
	<E extends Enum<E>> E enumerato(Class<E> classe, String chiave, String testo) {
		try {
			return Enum.valueOf(classe, testo);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(conLaRiga("Il campo " + chiave + " non è un valore di " + classe.getSimpleName() + ": " + testo));
		}
	}

	/**
	 * Il messaggio di un errore di questa riga: il messaggio, e poi la riga intera.
	 */
	String conLaRiga(String messaggio) {
		return messaggio + " in " + riga;
	}
}
