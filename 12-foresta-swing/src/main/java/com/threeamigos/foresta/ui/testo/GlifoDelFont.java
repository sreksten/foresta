package com.threeamigos.foresta.ui.testo;

/**
 * Un carattere di un font a immagine: una piccola immagine, alta come un riquadro (tutti i glifi di un font hanno la
 * stessa altezza, quindi la stessa linea di base) e larga fino al suo pixel acceso più a destra. Ogni pixel ha un
 * <b>livello</b> da 0 a 255, quanto il pixel copre lo sfondo: 0 è trasparente, 255 è il colore pieno del testo, in mezzo
 * il colore sfuma su quello che c'è dietro (è l'alfa del colore: il colore resta lo stesso, non si scurisce).
 */
public final class GlifoDelFont {

	private final int larghezza;
	private final int altezza;
	private final byte[] livelli;

	/**
	 * @param livelli larghezza x altezza livelli, per righe dall'alto
	 */
	GlifoDelFont(int larghezza, int altezza, byte[] livelli) {
		if (livelli.length != larghezza * altezza) {
			throw new IllegalArgumentException("Servono " + larghezza * altezza + " livelli, ce ne sono " + livelli.length);
		}
		this.larghezza = larghezza;
		this.altezza = altezza;
		this.livelli = livelli;
	}

	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	/**
	 * Il livello del pixel, da 0 (trasparente) a 255 (il colore pieno)
	 */
	public int getLivello(int x, int y) {
		return livelli[y * larghezza + x] & 0xFF;
	}
}
