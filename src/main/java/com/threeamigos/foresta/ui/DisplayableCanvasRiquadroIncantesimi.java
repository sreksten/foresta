package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.GruppoGiocatore;

class DisplayableCanvasRiquadroIncantesimi {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI = 16;

	private int topLeftX;
	private int topLeftY;
	private int iconaIncantesimoX;
	private int nomeIncantesimoX;
	private int totaleIncantesimoX;
	private int iconaPozioneX;
	private int nomePozioneX;
	private int totalePozioneX;

	DisplayableCanvasRiquadroIncantesimi(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		int maxIconWidth = 0;
		for (BufferedImage image : ImageCache.spriteIncantesimi) {
			maxIconWidth = Math.max(maxIconWidth, image.getWidth());
		}
		iconaIncantesimoX = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
		nomeIncantesimoX = iconaIncantesimoX + maxIconWidth + 2;
		totaleIncantesimoX = topLeftX + (ImageCache.corniceIncantesimi.getWidth() / 2);

		maxIconWidth = 0;
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneForza.getWidth());
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneGrandeForza.getWidth());
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneMagia.getWidth());
		iconaPozioneX = topLeftX + ImageCache.corniceIncantesimi.getWidth() / 2;
		nomePozioneX = iconaPozioneX + maxIconWidth + 2;
		totalePozioneX = topLeftX + ImageCache.corniceIncantesimi.getWidth() - DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;		
	}

	void disegnaIncantesimi(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceIncantesimi, topLeftX, topLeftY, null);
		
		graphics.setClip(topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI,
				topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI,
				ImageCache.corniceIncantesimi.getWidth() - 2* DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI,
				ImageCache.corniceIncantesimi.getHeight() - 2 * DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI);

		GruppoGiocatore g = GruppoGiocatore.getIstanza();
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		int locYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
		Image doomdark = null;
		DoomdarkColorModel.Color color = DoomdarkColorModel.Color.MEDIUM_GRAY;
		for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
			if (color == DoomdarkColorModel.Color.LIGHT_GRAY) {
				color = DoomdarkColorModel.Color.MEDIUM_GRAY;
			} else {
				color = DoomdarkColorModel.Color.LIGHT_GRAY;
			}
			BufferedImage iconaIncantesimo = ImageCache.spriteIncantesimi[classeIncantesimo.ordinal()];
			graphics.drawImage(iconaIncantesimo, iconaIncantesimoX, locYOffset - (iconaIncantesimo.getHeight() - fontMedium.getHeight()) / 2, null);
			doomdark = DoomdarkTextProducer.getImage(classeIncantesimo.getIstanza().getNomeAbbreviato(), fontMedium, color);
			graphics.drawImage(doomdark, nomeIncantesimoX, locYOffset, null);
			doomdark = DoomdarkTextProducer.getImage(g.getIncantesimi(classeIncantesimo), fontMedium, color);
			graphics.drawImage(doomdark, totaleIncantesimoX - doomdark.getWidth(null), locYOffset, null);
			locYOffset += fontMedium.getHeight();
		}
		
		locYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
		BufferedImage iconaPozioneForza = ImageCache.spritePozioneForza;
		graphics.drawImage(iconaPozioneForza, iconaPozioneX, locYOffset - (iconaPozioneForza.getHeight() - fontMedium.getHeight()) / 2, null);
		doomdark = DoomdarkTextProducer.getImage("Forza", fontMedium, color);
		graphics.drawImage(doomdark, nomePozioneX, locYOffset, null);
		doomdark = DoomdarkTextProducer.getImage(g.getPozioniForza(), fontMedium, color);
		graphics.drawImage(doomdark, totalePozioneX - doomdark.getWidth(null), locYOffset, null);
		locYOffset += fontMedium.getHeight();

		BufferedImage iconaPozioneGrandeForza = ImageCache.spritePozioneGrandeForza;
		graphics.drawImage(iconaPozioneGrandeForza, iconaPozioneX, locYOffset - (iconaPozioneGrandeForza.getHeight() - fontMedium.getHeight()) / 2, null);
		doomdark = DoomdarkTextProducer.getImage("G. Forza", fontMedium, color);
		graphics.drawImage(doomdark, nomePozioneX, locYOffset, null);
		doomdark = DoomdarkTextProducer.getImage(g.getPozioniGrandeForza(), fontMedium, color);
		graphics.drawImage(doomdark, totalePozioneX - doomdark.getWidth(null), locYOffset, null);
		locYOffset += fontMedium.getHeight();

		BufferedImage iconaPozioneMagia = ImageCache.spritePozioneMagia;
		graphics.drawImage(iconaPozioneMagia, iconaPozioneX, locYOffset - (iconaPozioneMagia.getHeight() - fontMedium.getHeight()) / 2, null);
		doomdark = DoomdarkTextProducer.getImage("Magia", fontMedium, color);
		graphics.drawImage(doomdark, nomePozioneX, locYOffset, null);
		doomdark = DoomdarkTextProducer.getImage(g.getPozioniMagia(), fontMedium, color);
		graphics.drawImage(doomdark, totalePozioneX - doomdark.getWidth(null), locYOffset, null);

		graphics.setClip(null);		
	}

	SpriteInterface variaIncantesimi(ClassiIncantesimo classeIncantesimo, int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteIncantesimi[classeIncantesimo.ordinal()];
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + classeIncantesimo.ordinal() * fontMedium.getHeight();
		return new SpriteATempo(icona, variazione, fontMedium, totaleIncantesimoX, y);
	}

	SpriteInterface variaPozioniForza(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneForza;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
		return new SpriteATempo(icona, variazione, fontMedium, totalePozioneX, y);
	}

	SpriteInterface variaPozioniGrandeForza(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneGrandeForza;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + fontMedium.getHeight();
		return new SpriteATempo(icona, variazione, fontMedium, totalePozioneX, y);
	}

	SpriteInterface variaPozioniMagia(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneMagia;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + 2 * fontMedium.getHeight();
		return new SpriteATempo(icona, variazione, fontMedium, totalePozioneX, y);
	}
}
