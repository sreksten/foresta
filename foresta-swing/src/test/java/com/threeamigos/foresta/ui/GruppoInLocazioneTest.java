package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.VersiDeiPersonaggi;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroLocazione;

/**
 * Il gruppo del giocatore nel riquadro della locazione: a destra, il capo più in basso e vicino al bordo, gli altri
 * verso il centro, senza sovrapporsi, e un poco più in alto.
 */
class GruppoInLocazioneTest {

	private static BufferedImage immagine(int larghezza, int altezza) {
		return new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
	}

	private static final int LARGHEZZA = 600;
	private static final int OGGETTO = 40;

	@Test
	void ilCapoStaAccantoAllOggettoEGliAltriVannoVersoDestraEInBasso() {
		List<BufferedImage> immagini = Arrays.asList(immagine(60, 140), immagine(60, 140), immagine(60, 140));
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(100, LARGHEZZA, OGGETTO, 320, immagini);
		assertEquals(3, posizioni.size());
		// Il capo parte a destra dell'oggetto, che sta al centro dello spazio, a poca distanza
		assertEquals(100 + (LARGHEZZA + OGGETTO) / 2 + DisplayableCanvasRiquadroLocazione.STACCO_DALL_OGGETTO, posizioni.get(0).x);
		for (int i = 1; i < posizioni.size(); i++) {
			assertTrue(posizioni.get(i).x > posizioni.get(i - 1).x, "ogni personaggio è più verso il bordo del precedente");
			assertTrue(posizioni.get(i).y > posizioni.get(i - 1).y, "ogni personaggio è più in basso del precedente");
		}
		// L'ultimo non esce dallo spazio e poggia un passo sopra il fondo
		Point ultimo = posizioni.get(2);
		assertTrue(ultimo.x + 60 <= 100 + LARGHEZZA);
		assertEquals(ImageCache.SPACING + 320 - 6, ultimo.y + 140);
		// Il primo parte da tanti passi dal fondo quanti sono i personaggi
		assertEquals(ImageCache.SPACING + 320 - 3 * 6, posizioni.get(0).y + 140);
	}

	@Test
	void gliAvversariSpecchianoIlGruppoASinistraDellOggetto() {
		List<BufferedImage> immagini = Arrays.asList(immagine(60, 140), immagine(60, 140));
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiAvversari(100, LARGHEZZA, OGGETTO, 320, immagini);
		assertEquals(100 + (LARGHEZZA - OGGETTO) / 2 - DisplayableCanvasRiquadroLocazione.STACCO_DALL_OGGETTO,
				posizioni.get(0).x + 60);
		assertTrue(posizioni.get(1).x < posizioni.get(0).x);
		assertTrue(posizioni.get(1).y > posizioni.get(0).y);
	}

	@Test
	void ognunoStaSubitoDopoIlPrecedenteSenzaSovrapporsi() {
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(0, LARGHEZZA, OGGETTO, 320,
				Arrays.asList(immagine(112, 142), immagine(56, 138)));
		assertEquals(DisplayableCanvasRiquadroLocazione.DISTANZA_FRA_PERSONAGGI, posizioni.get(1).x - (posizioni.get(0).x + 112));
	}

	@Test
	void ilGruppoNonEsceDalloSpazioEAccorciaLeDistanze() {
		List<BufferedImage> immagini = Arrays.asList(immagine(60, 140), immagine(60, 140), immagine(60, 140), immagine(60, 140),
				immagine(60, 140), immagine(60, 140), immagine(60, 140), immagine(60, 140));
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(100, LARGHEZZA, OGGETTO, 320, immagini);
		for (Point posizione : posizioni) {
			assertTrue(posizione.x + 60 <= 100 + LARGHEZZA, "nessuno esce a destra");
		}
	}

	@Test
	void unGruppoVuotoNonHaPosizioni() {
		assertTrue(DisplayableCanvasRiquadroLocazione.disponiGruppo(0, LARGHEZZA, OGGETTO, 320, Collections.emptyList()).isEmpty());
	}

	@Test
	void specchiareRovesciaLeColonneESpecchiandoDueVolteSiTornaAllOriginale() {
		BufferedImage originale = immagine(3, 2);
		originale.setRGB(0, 0, 0xFFFF0000);
		originale.setRGB(2, 1, 0xFF00FF00);
		BufferedImage specchiata = VersiDeiPersonaggi.specchia(originale);
		assertEquals(0xFFFF0000, specchiata.getRGB(2, 0));
		assertEquals(0xFF00FF00, specchiata.getRGB(0, 1));
		assertEquals(0, specchiata.getRGB(0, 0));
		BufferedImage ritorno = VersiDeiPersonaggi.specchia(specchiata);
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 3; x++) {
				assertEquals(originale.getRGB(x, y), ritorno.getRGB(x, y));
			}
		}
	}

	@Test
	void unoSpriteATempoNonSiSovrapponeAQuelliVicini() {
		int passo = 30;
		// Nessuno prima: parte dove deve
		assertEquals(100, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Collections.emptyList(), 200, 100, passo));
		// Uno già lì: il secondo parte più in basso, il terzo ancora più in basso
		List<Point> occupati = Collections.singletonList(new Point(200, 100));
		assertEquals(130, DisplayableCanvasRiquadroLocazione.primoPostoLibero(occupati, 200, 100, passo));
		occupati = Arrays.asList(new Point(200, 100), new Point(200, 130));
		assertEquals(160, DisplayableCanvasRiquadroLocazione.primoPostoLibero(occupati, 200, 100, passo));
		// Anche se è un po' più a lato, finché è vicino (un compagno dietro, 20 px più in là): parte subito sotto di lui
		assertEquals(124, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Collections.singletonList(new Point(220, 94)), 200, 100, passo));
	}

	@Test
	void glISpriteLontaniODistantiInVerticaleNonSiSpostano() {
		int passo = 30;
		// Troppo lontano in orizzontale
		assertEquals(100, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Collections.singletonList(new Point(400, 100)), 200, 100, passo));
		// Già abbastanza più in basso, o più in alto, di un passo intero
		assertEquals(100, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Collections.singletonList(new Point(200, 130)), 200, 100, passo));
		assertEquals(100, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Collections.singletonList(new Point(200, 70)), 200, 100, passo));
	}
}
