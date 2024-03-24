package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Random;

class DisplayableCanvasRiquadroLocazione {

	private int topLeftX;
	private int topLeftY;

	private Map<Personaggio, CoordinateMD> mappaCoordinate = new HashMap<>();
	
	DisplayableCanvasRiquadroLocazione(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
	}
	
	void assegnaCoordinateAgliAvversari() {
		mappaCoordinate.clear();
		GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();
		int personaggi = gruppoAvversario.getNumeroPersonaggi();
		BufferedImage d;
		Personaggio personaggioCorrente;
		for (int i = personaggi - 1; i >= 0; i--) {
			personaggioCorrente = gruppoAvversario.getPersonaggio(i);
			d = personaggioCorrente.getImmagine();
			mappaCoordinate.put(personaggioCorrente, new CoordinateMD(topLeftX + i * 20 + Random.getInt(10),
					ImageCache.SPACING + ImageCache.locazioni.get(ClassiLocazione.BOSCO).getHeight() - i * 6 - d.getHeight()));
		}

	}

	void disegnaLocazione(Graphics2D graphics) {
		GruppoGiocatore g = GruppoGiocatore.getIstanza();
		GruppoAvversario gng = GruppoAvversario.getIstanza();
		ClassiLocazione classeLocazione = g.getClasseLocazione();
		BufferedImage locazione = ImageCache.locazioni.get(classeLocazione);
		int locXOffset = topLeftX;
		graphics.drawImage(locazione, locXOffset, topLeftY, null);

		int personaggi = gng.getNumeroPersonaggi();
		BufferedImage d;
		Personaggio personaggioCorrente;
		for (int i = personaggi - 1; i >= 0; i--) {
			personaggioCorrente = gng.getPersonaggio(i);
			if (personaggioCorrente.isVivo()) {
				d = personaggioCorrente.getImmagine();
				CoordinateMD coordinate = mappaCoordinate.get(personaggioCorrente);
				graphics.drawImage(d, coordinate.getX(), coordinate.getY(), null);
			}
		}

		Oggetto oggetto = classeLocazione.getIstanza().getOggetto();
		if (oggetto != null && oggetto.getClasse() != ClassiOggetto.ARTEFATTO) {
			d = oggetto.getClasse().getImmagine();
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
	
	SpriteInterface variaForza(Personaggio personaggio, int variazione) {
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
		ClassiLocazione classeLocazione = GruppoGiocatore.getIstanza().getClasseLocazione();
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
