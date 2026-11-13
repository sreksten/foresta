package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La strega: creatura umanoide, boss del suo castello. Parte con salute e magia piene ed è immune agli incantesimi di Acqua,
 * Aria, Fuoco e Terra.
 */
public class Strega extends PersonaggioBase implements Personaggio {

	public Strega(int livello) {
		super(TipoPersonaggio.STREGA, livello);
	}

}
