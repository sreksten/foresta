package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DisplayableCanvasRiquadroLocazione {

	private final int topLeftX;
	private final int topLeftY;

	private final Map<Personaggio, CoordinateMD> mappaCoordinate = new HashMap<>();
	
	DisplayableCanvasRiquadroLocazione(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
	}
	
	void assegnaCoordinateAgliAvversari() {
		mappaCoordinate.clear();
		GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();
		int i = 0;
		for (Personaggio personaggioCorrente : gruppoAvversario.getPersonaggi()) {
			BufferedImage d = personaggioCorrente.getImmagine();
			CoordinateMD coordinate = new CoordinateMD(topLeftX + i++ * 20 + Dado.tira(10),
					ImageCache.SPACING + ImageCache.locazioni.get(ClassiLocazione.BOSCO).getHeight() - i * 6 - d.getHeight());
			mappaCoordinate.put(personaggioCorrente, coordinate);
		}
	}

	void disegnaLocazione(Graphics2D graphics) {
		GruppoGiocatore g = GruppoGiocatore.getIstanza();
		GruppoAvversario gng = GruppoAvversario.getIstanza();
		ClassiLocazione classeLocazione = g.getClasseLocazioneCorrente();
		BufferedImage locazione = ImageCache.locazioni.get(classeLocazione);
		int locXOffset = topLeftX;
		graphics.drawImage(locazione, locXOffset, topLeftY, null);

		// Lista invertita
		List<Personaggio> avversariDaDisegnare = new ArrayList<>();
		gng.getPersonaggiVivi().forEach(p -> avversariDaDisegnare.add(0, p));

		for (Personaggio personaggioCorrente : avversariDaDisegnare) {
			BufferedImage d = personaggioCorrente.getImmagine();
			CoordinateMD coordinate = mappaCoordinate.get(personaggioCorrente);
			graphics.drawImage(d, coordinate.getX(), coordinate.getY(), null);
		}

		Oggetto oggetto = classeLocazione.getIstanza().getOggetto();
		if (oggetto != null && oggetto.getClasse() != ClassiOggetto.ARTEFATTO) {
			BufferedImage d = oggetto.getClasse().getImmagine();
			graphics.drawImage(d, locXOffset + locazione.getWidth() - d.getWidth() - 5, ImageCache.SPACING + locazione.getHeight() - d.getHeight() - 5, null);
		}
	}
	
	SpriteInterface notificaMorte(Personaggio personaggio) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		return new SpriteInDissolvenza(personaggio.getImmagine(), coordinate.getX(), coordinate.getY());
	}
	
	SpriteInterface variaSalute(Personaggio personaggio, int variazione) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		return new SpriteATempo(ImageCache.spriteCombattimento, variazione, DoomdarkFontMedium.getInstance(), coordinate.getX() + personaggio.getImmagine().getWidth(), coordinate.getY());
	}
	
	SpriteInterface variaMagia(Personaggio personaggio, int variazione) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		return new SpriteATempo(ImageCache.spriteMagia, variazione, DoomdarkFontMedium.getInstance(), coordinate.getX() + personaggio.getImmagine().getWidth(), coordinate.getY());
	}
	
	SpriteInterface raccogliOggetto() {
		ClassiLocazione classeLocazione = GruppoGiocatore.getIstanza().getClasseLocazioneCorrente();
		Locazione l = classeLocazione.getIstanza();
		BufferedImage locazione = ImageCache.locazioni.get(classeLocazione);
		Oggetto oggetto = l.getOggetto();
		if (oggetto != null && oggetto.getClasse() != ClassiOggetto.ARTEFATTO) {
			BufferedImage d = oggetto.getClasse().getImmagine();
			return new SpriteATempo(d, topLeftX + locazione.getWidth() - d.getWidth() - 5, ImageCache.SPACING + locazione.getHeight() - d.getHeight() - 5);
		}
		return null;
	}
}
