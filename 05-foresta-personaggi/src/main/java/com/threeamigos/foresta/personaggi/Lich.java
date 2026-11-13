package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il lich: non morto, boss del suo castello. Parte con salute e magia piene; oltre a ciò che vale per tutti i non morti è
 * immune ad avvelenamento, spavento e silenzio.
 */
public class Lich extends PersonaggioBase implements Personaggio {

	public Lich(int livello) {
		super(TipoPersonaggio.LICH, livello);
	}

}
