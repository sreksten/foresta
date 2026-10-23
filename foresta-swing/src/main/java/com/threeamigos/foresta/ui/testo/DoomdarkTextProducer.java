package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trasforma una stringa in un'immagine scritta con un font bitmap e un colore, anche su più righe entro una larghezza massima.
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

	private static BufferedImage buildImageSource(String resource, DoomdarkFont font, DoomdarkColorModel.Color color) {
		// Un testo vuoto darebbe un'immagine larga zero, che BufferedImage non crea: si produce comunque un pixel
		int imageWidth = Math.max(1, FontTool.getWidth(font, resource));
		int fontHeight = font.getHeight();
		int[] textData = new int[imageWidth * fontHeight]; // buffer per l'immagine temporanea
		int textDataIndex = 0;
		for (int charIndex = 0; charIndex < resource.length(); charIndex++) {
			if (!font.supportaIlCarattere(resource.charAt(charIndex))) {
				segnalaCarattereMancante(resource.charAt(charIndex), resource);
				continue; // come FontTool.getWidth, che non gli lascia posto
			}
			byte[] charData = font.getGlyphData(resource.charAt(charIndex));
			int charWidth = charData[0]; // larghezza del carattere
			int dataWidth = font.getDataWidthInBytes();
			for (int bits = 0; bits < charWidth; bits++) {
				for (int row = 0; row < fontHeight; row++) {
					textData[textDataIndex + imageWidth * row] = (charData[row * dataWidth + 1 + (bits >> 3)] >> (7 - (bits & 0b111))) & 1;
				}
				textDataIndex++;
			}
			textDataIndex++;
		}
		return creaImmagine(imageWidth, fontHeight, color, textData);
	}

	/**
	 * Dai bit dei caratteri (0 sfondo, 1 primo piano) a un'immagine nel formato dello schermo, non a un'immagine
	 * del Toolkit: quest'ultima la grafica non sa tenerla in memoria video, e ogni disegno la riconverte.
	 */
	private static BufferedImage creaImmagine(int larghezza, int altezza, DoomdarkColorModel.Color color, int[] bit) {
		IndexColorModel modello = DoomdarkColorModel.getColorModel(color);
		int[] argb = new int[]{modello.getRGB(0), modello.getRGB(1)};
		int[] pixel = new int[larghezza * altezza];
		for (int i = 0; i < pixel.length && i < bit.length; i++) {
			pixel[i] = argb[bit[i]];
		}
		BufferedImage immagine = BufferedImageBuilder.creaCompatibile(larghezza, altezza, Transparency.TRANSLUCENT);
		immagine.setRGB(0, 0, larghezza, altezza, pixel, 0, larghezza);
		return immagine;
	}

	// Rendering del testo
	private static BufferedImage buildImageSource(String multiLineResource, DoomdarkFont font, DoomdarkColorModel.Color color, int maxWidth) {
		List<String> strings = FontTool.split(font, multiLineResource, maxWidth);
		int imageWidth = 0;
		for (String string : strings) {
			int stringWidth = FontTool.getWidth(font, string);
			if (stringWidth > imageWidth) {
				imageWidth = stringWidth;
			}
		}
		imageWidth = Math.max(1, imageWidth);
		int fontHeight = font.getHeight();
		int[] textData = new int[imageWidth * fontHeight * strings.size()]; // buffer per l'immagine temporanea
		for (int currentStringIndex = 0; currentStringIndex < strings.size(); currentStringIndex++) {
			int textDataIndex = currentStringIndex * imageWidth * fontHeight;
			String resource = strings.get(currentStringIndex);
			for (int charIndex = 0; charIndex < resource.length(); charIndex++) {
				if (!font.supportaIlCarattere(resource.charAt(charIndex))) {
					segnalaCarattereMancante(resource.charAt(charIndex), resource);
					continue; // come FontTool.getWidth, che non gli lascia posto
				}
				byte[] charData = font.getGlyphData(resource.charAt(charIndex));
				int charWidth = charData[0]; // larghezza del carattere
				int dataWidth = font.getDataWidthInBytes();
				for (int bits = 0; bits < charWidth; bits++) {
					for (int row = 0; row < fontHeight; row++) {
						textData[textDataIndex + imageWidth * row] = (charData[row * dataWidth + 1 + (bits >> 3)] >> (7 - (bits & 0b111))) & 1;
					}
					textDataIndex++;
				}
				textDataIndex++;
			}
		}
		return creaImmagine(imageWidth, fontHeight * strings.size(), color, textData);
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
