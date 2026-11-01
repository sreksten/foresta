package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;

import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trasforma una stringa in un'immagine scritta con un font Doomdark e un colore, anche su più righe entro una
 * larghezza massima, prendendo i glifi dall'immagine del font ({@link DoomdarkFont}). Ogni pixel del glifo ha
 * un livello da 0 a 255 che è la trasparenza del colore del testo: 0 trasparente, 255 il colore pieno, in mezzo il colore
 * sfuma sullo sfondo (il colore resta lo stesso, cambia l'alfa).
 */
public class DoomdarkTextProducer {

	// I caratteri mancanti già segnalati: un testo si ridisegna spesso, l'errore basta una volta per carattere
	private static final Set<Character> CARATTERI_SEGNALATI = ConcurrentHashMap.newKeySet();

	private DoomdarkTextProducer() {
	}

	private static void segnalaCarattereMancante(char c, String testo) {
		if (CARATTERI_SEGNALATI.add(c)) {
			BusEventi.pubblica(new InternoErrore("Carattere non supportato dal font: '" + c + "' [" + (int) c + "] nel testo \"" + testo + "\""));
		}
	}

	/**
	 * Scrive una riga di testo nel buffer dei livelli, a partire dalla riga indicata
	 */
	private static void scrivi(String testo, DoomdarkFont font, int[] valori, int larghezzaImmagine, int primaRiga) {
		int altezza = font.getHeight();
		int colonna = 0;
		for (int i = 0; i < testo.length(); i++) {
			char carattere = testo.charAt(i);
			if (!font.supportaIlCarattere(carattere)) {
				segnalaCarattereMancante(carattere, testo);
				continue; // come FontTool.getWidth, che non gli lascia posto
			}
			int larghezzaDelGlifo = font.getGlyphWidth(carattere);
			for (int x = 0; x < larghezzaDelGlifo; x++) {
				for (int riga = 0; riga < altezza; riga++) {
					valori[(primaRiga + riga) * larghezzaImmagine + colonna] = font.getLivello(carattere, x, riga);
				}
				colonna++;
			}
			colonna += font.getSpacing();
		}
	}

	private static BufferedImage buildImageSource(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore) {
		// Un testo vuoto darebbe un'immagine larga zero, che BufferedImage non crea: si produce comunque un pixel
		int larghezza = Math.max(1, FontTool.getWidth(font, testo));
		int[] valori = new int[larghezza * font.getHeight()];
		scrivi(testo, font, valori, larghezza, 0);
		return creaImmagine(larghezza, font.getHeight(), colore, valori);
	}

	private static BufferedImage buildImageSource(String testoSuPiuRighe, DoomdarkFont font, DoomdarkColorModel.Color colore, int larghezzaMassima) {
		List<String> righe = FontTool.split(font, testoSuPiuRighe, larghezzaMassima);
		int larghezza = 0;
		for (String riga : righe) {
			larghezza = Math.max(larghezza, FontTool.getWidth(font, riga));
		}
		larghezza = Math.max(1, larghezza);
		int altezza = font.getHeight();
		int[] valori = new int[larghezza * altezza * righe.size()];
		for (int r = 0; r < righe.size(); r++) {
			scrivi(righe.get(r), font, valori, larghezza, r * altezza);
		}
		return creaImmagine(larghezza, altezza * righe.size(), colore, valori);
	}

	/**
	 * Dai livelli dei glifi a un'immagine nel formato dello schermo, non a un'immagine del Toolkit: quest'ultima la
	 * grafica non sa tenerla in memoria video, e ogni disegno la riconverte.
	 */
	private static BufferedImage creaImmagine(int larghezza, int altezza, DoomdarkColorModel.Color colore, int[] valori) {
		IndexColorModel modello = DoomdarkColorModel.getColorModel(colore);
		int primoPiano = modello.getRGB(1);
		int[] pixel = new int[larghezza * altezza];
		for (int i = 0; i < pixel.length && i < valori.length; i++) {
			pixel[i] = pixel(primoPiano, valori[i]);
		}
		BufferedImage immagine = BufferedImageBuilder.creaCompatibile(larghezza, altezza, Transparency.TRANSLUCENT);
		immagine.setRGB(0, 0, larghezza, altezza, pixel, 0, larghezza);
		return immagine;
	}

	/**
	 * Il pixel del testo (ARGB, non premoltiplicato) di un pixel di glifo con quel livello: il colore del testo, con la
	 * sua alfa moltiplicata per il livello diviso 255. A livello 0 è del tutto trasparente, a 255 è il colore pieno, in
	 * mezzo il colore resta lo stesso e sfuma sullo sfondo quando si disegna.
	 */
	static int pixel(int primoPiano, int livello) {
		if (livello <= 0) {
			return 0;
		}
		if (livello >= 255) {
			return primoPiano;
		}
		int alfa = (primoPiano >>> 24) * livello / 255;
		return (alfa << 24) | (primoPiano & 0xFFFFFF);
	}

	public static Image getImage(int number, DoomdarkFont font) {
		return getImage(String.valueOf(number), font, DoomdarkColorModel.Color.LIGHT_GRAY);
	}

	public static Image getImage(String text, DoomdarkFont font) {
		return buildImageSource(text, font, DoomdarkColorModel.Color.LIGHT_GRAY);
	}

	public static Image getImage(int number, DoomdarkFont font, DoomdarkColorModel.Color color) {
		return getImage(String.valueOf(number), font, color);
	}

	public static Image getImage(String text, DoomdarkFont font, DoomdarkColorModel.Color color) {
		return buildImageSource(text, font, color);
	}

	public static Image getImage(int number, DoomdarkFont font, int maxWidth) {
		return getImage(String.valueOf(number), font, DoomdarkColorModel.Color.LIGHT_GRAY, maxWidth);
	}

	public static Image getImage(String text, DoomdarkFont font, int maxWidth) {
		return buildImageSource(text, font, DoomdarkColorModel.Color.LIGHT_GRAY, maxWidth);
	}

	public static Image getImage(int number, DoomdarkFont font, DoomdarkColorModel.Color color, int maxWidth) {
		return getImage(String.valueOf(number), font, color, maxWidth);
	}

	public static Image getImage(String text, DoomdarkFont font, DoomdarkColorModel.Color color, int maxWidth) {
		return buildImageSource(text, font, color, maxWidth);
	}
}
