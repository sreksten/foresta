package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La druida: il Druido al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Druido}).
 */
public class Druida extends PersonaggioBase implements Personaggio {

	public Druida(int livello) {
		super(TipoPersonaggio.DRUIDA, livello);
	}

	public Druida(String nome, int livello) {
		super(nome, TipoPersonaggio.DRUIDA, livello);
	}
	

}
