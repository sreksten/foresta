package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.modellodati.NotizieMD;
import com.threeamigos.foresta.strumenti.logger.Logger;

import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Un rettangolo con i glifi del font a immagine ({@link DoomdarkFont}):
 * rettangolo di testo scorrevole in cui le righe già spezzate alla larghezza del rettangolo vengono conservate in uno
 * storico, e il raster viene ridisegnato a partire da quello: è ciò che permette di tornare indietro con la rotella
 * invece di limitarsi a far scorrere via il testo vecchio. Lo storico condivide il limite con quanto ricordato da
 * {@code motore.Notizie} (vedi {@link NotizieMD#MASSIMO_MESSAGGI_RICORDATI}), così che dopo un ricaricamento il pannello
 * possa essere ripopolato per intero.
 * <p>
 * Una riga di testo è alta quanto l'immagine del font, con le righe in più per accenti e cediglie, e fra una riga e
 * l'altra c'è un pixel solo, perché le righe in più le separano già. Il raster è bianco, con l'alfa del livello del
 * glifo: i bordi sfumano sullo sfondo.
 */
public class DoomdarkTextRectangle2x {

	// Pixel aggiuntivi fra il fondo di una riga e l'inizio della successiva
	private static final int INTERLINEA = 1;
	private static final int BIANCO = 0xFFFFFFFF;
	private static final int TRASPARENTE = 0x00000000;

	// Le righe già spezzate alla larghezza del rettangolo, dalla più vecchia alla più recente
	private final List<String> righe = new ArrayList<>();
	// Il raster prima della consegna, in ARGB
	private final int[] textData;

	private final int width;
	private final int height;
	private final DoomdarkFont font = DoomdarkFont.medio();
	private final int fontHeight = font.getHeight();
	private final int charPadding = font.getPadding();
	// Distanza fra le cime di due righe consecutive
	private final int passoRiga = fontHeight + INTERLINEA;
	private final int righeVisibili;

	// Quante righe si è tornati indietro rispetto alla più recente
	private int offsetRighe;
	// Il raster va ricostruito solo quando il testo o lo scorrimento sono cambiati
	private boolean daRidisegnare = true;
	// Cresce a ogni cambiamento: chi conserva un'immagine del testo sa quando rifarla
	private int versione;

	public DoomdarkTextRectangle2x(int width, int height) {
		this.width = width;
		this.height = height;
		// La riga più in basso sta a height - fontHeight, le altre a salire di passoRiga
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

	// scrive una stringa alla quota indicata
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
				for (int riga = 0; riga < fontHeight; riga++) {
					textData[(quota + riga) * width + colonna] = DoomdarkTextProducer.pixel(BIANCO, font.getLivello(carattere, x, riga));
				}
				colonna++;
			}
			colonna += font.getSpacing();
		}
	}

	/**
	 * Ridisegna il raster con la finestra di righe che lo scorrimento corrente inquadra:
	 * la più recente in basso, le precedenti a salire.
	 */
	private void ridisegna() {
		Arrays.fill(textData, TRASPARENTE);
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

	/**
	 * Aggiunge una riga in fondo, spezzandola se è più larga del rettangolo, e la ricorda nello storico.
	 */
	public final synchronized void addString(String s) {
		// Gestisce i \n letterali come vere newline
		String[] lines = s.split("\\\\n");
		for (String line : lines) {
			righe.addAll(FontTool.split(font, line, width));
		}
		while (righe.size() > NotizieMD.MASSIMO_MESSAGGI_RICORDATI) {
			righe.remove(0);
		}
		// Un messaggio nuovo riporta in fondo: nel mezzo di una partita non deve poter
		// passare inosservato perché si stava rileggendo il testo vecchio
		offsetRighe = 0;
		segnaCambiato();
	}

	/**
	 * Svuota il rettangolo e lo storico.
	 */
	public final synchronized void clear() {
		righe.clear();
		imbottisci();
		offsetRighe = 0;
		segnaCambiato();
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
			segnaCambiato();
		}
	}

	private void segnaCambiato() {
		daRidisegnare = true;
		versione++;
	}

	/**
	 * Se si può ancora scorrere all'indietro verso i messaggi più vecchi (freccia su).
	 */
	public synchronized boolean isScorribileSu() {
		return offsetRighe < Math.max(0, righe.size() - righeVisibili);
	}

	/**
	 * Se si è già scorsi all'indietro e si può tornare verso i messaggi più recenti (freccia giù).
	 */
	public synchronized boolean isScorribileGiu() {
		return offsetRighe > 0;
	}

	/**
	 * Un numero che cambia ogni volta che il contenuto visibile cambia (testo aggiunto,
	 * svuotato o fatto scorrere): finché resta uguale, getImageSource() darebbe lo
	 * stesso raster.
	 */
	public synchronized int getVersione() {
		return versione;
	}

	/**
	 * Quante righe di testo stanno nel rettangolo
	 */
	public int getRigheVisibili() {
		return righeVisibili;
	}

	public synchronized MemoryImageSource getImageSource() {
		if (daRidisegnare) {
			ridisegna();
		}
		return new MemoryImageSource(width, height, ColorModel.getRGBdefault(), textData, 0, width);
	}
}
