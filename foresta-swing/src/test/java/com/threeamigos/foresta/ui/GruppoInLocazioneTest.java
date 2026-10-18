package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.VersiDeiPersonaggi;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroLocazione;

/**
 * I personaggi nel riquadro della locazione: lo spazio si divide in quattro, il gruppo si dispone attorno alla terza
 * colonna e gli avversari attorno alla prima, con il personaggio centrale (o lo spazio fra i due centrali) sulla colonna;
 * una fila che non ci sta occupa tutta la sua metà, con i centri a passo uguale; chi è più vicino al centro sta più in
 * alto.
 */
class GruppoInLocazioneTest {

	private static BufferedImage immagine(int larghezza, int altezza) {
		return new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
	}

	// Lo spazio a disposizione e lo spazio libero in mezzo (un cofano): le colonne stanno a 300 e a 900, ogni fila ha uno
	// spazio largo 600 - 70 = 530, che comincia a 670 (il gruppo) o finisce a 530 (gli avversari)
	private static final int LARGHEZZA = 1200;
	private static final int CENTRO = 140;
	private static final int D = DisplayableCanvasRiquadroLocazione.DISTANZA_FRA_PERSONAGGI;

	private static List<BufferedImage> immagini(int numero, int larghezza) {
		List<BufferedImage> immagini = new ArrayList<>();
		for (int i = 0; i < numero; i++) {
			immagini.add(immagine(larghezza, 140));
		}
		return immagini;
	}

	@Test
	void unGruppoDispariCheCiStaHaIlPersonaggioCentraleSullaTerzaColonna() {
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(100, LARGHEZZA, CENTRO, 320, immagini(3, 60));
		int colonna = 100 + LARGHEZZA * 3 / 4;
		assertEquals(colonna - 30, posizioni.get(1).x, "il centrale è centrato sulla colonna");
		assertEquals(posizioni.get(1).x - D - 60, posizioni.get(0).x, "il primo, a sinistra, verso il centro");
		assertEquals(posizioni.get(1).x + 60 + D, posizioni.get(2).x);
		// Più si va verso il bordo, più si scende
		assertTrue(posizioni.get(1).y > posizioni.get(0).y);
		assertTrue(posizioni.get(2).y > posizioni.get(1).y);
		assertEquals(ImageCache.SPACING + 320 - 6, posizioni.get(2).y + 140);
		assertEquals(ImageCache.SPACING + 320 - 3 * 6, posizioni.get(0).y + 140);
	}

	@Test
	void unGruppoPariCheCiStaHaLaColonnaFraIDueCentrali() {
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini(4, 100));
		int colonna = LARGHEZZA * 3 / 4;
		assertEquals(colonna - D / 2 - 100, posizioni.get(1).x);
		assertEquals(colonna + D / 2, posizioni.get(2).x);
		assertEquals(posizioni.get(1).x - D - 100, posizioni.get(0).x);
	}

	@Test
	void unoSoloStaCentratoSullaSuaColonna() {
		assertEquals(LARGHEZZA * 3 / 4 - 30,
				DisplayableCanvasRiquadroLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini(1, 60)).get(0).x);
		assertEquals(LARGHEZZA / 4 - 30,
				DisplayableCanvasRiquadroLocazione.disponiAvversari(0, LARGHEZZA, CENTRO, 320, immagini(1, 60)).get(0).x);
	}

	@Test
	void gliAvversariCheCiStannoSonoAttornoAllaPrimaColonnaIlPrimoVersoIlCentro() {
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiAvversari(0, LARGHEZZA, CENTRO, 320, immagini(3, 60));
		assertEquals(LARGHEZZA / 4 - 30, posizioni.get(1).x);
		assertEquals(posizioni.get(1).x + 60 + D, posizioni.get(0).x, "il primo è più vicino al centro");
		assertEquals(posizioni.get(1).x - D - 60, posizioni.get(2).x);
		assertTrue(posizioni.get(1).y > posizioni.get(0).y, "più si va verso il bordo, più si scende");
	}

	@Test
	void unGruppoCheNonCiStaSiDisponePerIlCentroAPassoUguale() {
		// Otto larghi 100 (più gli spazi) non stanno in 530: il passo è (530 - 50 - 50) / 7
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini(8, 100));
		for (int i = 0; i < 8; i++) {
			assertEquals(Math.round(670 + 50 + i * 430 / 7.0d), posizioni.get(i).x + 50, "il centro del personaggio " + i);
		}
		assertEquals(LARGHEZZA, posizioni.get(7).x + 100, "l'ultimo arriva al bordo");
	}

	@Test
	void gliAvversariCheNonCiStannoSiDispongonoASpecchio() {
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiAvversari(0, LARGHEZZA, CENTRO, 320, immagini(8, 100));
		for (int i = 0; i < 8; i++) {
			assertEquals(Math.round(530 - 50 - i * 430 / 7.0d), posizioni.get(i).x + 50, "il centro dell'avversario " + i);
		}
		assertEquals(0, posizioni.get(7).x, "l'ultimo arriva al bordo");
	}

	@Test
	void conImmaginiDiverseContanoLaMetaDelPrimoEDellUltimo() {
		// Il primo largo 300 (braccia aperte), l'ultimo largo 100, sei in tutto: passo (530 - 150 - 50) / 5 = 66, a centri
		// uguali qualunque sia la larghezza di quelli in mezzo
		List<BufferedImage> immagini = Arrays.asList(immagine(300, 140), immagine(60, 140), immagine(200, 140), immagine(60, 140),
				immagine(60, 140), immagine(100, 140));
		List<Point> posizioni = DisplayableCanvasRiquadroLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini);
		for (int i = 0; i < 6; i++) {
			assertEquals(670 + 150 + i * 66, posizioni.get(i).x + immagini.get(i).getWidth() / 2, "il centro del personaggio " + i);
		}
	}

	@Test
	void unoSoloPiuLargoDellaSuaMetaStaComunqueSullaColonna() {
		assertEquals(LARGHEZZA * 3 / 4 - 300,
				DisplayableCanvasRiquadroLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini(1, 600)).get(0).x);
	}

	@Test
	void unGruppoVuotoNonHaPosizioni() {
		assertTrue(DisplayableCanvasRiquadroLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, Collections.emptyList()).isEmpty());
		assertTrue(DisplayableCanvasRiquadroLocazione.disponiAvversari(0, LARGHEZZA, CENTRO, 320, Collections.emptyList()).isEmpty());
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
