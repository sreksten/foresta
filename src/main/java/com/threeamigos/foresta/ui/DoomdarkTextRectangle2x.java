package com.threeamigos.foresta.ui;

import java.awt.image.MemoryImageSource;
import java.util.List;

import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.ui.DoomdarkFont.UnsupportedCharacterException;

public class DoomdarkTextRectangle2x {

	// I testi contenuti
	private String[] strings;
	// buffer per l'immagine temporanea che tiene il testo prima del rendering
	private int[] textdata;

	private int width;
	private int height;
	private int fontHeight = DoomdarkFontMedium.getInstance().getHeight();
	private int charPadding = DoomdarkFontMedium.getInstance().getPadding();
	private DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();

	public DoomdarkTextRectangle2x(int width, int height) {
		this.width = width;
		this.height = height;
		int maxStrings = height / fontHeight;
		strings = new String[maxStrings];
		textdata = new int[width * height];
	}

	// muove verso l'alto il buffer temporaneo
	private final void scrollUp() {
		System.arraycopy(textdata, (fontHeight + 1) * width, textdata, 0, (height - fontHeight - 1) * width);
		for (int i = (height - fontHeight) * width; i < textdata.length; i++) {
			textdata[i] = 0x00;
		}
		for (int i = 0; i < strings.length - 1; i++) {
			strings[i] = strings[i + 1];
		}
	}

	// renderizza una stringa
	private final void render(String resource) {
		int textDataIndex = width * (height - fontHeight) + charPadding; // punta alla prima cella in alto a sx dell'ultima riga di testo
		int rowDataIndex = 1; // scorre lungo la riga per scoprire quando siamo usciti
		for (int charIndex = 0; charIndex < resource.length(); charIndex++) {
			byte[] charData = null;
			try {
				charData = fontMedium.getGlyphData(resource.charAt(charIndex));
			} catch (UnsupportedCharacterException e) {
				Logger.log(e);
				continue;
			}
			int charWidth = charData[0];
			int dataWidth = DoomdarkFontMedium.getInstance().getDataWidthInBytes();
			for (int bits = 0; bits < charWidth; bits++) {
				// Questo controllo lo metto all'inizio così se la larghezza del canvas è zero non dà errore e torna
				if (rowDataIndex >= width - charPadding) {
					return;
				}
				for (int row = 0; row < fontHeight; row++) {
					textdata[textDataIndex + width * row] = (charData[row * dataWidth + 1 + (bits >> 3)] >> (7 - (bits & 0b111))) & 1;
				}
				textDataIndex++;
				rowDataIndex++;
			}
			textDataIndex++;
			rowDataIndex++;
		}
	}

	private final void drawString(String s) {
		scrollUp();
		strings[strings.length - 1] = s;
		render(s);
	}

	public final void addString(String s) {
		List<String> substrings = FontTool.split(fontMedium, s, width);
		for (String substring : substrings) {
			drawString(substring);
		}
	}

	public final void clear() {
		for (int i = 0; i < strings.length; i++) {
			strings[i] = "";
		}
		int l = textdata.length;
		for (int i = 0; i < l; i++) {
			textdata[i] = 0;
		}
	}

	public MemoryImageSource getImageSource() {
		return new MemoryImageSource(width, height, DoomdarkColorModel.getColorModel(DoomdarkColorModel.Color.WHITE), textdata, 0, width);
	}
}
