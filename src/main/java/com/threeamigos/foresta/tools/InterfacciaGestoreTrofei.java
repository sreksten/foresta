package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.modellodati.TrofeiMD;

public interface InterfacciaGestoreTrofei {

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
