package com.threeamigos.foresta.artefatti.leggendari;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Un set di oggetti leggendari, letto da una riga di SET_LEGGENDARIO in leggendari.txt: la chiave a cui rimandano i
 * pezzi (il loro campo SET=), il nome, il moltiplicatore dei bonus quando un personaggio lo completa e, facoltativo, quanti
 * pezzi bastano per completarlo (PEZZI=; senza, servono tutti).
 * <pre>
 * CHIAVE=GIOVENTU_RIBELLE;NOME=la Gioventù Ribelle;MOLTIPLICATORE=1.3
 * CHIAVE=SANTA_IGNORANZA;NOME=la Santa Ignoranza;MOLTIPLICATORE=1.4;PEZZI=2
 * </pre>
 * Un set che si completa con meno pezzi di quanti ne ha può avere pezzi che non si indossano insieme (più armi, di cui
 * se ne impugna una sola).
 * Quali sono i pezzi lo dicono le righe degli oggetti (vedi CatalogoLeggendari).
 */
public final class SetLeggendario {

	private static final String SEPARATORE = ";";
	private static final String UGUALE = "=";
	/**
	 * Un set ha almeno due pezzi, e non si completa con meno di due
	 */
	public static final int PEZZI_MINIMI = 2;

	private final String chiave;
	private final String nome;
	private final double moltiplicatore;
	// Quanti pezzi bastano per completarlo; null se servono tutti
	private final Integer pezziRichiesti;

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
			if (!"CHIAVE".equals(chiaveCampo) && !"NOME".equals(chiaveCampo) && !"MOLTIPLICATORE".equals(chiaveCampo)
					&& !"PEZZI".equals(chiaveCampo)) {
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
		String pezzi = campi.get("PEZZI");
		pezziRichiesti = pezzi == null ? null : Integer.valueOf(pezzi);
		if (pezziRichiesti != null && pezziRichiesti < PEZZI_MINIMI) {
			throw new IllegalArgumentException("Un set si completa con almeno " + PEZZI_MINIMI + " pezzi: " + riga);
		}
	}

	private static String obbligatorio(Map<String, String> campi, String chiave, String riga) {
		String valore = campi.get(chiave);
		if (valore == null || valore.isEmpty()) {
			throw new IllegalArgumentException("Manca il campo " + chiave + " in " + riga);
		}
		return valore;
	}

	/**
	 * Legge la riga di leggendari.txt che descrive il set.
	 */
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
	 * Per quanto si moltiplicano i bonus dei pezzi quando un personaggio completa il set.
	 */
	public double getMoltiplicatore() {
		return moltiplicatore;
	}

	/**
	 * Quanti pezzi bastano per completarlo, su quelli che ha: tutti, se la riga non dice PEZZI=.
	 *
	 * @param pezziDelSet quanti pezzi ha il set
	 */
	public int getPezziRichiesti(int pezziDelSet) {
		return pezziRichiesti == null ? pezziDelSet : pezziRichiesti;
	}

	/**
	 * Se si completa con una parte dei suoi pezzi (PEZZI=), e non con tutti.
	 */
	public boolean isCompletabileInParte() {
		return pezziRichiesti != null;
	}
}
