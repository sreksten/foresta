package com.threeamigos.foresta.artefatti.tipi;

import com.threeamigos.foresta.artefatti.leggendari.RegoleSetLeggendari;

/**
 * Dove sta un singolo pezzo di un set leggendario, visto da un altro pezzo dello stesso set.
 * <p>
 * Vedi anche RegoleSetLeggendari.pezzi.
 *
 * @author Stefano Reksten
 */
public enum StatoPezzoDelSet {

	/**
	 * È indossato dallo stesso Personaggio.
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
