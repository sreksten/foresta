package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La strega: creatura umanoide, boss del suo castello. Parte con salute e magia piene. Acqua, aria e terra non la toccano,
 * incantesimi compresi; il fuoco invece la brucia più di tutti (vedi TipoPersonaggio).
 */
public class Strega extends PersonaggioBase {

	public Strega(int livello) {
		super(TipoPersonaggio.STREGA, livello);
	}

}
