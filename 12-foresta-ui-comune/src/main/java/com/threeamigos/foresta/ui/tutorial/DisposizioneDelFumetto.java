package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Punto;

/**
 * Dove sta il fumetto del tutorial e i suoi due pulsanti, senza toolkit: il fumetto sta dalla parte dello schermo dove c'è più posto rispetto al
 * bersaglio, centrato su di lui, senza uscire dallo schermo; il pollice (avanti, «ho capito») sta in basso a destra, l'annulla (spegne il tutorial)
 * alla sua sinistra. La punta della coda va al centro del bersaglio.
 *
 * @author Stefano Reksten
 */
public final class DisposizioneDelFumetto {

	/** Lo spazio fra il bordo del fumetto e i pulsanti, e fra il fumetto e il bordo dello schermo */
	public static final int MARGINE = 8;
	/** Lo spazio fra il fumetto e il bersaglio, perché la coda si veda */
	public static final int DISTANZA_DAL_BERSAGLIO = 14;

	private DisposizioneDelFumetto() {
	}

	/**
	 * L'area che contiene tutte e due.
	 */
	public static Area unione(Area a, Area b) {
		int x = Math.min(a.getX(), b.getX());
		int y = Math.min(a.getY(), b.getY());
		int destra = Math.max(a.getX() + a.getLarghezza(), b.getX() + b.getLarghezza());
		int sotto = Math.max(a.getY() + a.getAltezza(), b.getY() + b.getAltezza());
		return new Area(x, y, destra - x, sotto - y);
	}

	/**
	 * La larghezza del fumetto: quella del testo, o quanto serve ai due pulsanti se è più larga.
	 */
	public static int larghezza(int larghezzaDelTesto, int latoDelPulsante) {
		return Math.max(larghezzaDelTesto, 2 * latoDelPulsante + 3 * MARGINE);
	}

	/**
	 * L'altezza del fumetto: quella del testo più la riga dei pulsanti.
	 */
	public static int altezza(int altezzaDelTesto, int latoDelPulsante) {
		return altezzaDelTesto + latoDelPulsante + MARGINE;
	}

	/**
	 * L'angolo in alto a sinistra del fumetto, sempre nello stesso punto: al centro dello schermo. Da pagina a pagina e da suggerimento a suggerimento
	 * il fumetto non si muove; cambia solo dove punta la coda ({@link #puntaVerso}).
	 */
	public static Punto posizioneFissa(int larghezza, int altezza, int larghezzaSchermo, int altezzaSchermo) {
		return new Punto(dentro((larghezzaSchermo - larghezza) / 2, larghezza, larghezzaSchermo), dentro((altezzaSchermo - altezza) / 2, altezza, altezzaSchermo));
	}

	/**
	 * L'angolo in alto a sinistra del fumetto vicino al bersaglio (non lo usa più nessuno: oggi il fumetto sta fermo, vedi {@link #posizioneFissa}). Per un bersaglio alto più di un terzo dello schermo sta di fianco; altrimenti sopra se il bersaglio è
	 * nella metà bassa dello schermo e sotto se è in quella alta (e se lì non c'è posto, dall'altra parte), poi a sinistra e a destra; sempre
	 * centrato su di lui e senza coprirlo né uscire dallo schermo. Se non c'è posto da nessuna parte (un bersaglio enorme) sta dove si può, dentro
	 * lo schermo.
	 */
	public static Punto posizione(Area bersaglio, int larghezza, int altezza, int larghezzaSchermo, int altezzaSchermo) {
		int centroX = bersaglio.getX() + bersaglio.getLarghezza() / 2;
		int centroY = bersaglio.getY() + bersaglio.getAltezza() / 2;
		int xCentrata = dentro(centroX - larghezza / 2, larghezza, larghezzaSchermo);
		int yCentrata = dentro(centroY - altezza / 2, altezza, altezzaSchermo);
		int sopra = bersaglio.getY() - DISTANZA_DAL_BERSAGLIO - altezza;
		int sotto = bersaglio.getY() + bersaglio.getAltezza() + DISTANZA_DAL_BERSAGLIO;
		int aSinistra = bersaglio.getX() - DISTANZA_DAL_BERSAGLIO - larghezza;
		int aDestra = bersaglio.getX() + bersaglio.getLarghezza() + DISTANZA_DAL_BERSAGLIO;
		boolean sopraEntra = sopra >= MARGINE;
		boolean sottoEntra = sotto + altezza <= altezzaSchermo - MARGINE;
		boolean primaSopra = centroY > altezzaSchermo / 2;
		// Un bersaglio alto (una colonna di riquadri) ha il fumetto di fianco, dalla parte dove c'è più posto: sopra o sotto coprirebbe altro
		if (bersaglio.getAltezza() > altezzaSchermo / 3) {
			boolean sinistraEntra = aSinistra >= MARGINE;
			boolean destraEntra = aDestra + larghezza <= larghezzaSchermo - MARGINE;
			if (sinistraEntra && (!destraEntra || centroX > larghezzaSchermo / 2)) {
				return new Punto(aSinistra, yCentrata);
			}
			if (destraEntra) {
				return new Punto(aDestra, yCentrata);
			}
		}
		if (primaSopra && sopraEntra) {
			return new Punto(xCentrata, sopra);
		}
		if (!primaSopra && sottoEntra) {
			return new Punto(xCentrata, sotto);
		}
		if (sopraEntra) {
			return new Punto(xCentrata, sopra);
		}
		if (sottoEntra) {
			return new Punto(xCentrata, sotto);
		}
		if (aSinistra >= MARGINE) {
			return new Punto(aSinistra, yCentrata);
		}
		if (aDestra + larghezza <= larghezzaSchermo - MARGINE) {
			return new Punto(aDestra, yCentrata);
		}
		return new Punto(xCentrata, yCentrata);
	}

	// Il più vicino a quel valore che sta dentro lo schermo, con il margine
	private static int dentro(int valore, int dimensione, int dimensioneSchermo) {
		return Math.max(MARGINE, Math.min(valore, dimensioneSchermo - dimensione - MARGINE));
	}

	/**
	 * Il pollice: in basso a destra del fumetto.
	 */
	public static Area pollice(Punto fumetto, int larghezza, int altezza, int lato) {
		return new Area(fumetto.x + larghezza - MARGINE - lato, fumetto.y + altezza - MARGINE - lato, lato, lato);
	}

	/**
	 * L'annulla: a sinistra del pollice.
	 */
	public static Area annulla(Punto fumetto, int larghezza, int altezza, int lato) {
		Area pollice = pollice(fumetto, larghezza, altezza, lato);
		return new Area(pollice.getX() - MARGINE - lato, pollice.getY(), lato, lato);
	}

	/**
	 * Il punto verso cui va la coda: il centro del bersaglio.
	 */
	public static Punto puntaVerso(Area bersaglio) {
		return new Punto(bersaglio.getX() + bersaglio.getLarghezza() / 2, bersaglio.getY() + bersaglio.getAltezza() / 2);
	}
}
