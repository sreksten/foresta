package com.threeamigos.foresta.ui;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * La misura di uno schermo vero: i pixel e la densità (quanti pixel fanno un dp, come su Android: un dp è un pixel a 160 punti per
 * pollice). Il gioco è sempre in orizzontale, quindi il lato lungo è la larghezza. Viene da un profilo di prova
 * ({@link com.threeamigos.foresta.tipi.SchermoDiProva}), dall'argomento {@code SCHERMO=} o, su Android, dal dispositivo; la scelta del
 * layout la fa {@link SceltaDelLayout}, sui dp.
 */
public final class MisuraDelloSchermo {

	private static final Pattern FORMATO = Pattern.compile("(\\d+)[xX](\\d+)(?:@(\\d+(?:\\.\\d+)?))?");

	private final int larghezza;
	private final int altezza;
	private final double densita;

	/**
	 * @param lato1   un lato in pixel
	 * @param lato2   l'altro lato in pixel: il più lungo dei due diventa la larghezza
	 * @param densita quanti pixel fanno un dp (1 per uno schermo a 160 punti per pollice)
	 */
	public MisuraDelloSchermo(int lato1, int lato2, double densita) {
		if (lato1 <= 0 || lato2 <= 0 || !(densita > 0)) {
			throw new IllegalArgumentException("Misura dello schermo non valida: " + lato1 + "x" + lato2 + "@" + densita);
		}
		this.larghezza = Math.max(lato1, lato2);
		this.altezza = Math.min(lato1, lato2);
		this.densita = densita;
	}

	/**
	 * Legge una misura scritta come {@code 800x480} o {@code 2400x1080@2.625} (senza densità vale 1).
	 *
	 * @throws IllegalArgumentException se il testo non è una misura
	 */
	public static MisuraDelloSchermo leggi(String testo) {
		Matcher matcher = FORMATO.matcher(testo.trim());
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Non è una misura dello schermo: " + testo);
		}
		double densita = matcher.group(3) == null ? 1 : Double.parseDouble(matcher.group(3));
		return new MisuraDelloSchermo(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), densita);
	}

	/** La larghezza in pixel (il lato lungo) */
	public int getLarghezza() {
		return larghezza;
	}

	/** L'altezza in pixel (il lato corto) */
	public int getAltezza() {
		return altezza;
	}

	public double getDensita() {
		return densita;
	}

	public double getLarghezzaDp() {
		return larghezza / densita;
	}

	public double getAltezzaDp() {
		return altezza / densita;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "%dx%d@%s (%.0fx%.0f dp)", larghezza, altezza, densita, getLarghezzaDp(), getAltezzaDp());
	}
}
