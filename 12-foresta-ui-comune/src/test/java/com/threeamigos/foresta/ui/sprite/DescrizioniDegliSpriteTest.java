package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DescrizioniDegliSpriteTest {

	private static final DoomdarkFont FONT = DoomdarkFont.medio();

	@Test
	void laVariazioneHaIlSegnoEIlColoreGiusti() {
		assertEquals("+5", DescrizioneSpriteATempo.testoDellaVariazione(5));
		assertEquals("+0", DescrizioneSpriteATempo.testoDellaVariazione(0));
		assertEquals("-3", DescrizioneSpriteATempo.testoDellaVariazione(-3));
		assertEquals(DoomdarkColorModel.Color.GREEN, DescrizioneSpriteATempo.coloreDellaVariazione(0));
		assertEquals(DoomdarkColorModel.Color.RED, DescrizioneSpriteATempo.coloreDellaVariazione(-1));
	}

	@Test
	void senzaIconaLImmagineEIlTestoPiuDueDiContorno() {
		DescrizioneSpriteATempo d = DescrizioneSpriteATempo.conVariazione(null, null, 7, FONT, 100, 50, false, "x");
		int larghezzaTesto = FontTool.getWidth(FONT, "+7");
		assertEquals(larghezzaTesto + 2, d.getLarghezza());
		assertEquals(FONT.getHeight() + 2, d.getAltezza());
		assertEquals(0, d.getXTesto());
		// Ancorato a destra: l'angolo sinistro sta a sinistra del punto di quanto è larga l'immagine
		assertEquals(100 - d.getLarghezza(), d.getXIniziale(), 1e-6);
		assertEquals(50, d.getYIniziale(), 1e-6);
	}

	@Test
	void conUnIconaPiuAltaDelTestoIlTestoSiCentraInVerticale() {
		Misura icona = new Misura(16, FONT.getHeight() + 12);
		DescrizioneSpriteATempo d = DescrizioneSpriteATempo.conVariazione("i", icona, -2, FONT, 10, 40, true, "x");
		assertEquals(icona.altezza, d.getAltezza());
		assertEquals(0, d.getYIcona());
		assertEquals(5, d.getYTesto());
		assertEquals(17, d.getXTesto());
		assertEquals(16 + FontTool.getWidth(FONT, "-2") + 3, d.getLarghezza());
		// Ancorato a sinistra: parte dal punto, ma sale di quanto il testo scende
		assertEquals(10, d.getXIniziale(), 1e-6);
		assertEquals(40 - 5, d.getYIniziale(), 1e-6);
	}

	@Test
	void conUnIconaPiuBassaDelTestoESiCentraLIcona() {
		Misura icona = new Misura(8, 4);
		DescrizioneSpriteATempo d = DescrizioneSpriteATempo.conVariazione("i", icona, 1, FONT, 0, 0, true, "x");
		assertEquals((FONT.getHeight() + 2 - 4) >> 1, d.getYIcona());
		assertEquals(0, d.getYTesto());
		assertEquals(FONT.getHeight() + 2, d.getAltezza());
	}

	@Test
	void unaImmagineGiaPronta() {
		DescrizioneSpriteATempo d = DescrizioneSpriteATempo.immagine("img", new Misura(20, 30), 5, 6, "mappa");
		assertTrue(d.isSoloImmagine());
		assertEquals(20, d.getLarghezza());
		assertEquals(30, d.getAltezza());
		assertEquals(5, d.getXIniziale(), 1e-6);
		assertEquals(6, d.getYIniziale(), 1e-6);
		assertEquals("mappa", d.getDescrizione());
	}

	@Test
	void ilContornoHaOttoPuntiEEscludeIlCentro() {
		int[][] contorno = DescrizioneSpriteATempo.getContorno();
		assertEquals(8, contorno.length);
		for (int[] p : contorno) {
			assertFalse(p[0] == 1 && p[1] == 1);
		}
	}

	@Test
	void ilCentroEntroTieneLoSpriteDentroOAlCentroSeNonCiStaIn() {
		assertEquals(300f, DescrizioneSpriteEffetto.centroEntro(300f, 100f, 0f, 800f), 1e-6);
		assertEquals(50f, DescrizioneSpriteEffetto.centroEntro(10f, 100f, 0f, 800f), 1e-6);
		assertEquals(750f, DescrizioneSpriteEffetto.centroEntro(790f, 100f, 0f, 800f), 1e-6);
		assertEquals(50f, DescrizioneSpriteEffetto.centroEntro(10f, 200f, 0f, 100f), 1e-6);
	}

	@Test
	void unAnnuncioHaEtichettaEMessaggioCentratiESeparati() {
		DescrizioneAnnuncioGlobale a = new DescrizioneAnnuncioGlobale("Missione", "Il drago", 800, 600, false);
		assertEquals(2, a.getRighe().size());
		assertEquals("Missione", a.getRighe().get(0).getTesto());
		assertEquals(0, a.getRighe().get(0).getY());
		assertTrue(a.getRighe().get(1).getY() > a.getRighe().get(0).getY());
		// La riga più larga sta a sinistra del tutto; l'altra è centrata
		int x0 = a.getRighe().get(0).getX();
		int x1 = a.getRighe().get(1).getX();
		assertEquals(0, Math.min(x0, x1));
		assertFalse(a.isSostituibile());
		assertTrue(new DescrizioneAnnuncioGlobale("a", "b", 800, 600, true).isSostituibile());
	}

	@Test
	void unAnnuncioLungoVaACapoEntroMetaSchermo() {
		DescrizioneAnnuncioGlobale a = new DescrizioneAnnuncioGlobale("Missione", "una frase molto molto molto lunga che non sta su una riga sola", 400, 300, false);
		assertTrue(a.getRighe().size() > 2);
		assertTrue(a.getLarghezza() <= 200 + 40);
	}

	@Test
	void unFumettoVaACapoEHaLeMisureDelleSueRighe() {
		DescrizioneFumetto f = new DescrizioneFumetto("ciao a tutti quanti voi che siete qui", 100, FONT);
		assertTrue(f.getRighe().size() > 1);
		assertEquals(DescrizioneFumetto.SMUSSAMENTO_BORDO, f.getRighe().get(0).getY());
		assertEquals(DescrizioneFumetto.SMUSSAMENTO_BORDO, f.getRighe().get(0).getX());
		assertTrue(f.getLarghezza() <= 100);
		assertTrue(f.getAltezza() > 2 * DescrizioneFumetto.SMUSSAMENTO_BORDO + FONT.getHeight());
	}

	@Test
	void laPuntaDelFumettoParteDalloSmussoGiusto() {
		// Nuvola larga 100 e alta 40 con l'angolo in basso a sinistra in (200, 300): (200..300) x (260..300)
		int[] sinistraSotto = DescrizioneFumetto.punta(200, 300, 100, 40, 250, 400);
		assertNotNull(sinistraSotto);
		// Il punto è sotto e dentro i limiti orizzontali: parte dal centro del lato inferiore
		assertArrayEquals(new int[] {242, 300, 258, 300, 250, 400}, sinistraSotto);
		int[] aSinistraSotto = DescrizioneFumetto.punta(200, 300, 100, 40, 100, 400);
		assertArrayEquals(new int[] {200, 292, 208, 300, 100, 400}, aSinistraSotto);
		int[] aDestraSopra = DescrizioneFumetto.punta(200, 300, 100, 40, 400, 100);
		assertArrayEquals(new int[] {292, 260, 300, 268, 400, 100}, aDestraSopra);
	}

	@Test
	void seIlPuntoECopertoDallaNuvolaNonCeLaPunta() {
		assertNull(DescrizioneFumetto.punta(200, 300, 100, 40, 250, 280));
	}
}
