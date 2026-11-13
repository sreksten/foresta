package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La ladra: il Ladro al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Ladro}).
 */
public class Ladra extends PersonaggioBase {

	public Ladra(int livello) {
		super(TipoPersonaggio.LADRA, livello);
	}

	public Ladra(String nome, int livello) {
		super(nome, TipoPersonaggio.LADRA, livello);
	}

}
