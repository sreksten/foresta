package com.threeamigos.foresta.missioni;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Un set di oggetti leggendari, letto da una riga di SET_LEGGENDARIO in leggendari.txt: la chiave a cui rimandano i
 * pezzi (il loro campo SET=), il nome e il moltiplicatore dei bonus quando un personaggio li indossa tutti.
 * <pre>
 * CHIAVE=GIOVENTU_RIBELLE;NOME=la Gioventù Ribelle;MOLTIPLICATORE=1.3
 * </pre>
 * Quali sono i pezzi lo dicono le righe degli oggetti (vedi CatalogoLeggendari).
 */
public final class SetLeggendario {

	private static final String SEPARATORE = ";";
	private static final String UGUALE = "=";

	private final String chiave;
	private final String nome;
	private final double moltiplicatore;

	private SetLeggendario(String riga) {
		Objects.requireNonNull(riga);
		Map<String, String> campi = new HashMap<>();
		for (String campo : riga.split(SEPARATORE)) {
			if (campo.trim().isEmpty()) {
				continue;
			}
			int uguale = campo.indexOf(UGUALE);
			if (uguale < 0) {
				throw new IllegalArgumentException("Un campo è CHIAVE=valore: " + campo + " in " + riga);
			}
			String chiaveCampo = campo.substring(0, uguale).trim();
			if (!"CHIAVE".equals(chiaveCampo) && !"NOME".equals(chiaveCampo) && !"MOLTIPLICATORE".equals(chiaveCampo)) {
				throw new IllegalArgumentException("Campo sconosciuto: " + chiaveCampo + " in " + riga);
			}
			if (campi.put(chiaveCampo, campo.substring(uguale + 1).trim()) != null) {
				throw new IllegalArgumentException("Campo ripetuto: " + chiaveCampo + " in " + riga);
			}
		}
		chiave = obbligatorio(campi, "CHIAVE", riga);
		nome = obbligatorio(campi, "NOME", riga);
		moltiplicatore = Double.parseDouble(obbligatorio(campi, "MOLTIPLICATORE", riga));
		if (moltiplicatore <= 1.0d) {
			throw new IllegalArgumentException("Il moltiplicatore di un set è più di 1: " + riga);
		}
	}

	private static String obbligatorio(Map<String, String> campi, String chiave, String riga) {
		String valore = campi.get(chiave);
		if (valore == null || valore.isEmpty()) {
			throw new IllegalArgumentException("Manca il campo " + chiave + " in " + riga);
		}
		return valore;
	}

	public static SetLeggendario da(String riga) {
		return new SetLeggendario(riga);
	}

	/**
	 * La chiave del set, quella che i pezzi scrivono in SET=: "GIOVENTU_RIBELLE".
	 */
	public String getChiave() {
		return chiave;
	}

	/**
	 * Il nome del set, con l'articolo: "la Gioventù Ribelle".
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Per quanto si moltiplicano i bonus dei pezzi quando un personaggio li indossa tutti.
	 */
	public double getMoltiplicatore() {
		return moltiplicatore;
	}
}
