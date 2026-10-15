package com.threeamigos.foresta.tipi;

/**
 * Gli intermezzi fissi del gioco (quelli dei passi delle missioni non hanno un tipo): l'identificativo con cui
 * IntermezziMD ricorda quelli già mostrati. L'intermezzo vero lo costruisce intermezzi.FabbricaIntermezzi.
 */
public enum TipoIntermezzo {

	INTERMEZZO_INTRODUTTIVO,
	INTERMEZZO_FINE_PRIMA_LOCAZIONE,
	INTERMEZZO_LOCANDA_PRIMA_VISITA,
	/** Il bardo della locanda insegna al gruppo l'Urlo della Banshee, dopo il viaggio della moglie */
	INTERMEZZO_URLO_DELLA_BANSHEE,
	INTERMEZZO_ARMAIOLO,
	INTERMEZZO_ALCHIMISTA,
	INTERMEZZO_VENDITORE_DI_PERGAMENE,
	INTERMEZZO_INCANTATORE,
	INTERMEZZO_ACCAMPAMENTO,
	/** L'ultimo eremita disturbato dice al gruppo quello che pensa, a missione DisturbatoreDellaQuietePubblica completata */
	INTERMEZZO_EREMITA_ROMPISCATOLE,
	/** Di ripiego: la visita successiva alla locanda, che aspetta se all'ingresso scatta altro (per esempio l'incarico di una missione) */
	INTERMEZZO_LOCANDA_VISITA_SUCCESSIVA,
	/** Di ripiego: chi entra a ore impossibili, solo se all'ingresso non scatta nient'altro */
	INTERMEZZO_NOTTE_ARMAIOLO,
	INTERMEZZO_NOTTE_ALCHIMISTA,
	INTERMEZZO_NOTTE_VENDITORE_DI_PERGAMENE,
	INTERMEZZO_NOTTE_INCANTATORE,
	INTERMEZZO_NOTTE_LOCANDA;

	private final boolean diProva;

	TipoIntermezzo() {
		this(false);
	}

	TipoIntermezzo(boolean diProva) {
		this.diProva = diProva;
	}

	public boolean isDiProva() {
		return diProva;
	}
}
