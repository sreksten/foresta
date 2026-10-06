package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.TrofeiMD;

/**
 * Dove stanno i trofei vinti, che valgono da una partita all'altra: Main passa a RegistroTrofei l'implementazione
 * (GestoreTrofeiSuFile), i test un gestore in memoria.
 */
public interface GestoreTrofei {

	/**
	 * Recupera i trofei vinti salvati da qualche parte
	 * @param trofeiMD dove leggerli
	 * @return true se la lettura è andata a buon fine
	 */
	boolean carica(TrofeiMD trofeiMD);

	/**
	 * Salva i trofei vinti in qualche modo
	 * @param trofeiMD i trofei da salvare
	 * @return true se la scrittura è andata a buon fine
	 */
	boolean salva(TrofeiMD trofeiMD);

}
