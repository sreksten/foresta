package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.RegistroMissioni;

import java.awt.*;

class DisplayableCanvasRiquadroMissioni implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE = 16;
	private static final int SPACING = 4;
	// Pixel di scorrimento per ogni scatto della rotella
	private static final int PASSO_SCORRIMENTO = 1;

	private final int topLeftX;
	private final int topLeftY;
	private final int innerWidth;
	private final int innerHeight;

	int offsetY = 0;

	DisplayableCanvasRiquadroMissioni(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		innerWidth = ImageCache.corniceGrande.getWidth() - ((DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE  + SPACING) << 1);
		innerHeight = ImageCache.corniceGrande.getHeight() - ((DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING) << 1);
	}

	void disegnaMissioni(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);

		ComponenteScorrevole componenteScorrevole = new ComponenteScorrevole(innerWidth, 10, SPACING);
		for (Missione missione : RegistroMissioni.getMissioniAttive()) {
				aggiungiAComponenteScorrevole(componenteScorrevole, missione);
		}
		// L'elenco può essere cambiato dall'ultimo scorrimento: l'offset va rimesso nei limiti
		offsetY = componenteScorrevole.limitaOffset(innerHeight, offsetY);
		Image image = componenteScorrevole.produci(innerHeight, offsetY);
		graphics.drawImage(image, topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE  + SPACING,
				topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING, null);
	}

	private void aggiungiAComponenteScorrevole(ComponenteScorrevole componenteScorrevole, Missione missione) {
		ComponenteScorrevole.Nodo nodo = componenteScorrevole.creaNodo(missione.getNome(), DoomdarkFontMedium.getInstance(),
				missione.getDescrizione(), DoomdarkFontSmall.getInstance());
		java.util.List<Missione> missioniSecondarie = missione.getMissioniSecondarie();
		for (Missione missioneSecondaria : missioniSecondarie) {
				aggiungiANodo(nodo, missioneSecondaria);
		}
	}

	private void aggiungiANodo(ComponenteScorrevole.Nodo nodo, Missione missione) {
		ComponenteScorrevole.Nodo nodoFiglio = nodo.creaNodo(missione.getNome(), DoomdarkFontMedium.getInstance(),
				missione.getDescrizione(), DoomdarkFontSmall.getInstance());
		java.util.List<Missione> missioniSecondarie = missione.getMissioniSecondarie();
		for (Missione missioneSecondaria : missioniSecondarie) {
			aggiungiANodo(nodoFiglio, missioneSecondaria);
		}
	}

	void disegnaMissioniOLD(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();
		int totalRenderedTextHeight = 0;
		int locXOffset = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING;
		int locYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING;
		Image doomdark;
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

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (movimentoRotella == MovimentoRotella.SU) {
			Logger.log("Scrolling up by " + numeroRotazioni + " units");
			offsetY = Math.max(0, offsetY - numeroRotazioni * PASSO_SCORRIMENTO);
		} else if (movimentoRotella == MovimentoRotella.GIU) {
			Logger.log("Scrolling down by " + numeroRotazioni + " units");
			offsetY += numeroRotazioni * PASSO_SCORRIMENTO;
		}
		// Il limite superiore dipende dall'altezza della lista e viene applicato al disegno
	}
}
