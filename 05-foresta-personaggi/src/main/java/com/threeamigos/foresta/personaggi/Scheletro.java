package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Lo scheletro: non morto. Oltre a ciò che vale per tutti i non morti è immune ad avvelenamento e spavento.
 */
public class Scheletro extends PersonaggioBase implements Personaggio {

	public Scheletro(int livello) {
		super(TipoPersonaggio.SCHELETRO, livello);
	}

}
