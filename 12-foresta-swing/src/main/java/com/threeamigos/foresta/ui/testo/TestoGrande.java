package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.ImmaginiAwt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Il testo nell'alfabeto grande, quello della storia: i glifi sono quelli dell'atlante (vedi ImageCache.glifoGrande),
 * che ha le maiuscole, anche accentate, le cifre e la punteggiatura; le minuscole si scrivono con le maiuscole, e i
 * caratteri che l'atlante non ha restano uno spazietto.
 */
public final class TestoGrande {

	/**
	 * Lo spazio fra un carattere e l'altro.
	 */
	static final int SPAZIO_FRA_CARATTERI = LayoutDelTestoGrande.SPAZIO_FRA_CARATTERI;
	/**
	 * Da una riga all'altra.
	 */
	public static final int ALTEZZA_RIGA = LayoutDelTestoGrande.ALTEZZA_RIGA;
	/**
	 * L'altezza delle lettere.
	 */
	public static final int ALTEZZA_GLIFO = LayoutDelTestoGrande.ALTEZZA_GLIFO;

	private TestoGrande() {
	}

	/**
	 * Il glifo del carattere, o null se l'alfabeto non ce l'ha. L'atlante ha le maiuscole, accentate comprese: le
	 * minuscole si scrivono con quelle. È alto quanto un riquadro dell'atlante: {@link #ALTEZZA_GLIFO} sono le lettere, il
	 * resto è lo spazio per accenti e cediglie.
	 */
	static BufferedImage glifo(char c) {
		return ImageCache.glifoGrande(Character.toUpperCase(c));
	}

	/**
	 * L'altezza dell'immagine di una riga: quella di un riquadro dell'atlante, accenti e cediglie compresi.
	 */
	public static int altezzaConAccenti() {
		return LayoutDelTestoGrande.altezzaConAccenti();
	}

	static int larghezzaCarattere(char c) {
		return LayoutDelTestoGrande.larghezzaCarattere(c);
	}

	/**
	 * Quanto è largo il testo, su una riga.
	 */
	public static int larghezza(String testo) {
		return LayoutDelTestoGrande.larghezza(testo);
	}

	/**
	 * Le righe del testo, andando a capo fra una parola e l'altra per non superare la larghezza massima.
	 */
	public static List<String> righe(String testo, int larghezzaMassima) {
		return LayoutDelTestoGrande.righe(testo, larghezzaMassima);
	}

	/**
	 * Disegna una riga a partire da x, y.
	 */
	public static void disegnaRiga(Graphics2D graphics, String riga, int x, int y) {
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
	public static BufferedImage immagine(String testo, int larghezzaMassima, boolean centrato) {
		return ImmaginiAwt.buffered(LayoutDelTestoGrande.immagine(testo, larghezzaMassima, centrato));
	}

	/**
	 * Come {@link #immagine(String, int, boolean)}, ma con {@code interlinea} pixel in più fra una riga e l'altra, oltre ai
	 * {@link #ALTEZZA_RIGA}.
	 */
	public static BufferedImage immagine(String testo, int larghezzaMassima, boolean centrato, int interlinea) {
		return ImmaginiAwt.buffered(LayoutDelTestoGrande.immagine(testo, larghezzaMassima, centrato, interlinea));
	}

	/**
	 * La stessa immagine con un alone nero sfumato attorno alle lettere (o a qualunque disegno, come i loghi), perché
	 * si leggano anche su uno sfondo chiaro o movimentato: l'immagine si allarga di {@code raggio} pixel per lato.
	 */
	public static BufferedImage conAlone(BufferedImage immagine, int raggio) {
		return ImmaginiAwt.buffered(LayoutDelTestoGrande.conAlone(ImmaginiAwt.pixel(immagine), raggio));
	}

	/**
	 * La stessa immagine, in grigio scuro: per quello che manca ancora (un trofeo non vinto).
	 */
	public static BufferedImage scura(BufferedImage immagine) {
		return ImmaginiAwt.buffered(LayoutDelTestoGrande.scura(ImmaginiAwt.pixel(immagine)));
	}
}
