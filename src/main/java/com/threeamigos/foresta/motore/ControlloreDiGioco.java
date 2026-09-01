package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.Temporizzatore;

/**
 * L'automa a stati finiti che tiene traccia dello svolgimento del gioco.
 * L'unica implementazione è Automa, e non dovrebbero servirne altre.
 */
public interface ControlloreDiGioco {

	void setTemporizzatore(Temporizzatore t);

	void inizia();

	/**
	 * Riceve un testo dall'interfaccia utente
	 */
	void riceviTesto(String s);

	void processaAzione(Comando azione);

}
