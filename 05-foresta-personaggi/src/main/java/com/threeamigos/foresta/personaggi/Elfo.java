package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'elfo giocante, al maschile: umanoide, corrompibile e amichevole. Come il Mago ha il dardo arcano come incantesimo
 * innato e, come il Ladro, può impugnare una seconda arma. Il femminile è l'{@link Elfa}.
 */
public class Elfo extends PersonaggioBase {

	public Elfo(int livello) {
		super(TipoPersonaggio.ELFO, livello);
	}

	public Elfo(String nome, int livello) {
		super(nome, TipoPersonaggio.ELFO, livello);
	}

}
