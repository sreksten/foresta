package com.threeamigos.foresta.motore;

/**
 * L'automa a stati finiti che tiene traccia dello svolgimento del gioco.
 * L'unica implementazione è Automa, e non dovrebbero servirne altre.
 */
public interface ControlloreDiGioco {

	void inizia();

	void processaAzione(Comando azione);

}
