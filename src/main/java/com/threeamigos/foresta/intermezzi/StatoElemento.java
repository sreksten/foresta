package com.threeamigos.foresta.intermezzi;

/**
 * Come appare un elemento in un certo istante: posizione del centro dell'immagine in
 * frazioni dello schermo (0 = bordo sinistro o superiore, 1 = bordo destro o inferiore),
 * scala rispetto alla dimensione originale, opacità (0 = invisibile, 1 = pieno) e se
 * l'immagine va disegnata rovesciata orizzontalmente.
 */
public final class StatoElemento {

	private final double x;
	private final double y;
	private final double scala;
	private final double opacita;
	private final boolean specchiato;

	public StatoElemento(double x, double y, double scala, double opacita, boolean specchiato) {
		this.x = x;
		this.y = y;
		this.scala = scala;
		this.opacita = opacita;
		this.specchiato = specchiato;
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

	public boolean isSpecchiato() {
		return specchiato;
	}

	StatoElemento conSpecchiato(boolean specchiato) {
		return new StatoElemento(x, y, scala, opacita, specchiato);
	}

	/**
	 * Lo stato a una frazione (0-1) del percorso fra questo stato e quello indicato. Il
	 * verso non si interpola: è quello dello stato di arrivo per tutto il tratto.
	 */
	StatoElemento interpola(StatoElemento verso, double frazione) {
		return new StatoElemento(
				x + (verso.x - x) * frazione,
				y + (verso.y - y) * frazione,
				scala + (verso.scala - scala) * frazione,
				opacita + (verso.opacita - opacita) * frazione,
				verso.specchiato);
	}
}
