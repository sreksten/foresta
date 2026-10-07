package com.threeamigos.foresta.ui;

/**
 * Di quanto ingrandire all'avvio le immagini che nei file sono a metà risoluzione (2.0 = 200%).
 * Si imposta una volta sola, prima di creare la UI (vedi l'argomento ZOOM= di Main): ImageCache e
 * DimensioniRisorsa lo leggono, e ImageCache lo fa al primo uso.
 */
public final class LivelloDiZoom {

	public static final double PREDEFINITO = 2.0;

	private static double valore = PREDEFINITO;

	private LivelloDiZoom() {
	}

	public static void imposta(double nuovoValore) {
		if (!(nuovoValore > 0)) {
			throw new IllegalArgumentException("Livello di zoom non valido: " + nuovoValore);
		}
		valore = nuovoValore;
	}

	public static double valore() {
		return valore;
	}
}
