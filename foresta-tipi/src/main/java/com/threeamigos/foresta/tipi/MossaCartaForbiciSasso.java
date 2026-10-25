package com.threeamigos.foresta.tipi;

/**
 * Le tre mosse di carta, forbici e sasso: la carta avvolge il sasso, il sasso rompe le forbici, le forbici tagliano la
 * carta. Ognuna corrisponde a un comando del giocatore, che dà {@link ComandiAssociati}.
 *
 * @author Stefano Reksten
 */
public enum MossaCartaForbiciSasso {

	CARTA,
	FORBICE,
	SASSO;

	/**
	 * Se questa mossa batte l'altra: una mossa non batte mai se stessa.
	 */
	public boolean batte(MossaCartaForbiciSasso altra) {
		return (this == CARTA && altra == SASSO) || (this == SASSO && altra == FORBICE) || (this == FORBICE && altra == CARTA);
	}

}
