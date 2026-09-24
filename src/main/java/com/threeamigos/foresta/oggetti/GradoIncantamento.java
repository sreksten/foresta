package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Costanti;

/**
 * I gradini degli effetti di una pergamena (vedi artefatti_e_incantamenti.md, §8).
 * Il prezzo base è quello di un incantamento con entrambe le parti, fissa e percentuale.
 */
public enum GradoIncantamento {

	MINORE("minore", 5, 0.05, 15),
	MEDIO("medio", 10, 0.10, 30),
	MAGGIORE("maggiore", 15, 0.20, 50);

	private final String nome;
	private final int bonusFisso;
	private final double coefficiente;
	private final int prezzoBase;

	GradoIncantamento(String nome, int bonusFisso, double coefficiente, int prezzoBase) {
		this.nome = nome;
		this.bonusFisso = bonusFisso;
		this.coefficiente = coefficiente;
		this.prezzoBase = prezzoBase;
	}

	/**
	 * Il grado adatto al livello di riferimento: minore fino al 3, medio dal 4 al 7, maggiore dall'8 in su.
	 */
	public static GradoIncantamento perLivello(int livello) {
		if (livello >= Costanti.GRADO_INCANTAMENTO_MAGGIORE_DAL_LIVELLO) {
			return MAGGIORE;
		}
		if (livello >= Costanti.GRADO_INCANTAMENTO_MEDIO_DAL_LIVELLO) {
			return MEDIO;
		}
		return MINORE;
	}

	public String getNome() {
		return nome;
	}

	public int getBonusFisso() {
		return bonusFisso;
	}

	/**
	 * Es. 0.1 per +10%
	 */
	public double getCoefficiente() {
		return coefficiente;
	}

	public int getPrezzoBase() {
		return prezzoBase;
	}

	/**
	 * Posizione del grado (1, 2, 3), usata per i modificatori fissi di attributo, che sono piccoli.
	 */
	public int getGradino() {
		return ordinal() + 1;
	}
}
