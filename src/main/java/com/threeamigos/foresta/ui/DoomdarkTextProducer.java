package com.threeamigos.foresta.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.List;

public class DoomdarkTextProducer {
	
	private DoomdarkTextProducer() {
	}

	private static final Image buildImageSource(String resource, DoomdarkFont font, DoomdarkColorModel.Color color) {		
		int imageWidth = FontTool.getWidth(font, resource);
		int fontHeight = font.getHeight();
		int[] textData = new int[imageWidth * fontHeight]; // buffer per l'immagine temporanea
		int textDataIndex = 0;
		for (int charIndex = 0; charIndex < resource.length(); charIndex++) {
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
	private static final Image buildImageSource(String multiLineResource, DoomdarkFont font, DoomdarkColorModel.Color color, int maxWidth) {
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

	public static final Image getImage(int number, DoomdarkFont font) {
		return getImage(String.valueOf(number), font, DoomdarkColorModel.Color.LIGHT_GRAY);
	}

	public static final Image getImage(String text, DoomdarkFont font) {
		return buildImageSource(text, font, DoomdarkColorModel.Color.LIGHT_GRAY);
	}
	public static final Image getImage(int number, DoomdarkFont font, DoomdarkColorModel.Color color) {
		return getImage(String.valueOf(number), font, color);
	}

	public static final Image getImage(String text, DoomdarkFont font, DoomdarkColorModel.Color color) {
		return buildImageSource(text, font, color);
	}

	public static final Image getImage(int number, DoomdarkFont font, int maxWidth) {
		return getImage(String.valueOf(number), font, DoomdarkColorModel.Color.LIGHT_GRAY, maxWidth);
	}

	public static final Image getImage(String text, DoomdarkFont font, int maxWidth) {
		return buildImageSource(text, font, DoomdarkColorModel.Color.LIGHT_GRAY, maxWidth);
	}
	public static final Image getImage(int number, DoomdarkFont font, DoomdarkColorModel.Color color, int maxWidth) {
		return getImage(String.valueOf(number), font, color, maxWidth);
	}

	public static final Image getImage(String text, DoomdarkFont font, DoomdarkColorModel.Color color, int maxWidth) {
		return buildImageSource(text, font, color, maxWidth);
	}
}
