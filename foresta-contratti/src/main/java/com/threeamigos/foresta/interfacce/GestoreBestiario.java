package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.BestiarioMD;

/**
 * Dove sta il bestiario, che vale da una partita all'altra: Main passa a RegistroBestiario l'implementazione
 * (GestoreBestiarioSuFile), i test un gestore in memoria.
 */
public interface GestoreBestiario {

	/**
	 * Recupera il bestiario salvato da qualche parte
	 * @param bestiarioMD dove leggerlo
	 * @return true se la lettura è andata a buon fine
	 */
	boolean carica(BestiarioMD bestiarioMD);

	/**
	 * Salva il bestiario in qualche modo
	 * @param bestiarioMD il bestiario da salvare
	 * @return true se la scrittura è andata a buon fine
	 */
	boolean salva(BestiarioMD bestiarioMD);

}
