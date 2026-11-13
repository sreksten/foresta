package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La chierica: il Chierico al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Chierico}).
 */
public class Chierica extends PersonaggioBase implements Personaggio {

	public Chierica(int livello) {
		super(TipoPersonaggio.CHIERICA, livello);
	}

	public Chierica(String nome, int livello) {
		super(nome, TipoPersonaggio.CHIERICA, livello);
	}

}
