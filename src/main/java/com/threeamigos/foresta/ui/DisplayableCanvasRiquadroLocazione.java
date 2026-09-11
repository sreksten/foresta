package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DisplayableCanvasRiquadroLocazione implements Finestra {

	private static final int SOLLEVAMENTO_ANCORA = 8;
	private static final int SCOSTAMENTO_SOVRAPPOSIZIONE = 20;

	private final int topLeftX;
	private final int topLeftY;

	private final Map<Personaggio, CoordinateMD> mappaCoordinate = new HashMap<>();
	private final Map<Personaggio, BufferedImage> mappaImmagini = new HashMap<>();
	private final List<EffettoAttivo> effettiAttivi = new ArrayList<>();

	private static final class EffettoAttivo {
		final int yIniziale;
		final SpriteEffetto sprite;
		EffettoAttivo(int yIniziale, SpriteEffetto sprite) {
			this.yIniziale = yIniziale;
			this.sprite = sprite;
		}
	}

	DisplayableCanvasRiquadroLocazione(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
	}

	void assegnaCoordinateAgliAvversari() {
		mappaCoordinate.clear();
		mappaImmagini.clear();
		effettiAttivi.clear();
		GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();
		int i = 0;
		for (Personaggio personaggioCorrente : gruppoAvversario.getPersonaggi()) {
			BufferedImage d = ClassePersonaggioImmagine.getImmagine(personaggioCorrente.getClasse());
			mappaImmagini.put(personaggioCorrente, d);
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
			BufferedImage d = mappaImmagini.get(personaggioCorrente);
			CoordinateMD coordinate = mappaCoordinate.get(personaggioCorrente);
			graphics.drawImage(d, coordinate.getX(), coordinate.getY(), null);
		}

		Oggetto oggetto = g.getLocazioneCorrente().getOggetto();
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
		return new SpriteInDissolvenza(mappaImmagini.get(personaggio), coordinate.getX(), coordinate.getY());
	}

	SpriteInterface variaLivello(Personaggio personaggio, int variazione) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		return new SpriteATempo(ImageCache.spriteAumentoLivello, variazione, DoomdarkFontMedium.getInstance(), coordinate.getX() + mappaImmagini.get(personaggio).getWidth(), coordinate.getY());
	}
	
	SpriteInterface variaSalute(Personaggio personaggio, int variazione) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		return new SpriteATempo(ImageCache.spriteCombattimento, variazione, DoomdarkFontMedium.getInstance(), coordinate.getX() + mappaImmagini.get(personaggio).getWidth(), coordinate.getY());
	}
	
	SpriteInterface variaMagia(Personaggio personaggio, int variazione) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		return new SpriteATempo(ImageCache.spriteMagia, variazione, DoomdarkFontMedium.getInstance(), coordinate.getX() + mappaImmagini.get(personaggio).getWidth(), coordinate.getY());
	}
	
	SpriteInterface raccogliOggetto() {
		GruppoGiocatore g = GruppoGiocatore.getIstanza();
		BufferedImage locazione = ImageCache.locazioni.get(g.getClasseLocazioneCorrente());
		Oggetto oggetto = g.getLocazioneCorrente().getOggetto();
		if (oggetto != null && oggetto.getClasse() != ClassiOggetto.ARTEFATTO) {
			BufferedImage d = oggetto.getClasse().getImmagine();
			return new SpriteATempo(d, topLeftX + locazione.getWidth() - d.getWidth() - 5, ImageCache.SPACING + locazione.getHeight() - d.getHeight() - 5);
		}
		return null;
	}

	SpriteInterface aggiungiEffettoDiStato(Personaggio personaggio, TipoEffettoDiStato effettoDiStato) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}

		BufferedImage image = mappaImmagini.get(personaggio);
		int x = coordinate.getX() + image.getWidth();
		int yIniziale = coordinate.getY() + image.getHeight() / 3 - SOLLEVAMENTO_ANCORA;
		SpriteEffetto sprite = new SpriteEffetto(effettoDiStato.getDescrizione(), DoomdarkFontMedium.getInstance(),
				DoomdarkColorModel.Color.YELLOW, x, yIniziale + calcolaOffsetVerticale(yIniziale));
		effettiAttivi.add(new EffettoAttivo(yIniziale, sprite));
		return sprite;
	}

	SpriteInterface aggiungiInterazioneElementale(Personaggio personaggio, TipoInterazioneElementale interazioneElementale) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		BufferedImage image = mappaImmagini.get(personaggio);
		int x = coordinate.getX() + image.getWidth();
		int yIniziale = coordinate.getY() + image.getHeight() * 2 / 3 - SOLLEVAMENTO_ANCORA;
		SpriteEffetto sprite = new SpriteEffetto(interazioneElementale.getDescrizione(), DoomdarkFontMedium.getInstance(),
				DoomdarkColorModel.Color.GREEN, x, yIniziale + calcolaOffsetVerticale(yIniziale));
		effettiAttivi.add(new EffettoAttivo(yIniziale, sprite));
		return sprite;
	}

	private int calcolaOffsetVerticale(int yIniziale) {
		effettiAttivi.removeIf(e -> !e.sprite.isActive());
		long occupati = effettiAttivi.stream()
				.filter(e -> Math.abs(e.yIniziale - yIniziale) < SCOSTAMENTO_SOVRAPPOSIZIONE)
				.count();
		if (occupati == 0) {
			return 0;
		}
		int passo = (int) ((occupati + 1) / 2);
		int segno = (occupati % 2 == 1) ? 1 : -1;
		return segno * passo * SCOSTAMENTO_SOVRAPPOSIZIONE;
	}
}
