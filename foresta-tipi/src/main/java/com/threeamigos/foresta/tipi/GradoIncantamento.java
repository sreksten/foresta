package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * I gradini degli effetti di un ingrediente magico e degli incantamenti degli artefatti (vedi
 * artefatti_e_incantamenti.md). Il prezzo è calcolato tramite il ListinoPergamene.
 * <p>
 * Il nome di ogni grado ("minore", "medio", "maggiore") non sta qui ma nel file
 * {@code grado_incantamento_<lingua>.properties}, alla voce {@code COSTANTE.NOME}, in modo che il gioco si possa tradurre
 * (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum GradoIncantamento {

	MINORE(5, 0.05),
	MEDIO(10, 0.10),
	MAGGIORE(15, 0.20);

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

	/**
	 * Il grado degli effetti di un ingrediente magico: minore fino al livello 2, medio (nel nome non si dice) al
	 * 3, maggiore dal 5.
	 */
	public static GradoIncantamento perIngrediente(int livello) {
		if (livello >= Costanti.INGREDIENTE_GRADO_MAGGIORE_DAL_LIVELLO) {
			return MAGGIORE;
		}
		if (livello >= Costanti.INGREDIENTE_GRADO_MEDIO_DAL_LIVELLO) {
			return MEDIO;
		}
		return MINORE;
	}

	private final int bonusFisso;
	private final double coefficiente;

	GradoIncantamento(int bonusFisso, double coefficiente) {
		this.bonusFisso = bonusFisso;
		this.coefficiente = coefficiente;
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

	/**
	 * Posizione del grado (1, 2, 3), usata per i modificatori fissi di attributo, che sono piccoli.
	 */
	public int getGradino() {
		return ordinal() + 1;
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

}
