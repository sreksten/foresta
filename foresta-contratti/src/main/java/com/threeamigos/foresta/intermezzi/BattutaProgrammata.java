package com.threeamigos.foresta.intermezzi;

/**
 * Una battuta con il suo intervallo effettivo nella pagina, in secondi dall'inizio.
 */
public final class BattutaProgrammata {

	private final BattutaIntermezzo battuta;
	private final double inizio;
	private final double fine;

	BattutaProgrammata(BattutaIntermezzo battuta, double inizio, double fine) {
		this.battuta = battuta;
		this.inizio = inizio;
		this.fine = fine;
	}

	public BattutaIntermezzo getBattuta() {
		return battuta;
	}

	public double getInizio() {
		return inizio;
	}

	public double getFine() {
		return fine;
	}

	public boolean isVisibileAl(double secondi) {
		return secondi >= inizio && secondi < fine;
	}
}
