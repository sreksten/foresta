package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il drago: bestia draconica, volante e gigantesca, il boss del suo castello. Parte con salute e magia piene ed è immune
 * al Fuoco e allo stato bruciato.
 */
public class Drago extends PersonaggioBase implements Personaggio {

	public Drago(int livello) {
		super(TipoPersonaggio.DRAGO, livello);
	}

}
