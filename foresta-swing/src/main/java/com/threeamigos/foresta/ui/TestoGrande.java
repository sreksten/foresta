package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Il testo nell'alfabeto grande, quello della storia: solo lettere minuscole senza accento, cifre e ' , . ? (vedi
 * ImageCache.lettere). Le vocali accentate si scrivono, come d'uso con questo alfabeto, con la vocale e l'apostrofo
 * ("è" diventa "e'"); le maiuscole diventano minuscole; gli altri caratteri restano uno spazietto.
 */
final class TestoGrande {

	/**
	 * Lo spazio fra un carattere e l'altro.
	 */
	static final int SPAZIO_FRA_CARATTERI = 1;
	/**
	 * Da una riga all'altra.
	 */
	static final int ALTEZZA_RIGA = 36;
	private static final int LARGHEZZA_SPAZIO = 10;
	private static final int ALTEZZA_GLIFO = 28;
	/**
	 * Quanto della luminosità resta al testo grigio scuro.
	 */
	private static final float LUMINOSITA_SCURO = 0.3f;
	/**
	 * Quanto è scuro l'alone attorno alle lettere: più di 1 lo rende pieno vicino alle lettere.
	 */
	private static final float FORZA_ALONE = 4f;

	private TestoGrande() {
	}

	/**
	 * Il testo come lo scrive questo alfabeto: minuscolo, con le vocali accentate scritte con l'apostrofo.
	 */
	static String normalizza(String testo) {
		StringBuilder normalizzato = new StringBuilder();
		for (char c : testo.toLowerCase().toCharArray()) {
			switch (c) {
				case 'à':
				case 'á':
					normalizzato.append("a'");
					break;
				case 'è':
				case 'é':
					normalizzato.append("e'");
					break;
				case 'ì':
				case 'í':
					normalizzato.append("i'");
					break;
				case 'ò':
				case 'ó':
					normalizzato.append("o'");
					break;
				case 'ù':
				case 'ú':
					normalizzato.append("u'");
					break;
				default:
					normalizzato.append(c);
					break;
			}
		}
		return normalizzato.toString();
	}

	/**
	 * Il glifo del carattere (già normalizzato), o null se l'alfabeto non ce l'ha.
	 */
	static BufferedImage glifo(char c) {
		if (c >= 'a' && c <= 'z') {
			return ImageCache.lettere[c - 'a'];
		} else if (c >= '0' && c <= '9') {
			return ImageCache.cifre[c - '0'];
		} else if (c == '\'') {
			return ImageCache.apostrofo;
		} else if (c == ',') {
			return ImageCache.virgola;
		} else if (c == '.') {
			return ImageCache.punto;
		} else if (c == '?') {
			return ImageCache.puntodd;
		}
		return null;
	}

	static int larghezzaCarattere(char c) {
		BufferedImage glifo = glifo(c);
		if (glifo != null) {
			return glifo.getWidth();
		}
		return c == ' ' ? LARGHEZZA_SPAZIO : 1;
	}

	/**
	 * Quanto è largo il testo (già normalizzato), su una riga.
	 */
	static int larghezza(String testo) {
		int larghezza = 0;
		for (char c : testo.toCharArray()) {
			larghezza += larghezzaCarattere(c) + SPAZIO_FRA_CARATTERI;
		}
		return larghezza;
	}

	/**
	 * Le righe del testo, andando a capo fra una parola e l'altra per non superare la larghezza massima.
	 */
	static List<String> righe(String testo, int larghezzaMassima) {
		List<String> righe = new ArrayList<>();
		StringBuilder riga = new StringBuilder();
		StringTokenizer parole = new StringTokenizer(normalizza(testo), " ");
		while (parole.hasMoreTokens()) {
			String parola = parole.nextToken();
			String conParola = riga.length() == 0 ? parola : riga + " " + parola;
			if (riga.length() > 0 && larghezza(conParola) > larghezzaMassima) {
				righe.add(riga.toString());
				riga = new StringBuilder(parola);
			} else {
				riga = new StringBuilder(conParola);
			}
		}
		if (riga.length() > 0) {
			righe.add(riga.toString());
		}
		return righe;
	}

	/**
	 * Disegna una riga (già normalizzata) a partire da x, y.
	 */
	static void disegnaRiga(Graphics2D graphics, String riga, int x, int y) {
		for (char c : riga.toCharArray()) {
			BufferedImage glifo = glifo(c);
			if (glifo != null) {
				graphics.drawImage(glifo, x, y, null);
			}
			x += larghezzaCarattere(c) + SPAZIO_FRA_CARATTERI;
		}
	}

	/**
	 * Il testo in un'immagine larga quanto la larghezza massima, a capo dove serve, con le righe centrate o allineate
	 * a sinistra.
	 */
	static BufferedImage immagine(String testo, int larghezzaMassima, boolean centrato) {
		List<String> righe = righe(testo, larghezzaMassima);
		BufferedImage immagine = new BufferedImage(Math.max(1, larghezzaMassima),
				Math.max(1, (righe.size() - 1) * ALTEZZA_RIGA + ALTEZZA_GLIFO), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = immagine.createGraphics();
		try {
			for (int i = 0; i < righe.size(); i++) {
				String riga = righe.get(i);
				int x = centrato ? (larghezzaMassima - larghezza(riga)) / 2 : 0;
				disegnaRiga(graphics, riga, x, i * ALTEZZA_RIGA);
			}
		} finally {
			graphics.dispose();
		}
		return immagine;
	}

	/**
	 * La stessa immagine con un alone nero sfumato attorno alle lettere (o a qualunque disegno, come i loghi), perché
	 * si leggano anche su uno sfondo chiaro o movimentato: l'immagine si allarga di {@code raggio} pixel per lato.
	 */
	static BufferedImage conAlone(BufferedImage immagine, int raggio) {
		// Un margine doppio: ConvolveOp con EDGE_NO_OP non sfuma i pixel entro raggio dal bordo dell'immagine di
		// lavoro, li copia soltanto. Calcolando la sfumatura su un margine doppio, quel bordo non sfumato cade fuori
		// dal ritaglio finale, e l'alone non resta tagliato sopra e sotto (e ai lati).
		int margine = 2 * raggio;
		int larghezzaConMargine = immagine.getWidth() + 2 * margine;
		int altezzaConMargine = immagine.getHeight() + 2 * margine;
		// La sagoma nera delle lettere
		BufferedImage sagoma = new BufferedImage(larghezzaConMargine, altezzaConMargine, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < immagine.getHeight(); y++) {
			for (int x = 0; x < immagine.getWidth(); x++) {
				int alfa = immagine.getRGB(x, y) >>> 24;
				sagoma.setRGB(x + margine, y + margine, alfa << 24);
			}
		}
		// Sfumata, e rinforzata perché non svanisca troppo presto
		float[] nucleo = new float[(2 * raggio + 1) * (2 * raggio + 1)];
		Arrays.fill(nucleo, FORZA_ALONE / nucleo.length);
		BufferedImage aloneConMargine = new ConvolveOp(new Kernel(2 * raggio + 1, 2 * raggio + 1, nucleo), ConvolveOp.EDGE_NO_OP, null)
				.filter(sagoma, null);
		BufferedImage alone = new BufferedImage(immagine.getWidth() + 2 * raggio, immagine.getHeight() + 2 * raggio,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = alone.createGraphics();
		try {
			graphics.drawImage(aloneConMargine, -raggio, -raggio, null);
			graphics.drawImage(immagine, raggio, raggio, null);
		} finally {
			graphics.dispose();
		}
		return alone;
	}

	/**
	 * La stessa immagine, in grigio scuro: per quello che manca ancora (un trofeo non vinto).
	 */
	static BufferedImage scura(BufferedImage immagine) {
		return new RescaleOp(new float[] {LUMINOSITA_SCURO, LUMINOSITA_SCURO, LUMINOSITA_SCURO, 1f}, new float[4], null)
				.filter(immagine, null);
	}
}
