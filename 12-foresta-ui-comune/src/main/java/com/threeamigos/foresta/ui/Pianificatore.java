package com.threeamigos.foresta.ui;

/**
 * Chi fa scattare un'azione dopo un po' di tempo o a intervalli regolari, sul thread dell'interfaccia: Swing con un suo <code>Timer</code>, libGDX
 * a ogni fotogramma. Il {@link RouterDegliInput} lo usa per distinguere il doppio click dal click singolo e per dividere la rotella in passi.
 */
public interface Pianificatore {

	/**
	 * Un'azione pianificata
	 */
	interface Pianificata {
		void annulla();

		boolean isAttiva();
	}

	/**
	 * Esegue l'azione una volta, dopo quei millisecondi
	 */
	Pianificata dopo(int millisecondi, Runnable azione);

	/**
	 * Esegue l'azione a ogni periodo, finché non la si annulla
	 */
	Pianificata ogni(int millisecondi, Runnable azione);
}
