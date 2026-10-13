package com.threeamigos.foresta.missioni.leggende;

import com.threeamigos.foresta.oggetti.leggendari.OggettoLeggendario;

/**
 * Una missione che mette in palio un oggetto leggendario (vedi {@link OggettoLeggendario}): le leggende (vedi
 * LaLeggenda) e i tornei (vedi IlTorneo). Ogni leggendario esce una volta sola per partita, in una sola di queste
 * missioni (vedi {@link PescaLeggendaria#giaPescati}).
 */
public interface ConLeggendario {

	/**
	 * Il leggendario in palio, o null finché la missione non l'ha pescato.
	 */
	OggettoLeggendario getLeggendario();
}
