package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il chierico giocante, al maschile: il sacerdote che combatte in prima linea, con mazza, scudo e armatura. Non ha l'alba
 * sacra e la luce divina innate (sono del Sacerdote): sa scacciare i non morti (vedi {@code ScacciareINonMorti}) e imporre
 * le mani ({@code ImposizioneDelleMani}). Il femminile è la {@link Chierica}.
 */
public class Chierico extends PersonaggioBase {

	public Chierico(int livello) {
		super(TipoPersonaggio.CHIERICO, livello);
	}

	public Chierico(String nome, int livello) {
		super(nome, TipoPersonaggio.CHIERICO, livello);
	}

}
