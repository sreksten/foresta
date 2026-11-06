package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.util.Arrays;
import java.util.List;

/**
 * La torcia dei corridori del logo, come pixel: una copia del logo visibile solo entro un piccolo raggio attorno alla posizione corrente di ciascun
 * corridore, ricreata da zero a ogni fotogramma (nessun accumulo: e' una torcia che scorre, non una rivelazione progressiva). E' quel che fa in Swing
 * il gradiente radiale applicato con {@code AlphaComposite.DstIn}, per i toolkit che non hanno una composizione del genere (libGDX): per ogni
 * corridore l'opacita' va da {@link #ALPHA_MASSIMA} al centro a 0 al bordo del cerchio, e quelle dei corridori vicini si sommano come una sopra
 * l'altra; il logo ha poi la sua alpha moltiplicata per quella della torcia.
 */
public final class TorciaDelLogo {

	/** L'opacita' massima del logo sotto la torcia (0-255): voluta, cosi' il logo «si intravede» e basta, non appare a piena opacita' */
	public static final int ALPHA_MASSIMA = 200;

	private final ImmagineDiPixel logo;
	private final int[] pixel;
	private final float[] copertura;

	public TorciaDelLogo(ImmagineDiPixel logo) {
		this.logo = logo;
		this.pixel = new int[logo.getLarghezza() * logo.getAltezza()];
		this.copertura = new float[pixel.length];
	}

	/**
	 * Ricalcola i pixel del logo rivelato dalla torcia dei corridori già partiti. Restituisce lo stesso array a ogni chiamata (ARGB, per righe,
	 * trasparente dove non arriva nessuna torcia).
	 */
	public int[] rivela(List<AnimazioneDelLogo.Corridore> corridori, int raggio) {
		int w = logo.getLarghezza();
		int h = logo.getAltezza();
		Arrays.fill(pixel, 0);
		Arrays.fill(copertura, 0f);
		for (AnimazioneDelLogo.Corridore corridore : corridori) {
			if (!corridore.isPartito()) {
				continue; // ritardo non ancora scaduto: nessuna torcia
			}
			AnimazioneDelLogo.Posizione p = corridore.getPosizione();
			int x0 = Math.max(0, (int) Math.floor(p.x - raggio));
			int x1 = Math.min(w - 1, (int) Math.ceil(p.x + raggio));
			int y0 = Math.max(0, (int) Math.floor(p.y - raggio));
			int y1 = Math.min(h - 1, (int) Math.ceil(p.y + raggio));
			for (int y = y0; y <= y1; y++) {
				for (int x = x0; x <= x1; x++) {
					double distanza = Math.hypot(x + 0.5 - p.x, y + 0.5 - p.y);
					if (distanza >= raggio) {
						continue;
					}
					float propria = (float) (ALPHA_MASSIMA * (1.0 - distanza / raggio) / 255.0);
					int i = y * w + x;
					// una torcia sopra l'altra: 1 - (1 - a)(1 - b)
					copertura[i] = 1f - (1f - copertura[i]) * (1f - propria);
				}
			}
		}
		for (int i = 0; i < pixel.length; i++) {
			if (copertura[i] > 0f) {
				int argb = logo.getArgb(i % w, i / w);
				int alpha = Math.round((argb >>> 24) * copertura[i]);
				pixel[i] = (alpha << 24) | (argb & 0xFFFFFF);
			}
		}
		return pixel;
	}
}
