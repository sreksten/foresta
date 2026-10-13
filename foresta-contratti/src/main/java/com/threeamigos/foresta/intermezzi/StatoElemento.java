package com.threeamigos.foresta.intermezzi;

/**
 * Come appare un elemento in un certo istante: posizione del centro dell'immagine in
 * frazioni dello schermo (0 = bordo sinistro o superiore, 1 = bordo destro o inferiore),
 * scala rispetto alla dimensione originale, opacità (0 = invisibile, 1 = pieno) e da che
 * parte deve guardare l'elemento (null = così com'è disegnato; per un personaggio la UI specchia l'immagine
 * solo se la sua classe guarda dall'altra parte).
 */
public final class StatoElemento {

	private final double x;
	private final double y;
	private final double scala;
	private final double opacita;
	private final Verso verso;

	public StatoElemento(double x, double y, double scala, double opacita, Verso verso) {
		this.x = x;
		this.y = y;
		this.scala = scala;
		this.opacita = opacita;
		this.verso = verso;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getScala() {
		return scala;
	}

	public double getOpacita() {
		return opacita;
	}

	public Verso getVerso() {
		return verso;
	}

	StatoElemento conVerso(Verso verso) {
		return new StatoElemento(x, y, scala, opacita, verso);
	}

	/**
	 * Lo stato a una frazione (0-1) del percorso fra questo stato e quello indicato. Il
	 * verso non si interpola: è quello dello stato di arrivo per tutto il tratto.
	 */
	StatoElemento interpola(StatoElemento arrivo, double frazione) {
		return new StatoElemento(
				x + (arrivo.x - x) * frazione,
				y + (arrivo.y - y) * frazione,
				scala + (arrivo.scala - scala) * frazione,
				opacita + (arrivo.opacita - opacita) * frazione,
				arrivo.verso);
	}
}
