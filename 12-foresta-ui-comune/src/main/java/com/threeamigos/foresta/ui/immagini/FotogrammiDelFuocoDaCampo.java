package com.threeamigos.foresta.ui.immagini;

/**
 * Un fuoco da campo disegnato via codice, senza risorse grafiche e senza toolkit: due ciocchi incrociati e tre strati di fiamma (rosso, arancio, giallo) le
 * cui punte oscillano a ritmi diversi, più qualche scintilla. Gli otto fotogrammi sono calcolati una volta sola, pixel per pixel, e ripetuti a dieci al
 * secondo.
 */
public final class FotogrammiDelFuocoDaCampo {

	public static final int LARGHEZZA = 48;
	public static final int ALTEZZA = 60;
	public static final int FOTOGRAMMI = 8;
	private static final double FOTOGRAMMI_AL_SECONDO = 10;

	private static final int ROSSO = 0xC8280A;
	private static final int ARANCIO = 0xF58214;
	private static final int GIALLO = 0xFFE15A;
	private static final int CIOCCO = 0x643C1E;
	private static final int CIOCCO_SCURO = 0x412612;

	private static ImmagineDiPixel[] fotogrammi;

	private FotogrammiDelFuocoDaCampo() {
	}

	/**
	 * Il fotogramma da mostrare a quel numero di secondi dall'inizio della pagina
	 */
	public static ImmagineDiPixel al(double secondi) {
		return fotogrammi()[indiceAl(secondi)];
	}

	public static int indiceAl(double secondi) {
		return (int) Math.floor(Math.max(0, secondi) * FOTOGRAMMI_AL_SECONDO) % FOTOGRAMMI;
	}

	public static synchronized ImmagineDiPixel[] fotogrammi() {
		if (fotogrammi == null) {
			ImmagineDiPixel[] nuovi = new ImmagineDiPixel[FOTOGRAMMI];
			for (int i = 0; i < FOTOGRAMMI; i++) {
				nuovi[i] = disegnaFotogramma(i);
			}
			fotogrammi = nuovi;
		}
		return fotogrammi;
	}

	private static ImmagineDiPixel disegnaFotogramma(int indice) {
		int[] argb = new int[LARGHEZZA * ALTEZZA];
		double fase = 2 * Math.PI * indice / FOTOGRAMMI;

		// Fiamme, dalla più esterna alla più interna
		disegnaFiamma(argb, ROSSO, 20, 46, fase, 0);
		disegnaFiamma(argb, ARANCIO, 14, 34, fase, 1.7);
		disegnaFiamma(argb, GIALLO, 8, 20, fase, 3.1);

		// Scintille che salgono
		for (int s = 0; s < 3; s++) {
			double salita = ((indice + s * 3) % FOTOGRAMMI) / (double) FOTOGRAMMI;
			int x = LARGHEZZA / 2 + (int) Math.round(Math.sin(fase + s * 2.1) * 8) + (s - 1) * 5;
			int y = ALTEZZA - 16 - (int) Math.round(salita * 38);
			riempiRettangolo(argb, x, y, 2, 2, GIALLO);
		}

		// Ciocchi incrociati alla base, davanti alle fiamme
		riempiPoligono(argb, new int[]{4, 10, 44, 38}, new int[]{ALTEZZA - 10, ALTEZZA - 14, ALTEZZA - 4, ALTEZZA}, CIOCCO_SCURO);
		riempiPoligono(argb, new int[]{38, 44, 10, 4}, new int[]{ALTEZZA - 14, ALTEZZA - 10, ALTEZZA, ALTEZZA - 4}, CIOCCO);
		return new ImmagineDiPixel(LARGHEZZA, ALTEZZA, argb);
	}

	/**
	 * Una lingua di fuoco: larga alla base, con la punta che ondeggia di lato e cambia altezza nel tempo; lo sfasamento distingue gli strati.
	 */
	private static void disegnaFiamma(int[] argb, int colore, int semilarghezza, int altezza, double fase, double sfasamento) {
		int centro = LARGHEZZA / 2;
		int base = ALTEZZA - 8;
		int altezzaPunta = altezza + (int) Math.round(Math.sin(fase * 2 + sfasamento) * altezza * 0.15);
		int scartoPunta = (int) Math.round(Math.sin(fase + sfasamento) * semilarghezza * 0.4);
		int scartoSpalle = (int) Math.round(Math.cos(fase * 3 + sfasamento) * semilarghezza * 0.2);
		int[] x = {centro - semilarghezza, centro - semilarghezza * 3 / 4 + scartoSpalle, centro + scartoPunta,
				centro + semilarghezza * 3 / 4 + scartoSpalle, centro + semilarghezza};
		int[] y = {base, base - altezza / 2, base - altezzaPunta, base - altezza / 2, base};
		riempiPoligono(argb, x, y, colore);
	}

	private static void riempiRettangolo(int[] argb, int x, int y, int larghezza, int altezza, int colore) {
		for (int j = Math.max(0, y); j < Math.min(ALTEZZA, y + altezza); j++) {
			for (int i = Math.max(0, x); i < Math.min(LARGHEZZA, x + larghezza); i++) {
				argb[j * LARGHEZZA + i] = 0xFF000000 | colore;
			}
		}
	}

	/**
	 * Riempie un poligono (regola pari-dispari): un pixel è dentro se lo è il suo centro.
	 */
	private static void riempiPoligono(int[] argb, int[] px, int[] py, int colore) {
		int n = px.length;
		for (int y = 0; y < ALTEZZA; y++) {
			double yCentro = y + 0.5;
			double[] incroci = new double[n];
			int quanti = 0;
			for (int i = 0; i < n; i++) {
				int j = (i + 1) % n;
				double y1 = py[i];
				double y2 = py[j];
				if ((y1 <= yCentro && y2 > yCentro) || (y2 <= yCentro && y1 > yCentro)) {
					incroci[quanti++] = px[i] + (yCentro - y1) * (px[j] - px[i]) / (y2 - y1);
				}
			}
			java.util.Arrays.sort(incroci, 0, quanti);
			for (int k = 0; k + 1 < quanti; k += 2) {
				int da = (int) Math.ceil(incroci[k] - 0.5);
				int a = (int) Math.ceil(incroci[k + 1] - 0.5);
				for (int x = Math.max(0, da); x < Math.min(LARGHEZZA, a); x++) {
					argb[y * LARGHEZZA + x] = 0xFF000000 | colore;
				}
			}
		}
	}
}
