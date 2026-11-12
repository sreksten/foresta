package com.threeamigos.foresta.ui.componenti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Una cornice alta (quella dell'inventario) su uno schermo più basso di lei: invece di rimpicciolirla la si taglia nel mezzo, e si disegnano la sua
 * parte di sopra e la sua parte di sotto una attaccata all'altra (layout_telefono.md §6). Lo stesso in larghezza ({@link #colonne}), quando la cornice
 * deve stringersi per lasciare spazio alla colonna centrale: la sua parte di sinistra e quella di destra; con i due tagli i pezzi sono quattro. I pixel non si ingrandiscono e non si deformano: sono due ritagli
 * della stessa immagine. Le fasce si contano in righe dell'immagine originale, senza zoom, perché con lo zoom 2 ogni riga resti intera; se la cornice ci sta,
 * la fascia è una sola, l'immagine intera.
 */
public final class CorniceTagliata {

	/**
	 * Un ritaglio dell'immagine: da quale riga (dell'immagine senza zoom) e quante, e a che distanza dalla cima della cornice va disegnato e quanto alto
	 * (con lo zoom)
	 */
	public static final class Fascia {
		private final int ySorgente;
		private final int altezzaSorgente;
		private final int yDestinazione;
		private final int altezzaDestinazione;

		Fascia(int ySorgente, int altezzaSorgente, int yDestinazione, int altezzaDestinazione) {
			this.ySorgente = ySorgente;
			this.altezzaSorgente = altezzaSorgente;
			this.yDestinazione = yDestinazione;
			this.altezzaDestinazione = altezzaDestinazione;
		}

		public int getYSorgente() {
			return ySorgente;
		}

		public int getAltezzaSorgente() {
			return altezzaSorgente;
		}

		/** Dalla cima della cornice */
		public int getYDestinazione() {
			return yDestinazione;
		}

		public int getAltezzaDestinazione() {
			return altezzaDestinazione;
		}
	}

	private CorniceTagliata() {
	}

	/**
	 * Le fasce da disegnare, dall'alto in basso.
	 *
	 * @param altezzaImmagine    l'altezza dell'immagine, senza zoom
	 * @param zoom               lo zoom con cui si disegna
	 * @param altezzaDisponibile quanto può essere alta la cornice disegnata, con lo zoom
	 */
	public static List<Fascia> fasce(int altezzaImmagine, double zoom, int altezzaDisponibile) {
		if (Math.round(altezzaImmagine * zoom) <= altezzaDisponibile) {
			return Collections.singletonList(new Fascia(0, altezzaImmagine, 0, (int) Math.round(altezzaImmagine * zoom)));
		}
		// Le righe dell'immagine che ci stanno: metà da sopra (quella in più, se sono dispari) e metà da sotto
		int righe = Math.max(0, (int) Math.floor(altezzaDisponibile / zoom));
		int sopra = (righe + 1) / 2;
		int sotto = righe - sopra;
		List<Fascia> fasce = new ArrayList<>();
		int altezzaSopra = (int) Math.round(sopra * zoom);
		fasce.add(new Fascia(0, sopra, 0, altezzaSopra));
		fasce.add(new Fascia(altezzaImmagine - sotto, sotto, altezzaSopra, (int) Math.round(sotto * zoom)));
		return fasce;
	}

	/**
	 * Le colonne da disegnare, da sinistra a destra: come {@link #fasce}, ma in larghezza (in una {@link Fascia} la y vale come x, l'altezza come larghezza)
	 *
	 * @param larghezzaImmagine    la larghezza dell'immagine, senza zoom
	 * @param zoom                 lo zoom con cui si disegna
	 * @param larghezzaDisponibile quanto può essere larga la cornice disegnata, con lo zoom
	 */
	public static List<Fascia> colonne(int larghezzaImmagine, double zoom, int larghezzaDisponibile) {
		return fasce(larghezzaImmagine, zoom, larghezzaDisponibile);
	}

	/**
	 * Quanto è larga la cornice disegnata: la somma delle colonne
	 */
	public static int larghezza(List<Fascia> colonne) {
		return altezza(colonne);
	}

	/**
	 * Quanto è alta la cornice disegnata: la somma delle fasce
	 */
	public static int altezza(List<Fascia> fasce) {
		int altezza = 0;
		for (Fascia fascia : fasce) {
			altezza += fascia.getAltezzaDestinazione();
		}
		return altezza;
	}
}
