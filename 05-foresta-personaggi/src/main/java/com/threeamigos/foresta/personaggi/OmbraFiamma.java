package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'ombrafiamma: un personaggio segreto, immortale, che parte con salute e magia piene. Ha un carisma che fa riuscire sempre
 * l'amicizia (una faccia in più del dado dell'amicizia), e non partecipa alle sfide a carta, forbici e sasso.
 */
public class OmbraFiamma extends PersonaggioBase implements Personaggio {

	public OmbraFiamma(int livello) {
		super(TipoPersonaggio.OMBRAFIAMMA, livello);
	}

	public OmbraFiamma(String nome, int livello) {
		super(nome, TipoPersonaggio.OMBRAFIAMMA, livello);
	}

	// L'ombrafiamma è un personaggio segreto con un carisma altissimo: una faccia in più del dado dell'amicizia (vedi
	// CostantiPersonaggi.FACCE_DEL_DADO_DELL_AMICIZIA). Nel tiro però il carisma ha un tetto pari alle facce, quindi
	// l'amicizia può comunque fallire con un 12
	@Override
	public int get(TipoAttributo tipoAttributo) {
		return tipoAttributo == TipoAttributo.CARISMA ? CostantiPersonaggi.FACCE_DEL_DADO_DELL_AMICIZIA + 1 : super.get(tipoAttributo);
	}

	@Override
	public boolean isImmortale() {
		return true;
	}
}
