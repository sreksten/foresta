package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.SuggerimentiMD;

/**
 * Dove sta il tutorial, che vale da una partita all'altra: il motore passa a {@code RegistroSuggerimenti} l'implementazione su file, i test un
 * gestore in memoria. Finché il tutorial non è finito non se ne passa nessuno e non si salva niente.
 */
public interface GestoreSuggerimenti {

	/**
	 * Recupera il tutorial salvato da qualche parte
	 * @param suggerimentiMD dove leggerlo
	 * @return true se la lettura è andata a buon fine
	 */
	boolean carica(SuggerimentiMD suggerimentiMD);

	/**
	 * Salva il tutorial in qualche modo
	 * @param suggerimentiMD il tutorial da salvare
	 * @return true se la scrittura è andata a buon fine
	 */
	boolean salva(SuggerimentiMD suggerimentiMD);

}
