package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I personaggi nel riquadro della locazione: lo spazio si divide in quattro, il gruppo si dispone attorno alla terza
 * colonna e gli avversari attorno alla prima, con il personaggio centrale (o lo spazio fra i due centrali) sulla colonna;
 * una fila che non ci sta occupa tutta la sua metà, con i centri a passo uguale; chi è più vicino al centro sta più in
 * alto.
 */
class DisposizioneNellaLocazioneTest {

	private static Misura immagine(int larghezza, int altezza) {
		return new Misura(larghezza, altezza);
	}

	// Lo spazio a disposizione e lo spazio libero in mezzo (un cofano): le colonne stanno a 300 e a 900, ogni fila ha uno
	// spazio largo 600 - 70 = 530, che comincia a 670 (il gruppo) o finisce a 530 (gli avversari)
	private static final int LARGHEZZA = 1200;
	private static final int CENTRO = 140;
	private static final int D = DisposizioneNellaLocazione.DISTANZA_FRA_PERSONAGGI;

	private static List<Misura> immagini(int numero, int larghezza) {
		List<Misura> immagini = new ArrayList<>();
		for (int i = 0; i < numero; i++) {
			immagini.add(immagine(larghezza, 140));
		}
		return immagini;
	}

	@Test
	void unGruppoDispariCheCiStaHaIlPersonaggioCentraleSullaTerzaColonna() {
		List<Punto> posizioni = DisposizioneNellaLocazione.disponiGruppo(100, LARGHEZZA, CENTRO, 320, immagini(3, 60));
		int colonna = 100 + LARGHEZZA * 3 / 4;
		assertEquals(colonna - 30, posizioni.get(1).x, "il centrale è centrato sulla colonna");
		assertEquals(posizioni.get(1).x - D - 60, posizioni.get(0).x, "il primo, a sinistra, verso il centro");
		assertEquals(posizioni.get(1).x + 60 + D, posizioni.get(2).x);
		// Più si va verso il bordo, più si scende
		assertTrue(posizioni.get(1).y > posizioni.get(0).y);
		assertTrue(posizioni.get(2).y > posizioni.get(1).y);
		assertEquals(Finestra.SPACING + 320 - 6, posizioni.get(2).y + 140);
		assertEquals(Finestra.SPACING + 320 - 3 * 6, posizioni.get(0).y + 140);
	}

	@Test
	void unGruppoPariCheCiStaHaLaColonnaFraIDueCentrali() {
		List<Punto> posizioni = DisposizioneNellaLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini(4, 100));
		int colonna = LARGHEZZA * 3 / 4;
		assertEquals(colonna - D / 2 - 100, posizioni.get(1).x);
		assertEquals(colonna + D / 2, posizioni.get(2).x);
		assertEquals(posizioni.get(1).x - D - 100, posizioni.get(0).x);
	}

	@Test
	void unoSoloStaCentratoSullaSuaColonna() {
		assertEquals(LARGHEZZA * 3 / 4 - 30,
				DisposizioneNellaLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini(1, 60)).get(0).x);
		assertEquals(LARGHEZZA / 4 - 30,
				DisposizioneNellaLocazione.disponiAvversari(0, LARGHEZZA, CENTRO, 320, immagini(1, 60)).get(0).x);
	}

	@Test
	void gliAvversariCheCiStannoSonoAttornoAllaPrimaColonnaIlPrimoVersoIlCentro() {
		List<Punto> posizioni = DisposizioneNellaLocazione.disponiAvversari(0, LARGHEZZA, CENTRO, 320, immagini(3, 60));
		assertEquals(LARGHEZZA / 4 - 30, posizioni.get(1).x);
		assertEquals(posizioni.get(1).x + 60 + D, posizioni.get(0).x, "il primo è più vicino al centro");
		assertEquals(posizioni.get(1).x - D - 60, posizioni.get(2).x);
		assertTrue(posizioni.get(1).y > posizioni.get(0).y, "più si va verso il bordo, più si scende");
	}

	@Test
	void unGruppoCheNonCiStaSiDisponePerIlCentroAPassoUguale() {
		// Otto larghi 100 (più gli spazi) non stanno in 530: il passo è (530 - 50 - 50) / 7
		List<Punto> posizioni = DisposizioneNellaLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini(8, 100));
		for (int i = 0; i < 8; i++) {
			assertEquals(Math.round(670 + 50 + i * 430 / 7.0d), posizioni.get(i).x + 50, "il centro del personaggio " + i);
		}
		assertEquals(LARGHEZZA, posizioni.get(7).x + 100, "l'ultimo arriva al bordo");
	}

	@Test
	void gliAvversariCheNonCiStannoSiDispongonoASpecchio() {
		List<Punto> posizioni = DisposizioneNellaLocazione.disponiAvversari(0, LARGHEZZA, CENTRO, 320, immagini(8, 100));
		for (int i = 0; i < 8; i++) {
			assertEquals(Math.round(530 - 50 - i * 430 / 7.0d), posizioni.get(i).x + 50, "il centro dell'avversario " + i);
		}
		assertEquals(0, posizioni.get(7).x, "l'ultimo arriva al bordo");
	}

	@Test
	void conImmaginiDiverseContanoLaMetaDelPrimoEDellUltimo() {
		// Il primo largo 300 (braccia aperte), l'ultimo largo 100, sei in tutto: passo (530 - 150 - 50) / 5 = 66, a centri
		// uguali qualunque sia la larghezza di quelli in mezzo
		List<Misura> immagini = Arrays.asList(immagine(300, 140), immagine(60, 140), immagine(200, 140), immagine(60, 140),
				immagine(60, 140), immagine(100, 140));
		List<Punto> posizioni = DisposizioneNellaLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini);
		for (int i = 0; i < 6; i++) {
			assertEquals(670 + 150 + i * 66, posizioni.get(i).x + immagini.get(i).larghezza / 2, "il centro del personaggio " + i);
		}
	}

	@Test
	void unoSoloPiuLargoDellaSuaMetaStaComunqueSullaColonna() {
		assertEquals(LARGHEZZA * 3 / 4 - 300,
				DisposizioneNellaLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, immagini(1, 600)).get(0).x);
	}

	@Test
	void unGruppoVuotoNonHaPosizioni() {
		assertTrue(DisposizioneNellaLocazione.disponiGruppo(0, LARGHEZZA, CENTRO, 320, Collections.emptyList()).isEmpty());
		assertTrue(DisposizioneNellaLocazione.disponiAvversari(0, LARGHEZZA, CENTRO, 320, Collections.emptyList()).isEmpty());
	}

	@Test
	void unoSpriteATempoNonSiSovrapponeAQuelliVicini() {
		int passo = 30;
		// Nessuno prima: parte dove deve
		assertEquals(100, DisposizioneNellaLocazione.primoPostoLibero(Collections.emptyList(), 200, 100, passo));
		// Uno già lì: il secondo parte più in basso, il terzo ancora più in basso
		List<Punto> occupati = Collections.singletonList(new Punto(200, 100));
		assertEquals(130, DisposizioneNellaLocazione.primoPostoLibero(occupati, 200, 100, passo));
		occupati = Arrays.asList(new Punto(200, 100), new Punto(200, 130));
		assertEquals(160, DisposizioneNellaLocazione.primoPostoLibero(occupati, 200, 100, passo));
		// Anche se è un po' più a lato, finché è vicino (un compagno dietro, 20 px più in là): parte subito sotto di lui
		assertEquals(124, DisposizioneNellaLocazione.primoPostoLibero(Collections.singletonList(new Punto(220, 94)), 200, 100, passo));
	}

	@Test
	void glISpriteLontaniODistantiInVerticaleNonSiSpostano() {
		int passo = 30;
		// Troppo lontano in orizzontale
		assertEquals(100, DisposizioneNellaLocazione.primoPostoLibero(Collections.singletonList(new Punto(400, 100)), 200, 100, passo));
		// Già abbastanza più in basso, o più in alto, di un passo intero
		assertEquals(100, DisposizioneNellaLocazione.primoPostoLibero(Collections.singletonList(new Punto(200, 130)), 200, 100, passo));
		assertEquals(100, DisposizioneNellaLocazione.primoPostoLibero(Collections.singletonList(new Punto(200, 70)), 200, 100, passo));
	}
}
