package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.GruppoGiocatore;

import java.awt.*;
import java.awt.image.BufferedImage;

class DisplayableCanvasRiquadroIncantesimiEPozioni implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI = 16;

	private final ClasseIncantesimo[] colonnaSinistra = { ClasseIncantesimo.ARIA, ClasseIncantesimo.ACQUA, ClasseIncantesimo.TERRA,
			ClasseIncantesimo.FUOCO, ClasseIncantesimo.FULMINE, ClasseIncantesimo.GELO, ClasseIncantesimo.VELENO };
	private final ClasseIncantesimo[] colonnaDestra = { ClasseIncantesimo.MORTE, ClasseIncantesimo.RESURREZIONE };

	private final int topLeftX;
	private final int topLeftY;
	private final int iconaSinistraX;
	private final int nomeSinistraX;
	private final int totaleSinistraX;
	private final int iconaDestraX;
	private final int nomeDestraX;
	private final int totaleDestraX;

	DisplayableCanvasRiquadroIncantesimiEPozioni(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		int maxIconWidth = 0;
		for (ClasseIncantesimo classeIncantesimo : colonnaSinistra) {
			maxIconWidth = Math.max(maxIconWidth, ImageCache.spriteIncantesimi[classeIncantesimo.ordinal()].getWidth());
		}
		iconaSinistraX = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
		nomeSinistraX = iconaSinistraX + maxIconWidth + 2;
		totaleSinistraX = topLeftX + (ImageCache.corniceIncantesimi.getWidth() / 2);

		maxIconWidth = 0;
		for (ClasseIncantesimo classeIncantesimo : colonnaDestra) {
			maxIconWidth = Math.max(maxIconWidth, ImageCache.spriteIncantesimi[classeIncantesimo.ordinal()].getWidth());
		}
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneSalute.getWidth());
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneSaluteGrande.getWidth());
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneMagia.getWidth());
		maxIconWidth = Math.max(maxIconWidth, ImageCache.spritePozioneMagiaGrande.getWidth());
		iconaDestraX = topLeftX + ImageCache.corniceIncantesimi.getWidth() / 2;
		nomeDestraX = iconaDestraX + maxIconWidth + 2;
		totaleDestraX = topLeftX + ImageCache.corniceIncantesimi.getWidth() - DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
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
		DoomdarkColorAlternante color = new DoomdarkColorAlternante();

		for (ClasseIncantesimo classeIncantesimo : colonnaSinistra) {
			disegnaIncantesimo(graphics, iconaSinistraX, nomeSinistraX, totaleSinistraX, locYOffset, classeIncantesimo, g.getIncantesimi(classeIncantesimo), color);
			locYOffset += fontMedium.getHeight();
		}
		
		locYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;

		for (ClasseIncantesimo classeIncantesimo : colonnaDestra) {
			disegnaIncantesimo(graphics, iconaDestraX, nomeDestraX, totaleDestraX, locYOffset, classeIncantesimo, g.getIncantesimi(classeIncantesimo), color);
			locYOffset += fontMedium.getHeight();
		}

		// Per lasciare spazio tra incantesimi e pozioni
		locYOffset += fontMedium.getHeight();

		disegnaOggetto(graphics, iconaDestraX, nomeDestraX, totaleDestraX, locYOffset, ImageCache.spritePozioneSalute, "Salute", g.getPozioniSalute(), color);
		locYOffset += fontMedium.getHeight();

		disegnaOggetto(graphics, iconaDestraX, nomeDestraX, totaleDestraX, locYOffset, ImageCache.spritePozioneSaluteGrande, "G. Salute", g.getPozioniSaluteGrande(), color);
		locYOffset += fontMedium.getHeight();

		disegnaOggetto(graphics, iconaDestraX, nomeDestraX, totaleDestraX, locYOffset, ImageCache.spritePozioneMagia, "Magia", g.getPozioniMagia(), color);
		locYOffset += fontMedium.getHeight();

		disegnaOggetto(graphics, iconaDestraX, nomeDestraX, totaleDestraX, locYOffset, ImageCache.spritePozioneMagiaGrande, "G. Magia", g.getPozioniMagiaGrande(), color);

		graphics.setClip(null);		
	}

	private void disegnaIncantesimo(Graphics2D graphics, int iconaX, int nomeX, int totaleX, int y, ClasseIncantesimo classeIncantesimo, int quantita, DoomdarkColorAlternante color) {
		disegnaOggetto(graphics, iconaX, nomeX, totaleX, y, ImageCache.spriteIncantesimi[classeIncantesimo.ordinal()], classeIncantesimo.getNomeAbbreviato(), quantita, color);
	}

	private void disegnaOggetto(Graphics2D graphics, int iconaX, int nomeX, int totaleX, int y, BufferedImage icona, String descrizione, int quantita, DoomdarkColorAlternante colorA) {
        DoomdarkColorModel.Color color = colorA.getColor();
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		graphics.drawImage(icona, iconaX, y - (icona.getHeight() - fontMedium.getHeight()) / 2, null);
		Image doomdark = DoomdarkTextProducer.getImage(descrizione, fontMedium, color);
		graphics.drawImage(doomdark, nomeX, y, null);
		doomdark = DoomdarkTextProducer.getImage(quantita, fontMedium, color);
		graphics.drawImage(doomdark, totaleX - doomdark.getWidth(null), y, null);
	}

	void gestisciEventoVariazioneIncantesimi(NotificaVariazioneDisponibilitaIncantesimi evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneIncantesimi(evento.getClasseIncantesimo(),
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneIncantesimi(ClasseIncantesimo classeIncantesimo, int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteIncantesimi[classeIncantesimo.ordinal()];
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + classeIncantesimo.ordinal() * fontMedium.getHeight();
		int x = totaleSinistraX;
		for (ClasseIncantesimo classeIncantesimoADestra : colonnaDestra) {
            if (classeIncantesimo == classeIncantesimoADestra) {
                x = totaleDestraX;
                break;
            }
		}
		return new SpriteATempo(icona, variazione, fontMedium, x, y, "Incantesimo " + classeIncantesimo + " variato");
	}

	void gestisciEventoVariazionePozioniSalute(NotificaVariazioneDisponibilitaPozioniSalute evento) {
		SpriteATempo sprite = costruisciSpritePerVariazionePozioniSalute(
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazionePozioniSalute(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneSalute;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + fontMedium.getHeight();
		return new SpriteATempo(icona, variazione, fontMedium, totaleDestraX, y, "Pozioni salute variate");
	}

	void gestisciEventoVariazionePozioniSaluteGrandi(NotificaVariazioneDisponibilitaPozioniSaluteGrandi evento) {
		SpriteATempo sprite = costruisciSpritePerVariazionePozioniSaluteGrandi(
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazionePozioniSaluteGrandi(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneSaluteGrande;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + 2 * fontMedium.getHeight();
		return new SpriteATempo(icona, variazione, fontMedium, totaleDestraX, y, "Pozioni salute grande variate");
	}

	void gestisciEventoVariazionePozioniMagia(NotificaVariazioneDisponibilitaPozioniMagia evento) {
		SpriteATempo sprite = costruisciSpritePerVariazionePozioniMagia(
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazionePozioniMagia(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneMagia;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + 3 * fontMedium.getHeight();
		return new SpriteATempo(icona, variazione, fontMedium, totaleDestraX, y, "Pozioni magia variate");
	}

	void gestisciEventoVariazionePozioniMagiaGrandi(NotificaVariazioneDisponibilitaPozioniMagiaGrandi evento) {
		SpriteATempo sprite = costruisciSpritePerVariazionePozioniMagiaGrandi(
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazionePozioniMagiaGrandi(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spritePozioneMagiaGrande;
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		final int y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + 4 * fontMedium.getHeight();
		return new SpriteATempo(icona, variazione, fontMedium, totaleDestraX, y, "Pozioni magia grande variate");
	}
}
