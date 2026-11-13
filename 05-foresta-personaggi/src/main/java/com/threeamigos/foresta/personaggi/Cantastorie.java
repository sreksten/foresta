package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La Cantastorie: il Bardo al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Bardo}).
 */
public class Cantastorie extends PersonaggioBase {

	public Cantastorie(int livello) {
		super(TipoPersonaggio.CANTASTORIE, livello);
	}

	public Cantastorie(String nome, int livello) {
		super(nome, TipoPersonaggio.CANTASTORIE, livello);
	}
	

}
