package com.threeamigos.foresta.intermezzi;

/**
 * Un tratto dell'animazione di un elemento: in un certo numero di secondi l'elemento
 * passa, in modo lineare, dallo stato raggiunto alla tappa precedente a quello di questa
 * tappa. I valori non indicati restano quelli della tappa precedente, quindi una tappa
 * senza valori è una semplice attesa. Il verso dell'immagine ({@link #specchiata(boolean)})
 * non si interpola: vale per tutta la durata della tappa.
 * <pre>
 *     Tappa.inSecondi(3).verso(0.8, 0.6).conScala(0.5).conOpacita(0)
 *     Tappa.inSecondi(2).verso(0.1, 0.6).specchiata(true)
 * </pre>
 */
public final class Tappa {

	private final double secondi;
	private double x = Double.NaN;
	private double y = Double.NaN;
	private double scala = Double.NaN;
	private double opacita = Double.NaN;
	private Boolean specchiata;

	private Tappa(double secondi) {
		if (secondi < 0) {
			throw new IllegalArgumentException("Una tappa non può durare " + secondi + " secondi");
		}
		this.secondi = secondi;
	}

	public static Tappa inSecondi(double secondi) {
		return new Tappa(secondi);
	}

	/** Posizione del centro dell'immagine, in frazioni dello schermo. */
	public Tappa verso(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Tappa conScala(double scala) {
		this.scala = scala;
		return this;
	}

	public Tappa conOpacita(double opacita) {
		this.opacita = opacita;
		return this;
	}

	/**
	 * Se durante questa tappa l'immagine va disegnata rovesciata orizzontalmente (true)
	 * o così com'è (false); se non indicato resta come nella tappa precedente.
	 */
	public Tappa specchiata(boolean specchiata) {
		this.specchiata = specchiata;
		return this;
	}

	double getSecondi() {
		return secondi;
	}

	/**
	 * Lo stato di arrivo, completando con quello di partenza i valori non indicati.
	 */
	StatoElemento arrivoDa(StatoElemento partenza) {
		return new StatoElemento(
				Double.isNaN(x) ? partenza.getX() : x,
				Double.isNaN(y) ? partenza.getY() : y,
				Double.isNaN(scala) ? partenza.getScala() : scala,
				Double.isNaN(opacita) ? partenza.getOpacita() : opacita,
				specchiata == null ? partenza.isSpecchiato() : specchiata);
	}
}
