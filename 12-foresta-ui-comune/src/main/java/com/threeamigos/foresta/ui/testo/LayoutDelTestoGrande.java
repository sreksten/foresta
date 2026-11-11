package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Il testo nell'alfabeto grande (quello della storia, dei titoli, dei punteggi della sfida), senza toolkit: i glifi sono quelli dell'atlante
 * <code>img/font/alfabeto_grande.png</code> (maiuscole, anche accentate, cifre e punteggiatura; le minuscole si scrivono con le maiuscole, e i caratteri che
 * l'atlante non ha restano uno spazietto), ingranditi dello zoom. Qui ci sono le misure, l'andare a capo, la composizione del testo in un'immagine di pixel,
 * l'alone nero attorno alle lettere e la versione in grigio scuro; chi disegna (Swing, libGDX) trasforma l'immagine di pixel nel suo toolkit.
 */
public final class LayoutDelTestoGrande {

	private static final String RISORSA_ALFABETO_GRANDE = "/com/threeamigos/foresta/img/font/alfabeto_grande.png";
	private static final int LARGHEZZA_DELLO_SPAZIO_NELL_ATLANTE = 5;

	/**
	 * Lo spazio fra un carattere e l'altro.
	 */
	public static final int SPAZIO_FRA_CARATTERI = 1;
	/**
	 * Da una riga all'altra.
	 */
	public static final int ALTEZZA_RIGA = 36;
	private static final int LARGHEZZA_SPAZIO = 10;
	/**
	 * L'altezza delle lettere.
	 */
	public static final int ALTEZZA_GLIFO = 28;
	/**
	 * Quanto della luminosità resta al testo grigio scuro.
	 */
	private static final float LUMINOSITA_SCURO = 0.3f;
	/**
	 * Quanto è scuro l'alone attorno alle lettere: più di 1 lo rende pieno vicino alle lettere.
	 */
	private static final float FORZA_ALONE = 4f;

	private static ImmagineDelFontAColori atlante;

	private LayoutDelTestoGrande() {
	}

	private static synchronized ImmagineDelFontAColori atlante() {
		if (atlante == null) {
			atlante = ImmagineDelFontAColori.carica(RISORSA_ALFABETO_GRANDE, LARGHEZZA_DELLO_SPAZIO_NELL_ATLANTE);
		}
		return atlante;
	}

	/**
	 * Il glifo del carattere, senza zoom, o null se l'alfabeto non ce l'ha. L'atlante ha le maiuscole, accentate comprese: le minuscole si scrivono con quelle.
	 * È alto quanto un riquadro dell'atlante: {@link #ALTEZZA_GLIFO} sono le lettere, il resto è lo spazio per accenti e cediglie.
	 */
	public static GlifoAColori glifo(char c) {
		char maiuscola = Character.toUpperCase(c);
		if (maiuscola == ' ') {
			return null; // lo spazio lo misura chi scrive il testo
		}
		return atlante().getGlifi().get(maiuscola);
	}

	private static int ingrandito(int misura) {
		return Math.max(1, (int) Math.round(misura * LivelloDiZoom.valore()));
	}

	/**
	 * L'altezza dell'immagine di una riga: quella di un riquadro dell'atlante, accenti e cediglie compresi.
	 */
	public static int altezzaConAccenti() {
		return ingrandito(glifo('A').getAltezza());
	}

	public static int larghezzaCarattere(char c) {
		GlifoAColori glifo = glifo(c);
		if (glifo != null) {
			return ingrandito(glifo.getLarghezza());
		}
		return c == ' ' ? LARGHEZZA_SPAZIO : 1;
	}

	/**
	 * Quanto è largo il testo, su una riga.
	 */
	public static int larghezza(String testo) {
		int larghezza = 0;
		for (char c : testo.toCharArray()) {
			larghezza += larghezzaCarattere(c) + SPAZIO_FRA_CARATTERI;
		}
		return larghezza;
	}

	/**
	 * Le righe del testo, andando a capo fra una parola e l'altra per non superare la larghezza massima. Una parola che da sola non ci sta (su uno schermo
	 * stretto) si spezza con un trattino all'ultimo carattere che ci sta, e prosegue sulla riga dopo.
	 */
	public static List<String> righe(String testo, int larghezzaMassima) {
		List<String> righe = new ArrayList<>();
		StringBuilder riga = new StringBuilder();
		StringTokenizer parole = new StringTokenizer(testo, " ");
		while (parole.hasMoreTokens()) {
			String parola = parole.nextToken();
			while (larghezza(parola) > larghezzaMassima && parola.length() > 1) {
				if (riga.length() > 0) {
					righe.add(riga.toString());
					riga = new StringBuilder();
				}
				int quanti = caratteriCheStannoColTrattino(parola, larghezzaMassima);
				righe.add(parola.substring(0, quanti) + "-");
				parola = parola.substring(quanti);
			}
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
	 * Quanti caratteri della parola stanno nella larghezza con il trattino dopo: almeno uno, perché la parola si accorci comunque
	 */
	private static int caratteriCheStannoColTrattino(String parola, int larghezzaMassima) {
		int quanti = 1;
		while (quanti < parola.length() - 1 && larghezza(parola.substring(0, quanti + 1) + "-") <= larghezzaMassima) {
			quanti++;
		}
		return quanti;
	}

	/**
	 * Il testo in un'immagine larga quanto la larghezza massima, a capo dove serve, con le righe centrate o allineate a sinistra.
	 */
	public static ImmagineDiPixel immagine(String testo, int larghezzaMassima, boolean centrato) {
		return immagine(testo, larghezzaMassima, centrato, 0);
	}

	/**
	 * Come {@link #immagine(String, int, boolean)}, ma con <code>interlinea</code> pixel in più fra una riga e l'altra, oltre ai {@link #ALTEZZA_RIGA}.
	 */
	public static ImmagineDiPixel immagine(String testo, int larghezzaMassima, boolean centrato, int interlinea) {
		List<String> righe = righe(testo, larghezzaMassima);
		int passo = ALTEZZA_RIGA + interlinea;
		int larghezza = Math.max(1, larghezzaMassima);
		int altezza = Math.max(1, (righe.size() - 1) * passo + altezzaConAccenti());
		int[] pixel = new int[larghezza * altezza];
		for (int i = 0; i < righe.size(); i++) {
			String riga = righe.get(i);
			int x = centrato ? (larghezzaMassima - larghezza(riga)) / 2 : 0;
			int y = i * passo;
			for (char c : riga.toCharArray()) {
				GlifoAColori glifo = glifo(c);
				if (glifo != null) {
					disegnaIlGlifo(pixel, larghezza, altezza, glifo, x, y);
				}
				x += larghezzaCarattere(c) + SPAZIO_FRA_CARATTERI;
			}
		}
		return new ImmagineDiPixel(larghezza, altezza, pixel);
	}

	/**
	 * Scrive una riga di testo, nell'alfabeto grande, nei pixel di un'immagine larga <code>larghezza</code> e alta <code>altezza</code>, con l'angolo in alto a
	 * sinistra in (x, y)
	 */
	public static void scrivi(int[] pixel, int larghezza, int altezza, String riga, int x, int y) {
		int colonna = x;
		for (char c : riga.toCharArray()) {
			GlifoAColori glifo = glifo(c);
			if (glifo != null) {
				disegnaIlGlifo(pixel, larghezza, altezza, glifo, colonna, y);
			}
			colonna += larghezzaCarattere(c) + SPAZIO_FRA_CARATTERI;
		}
	}

	/**
	 * Mette il glifo ingrandito dello zoom (i pixel si replicano, senza interpolazione) nell'immagine, con l'angolo in alto a sinistra in (x0, y0)
	 */
	private static void disegnaIlGlifo(int[] pixel, int larghezza, int altezza, GlifoAColori glifo, int x0, int y0) {
		int larghezzaIngrandita = ingrandito(glifo.getLarghezza());
		int altezzaIngrandita = ingrandito(glifo.getAltezza());
		for (int y = 0; y < altezzaIngrandita; y++) {
			int destinazioneY = y0 + y;
			if (destinazioneY < 0 || destinazioneY >= altezza) {
				continue;
			}
			int sorgenteY = Math.min(glifo.getAltezza() - 1, (int) (y / LivelloDiZoom.valore()));
			for (int x = 0; x < larghezzaIngrandita; x++) {
				int destinazioneX = x0 + x;
				if (destinazioneX < 0 || destinazioneX >= larghezza) {
					continue;
				}
				int sorgenteX = Math.min(glifo.getLarghezza() - 1, (int) (x / LivelloDiZoom.valore()));
				int argb = glifo.getPixel(sorgenteX, sorgenteY);
				if ((argb >>> 24) != 0) {
					pixel[destinazioneY * larghezza + destinazioneX] = argb;
				}
			}
		}
	}

	/**
	 * La stessa immagine con un alone nero sfumato attorno alle lettere (o a qualunque disegno, come i loghi), perché si leggano anche su uno sfondo chiaro o
	 * movimentato: l'immagine si allarga di <code>raggio</code> pixel per lato. L'alone è la trasparenza dell'immagine sfocata con una finestra quadrata
	 * (lato 2 × raggio + 1) e rinforzata di {@link #FORZA_ALONE} volte; i pixel entro il raggio dal bordo della zona di lavoro non si sfumano: per questo si lavora
	 * su un margine doppio, e l'alone non resta tagliato.
	 */
	public static ImmagineDiPixel conAlone(ImmagineDiPixel immagine, int raggio) {
		int margine = 2 * raggio;
		int larghezzaDiLavoro = immagine.getLarghezza() + 2 * margine;
		int altezzaDiLavoro = immagine.getAltezza() + 2 * margine;
		// L'alfa dell'immagine nella zona di lavoro
		int[] alfa = new int[larghezzaDiLavoro * altezzaDiLavoro];
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				alfa[(y + margine) * larghezzaDiLavoro + x + margine] = immagine.getArgb(x, y) >>> 24;
			}
		}
		// La somma su ogni finestra, con una tabella delle somme (immagine integrale)
		long[] somme = new long[(larghezzaDiLavoro + 1) * (altezzaDiLavoro + 1)];
		for (int y = 0; y < altezzaDiLavoro; y++) {
			long rigaFin = 0;
			for (int x = 0; x < larghezzaDiLavoro; x++) {
				rigaFin += alfa[y * larghezzaDiLavoro + x];
				somme[(y + 1) * (larghezzaDiLavoro + 1) + x + 1] = somme[y * (larghezzaDiLavoro + 1) + x + 1] + rigaFin;
			}
		}
		int lato = 2 * raggio + 1;
		float fattore = FORZA_ALONE / (lato * lato);
		int larghezza = immagine.getLarghezza() + 2 * raggio;
		int altezza = immagine.getAltezza() + 2 * raggio;
		int[] pixel = new int[larghezza * altezza];
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				// Il centro della finestra, nella zona di lavoro
				int cx = x + raggio;
				int cy = y + raggio;
				int x0 = cx - raggio;
				int y0 = cy - raggio;
				int x1 = cx + raggio + 1;
				int y1 = cy + raggio + 1;
				long somma = somme[y1 * (larghezzaDiLavoro + 1) + x1] - somme[y0 * (larghezzaDiLavoro + 1) + x1]
						- somme[y1 * (larghezzaDiLavoro + 1) + x0] + somme[y0 * (larghezzaDiLavoro + 1) + x0];
				int a = Math.min(255, Math.round(somma * fattore));
				pixel[y * larghezza + x] = a << 24;
			}
		}
		// Le lettere sopra l'alone
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				int sorgente = immagine.getArgb(x, y);
				if ((sorgente >>> 24) != 0) {
					int posto = (y + raggio) * larghezza + x + raggio;
					pixel[posto] = sopra(sorgente, pixel[posto]);
				}
			}
		}
		return new ImmagineDiPixel(larghezza, altezza, pixel);
	}

	/**
	 * La stessa immagine, in grigio scuro (la luminosità dei colori scende al {@link #LUMINOSITA_SCURO}, l'alfa resta): per quello che manca ancora (un
	 * trofeo non vinto).
	 */
	public static ImmagineDiPixel scura(ImmagineDiPixel immagine) {
		int[] pixel = new int[immagine.getLarghezza() * immagine.getAltezza()];
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				int argb = immagine.getArgb(x, y);
				int r = Math.round(((argb >> 16) & 0xFF) * LUMINOSITA_SCURO);
				int g = Math.round(((argb >> 8) & 0xFF) * LUMINOSITA_SCURO);
				int b = Math.round((argb & 0xFF) * LUMINOSITA_SCURO);
				pixel[y * immagine.getLarghezza() + x] = (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
			}
		}
		return new ImmagineDiPixel(immagine.getLarghezza(), immagine.getAltezza(), pixel);
	}

	/**
	 * Compone un pixel sopra un altro (sorgente sopra, ARGB non premoltiplicati)
	 */
	public static int sopra(int sorgente, int sotto) {
		double alfaSorgente = (sorgente >>> 24) / 255.0;
		double alfaSotto = (sotto >>> 24) / 255.0;
		double alfa = alfaSorgente + alfaSotto * (1 - alfaSorgente);
		if (alfa <= 0) {
			return 0;
		}
		int risultato = (int) Math.round(alfa * 255) << 24;
		for (int spostamento = 16; spostamento >= 0; spostamento -= 8) {
			double s = (sorgente >> spostamento) & 0xFF;
			double b = (sotto >> spostamento) & 0xFF;
			risultato |= (int) Math.round((s * alfaSorgente + b * alfaSotto * (1 - alfaSorgente)) / alfa) << spostamento;
		}
		return risultato;
	}
}
