package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.GruppoGiocatore;

import java.awt.*;
import java.awt.image.BufferedImage;

class DisplayableCanvasRiquadroIncantesimi {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI = 16;

	private final int topLeftX;
	private final int topLeftY;
	private final int iconaIncantesimoX;
	private final int nomeIncantesimoX;
	private final int totaleIncantesimoX;
	private final int iconaPozioneX;
	private final int nomePozioneX;
	private final int totalePozioneX;

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
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneSalute.getWidth());
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneSaluteGrande.getWidth());
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
		BufferedImage iconaPozioneSalute = ImageCache.spritePozioneSalute;
		graphics.drawImage(iconaPozioneSalute, iconaPozioneX, locYOffset - (iconaPozioneSalute.getHeight() - fontMedium.getHeight()) / 2, null);
		doomdark = DoomdarkTextProducer.getImage("Salute", fontMedium, color);
		graphics.drawImage(doomdark, nomePozioneX, locYOffset, null);
		doomdark = DoomdarkTextProducer.getImage(g.getPozioniSalute(), fontMedium, color);
		graphics.drawImage(doomdark, totalePozioneX - doomdark.getWidth(null), locYOffset, null);
		locYOffset += fontMedium.getHeight();

		BufferedImage iconaPozioneSaluteGrande = ImageCache.spritePozioneSaluteGrande;
		graphics.drawImage(iconaPozioneSaluteGrande, iconaPozioneX, locYOffset - (iconaPozioneSaluteGrande.getHeight() - fontMedium.getHeight()) / 2, null);
		doomdark = DoomdarkTextProducer.getImage("G. Salute", fontMedium, color);
		graphics.drawImage(doomdark, nomePozioneX, locYOffset, null);
		doomdark = DoomdarkTextProducer.getImage(g.getPozioniSaluteGrande(), fontMedium, color);
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

	SpriteInterface variaPozioniSalute(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneSalute;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
		return new SpriteATempo(icona, variazione, fontMedium, totalePozioneX, y);
	}

	SpriteInterface variaPozioniSaluteGrande(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneSaluteGrande;
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
