package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.ui.DoomdarkFont.UnsupportedCharacterException;

import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.List;

public class DoomdarkTextRectangle {

	// I testi contenuti
	private final String[] strings;
	// buffer per l'immagine temporanea che tiene il testo prima del rendering
	private final int[] textData;

	private final int width;
	private final int height;
	private final int fontHeight = DoomdarkFontSmall.getInstance().getHeight();
	private final int charPadding = DoomdarkFontSmall.getInstance().getPadding();
	private final DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();

	public DoomdarkTextRectangle(int width, int height) {
		this.width = width;
		this.height = height;
		int maxStrings = height / fontHeight;
		strings = new String[maxStrings];
		textData = new int[width * height];
	}

	// muove verso l'alto il buffer temporaneo
	private void scrollUp() {
		System.arraycopy(textData, (fontHeight + 1) * width, textData, 0, (height - fontHeight - 1) * width);
		for (int i = (height - fontHeight) * width; i < textData.length; i++) {
			textData[i] = 0x00;
		}
		for (int i = 0; i < strings.length - 1; i++) {
			strings[i] = strings[i + 1];
		}
	}

	// renderizza una stringa
	private void render(String resource) {
		int textDataIndex = width * (height - fontHeight) + charPadding; // punta alla prima cella in alto a sx dell'ultima riga di testo
		int rowDataIndex = 1; // scorre lungo la riga per scoprire quando siamo usciti
		for (int charIndex = 0; charIndex < resource.length(); charIndex++) {
			byte[] charData;
			try {
				charData = fontSmall.getGlyphData(resource.charAt(charIndex));
			} catch (UnsupportedCharacterException e) {
				Logger.log(e);
				continue;
			}
			int charWidth = charData[0];
			for (int bits = 0; bits < charWidth; bits++) {
				// Questo controllo lo metto all'inizio così se la larghezza del canvas è zero non dà errore e torna
				if (rowDataIndex >= width - charPadding) {
					return;
				}
				for (int row = 0; row < fontHeight; row++) {
					textData[textDataIndex + (width * row)] = (charData[row + 1] >> (7 - bits)) & 1;
				}
				textDataIndex++;
				rowDataIndex++;
			}
			textDataIndex++;
			rowDataIndex++;
		}
	}

	private void drawString(String s) {
		scrollUp();
		strings[strings.length - 1] = s;
		render(s);
	}

	public final void addString(String s) {
		// Gestisce i \n letterali come vere newline
		String[] lines = s.split("\\\\n");
		for (String line : lines) {
			List<String> substrings = FontTool.split(fontSmall, line, width);
			for (String substring : substrings) {
				drawString(substring);
			}
		}
	}

	public final void clear() {
        Arrays.fill(strings, "");
		int l = textData.length;
		for (int i = 0; i < l; i++) {
			textData[i] = 0;
		}
	}

	public MemoryImageSource getImageSource() {
		return new MemoryImageSource(width, height, DoomdarkColorModel.getColorModel(DoomdarkColorModel.Color.WHITE), textData, 0, width);
	}
}
