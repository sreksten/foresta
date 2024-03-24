package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.Temporizzatore;

/**
 * L'automa a stati finiti che tiene traccia dello svolgimento del gioco.
 * L'unica implementazione e' Automa, e non dovrebbero servirne altre.
 */
public interface ControlloreDiGioco {

	public void setTemporizzatore(Temporizzatore t);

	public void inizia();

	/**
	 * Riceve un testo dall'UI
	 */
	public void riceviTesto(String s);

	public void processaAzione(Comando azione);

}
