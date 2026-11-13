package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il ladro giocante, al maschile: umanoide, corrompibile e amichevole. Può impugnare una seconda arma. Il femminile è la
 * {@link Ladra}.
 */
public class Ladro extends PersonaggioBase implements Personaggio {

	public Ladro(int livello) {
		super(TipoPersonaggio.LADRO, livello);
	}

	public Ladro(String nome, int livello) {
		super(nome, TipoPersonaggio.LADRO, livello);
	}

}
