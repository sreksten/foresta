package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.StatoPezzoDelSet;
import com.threeamigos.foresta.tipi.TipoArtefatto;

/**
 * Un pezzo di un set leggendario e dove sta, per i testi della UI (vedi VistaGruppoGiocatore.getPezziDelSet).
 */
public interface VistaPezzoDelSet {

	String getChiave();

	/**
	 * Il nome breve, con l'articolo
	 */
	String getNome();

	TipoArtefatto getTipo();

	StatoPezzoDelSet getStato();

}
