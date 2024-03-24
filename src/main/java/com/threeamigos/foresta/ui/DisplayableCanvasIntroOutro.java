package com.threeamigos.foresta.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.ClassiPersonaggio;
import com.threeamigos.foresta.tools.GestorePunteggi;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import com.threeamigos.foresta.tools.InterfacciaGestorePunteggi;
import com.threeamigos.foresta.tools.InterfacciaGestoreSalvataggi;
import com.threeamigos.foresta.tools.Misc;

public class DisplayableCanvasIntroOutro {

	private static final int CHAR_SPACING = 1;

	private int width;
	private int height;
	private int sequenza;
	private int xOffset;
	private int yOffset;
	private String messaggio;

	DisplayableCanvasIntroOutro(int width, int height) {
		this.width = width;
		this.height = height;

		if (width > 320) /* Mappa + spazio + locazione (bassa risoluzione Amiga :) */
			xOffset = (width - 320) >> 1;
		else
			xOffset = 0;
		if (height > 256)
			yOffset = (height - 256) >> 1;
		else
			yOffset = 0;
	}
	
	void resettaSequenza() {
		sequenza = 0;
	}
	
	void resettaSequenza(int sequenza) {
		this.sequenza = sequenza;
	}
	
	void incrementaSequenza(int lunghezzaMassima) {
		sequenza++;
		if (sequenza >= lunghezzaMassima) {
			sequenza = 0;
		}
	}
	
	void impostaMessaggio(String messaggio) {
		this.messaggio = messaggio;
	}

	void scrivi(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		if (messaggio != null && !messaggio.equals(""))
			drawStringCenteredWithAutoWrap(graphics, messaggio.toLowerCase(), 20);
	}

	void statistiche(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		int locXOffset = xOffset;
		int locYOffset = yOffset + 20;
		DoomdarkColorModel.Color color = DoomdarkColorModel.Color.MEDIUM_GRAY;
		Image doomdark = null;
		int giorni = LineaTemporale.getGiorno();
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		doomdark = DoomdarkTextProducer.getImage("Avversari uccisi in " + (giorni > 1 ? (Misc.getCardinaleM(giorni) + " giorni:") : "un giorno:"), fontMedium);
		graphics.drawImage(doomdark, locXOffset + 9, locYOffset, null);
		locYOffset += fontMedium.getHeight();
		for (ClassiPersonaggio classePersonaggio : ClassiPersonaggio.values()) {
			int m = Statistiche.getMostriUccisi(classePersonaggio);
			if (m > 0) {
				color = (color == DoomdarkColorModel.Color.MEDIUM_GRAY ? DoomdarkColorModel.Color.LIGHT_GRAY : DoomdarkColorModel.Color.MEDIUM_GRAY); 
				doomdark = DoomdarkTextProducer.getImage(m + " " + (m == 1 ? classePersonaggio.getIstanza().getNomeSingolare() : classePersonaggio.getIstanza().getNomePlurale()), fontMedium, color);
				graphics.drawImage(doomdark, locXOffset + 9, locYOffset, null);
				locYOffset += fontMedium.getHeight();
			}
		}
	}

	void intro(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		Logger.log("INTRO: Sequenza = " + sequenza);
		if (sequenza == 0) {
			BufferedImage d;
			d = ImageCache.logo3AM;
			graphics.drawImage(d, (width - d.getWidth()) >> 1, yOffset + 20, null);
			d = ImageCache.logoForesta;
			graphics.drawImage(d, (width - d.getWidth()) >> 1, yOffset + 80, null);
		} else if (sequenza == 1) {
			hiscore(graphics);
		} else {
			messaggio = Misc.STORIA[sequenza - 2];
			scrivi(graphics);
		}
	}
	
	void selezioneNuovoGiocoOCarica(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		drawStringCenteredWithAutoWrap(graphics, "1 - nuovo gioco", (height >> 1) - 50);
		drawStringCenteredWithAutoWrap(graphics, "2 - carica partita precedente", (height >> 1) + 50);
	}

	void selezioneSlotDaCaricare(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		drawStringCenteredWithAutoWrap(graphics, "seleziona lo slot da caricare", 50);
		for (InterfacciaGestoreSalvataggi.InterfacciaTestataSalvataggio testata : GestoreSalvataggi.getSalvataggiDisponibili()) {
			int id = Integer.parseInt(testata.getId());
			int coordinataY = getCoordinataY(id);
			String descrizione = testata.getDescrizione();
			StringTokenizer st = new StringTokenizer(descrizione, "|");
			String elencoClassiPersonaggio = st.nextToken();
			disegnaPersonaggi(graphics, id, elencoClassiPersonaggio, coordinataY);
			descrizione = st.nextToken();
			drawStringCenteredWithAutoWrap(graphics, id + " - " + descrizione.toLowerCase(), coordinataY);
		}
	}
	
	void selezioneSlotDaSalvare(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		drawStringCenteredWithAutoWrap(graphics, "seleziona lo slot per il salvataggio", 50);
		List<String> slotDisponibili = new ArrayList<>();
		for (int i = 1; i <= InterfacciaGestoreSalvataggi.NUMERO_MASSIMO; i++) {
			slotDisponibili.add(String.valueOf(i));
		}
		for (InterfacciaGestoreSalvataggi.InterfacciaTestataSalvataggio testata : GestoreSalvataggi.getSalvataggiDisponibili()) {
			slotDisponibili.remove(testata.getId());
			int id = Integer.parseInt(testata.getId());
			int coordinataY = getCoordinataY(id);
			String descrizione = testata.getDescrizione();
			StringTokenizer st = new StringTokenizer(descrizione, "|");
			String elencoClassiPersonaggio = st.nextToken();
			disegnaPersonaggi(graphics, id, elencoClassiPersonaggio, coordinataY);
			descrizione = st.nextToken();
			drawStringCenteredWithAutoWrap(graphics, id + " - " + descrizione.toLowerCase(), coordinataY);
		}
		for (String slotDisponibile : slotDisponibili) {
			drawStringCenteredWithAutoWrap(graphics, slotDisponibile + " - slot disponibile", getCoordinataY(Integer.parseInt(slotDisponibile)));
		}
	}

	void confermaUscita(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		drawStringCenteredWithAutoWrap(graphics, "uscire dal gioco?", 100);
	}

	void perso(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		messaggio = Misc.PERSO[sequenza];
		scrivi(graphics);
	}

	void vinto(Graphics2D graphics) {
		Image d = ImageCache.trionfo;
		graphics.drawImage(d, (width - d.getWidth(null)) / 2, (height - d.getHeight(null)) / 2, null);
		messaggio = Misc.VINTO[sequenza];
		scrivi(graphics);
	}

	void hiscore(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		Logger.log("HISCORE");
		int locXOffset = xOffset;
		int locYOffset = yOffset + 28;
		Image doomdark = null;
		DoomdarkColorModel.Color color = DoomdarkColorModel.Color.MEDIUM_GRAY;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		for (int posizione = 0; posizione < GestorePunteggi.getCardinalita(); posizione++) {
			InterfacciaGestorePunteggi.Record record = GestorePunteggi.getRecord(posizione);
			color = (color == DoomdarkColorModel.Color.MEDIUM_GRAY ? DoomdarkColorModel.Color.LIGHT_GRAY : DoomdarkColorModel.Color.MEDIUM_GRAY);
			doomdark = DoomdarkTextProducer.getImage(record.getNome(), fontMedium, color);
			graphics.drawImage(doomdark, locXOffset + 9, locYOffset, null);
			doomdark = DoomdarkTextProducer.getImage(record.getPunteggio(), fontMedium, color);
			graphics.drawImage(doomdark, width - locXOffset - doomdark.getWidth(null), locYOffset, null);
			locYOffset += fontMedium.getHeight();
		}
		for (int posizione = 0; posizione < GestorePunteggi.getCardinalita(); posizione++) {
			InterfacciaGestorePunteggi.Record record = GestorePunteggi.getRecord(posizione);
			int coordinataY = 100 + 50 * posizione;
			drawString(graphics, record.getNome().toLowerCase(), 50, coordinataY);
			String punteggio = String.valueOf(record.getPunteggio());
			drawString(graphics, punteggio, width - 50 - getWordWidth(punteggio), coordinataY);
		}
	}

	private final void disegnaOmbraDelDrago(Graphics2D graphics) {
		Image d = ImageCache.ombraDelDrago;
		graphics.drawImage(d, (width - d.getWidth(null)) >> 1, (height - d.getHeight(null)) >> 1, null);
	}
	
	private final void drawString(Graphics2D graphics, String s, int xOffset, int yOffset) {
		char[] caratteri = s.toCharArray();
		for (int i = 0; i < caratteri.length; i++) {
			BufferedImage image = getCharImage(caratteri[i]);
			graphics.drawImage(image, xOffset, yOffset, null);
			xOffset += CHAR_SPACING + getCharWidth(caratteri[i]);
			if (xOffset >= width) {
				break;
			}
		}
	}

	private final void drawStringCenteredWithAutoWrap(Graphics2D graphics, String s, int yOffset) {
		StringTokenizer st = new StringTokenizer(s, " ");
		int phraseWidth = 0;
		int wordWidth;
		StringBuilder phrase = new StringBuilder();
		String word;
		while (st.hasMoreTokens()) {
			word = st.nextToken();
			wordWidth = getWordWidth(word);
			if (phraseWidth > 0 && (phraseWidth + 2 * CHAR_SPACING + getCharWidth(' ') + wordWidth >= width)) {
				render(graphics, phrase.toString(), phraseWidth, yOffset);
				yOffset += 36;
				phrase = new StringBuilder();
				phraseWidth = 0;
			}
			if (phrase.length() > 0) {
				phrase.append(" ");
				phraseWidth += getCharWidth(' ');
			}
			phrase.append(word);
			phraseWidth += wordWidth + CHAR_SPACING;
		}
		render(graphics, phrase.toString(), phraseWidth, yOffset);
	}

	private final int getWordWidth(String s) {
		int wordWidth = 0;
		int l = s.length();
		for (int i = 0; i < l; i++)
			wordWidth += getCharWidth(s.charAt(i)) + CHAR_SPACING;
		return wordWidth;
	}

	private final void render(Graphics graphics, String phrase, int phraseWidth, int yOffset) {
		int l = phrase.length();
		int locXOffset = width - phraseWidth >> 1;
		char[] c = phrase.toCharArray();
		Image img;
		for (int i = 0; i < l; i++) {
			img = getCharImage(c[i]);
			if (img != null)
				graphics.drawImage(img, locXOffset, yOffset, null);
			locXOffset += getCharWidth(c[i]) + CHAR_SPACING;
		}
	}

	private final BufferedImage getCharImage(char c) {
		int index;
		if (c >= 'a' && c <= 'z') {
			index = c - 'a';
			return ImageCache.lettere[index];
		} else if (c >= '0' && c <= '9') {
			index = c - '0';
			return ImageCache.cifre[index];
		} else if (c == '\'') {
			return ImageCache.apostrofo;
		} else if (c == ',') {
			return ImageCache.virgola;
		} else if (c == '.') {
			return ImageCache.punto;
		} else if (c == '?') {
			return ImageCache.puntodd;
		}
		return null;
	}

	private final int getCharWidth(char c) {
		if (c >= 'a' && c <= 'z')
			return ImageCache.lettere[c - 'a'].getWidth();
		if (c >= '0' && c <= '9')
			return ImageCache.lettere[c - '0'].getWidth();
		if (c == '\'')
			return ImageCache.apostrofo.getWidth();
		if (c == ',')
			return ImageCache.virgola.getWidth();
		if (c == '.')
			return ImageCache.punto.getWidth();
		if (c == '?')
			return ImageCache.puntodd.getWidth();
		if (c == ' ')
			return 10;
		return 1;
	}
	
	private final int getCoordinataY(int id) {
		return 50 + 100 * id;
	}

	private final void disegnaPersonaggi(Graphics2D graphics, int id, String elenco, int coordinataY) {
		StringTokenizer st = new StringTokenizer(elenco, ",");
		List<BufferedImage> immagini = new ArrayList<>();
		List<Integer> coordinateX = new ArrayList<>();
		int coordinataX = (width >> 1) + 100 * (id - 3);
		int altezzaMinima = 999;
		while (st.hasMoreTokens()) {
			ClassiPersonaggio classePersonaggio = ClassiPersonaggio.values()[Integer.parseInt(st.nextToken())];
			BufferedImage immagine = classePersonaggio.getIstanza().getImmagine();
			immagini.add(0, immagine);
			coordinateX.add(0, coordinataX);
			coordinataX += immagine.getWidth() * 2 / 3;
			if (altezzaMinima > immagine.getHeight()) {
				altezzaMinima = immagine.getHeight();
			}
		}
		coordinataY += altezzaMinima * 2 / 3;
		for (int i = 0; i < immagini.size(); i++) {
			BufferedImage immagine = immagini.get(i);
			graphics.drawImage(immagine, coordinateX.get(i), coordinataY - immagine.getHeight(), null);
		}
	}
}
