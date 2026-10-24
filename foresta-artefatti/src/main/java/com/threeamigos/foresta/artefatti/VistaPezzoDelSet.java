package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.tipi.StatoPezzoDelSet;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;

/**
 * Un pezzo di un set leggendario e dove sta, per i testi della UI (vedi VistaGruppoGiocatore.getPezziDelSet).
 */
public interface VistaPezzoDelSet {

	/**
	 * La chiave del leggendario che è il pezzo.
	 */
	String getChiave();

	/**
	 * Il nome breve, con l'articolo
	 */
	String getNome();

	/**
	 * Il tipo di artefatto del pezzo.
	 */
	TipoArtefatto getTipo();

	/**
	 * Dove sta il pezzo: indossato, del gruppo o ancora da trovare.
	 */
	StatoPezzoDelSet getStato();

}
