package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaGemme;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaMonete;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePuntiEsperienzaPersonaggio;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;

import java.awt.*;
import java.awt.image.BufferedImage;

class DisplayableCanvasRiquadroStatistiche implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE = 6;

	private final DoomdarkFontMedium fontMedium = DoomdarkFontMedium.getInstance();
	private final DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

	private final int topLeftX;
	private final int topLeftY;
	private final int moneteY;
	private final int gemmeY;
	private final int puntiY;
	private final int scrittaX;
	private final int totaleX;
	
	DisplayableCanvasRiquadroStatistiche(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		moneteY = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE + 4;
		gemmeY = moneteY + fontMedium.getHeight() + 1;
		puntiY = gemmeY + fontMedium.getHeight() + 1;
		scrittaX = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE + 4;
		totaleX = topLeftX + ImageCache.cornicePiccola.getWidth() - DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE - 4;

		registratiAEventi();
	}

	private void registratiAEventi() {
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaGemme.class, this::gestisciEventoVariazioneGemme);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaMonete.class, this::gestisciEventoVariazioneMonete);
		BusEventi.iscriviti(NotificaVariazionePuntiEsperienzaPersonaggio.class, this::gestisciEventoVariazionePuntiEsperienza);
	}

	void disegnaStatistiche(Graphics2D graphics) {
		GruppoGiocatore gruppoGiocatore = GruppoGiocatore.getIstanza();
		graphics.drawImage(ImageCache.cornicePiccola, topLeftX, topLeftY, null);
		
		Image image = ImageCache.get("Monete", fontMedium, coloreTestata);
		graphics.drawImage(image, scrittaX, moneteY, null);
		image = ImageCache.get(gruppoGiocatore.getMonete(), fontMedium, coloreTestata);
		graphics.drawImage(image, totaleX - image.getWidth(null), moneteY, null);
		
		image = ImageCache.get("Gemme", fontMedium, coloreTestata);
		graphics.drawImage(image, scrittaX, gemmeY, null);
		image = ImageCache.get(gruppoGiocatore.getPreziosi(), fontMedium, coloreTestata);
		graphics.drawImage(image, totaleX - image.getWidth(null), gemmeY, null);

		image = ImageCache.get("Punti", fontMedium, coloreTestata);
		graphics.drawImage(image, scrittaX, puntiY, null);
		int puntiEsperienza = Statistiche.getPuntiEsperienza();
		int puntiPerProssimoLivello = Statistiche.getPuntiEsperienzaPerProssimoLivello();
		image = ImageCache.get(puntiEsperienza + "/" + puntiPerProssimoLivello, fontMedium, coloreTestata);
		graphics.drawImage(image, totaleX - image.getWidth(null), puntiY, null);
	}

	private void gestisciEventoVariazioneGemme(NotificaVariazioneDisponibilitaGemme evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneGemme(
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneGemme(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteGemma;
		return new SpriteATempo(icona, variazione, fontMedium, totaleX, gemmeY, "Gemme variate");
	}

	private void gestisciEventoVariazioneMonete(NotificaVariazioneDisponibilitaMonete evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneMonete(
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneMonete(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMoneta;
		return new SpriteATempo(icona, variazione, fontMedium, totaleX, moneteY, "Monete variate");
	}

	private void gestisciEventoVariazionePuntiEsperienza(NotificaVariazionePuntiEsperienzaPersonaggio evento) {
		SpriteATempo sprite = costruisciSpritePerVariazionePuntiEsperienza(
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazionePuntiEsperienza(int variazione) {
		if (variazione == 0) {
			return null;
		}
		return new SpriteATempo(null, variazione, fontMedium, totaleX, puntiY, "Punti esperienza variati");
	}
}
