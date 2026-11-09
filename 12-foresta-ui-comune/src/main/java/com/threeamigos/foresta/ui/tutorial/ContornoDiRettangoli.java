package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.Area;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Il contorno di un insieme di rettangoli (per esempio le icone della barra, che nel dock si ingrandiscono e si spostano), come una linea spezzata: tanti
 * segmenti orizzontali e verticali, ognuno un rettangolo sottile da riempire, senza toolkit. Dove i rettangoli si toccano o si sovrappongono il contorno è
 * uno solo, intorno a tutti; dove sono staccati, ognuno ha il suo. Serve a bordare quello a cui punta il tutorial anche quando non è un rettangolo solo.
 *
 * @author Stefano Reksten
 */
public final class ContornoDiRettangoli {

	private ContornoDiRettangoli() {
	}

	/**
	 * I segmenti del contorno dell'unione dei rettangoli, ingranditi di {@code margine} pixel per lato, ognuno spesso {@code spessore} pixel e centrato sul
	 * bordo (agli angoli i segmenti si sovrappongono, e non restano buchi). Vuoto se non ci sono rettangoli.
	 */
	public static List<Area> segmenti(List<Area> rettangoli, int margine, int spessore) {
		List<Area> segmenti = new ArrayList<>();
		if (rettangoli.isEmpty()) {
			return segmenti;
		}
		// Le coordinate dei bordi, senza doppioni: fra due coordinate vicine la griglia ha una cella tutta piena o tutta vuota
		int n = rettangoli.size();
		int[] xs = new int[2 * n];
		int[] ys = new int[2 * n];
		for (int i = 0; i < n; i++) {
			Area a = rettangoli.get(i);
			xs[2 * i] = a.getX() - margine;
			xs[2 * i + 1] = a.getX() + a.getLarghezza() + margine;
			ys[2 * i] = a.getY() - margine;
			ys[2 * i + 1] = a.getY() + a.getAltezza() + margine;
		}
		xs = senzaDoppioni(xs);
		ys = senzaDoppioni(ys);
		int colonne = xs.length - 1;
		int righe = ys.length - 1;
		boolean[][] piena = new boolean[righe][colonne];
		for (int i = 0; i < n; i++) {
			Area a = rettangoli.get(i);
			int x0 = indice(xs, a.getX() - margine);
			int x1 = indice(xs, a.getX() + a.getLarghezza() + margine);
			int y0 = indice(ys, a.getY() - margine);
			int y1 = indice(ys, a.getY() + a.getAltezza() + margine);
			for (int r = y0; r < y1; r++) {
				for (int c = x0; c < x1; c++) {
					piena[r][c] = true;
				}
			}
		}
		int meta = spessore / 2;
		// I bordi orizzontali: sopra la riga r (r = 0..righe), dove la cella sopra e quella sotto non sono uguali. Si uniscono le celle di fila
		for (int r = 0; r <= righe; r++) {
			int c = 0;
			while (c < colonne) {
				if (differenti(piena, r - 1, c, r, c)) {
					int inizio = c;
					while (c < colonne && differenti(piena, r - 1, c, r, c)) {
						c++;
					}
					segmenti.add(new Area(xs[inizio] - meta, ys[r] - meta, xs[c] - xs[inizio] + 2 * meta, spessore));
				} else {
					c++;
				}
			}
		}
		// I bordi verticali: a sinistra della colonna c (c = 0..colonne)
		for (int c = 0; c <= colonne; c++) {
			int r = 0;
			while (r < righe) {
				if (differenti(piena, r, c - 1, r, c)) {
					int inizio = r;
					while (r < righe && differenti(piena, r, c - 1, r, c)) {
						r++;
					}
					segmenti.add(new Area(xs[c] - meta, ys[inizio] - meta, spessore, ys[r] - ys[inizio] + 2 * meta));
				} else {
					r++;
				}
			}
		}
		return segmenti;
	}

	private static boolean differenti(boolean[][] piena, int r1, int c1, int r2, int c2) {
		return isPiena(piena, r1, c1) != isPiena(piena, r2, c2);
	}

	private static boolean isPiena(boolean[][] piena, int r, int c) {
		return r >= 0 && r < piena.length && c >= 0 && c < piena[0].length && piena[r][c];
	}

	private static int[] senzaDoppioni(int[] valori) {
		int[] copia = valori.clone();
		Arrays.sort(copia);
		int quanti = 0;
		for (int i = 0; i < copia.length; i++) {
			if (i == 0 || copia[i] != copia[i - 1]) {
				copia[quanti++] = copia[i];
			}
		}
		return Arrays.copyOf(copia, quanti);
	}

	private static int indice(int[] ordinati, int valore) {
		return Arrays.binarySearch(ordinati, valore);
	}
}
