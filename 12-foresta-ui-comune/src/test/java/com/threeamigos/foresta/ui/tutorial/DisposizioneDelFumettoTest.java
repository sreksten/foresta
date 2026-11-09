package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Punto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisposizioneDelFumettoTest {

	@Test
	void ilFumettoStaSempreAlCentroQualunqueSiaIlBersaglio() {
		Punto fisso = DisposizioneDelFumetto.posizioneFissa(300, 120, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertEquals((LARGHEZZA_SCHERMO - 300) / 2, fisso.x);
		assertEquals((ALTEZZA_SCHERMO - 120) / 2, fisso.y);
		// Un fumetto più grande dello schermo resta dentro, con il margine
		Punto enorme = DisposizioneDelFumetto.posizioneFissa(LARGHEZZA_SCHERMO + 500, ALTEZZA_SCHERMO + 500, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertEquals(DisposizioneDelFumetto.MARGINE, enorme.x);
		assertEquals(DisposizioneDelFumetto.MARGINE, enorme.y);
	}


	private static final int LARGHEZZA_SCHERMO = 1000;
	private static final int ALTEZZA_SCHERMO = 800;

	@Test
	void unBersaglioInAltoHaIlFumettoSottoEUnoInBassoSopra() {
		Area inAlto = new Area(400, 20, 200, 50);
		Punto sotto = DisposizioneDelFumetto.posizione(inAlto, 300, 120, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertEquals(20 + 50 + DisposizioneDelFumetto.DISTANZA_DAL_BERSAGLIO, sotto.y);
		assertEquals(500 - 150, sotto.x);

		Area inBasso = new Area(400, 700, 200, 50);
		Punto sopra = DisposizioneDelFumetto.posizione(inBasso, 300, 120, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertEquals(700 - DisposizioneDelFumetto.DISTANZA_DAL_BERSAGLIO - 120, sopra.y);
	}

	@Test
	void ilFumettoNonEsceMaiDalloSchermo() {
		Punto aSinistra = DisposizioneDelFumetto.posizione(new Area(0, 100, 20, 20), 300, 120, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertEquals(DisposizioneDelFumetto.MARGINE, aSinistra.x);
		Punto aDestra = DisposizioneDelFumetto.posizione(new Area(980, 100, 20, 20), 300, 120, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertEquals(LARGHEZZA_SCHERMO - 300 - DisposizioneDelFumetto.MARGINE, aDestra.x);
		Punto inCima = DisposizioneDelFumetto.posizione(new Area(400, 790, 20, 10), 300, 700, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertTrue(inCima.y >= DisposizioneDelFumetto.MARGINE);
	}

	@Test
	void unBersaglioAltoStaDiFiancoDallaParteDovECePiuPosto() {
		Area aDestra = new Area(700, 300, 300, 300);
		assertEquals(700 - DisposizioneDelFumetto.DISTANZA_DAL_BERSAGLIO - 300,
				DisposizioneDelFumetto.posizione(aDestra, 300, 120, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO).x);
		Area aSinistra = new Area(10, 300, 300, 300);
		assertEquals(10 + 300 + DisposizioneDelFumetto.DISTANZA_DAL_BERSAGLIO,
				DisposizioneDelFumetto.posizione(aSinistra, 300, 120, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO).x);
	}

	@Test
	void unBersaglioAltoQuantoLoSchermoHaIlFumettoAlSuoFianco() {
		// La colonna di destra, a tutto schermo: non c'è posto sopra né sotto, e il fumetto va a sinistra senza coprirla
		Area colonna = new Area(650, 10, 370, 780);
		Punto p = DisposizioneDelFumetto.posizione(colonna, 330, 320, LARGHEZZA_SCHERMO + 100, ALTEZZA_SCHERMO);
		assertEquals(650 - DisposizioneDelFumetto.DISTANZA_DAL_BERSAGLIO - 330, p.x);
		assertTrue(p.y >= DisposizioneDelFumetto.MARGINE && p.y + 320 <= ALTEZZA_SCHERMO - DisposizioneDelFumetto.MARGINE);
	}

	@Test
	void seSopraESottoNonEntranoVaAlFiancoESeNemmenoLiRestaDentroLoSchermo() {
		// Un fumetto alto quasi come lo schermo: non entra né sopra né sotto un bersaglio basso, e va a sinistra
		Punto aSinistra = DisposizioneDelFumetto.posizione(new Area(400, 100, 200, 20), 300, 700, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertEquals(400 - DisposizioneDelFumetto.DISTANZA_DAL_BERSAGLIO - 300, aSinistra.x);
		assertEquals(DisposizioneDelFumetto.MARGINE, aSinistra.y);
		// Un bersaglio largo e alto come lo schermo meno i margini: nessun fianco libero, il fumetto resta dentro lo schermo
		Punto ovunque = DisposizioneDelFumetto.posizione(new Area(10, 10, 980, 780), 300, 120, LARGHEZZA_SCHERMO, ALTEZZA_SCHERMO);
		assertTrue(ovunque.x >= DisposizioneDelFumetto.MARGINE && ovunque.x + 300 <= LARGHEZZA_SCHERMO - DisposizioneDelFumetto.MARGINE);
	}

	@Test
	void iPulsantiSonoInBassoADestraEStannoDentroIlFumetto() {
		Punto fumetto = new Punto(100, 200);
		Area pollice = DisposizioneDelFumetto.pollice(fumetto, 300, 120, 40);
		Area annulla = DisposizioneDelFumetto.annulla(fumetto, 300, 120, 40);
		assertEquals(100 + 300 - DisposizioneDelFumetto.MARGINE, pollice.getX() + pollice.getLarghezza());
		assertEquals(200 + 120 - DisposizioneDelFumetto.MARGINE, pollice.getY() + pollice.getAltezza());
		assertEquals(pollice.getY(), annulla.getY());
		assertEquals(pollice.getX() - DisposizioneDelFumetto.MARGINE, annulla.getX() + annulla.getLarghezza());
		assertFalse(pollice.contiene(annulla.getX(), annulla.getY()));
	}

	@Test
	void ilFumettoEAbbastanzaLargoEAltoPerIPulsanti() {
		assertEquals(2 * 40 + 3 * DisposizioneDelFumetto.MARGINE, DisposizioneDelFumetto.larghezza(20, 40));
		assertEquals(500, DisposizioneDelFumetto.larghezza(500, 40));
		assertEquals(100 + 40 + DisposizioneDelFumetto.MARGINE, DisposizioneDelFumetto.altezza(100, 40));
	}

	@Test
	void l_unioneContieneTutteEDue() {
		Area unione = DisposizioneDelFumetto.unione(new Area(10, 10, 20, 20), new Area(100, 50, 30, 30));
		assertEquals(10, unione.getX());
		assertEquals(10, unione.getY());
		assertEquals(120, unione.getLarghezza());
		assertEquals(70, unione.getAltezza());
	}

	@Test
	void laCodaVaAlCentroDelBersaglio() {
		Punto punta = DisposizioneDelFumetto.puntaVerso(new Area(100, 200, 40, 20));
		assertEquals(120, punta.x);
		assertEquals(210, punta.y);
	}

	@Test
	void laLarghezzaMassimaEUnTerzoDelloSchermoMaAlmeno300PixelEMaiOltreLoSchermo() {
		assertEquals(400, DisposizioneDelFumetto.larghezzaMassima(1200));
		assertEquals(300, DisposizioneDelFumetto.larghezzaMassima(600));
		assertEquals(250 - 4 * DisposizioneDelFumetto.MARGINE, DisposizioneDelFumetto.larghezzaMassima(250));
	}

	@Test
	void ilBordoStaIntornoAlBersaglioConLoSpessoreDelBordo() {
		java.util.List<Area> segmenti = DisposizioneDelFumetto.segmentiDelBordo(java.util.Collections.singletonList(new Area(10, 20, 30, 40)));
		assertEquals(4, segmenti.size());
		for (Area segmento : segmenti) {
			assertEquals(DisposizioneDelFumetto.SPESSORE_DEL_BORDO, Math.min(segmento.getLarghezza(), segmento.getAltezza()));
		}
	}
}
