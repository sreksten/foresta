package com.threeamigos.foresta.ui.sfx;

import java.util.Arrays;

/**
 * Le scie bianche dei corridori del logo, come pixel che si accumulano: la scia di un corridore cresce soltanto (fino a che il ciclo non ricomincia),
 * quindi basta aggiungere i tratti nuovi invece di ridisegnarla tutta a ogni fotogramma. E' quel che fa in Swing la polilinea tracciata quattro volte
 * con tratti via via piu' sottili e piu' opachi (alone ampio, alone intermedio, bordo del nucleo, nucleo netto), per i toolkit che non hanno una
 * polilinea con spessore (libGDX): per ogni pixel si tiene la distanza dal tratto piu' vicino, e da quella si ricava l'opacita' con le stesse
 * quattro passate, composte una sull'altra. Il colore e' sempre bianco; l'opacita' delle scie nel tempo la mette chi disegna.
 */
public final class ScieDelLogo {

	// Le quattro passate: larghezza del tratto e opacita' (0-255), dalla piu' larga e trasparente alla piu' sottile e piena
	private static final double[] LARGHEZZE = {14.0, 8.0, 3.0, 1.2};
	private static final int[] ALPHA = {35, 80, 210, 255};
	// Oltre questa distanza dal tratto non c'e' piu' niente da disegnare: meta' della passata piu' larga piu' mezzo pixel di sfumatura
	private static final double RAGGIO_MASSIMO = LARGHEZZE[0] / 2 + 0.5;

	private final int larghezza;
	private final int altezza;
	private final float[] distanza;
	private final int[] pixel;
	private boolean cambiato;

	public ScieDelLogo(int larghezza, int altezza) {
		this.larghezza = larghezza;
		this.altezza = altezza;
		this.distanza = new float[larghezza * altezza];
		this.pixel = new int[larghezza * altezza];
		azzera();
	}

	/** Toglie tutte le scie */
	public void azzera() {
		Arrays.fill(distanza, Float.MAX_VALUE);
		Arrays.fill(pixel, 0);
		cambiato = true;
	}

	/** Aggiunge alla scia il tratto fra due punti consecutivi del percorso */
	public void aggiungi(AnimazioneDelLogo.Posizione da, AnimazioneDelLogo.Posizione a) {
		double raggio = RAGGIO_MASSIMO;
		int x0 = Math.max(0, (int) Math.floor(Math.min(da.x, a.x) - raggio));
		int x1 = Math.min(larghezza - 1, (int) Math.ceil(Math.max(da.x, a.x) + raggio));
		int y0 = Math.max(0, (int) Math.floor(Math.min(da.y, a.y) - raggio));
		int y1 = Math.min(altezza - 1, (int) Math.ceil(Math.max(da.y, a.y) + raggio));
		double dx = a.x - da.x;
		double dy = a.y - da.y;
		double lunghezzaQuadrata = dx * dx + dy * dy;
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				double px = x + 0.5;
				double py = y + 0.5;
				double t = lunghezzaQuadrata > 0 ? ((px - da.x) * dx + (py - da.y) * dy) / lunghezzaQuadrata : 0;
				t = Math.max(0, Math.min(1, t));
				double d = Math.hypot(px - (da.x + t * dx), py - (da.y + t * dy));
				int i = y * larghezza + x;
				if (d < distanza[i]) {
					distanza[i] = (float) d;
					pixel[i] = alpha(d) << 24 | 0xFFFFFF;
					cambiato = true;
				}
			}
		}
	}

	/**
	 * L'opacita' (0-255) di un pixel a quella distanza dal tratto: le quattro passate, ciascuna con il bordo sfumato di un pixel, una sopra l'altra
	 */
	static int alpha(double distanza) {
		double trasparenza = 1.0;
		for (int k = 0; k < LARGHEZZE.length; k++) {
			double copertura = Math.max(0.0, Math.min(1.0, LARGHEZZE[k] / 2 - distanza + 0.5));
			trasparenza *= 1.0 - (ALPHA[k] / 255.0) * copertura;
		}
		return (int) Math.round((1.0 - trasparenza) * 255);
	}

	/** I pixel (ARGB, per righe); lo stesso array a ogni chiamata */
	public int[] getPixel() {
		return pixel;
	}

	/** Vero se i pixel sono cambiati dall'ultima volta che qualcuno ha chiesto {@link #segnaLetto()} */
	public boolean isCambiato() {
		return cambiato;
	}

	/** Chi ha ricopiato i pixel (in una texture, per esempio) lo dice qui */
	public void segnaLetto() {
		cambiato = false;
	}
}
