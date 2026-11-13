package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'elfa: l'Elfo al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Elfo}).
 */
public class Elfa extends PersonaggioBase implements Personaggio {

	public Elfa(int livello) {
		super(TipoPersonaggio.ELFA, livello);
	}

	public Elfa(String nome, int livello) {
		super(nome, TipoPersonaggio.ELFA, livello);
	}

}
