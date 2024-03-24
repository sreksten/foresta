package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.Image;

import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.motore.RegistroMissioni;

class DisplayableCanvasRiquadroMissioni {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE = 16;
	private static final int SPACING = 4;

	private int topLeftX;
	private int topLeftY;
	private int innerWidth;
	private int innerHeight;

	DisplayableCanvasRiquadroMissioni(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		innerWidth = ImageCache.corniceGrande.getWidth() - ((DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE  + SPACING) << 1);
		innerHeight = ImageCache.corniceGrande.getHeight() - ((DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING) << 1);
	}
	
	void disegnaMissioni(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();
		int totalRenderedTextHeight = 0;
		int locXOffset = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING;
		int locYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING;
		Image doomdark = null;
		DoomdarkColorModel.Color color = DoomdarkColorModel.Color.MEDIUM_GRAY;
		for (Missione missione : RegistroMissioni.getMissioniAttive()) {
			if (missione.isAttiva() && !missione.isCompleta()) {
				if (color == DoomdarkColorModel.Color.LIGHT_GRAY) {
					color = DoomdarkColorModel.Color.MEDIUM_GRAY;
				} else {
					color = DoomdarkColorModel.Color.LIGHT_GRAY;
				}
				doomdark = DoomdarkTextProducer.getImage(missione.getNome(), fontMedium, color, innerWidth);
				if (totalRenderedTextHeight + doomdark.getHeight(null) < innerHeight) {
					graphics.drawImage(doomdark, locXOffset, locYOffset, null);
					locYOffset += doomdark.getHeight(null);
					totalRenderedTextHeight += doomdark.getHeight(null);
					doomdark = DoomdarkTextProducer.getImage(missione.getDescrizione(), fontSmall, color, innerWidth);
					if (totalRenderedTextHeight + doomdark.getHeight(null) < innerHeight) {
						graphics.drawImage(doomdark, locXOffset, locYOffset, null);
						locYOffset += doomdark.getHeight(null);
						totalRenderedTextHeight += doomdark.getHeight(null);
					} else {
						return;
					}
				}
				locYOffset += SPACING;
			} else {
				return;
			}
		}
	}
}
