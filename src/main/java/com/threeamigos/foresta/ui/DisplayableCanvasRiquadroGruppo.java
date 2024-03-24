package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;

class DisplayableCanvasRiquadroGruppo {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE = 16;

	private int topLeftX;
	private int topLeftY;
	private int innerWidth;
	private Map<Object, Image> lightGrayMap = new HashMap<>();
	private Map<Object, Image> mediumGrayMap = new HashMap<>();
	private DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
	private int leftXOffsetLabelForza;
	private int rightXOffsetForza;
	private int leftXOffsetSeparatoreForza;
	private int rightXOffsetForzaMassima;
	private int leftXOffsetLabelMagia;
	private int rightXOffsetMagia;
	private int leftXOffsetSeparatoreMagia;
	private int rightXOffsetMagiaMassima;
	private int leftXOffsetLabelCoraggio;
	private int rightXOffsetCoraggio;
	private int leftXOffsetLabelValore;
	private int rightXOffsetValore;
	private int leftXOffsetLabelStanchezza;
	private int rightXOffsetStanchezza;
	private int leftXOffsetLabelCarisma;
	private int rightXOffsetCarisma;

	DisplayableCanvasRiquadroGruppo(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		innerWidth = ImageCache.corniceGrande.getWidth() - (DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE << 1);
		lightGrayMap.put(Personaggio.Caratteristica.FORZA, DoomdarkTextProducer.getImage("Fr:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(Personaggio.Caratteristica.MAGIA, DoomdarkTextProducer.getImage("Mg:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(Personaggio.Caratteristica.CORAGGIO, DoomdarkTextProducer.getImage("Cr:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(Personaggio.Caratteristica.VALORE, DoomdarkTextProducer.getImage("Vl:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(Personaggio.Caratteristica.STANCHEZZA, DoomdarkTextProducer.getImage("St:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put(Personaggio.Caratteristica.CARISMA, DoomdarkTextProducer.getImage("Ca:", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		lightGrayMap.put("/", DoomdarkTextProducer.getImage("/", fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY));
		mediumGrayMap.put(Personaggio.Caratteristica.FORZA, DoomdarkTextProducer.getImage("Fr:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(Personaggio.Caratteristica.MAGIA, DoomdarkTextProducer.getImage("Mg:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(Personaggio.Caratteristica.CORAGGIO, DoomdarkTextProducer.getImage("Cr:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(Personaggio.Caratteristica.VALORE, DoomdarkTextProducer.getImage("Vl:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(Personaggio.Caratteristica.STANCHEZZA, DoomdarkTextProducer.getImage("St:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put(Personaggio.Caratteristica.CARISMA, DoomdarkTextProducer.getImage("Ca:", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		mediumGrayMap.put("/", DoomdarkTextProducer.getImage("/", fontMedium, DoomdarkColorModel.Color.MEDIUM_GRAY));
		
		int glyph9Width = fontMedium.getGlyphWidth('9');
		
		 // Fr:999/999 Mg:99/99
		leftXOffsetLabelForza = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
		rightXOffsetForza = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 6;
		leftXOffsetSeparatoreForza = rightXOffsetForza;
		rightXOffsetForzaMassima = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 10;
		leftXOffsetLabelMagia = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 11;
		rightXOffsetMagia = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 16;
		leftXOffsetSeparatoreMagia = rightXOffsetMagia;
		rightXOffsetMagiaMassima = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 19;
		
		//Cr:99 Vl:99 St:99 Ca:99
		leftXOffsetLabelCoraggio = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
		rightXOffsetCoraggio = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 5;
		leftXOffsetLabelValore = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 6;
		rightXOffsetValore = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 11;
		leftXOffsetLabelStanchezza = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 12;
		rightXOffsetStanchezza = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 17;
		leftXOffsetLabelCarisma = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 18;
		rightXOffsetCarisma = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + glyph9Width * 23;
	}

	void disegnaStatus(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);
		
		GruppoGiocatore g = GruppoGiocatore.getIstanza();
		int locXOffset = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
		int locYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;

		int l = g.getNumeroPersonaggi();
		Personaggio p;
		for (int i = 0; i < l; i++) {
			p = g.getPersonaggio(i);
			Image doomdark = null;
			if (!p.isVivo()) {
				if (p.getNome() != null) {
					doomdark = DoomdarkTextProducer.getImage(new StringBuilder(p.getNome()).append('-').append(p.getNomeSingolare()).toString(), fontMedium, DoomdarkColorModel.Color.DARK_GRAY);
				} else {
					doomdark = DoomdarkTextProducer.getImage(p.getNomeSingolare(), fontMedium, DoomdarkColorModel.Color.DARK_GRAY);
				}
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);
				locYOffset += fontMedium.getHeight();
				doomdark = DoomdarkTextProducer.getImage(p.getCausaTrapasso(), fontMedium, DoomdarkColorModel.Color.DARK_GRAY, innerWidth);
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);
				locYOffset += fontMedium.getHeight() * 2;
			} else {
				DoomdarkColorModel.Color color = i % 2 == 0 ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.LIGHT_GRAY;
				Map<Object, Image> imageMap = i % 2 == 0 ? mediumGrayMap : lightGrayMap;
				if (p.getNome() != null) {
					doomdark = DoomdarkTextProducer.getImage(new StringBuilder(p.getNome()).append('-').append(p.getNomeSingolare()).toString(), fontMedium, color);
				} else {
					doomdark = DoomdarkTextProducer.getImage(p.getNomeSingolare(), fontMedium, color);
				}
				graphics.drawImage(doomdark, locXOffset, locYOffset, null);

				locYOffset += fontMedium.getHeight();
				graphics.drawImage(imageMap.get(Personaggio.Caratteristica.FORZA), leftXOffsetLabelForza, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getForza(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetForza - doomdark.getWidth(null), locYOffset, null);
				graphics.drawImage(imageMap.get("/"), leftXOffsetSeparatoreForza, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getForzaMassima(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetForzaMassima - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(Personaggio.Caratteristica.MAGIA), leftXOffsetLabelMagia, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getMagia(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagia - doomdark.getWidth(null), locYOffset, null);
				graphics.drawImage(imageMap.get("/"), leftXOffsetSeparatoreMagia, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getMagiaMassima(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetMagiaMassima - doomdark.getWidth(null), locYOffset, null);

				locYOffset += fontMedium.getHeight();
				graphics.drawImage(imageMap.get(Personaggio.Caratteristica.CORAGGIO), leftXOffsetLabelCoraggio, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getCoraggio(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetCoraggio - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(Personaggio.Caratteristica.VALORE), leftXOffsetLabelValore, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getValore(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetValore - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(Personaggio.Caratteristica.STANCHEZZA), leftXOffsetLabelStanchezza, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getStanchezza(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetStanchezza - doomdark.getWidth(null), locYOffset, null);

				graphics.drawImage(imageMap.get(Personaggio.Caratteristica.CARISMA), leftXOffsetLabelCarisma, locYOffset, null);
				doomdark = DoomdarkTextProducer.getImage(p.getCarisma(), fontMedium, color);
				graphics.drawImage(doomdark, rightXOffsetCarisma - doomdark.getWidth(null), locYOffset, null);

				locYOffset += fontMedium.getHeight();
			}
		}
	}

	private int getOrdinalePersonaggio(Personaggio personaggio) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		for (int i = 0; i < gruppo.getNumeroPersonaggi(); i++) {
			if (gruppo.getPersonaggio(i).equals(personaggio)) {
				return i;
			}
		}
		return -1;
	}

	SpriteInterface variaForza(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetForza, y);
	}

	SpriteInterface variaForzaMassima(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetForzaMassima, y);
	}

	SpriteInterface variaMagia(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMagia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetMagia, y);
	}
	
	SpriteInterface variaMagiaMassima(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMagia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetMagiaMassima, y);
	}
	
	SpriteInterface variaCoraggio(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 2);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetCoraggio, y);
	}
	
	SpriteInterface variaValore(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetValore, y);
	}
	
	SpriteInterface variaCarisma(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteAmicizia;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 1);
		return new SpriteATempo(icona, variazione, fontMedium, rightXOffsetCarisma, y);
	}

	SpriteInterface variaStanchezza(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteCombattimento;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + fontMedium.getHeight() * (ordinalePersonaggio * 3 + 2);
		DoomdarkColorModel.Color color = variazione < 0 ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
		return new SpriteATempo(icona, variazione, fontMedium, color, rightXOffsetStanchezza, y);
	}

	SpriteInterface variaTempo(Personaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinalePersonaggio = getOrdinalePersonaggio(personaggio);
		if (ordinalePersonaggio == -1) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteTempo;
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + ordinalePersonaggio * fontMedium.getHeight() * 3;
		return new SpriteATempo(icona, variazione, fontMedium, topLeftX + ((ImageCache.corniceGrande.getWidth() - (DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE << 1)) >> 1), y);
	}

}
