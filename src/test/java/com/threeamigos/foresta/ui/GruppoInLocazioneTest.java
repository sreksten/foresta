package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il gruppo del giocatore nel riquadro della locazione: a destra, il capo più in basso e vicino al bordo, gli altri
 * verso il centro e un poco più in alto.
 */
class GruppoInLocazioneTest {

	private static BufferedImage immagine(int larghezza, int altezza) {
		return new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
	}

	@Test
	void ilCapoStaInBassoAdestraEGliAltriSalgonoVersoIlCentro() {
		List<BufferedImage> immagini = Arrays.asList(immagine(60, 140), immagine(60, 140), immagine(60, 140));
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(100, 390, 320, immagini);
		assertEquals(3, posizioni.size());
		for (int i = 1; i < posizioni.size(); i++) {
			assertTrue(posizioni.get(i).x < posizioni.get(i - 1).x, "ogni personaggio è più verso il centro del precedente");
			assertTrue(posizioni.get(i).y < posizioni.get(i - 1).y, "ogni personaggio è più in alto del precedente");
		}
		// Il capo non esce dalla locazione a destra e poggia in basso
		Point capo = posizioni.get(0);
		assertTrue(capo.x + 60 <= 100 + 390);
		assertTrue(capo.y + 140 <= ImageCache.SPACING + 320);
	}

	@Test
	void ilPassoELoStessoPerImmaginiDiMisureDiverse() {
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(0, 390, 320,
				Arrays.asList(immagine(112, 142), immagine(56, 138)));
		// Il bordo destro di ognuno arretra sempre dello stesso passo; il fondo sale sempre dello stesso passo
		int bordoDestroCapo = posizioni.get(0).x + 112;
		int bordoDestroSecondo = posizioni.get(1).x + 56;
		assertEquals(40, bordoDestroCapo - bordoDestroSecondo);
		int fondoCapo = posizioni.get(0).y + 142;
		int fondoSecondo = posizioni.get(1).y + 138;
		assertEquals(6, fondoCapo - fondoSecondo);
	}

	@Test
	void unGruppoVuotoNonHaPosizioni() {
		assertTrue(DisplayableCanvasRiquadroLocazione.disponiGruppo(0, 390, 320, Arrays.<BufferedImage>asList()).isEmpty());
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
		assertEquals(100, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Arrays.<Point>asList(), 200, 100, passo));
		// Uno già lì: il secondo parte più in basso, il terzo ancora più in basso
		List<Point> occupati = Arrays.asList(new Point(200, 100));
		assertEquals(130, DisplayableCanvasRiquadroLocazione.primoPostoLibero(occupati, 200, 100, passo));
		occupati = Arrays.asList(new Point(200, 100), new Point(200, 130));
		assertEquals(160, DisplayableCanvasRiquadroLocazione.primoPostoLibero(occupati, 200, 100, passo));
		// Anche se è un po' più a lato, finché è vicino (un compagno dietro, 20 px più in là): parte subito sotto di lui
		assertEquals(124, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Arrays.asList(new Point(220, 94)), 200, 100, passo));
	}

	@Test
	void glISpriteLontaniODistantiInVerticaleNonSiSpostano() {
		int passo = 30;
		// Troppo lontano in orizzontale
		assertEquals(100, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Arrays.asList(new Point(400, 100)), 200, 100, passo));
		// Già abbastanza più in basso, o più in alto, di un passo intero
		assertEquals(100, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Arrays.asList(new Point(200, 130)), 200, 100, passo));
		assertEquals(100, DisplayableCanvasRiquadroLocazione.primoPostoLibero(Arrays.asList(new Point(200, 70)), 200, 100, passo));
	}
}
