package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;

import java.awt.*;
import java.awt.image.MemoryImageSource;
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

	private static Image buildImageSource(String resource, DoomdarkFont font, DoomdarkColorModel.Color color) {
		int imageWidth = FontTool.getWidth(font, resource);
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
		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(imageWidth, fontHeight, 
						DoomdarkColorModel.getColorModel(color), textData, 0, imageWidth));
	}

	// Rendering del testo
	private static Image buildImageSource(String multiLineResource, DoomdarkFont font, DoomdarkColorModel.Color color, int maxWidth) {
		List<String> strings = FontTool.split(font, multiLineResource, maxWidth);
		int imageWidth = 0;
		for (String string : strings) {
			int stringWidth = FontTool.getWidth(font, string);
			if (stringWidth > imageWidth) {
				imageWidth = stringWidth;
			}
		}
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
		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(imageWidth, fontHeight * strings.size(), 
						DoomdarkColorModel.getColorModel(color), textData, 0, imageWidth));
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
