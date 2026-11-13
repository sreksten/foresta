package com.threeamigos.foresta.artefatti.leggendari;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

	// I campi della riga di un set (vedi RigaDiCampi), tutti al massimo una volta
	private static final List<String> CAMPI = Arrays.asList("CHIAVE", "NOME", "MOLTIPLICATORE", "PEZZI");
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
		RigaDiCampi campi = new RigaDiCampi(riga, CAMPI, Collections.emptyList());
		chiave = campi.obbligatorio("CHIAVE");
		nome = campi.obbligatorio("NOME");
		moltiplicatore = campi.decimale("MOLTIPLICATORE", campi.obbligatorio("MOLTIPLICATORE"));
		if (moltiplicatore <= 1.0d) {
			throw new IllegalArgumentException(campi.conLaRiga("Il moltiplicatore di un set è più di 1"));
		}
		String pezzi = campi.facoltativo("PEZZI");
		pezziRichiesti = pezzi == null ? null : campi.intero("PEZZI", pezzi);
		if (pezziRichiesti != null && pezziRichiesti < PEZZI_MINIMI) {
			throw new IllegalArgumentException(campi.conLaRiga("Un set si completa con almeno " + PEZZI_MINIMI + " pezzi"));
		}
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
