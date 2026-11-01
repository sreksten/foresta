package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.strumenti.logger.Logger;

import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.List;

/**
 * Un rettangolo di testo con i glifi del font a immagine ({@link DoomdarkFont}): un
 * rettangolo di testo che scorre verso l'alto man mano che si aggiungono righe, nel font piccolo: il testo vecchio esce
 * in alto e quello nuovo entra in basso. Una riga è alta quanto l'immagine del font, con le righe in più per accenti e
 * cediglie, e fra una riga e l'altra c'è un pixel. Il raster è bianco, con l'alfa del livello del glifo (i bordi sfumano).
 */
public class DoomdarkTextRectangle {

	private static final int BIANCO = 0xFFFFFFFF;
	private static final int TRASPARENTE = 0x00000000;

	// I testi contenuti
	private final String[] strings;
	// Il raster prima della consegna, in ARGB
	private final int[] textData;

	private final int width;
	private final int height;
	private final DoomdarkFont font = DoomdarkFont.piccolo();
	private final int fontHeight = font.getHeight();
	private final int charPadding = font.getPadding();

	public DoomdarkTextRectangle(int width, int height) {
		this.width = width;
		this.height = height;
		int maxStrings = height / (fontHeight + 1);
		strings = new String[maxStrings];
		textData = new int[width * height];
	}

	// muove verso l'alto il raster
	private void scrollUp() {
		System.arraycopy(textData, (fontHeight + 1) * width, textData, 0, Math.max(0, height - fontHeight - 1) * width);
		for (int i = (height - fontHeight) * width; i < textData.length; i++) {
			textData[i] = TRASPARENTE;
		}
		for (int i = 0; i < strings.length - 1; i++) {
			strings[i] = strings[i + 1];
		}
	}

	// scrive una stringa nell'ultima riga di testo
	private void render(String resource) {
		int quota = height - fontHeight;
		int colonna = charPadding;
		for (int charIndex = 0; charIndex < resource.length(); charIndex++) {
			char carattere = resource.charAt(charIndex);
			if (!font.supportaIlCarattere(carattere)) {
				Logger.log(() -> "Carattere non supportato dal font: " + carattere + " [" + (int) carattere + "]");
				continue;
			}
			int larghezzaDelGlifo = font.getGlyphWidth(carattere);
			for (int x = 0; x < larghezzaDelGlifo; x++) {
				// Questo controllo lo metto all'inizio così se la larghezza del canvas è zero non dà errore e torna
				if (colonna >= width - charPadding) {
					return;
				}
				for (int riga = 0; riga < fontHeight; riga++) {
					textData[(quota + riga) * width + colonna] =
							DoomdarkTextProducer.pixel(BIANCO, font.getLivello(carattere, x, riga));
				}
				colonna++;
			}
			colonna += font.getSpacing();
		}
	}

	private void drawString(String s) {
		scrollUp();
		if (strings.length > 0) {
			strings[strings.length - 1] = s;
		}
		render(s);
	}

	/**
	 * Aggiunge una riga in fondo e fa scorrere il resto verso l'alto.
	 */
	public final void addString(String s) {
		// Gestisce i \n letterali come vere newline
		String[] lines = s.split("\\\\n");
		for (String line : lines) {
			List<String> substrings = FontTool.split(font, line, width);
			for (String substring : substrings) {
				drawString(substring);
			}
		}
	}

	/**
	 * Svuota il rettangolo.
	 */
	public final void clear() {
		Arrays.fill(strings, "");
		Arrays.fill(textData, TRASPARENTE);
	}

	public MemoryImageSource getImageSource() {
		return new MemoryImageSource(width, height, ColorModel.getRGBdefault(), textData, 0, width);
	}
}
