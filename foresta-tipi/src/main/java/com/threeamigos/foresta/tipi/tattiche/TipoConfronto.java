package com.threeamigos.foresta.tipi.tattiche;

/**
 * Il confronto di un filtro o di una condizione numerica (tattiche_di_combattimento_automatiche.md §11.5, §11.8). Tre valori: con la negazione
 * si ottengono "almeno" (<code>NON MINORE</code>) e "al massimo" (<code>NON MAGGIORE</code>).
 */
public enum TipoConfronto {

	/** Il valore è minore del riferimento */
	MINORE,
	/** Il valore è uguale al riferimento */
	UGUALE,
	/** Il valore è maggiore del riferimento */
	MAGGIORE;

	/**
	 * @return true se il valore sta nel rapporto indicato con il riferimento
	 */
	public boolean vale(double valore, double riferimento) {
		switch (this) {
			case MINORE:
				return valore < riferimento;
			case MAGGIORE:
				return valore > riferimento;
			default:
				return valore == riferimento;
		}
	}
}
