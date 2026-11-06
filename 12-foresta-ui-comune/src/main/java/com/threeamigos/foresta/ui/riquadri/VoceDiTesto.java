package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * Una scritta da disegnare in un riquadro: il testo, l'angolo in alto a sinistra (o, se allineata a destra, l'angolo in alto a destra), il colore e, se va a
 * capo, la larghezza massima (0 per una riga sola). Il modello di un riquadro le dà già con posizione, colore e allineamento; chi disegna (Swing, libGDX)
 * le mette come vuole. Il font è il medio, salvo dove il riquadro dice altrimenti.
 */
public final class VoceDiTesto {

	private final String testo;
	private final int x;
	private final int y;
	private final DoomdarkColorModel.Color colore;
	private final boolean allineataADestra;
	private final int larghezzaMassima;
	private final boolean conOmbra;

	public VoceDiTesto(String testo, int x, int y, DoomdarkColorModel.Color colore, boolean allineataADestra, int larghezzaMassima) {
		this(testo, x, y, colore, allineataADestra, larghezzaMassima, false);
	}

	public VoceDiTesto(String testo, int x, int y, DoomdarkColorModel.Color colore, boolean allineataADestra, int larghezzaMassima, boolean conOmbra) {
		this.testo = testo;
		this.x = x;
		this.y = y;
		this.colore = colore;
		this.allineataADestra = allineataADestra;
		this.larghezzaMassima = larghezzaMassima;
		this.conOmbra = conOmbra;
	}

	public String getTesto() {
		return testo;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public DoomdarkColorModel.Color getColore() {
		return colore;
	}

	public boolean isAllineataADestra() {
		return allineataADestra;
	}

	public int getLarghezzaMassima() {
		return larghezzaMassima;
	}

	/**
	 * Se sotto la scritta va messa la sua ombra nera (spostata di {@link PosizioneDellIntestazione#SCOSTAMENTO_OMBRA} pixel in basso e a destra)
	 */
	public boolean isConOmbra() {
		return conOmbra;
	}
}
