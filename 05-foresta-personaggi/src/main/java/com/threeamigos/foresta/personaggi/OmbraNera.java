package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'ombra nera: non morto volante, con le capacità comuni descritte in {@link NonMortoVolante}.
 */
public class OmbraNera extends NonMortoVolante {

	public OmbraNera(int livello) {
		super(TipoPersonaggio.OMBRA_NERA, livello);
	}
	

}
