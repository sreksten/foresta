package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.ui.DoomdarkFont.UnsupportedCharacterException;

import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rettangolo di testo scorrevole. Le righe già spezzate alla larghezza del rettangolo
 * vengono conservate in uno storico, e il raster viene ridisegnato a partire da quello:
 * è ciò che permette di tornare indietro con la rotella invece di limitarsi a far
 * scorrere via il testo vecchio. Lo storico condivide il limite con quanto ricordato
 * da {@link com.threeamigos.foresta.motore.Notizie} (vedi {@link Costanti#MASSIMO_MESSAGGI_RICORDATI}),
 * così che dopo un ricaricamento il pannello possa essere ripopolato per intero.
 */
public class DoomdarkTextRectangle2x {

	// Pixel aggiuntivi fra la base di una riga e l'inizio della successiva
	private static final int INTERLINEA = 4;

	// Le righe già spezzate alla larghezza del rettangolo, dalla più vecchia alla più recente
	private final List<String> righe = new ArrayList<>();
	// buffer per l'immagine temporanea che tiene il testo prima del rendering
	private final int[] textData;

	private final int width;
	private final int height;
	private final int fontHeight = DoomdarkFontMedium.getInstance().getHeight();
	private final int charPadding = DoomdarkFontMedium.getInstance().getPadding();
	private final DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
	// Distanza fra le basi di due righe consecutive
	private final int passoRiga = fontHeight + INTERLINEA;
	private final int righeVisibili;

	// Quante righe si è tornati indietro rispetto alla più recente
	private int offsetRighe;
	// Il raster va ricostruito solo quando il testo o lo scorrimento sono cambiati
	private boolean daRidisegnare = true;

	public DoomdarkTextRectangle2x(int width, int height) {
		this.width = width;
		this.height = height;
		// La riga più in basso poggia a height - fontHeight, le altre a salire di passoRiga
		righeVisibili = height < fontHeight ? 0 : (height - fontHeight) / passoRiga + 1;
		textData = new int[width * height];
		imbottisci();
	}

	/**
	 * Mette in testa allo storico un cuscino di righe vuote. Serve allo scorrimento: la
	 * parte alta del rettangolo è coperta da una sfumatura che spegne il testo, e senza
	 * il cuscino le righe più vecchie resterebbero inchiodate lassù, illeggibili. Con
	 * mezzo rettangolo di righe vuote sopra di loro, arrivano invece a metà altezza,
	 * dove la sfumatura non le tocca più.
	 * <p>
	 * Le righe vuote fanno parte dello storico come le altre, quindi sono le prime a
	 * essere dimenticate quando il testo cresce: sono un cuscino, non un ingombro fisso.
	 */
	private void imbottisci() {
		for (int i = 0; i < righeVisibili / 2; i++) {
			righe.add("");
		}
	}

	// renderizza una stringa alla quota indicata
	private void render(String resource, int quota) {
		int textDataIndex = width * quota + charPadding; // punta alla prima cella in alto a sx della riga di testo
		int rowDataIndex = 1; // scorre lungo la riga per scoprire quando siamo usciti
		for (int charIndex = 0; charIndex < resource.length(); charIndex++) {
			byte[] charData;
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
					textData[textDataIndex + width * row] = (charData[row * dataWidth + 1 + (bits >> 3)] >> (7 - (bits & 0b111))) & 1;
				}
				textDataIndex++;
				rowDataIndex++;
			}
			textDataIndex++;
			rowDataIndex++;
		}
	}

	/**
	 * Ridisegna il raster con la finestra di righe che lo scorrimento corrente inquadra:
	 * la più recente in basso, le precedenti a salire.
	 */
	private void ridisegna() {
		Arrays.fill(textData, 0);
		// Indice della riga che va in fondo al rettangolo
		int indiceUltimaRiga = righe.size() - 1 - offsetRighe;
		for (int i = 0; i < righeVisibili; i++) {
			int indiceRiga = indiceUltimaRiga - i;
			if (indiceRiga < 0) {
				break;
			}
			render(righe.get(indiceRiga), height - fontHeight - i * passoRiga);
		}
		daRidisegnare = false;
	}

	public final synchronized void addString(String s) {
		// Gestisce i \n letterali come vere newline
		String[] lines = s.split("\\\\n");
		for (String line : lines) {
			righe.addAll(FontTool.split(fontMedium, line, width));
		}
		while (righe.size() > Costanti.MASSIMO_MESSAGGI_RICORDATI) {
			righe.remove(0);
		}
		// Un messaggio nuovo riporta in fondo: nel mezzo di una partita non deve poter
		// passare inosservato perché si stava rileggendo il testo vecchio
		offsetRighe = 0;
		daRidisegnare = true;
	}

	public final synchronized void clear() {
		righe.clear();
		imbottisci();
		offsetRighe = 0;
		daRidisegnare = true;
	}

	/**
	 * Sposta indietro (o avanti) la finestra di righe inquadrata, senza uscire dallo
	 * storico disponibile.
	 *
	 * @param righeIndietro positivo per tornare al testo più vecchio
	 */
	public final synchronized void scorri(int righeIndietro) {
		int nuovoOffset = offsetRighe + righeIndietro;
		int offsetMassimo = Math.max(0, righe.size() - righeVisibili);
		nuovoOffset = Math.max(0, Math.min(nuovoOffset, offsetMassimo));
		if (nuovoOffset != offsetRighe) {
			offsetRighe = nuovoOffset;
			daRidisegnare = true;
		}
	}

	public synchronized MemoryImageSource getImageSource() {
		if (daRidisegnare) {
			ridisegna();
		}
		return new MemoryImageSource(width, height, DoomdarkColorModel.getColorModel(DoomdarkColorModel.Color.WHITE), textData, 0, width);
	}
}
