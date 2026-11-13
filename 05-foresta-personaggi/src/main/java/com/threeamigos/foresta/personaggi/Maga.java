package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La maga: il Mago al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Mago}).
 */
public class Maga extends PersonaggioBase {

	public Maga(int livello) {
		super(TipoPersonaggio.MAGA, livello);
	}

	public Maga(String nome, int livello) {
		super(nome, TipoPersonaggio.MAGA, livello);
	}

}
