package com.threeamigos.foresta.tipi;

/**
 * Dove sta un pezzo di un set leggendario, visto da un altro pezzo dello stesso set (vedi RegoleSetLeggendari.pezzi).
 */
public enum StatoPezzoDelSet {
	/**
	 * Lo indossa chi indossa l'altro pezzo.
	 */
	INDOSSATO,
	/**
	 * Ce l'ha il gruppo: nell'inventario, o addosso a un altro personaggio.
	 */
	DEL_GRUPPO,
	/**
	 * Il gruppo non ce l'ha.
	 */
	DA_TROVARE
}
