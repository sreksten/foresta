package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.strumenti.logger.Logger;

import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;

/**
 * Il raster di un rettangolo di testo scorrevole in font Doomdark: lo storico delle righe e lo scorrimento stanno in {@link StoricoDelTesto}
 * (che non usa il toolkit, e usa anche libGDX), qui c'è solo il disegno dei glifi dell'immagine del font in un raster ARGB, ricostruito quando
 * cambia la versione dello storico. Il raster è bianco, con l'alfa del livello del glifo: i bordi sfumano sullo sfondo.
 */
public class DoomdarkTextRectangle2x {

	private static final int BIANCO = 0xFFFFFFFF;
	private static final int TRASPARENTE = 0x00000000;

	private final StoricoDelTesto storico;
	// Il raster prima della consegna, in ARGB
	private final int[] textData;
	private final int width;
	private final int height;
	private final DoomdarkFont font;
	private final int fontHeight;
	private final int charPadding;
	// La versione dello storico con cui si è costruito il raster
	private int versioneDisegnata = -1;

	public DoomdarkTextRectangle2x(StoricoDelTesto storico) {
		this.storico = storico;
		width = storico.getLarghezza();
		height = storico.getAltezza();
		font = storico.getFont();
		fontHeight = font.getHeight();
		charPadding = font.getPadding();
		textData = new int[width * height];
	}

	public DoomdarkTextRectangle2x(int width, int height) {
		this(new StoricoDelTesto(width, height));
	}

	// scrive una stringa alla quota indicata: le righe del glifo che cadono fuori dal rettangolo non si scrivono
	private void render(String resource, int quota) {
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
				for (int riga = Math.max(0, -quota); riga < fontHeight && quota + riga < height; riga++) {
					textData[(quota + riga) * width + colonna] = DoomdarkTextProducer.pixel(BIANCO, font.getLivello(carattere, x, riga));
				}
				colonna++;
			}
			colonna += font.getSpacing();
		}
	}

	private void ridisegna() {
		Arrays.fill(textData, TRASPARENTE);
		storico.perOgniRigaVisibile(this::render);
	}

	public StoricoDelTesto getStorico() {
		return storico;
	}

	public void addString(String s) {
		storico.addString(s);
	}

	public void clear() {
		storico.clear();
	}

	public int getScorrimentoMassimo() {
		return storico.getScorrimentoMassimo();
	}

	public void impostaOffset(int pixel) {
		storico.impostaOffset(pixel);
	}

	public void scorri(int righeIndietro) {
		storico.scorri(righeIndietro);
	}

	public boolean isScorribileSu() {
		return storico.isScorribileSu();
	}

	public boolean isScorribileGiu() {
		return storico.isScorribileGiu();
	}

	/**
	 * Un numero che cambia ogni volta che il contenuto visibile cambia (testo aggiunto, svuotato o fatto scorrere)
	 */
	public int getVersione() {
		return storico.getVersione();
	}

	public int getRigheVisibili() {
		return storico.getRigheVisibili();
	}

	public synchronized MemoryImageSource getImageSource() {
		int versione = storico.getVersione();
		if (versione != versioneDisegnata) {
			ridisegna();
			versioneDisegnata = versione;
		}
		return new MemoryImageSource(width, height, ColorModel.getRGBdefault(), textData, 0, width);
	}
}
