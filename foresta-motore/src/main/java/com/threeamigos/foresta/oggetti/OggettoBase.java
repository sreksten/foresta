package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.strumenti.Dado;

/**
 * La base degli oggetti delle locazioni: tiene la quantità, che di solito si tira a caso fra uno e il massimo della classe, e
 * quando l'oggetto viene preso assegna i punti del suo valore e riapre le finestre di stato e di statistiche.
 */
public abstract class OggettoBase implements Oggetto {
	
	protected int quantita;
	
	OggettoBase() {
		int quantitaMassima = getClasse().getQuantitaMassima();
		quantita = Dado.tiraAncheAUnaFaccia(quantitaMassima);
	}

	/**
	 * Un oggetto in una quantità decisa da chi lo crea, non dal dado (vedi OggettoMissione).
	 */
	OggettoBase(int quantita) {
		this.quantita = quantita;
	}
	
	public final int getQuantita() {
		return quantita;
	}

}
