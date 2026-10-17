package com.threeamigos.foresta.tipi;

/**
 * Il genere di un {@link TipoLocazione}: le locazioni comuni della Foresta, le città e i castelli dei boss.
 * Tutte tranne le comuni sono locazioni uniche.
 *
 * @author Stefano Reksten
 */
public enum CategoriaLocazione {

	/**
	 * Bosco, Palude, Locanda, Rovine, ...
	 * <p>
	 * Nonostante Locande e Rovine siano comuni, ognuna di queste ha un nome proprio.
	 */
	STANDARD,

	/**
	 * Le quattro città all'interno della Foresta, ognuna con un nome proprio.
	 */
	CITTA,

	/**
	 * I castelli dei boss, ciascuno con un nome proprio.
	 */
	CASTELLO
}
