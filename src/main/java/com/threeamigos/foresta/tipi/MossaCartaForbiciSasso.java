package com.threeamigos.foresta.tipi;

/**
 * Le tre mosse di carta, forbici e sasso: la carta avvolge il sasso, il sasso rompe le forbici, le forbici tagliano la
 * carta. Ognuna corrisponde a un comando del giocatore ({@link Comando#CARTA}, {@link Comando#FORBICE},
 * {@link Comando#SASSO}).
 */
public enum MossaCartaForbiciSasso {

	CARTA(Comando.CARTA),
	FORBICE(Comando.FORBICE),
	SASSO(Comando.SASSO);

	private final Comando comando;

	MossaCartaForbiciSasso(Comando comando) {
		this.comando = comando;
	}

	public Comando getComando() {
		return comando;
	}

	/**
	 * Se questa mossa batte l'altra: una mossa non batte mai se stessa.
	 */
	public boolean batte(MossaCartaForbiciSasso altra) {
		return (this == CARTA && altra == SASSO) || (this == SASSO && altra == FORBICE) || (this == FORBICE && altra == CARTA);
	}

	/**
	 * Se un personaggio di quella classe può partecipare a carta, forbici e sasso: l'ombrafiamma è un personaggio segreto,
	 * per ora fuori dalla sfida (non sfida e non è un campione).
	 */
	public static boolean puoGiocare(TipoPersonaggio classe) {
		return classe != TipoPersonaggio.OMBRAFIAMMA;
	}

	/**
	 * La mossa che il giocatore ha scelto con quel comando, o null se il comando non è una delle tre.
	 */
	public static MossaCartaForbiciSasso da(Comando comando) {
		for (MossaCartaForbiciSasso mossa : values()) {
			if (mossa.comando == comando) {
				return mossa;
			}
		}
		return null;
	}
}
