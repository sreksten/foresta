package com.threeamigos.foresta.ui.componenti;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lo scorrimento verticale, senza disegnare: dove sta l'immagine, quando arriva, quando esce, e i limiti dello
 * scorrimento a mano.
 */
class ScorrimentoVerticaleTest {

    private static final Rectangle AREA = new Rectangle(0, 100, 200, 300);

    @Test
    void dalBassoSaleFinoAUscireInAlto() {
        ScorrimentoVerticale scorrimento = ScorrimentoVerticale.dalBasso(immagine(50), AREA, 40);
        // Parte appena sotto l'area: il fondo è sotto il fondo dell'area
        assertEquals(100 + 300 + 50, scorrimento.getFondo());
        scorrimento.avanza(10, 30);
        assertEquals(100 + 300 + 50 - 300, scorrimento.getFondo());
        assertFalse(scorrimento.isUscito());
        scorrimento.avanza(2, 30);
        assertTrue(scorrimento.isUscito());
    }

    @Test
    void conUnaQuotaSiFermaLi() {
        ScorrimentoVerticale scorrimento = ScorrimentoVerticale.dalBasso(immagine(50), AREA, 40).fermaA(20);
        scorrimento.avanza(5, 30);
        assertFalse(scorrimento.isArrivato());
        scorrimento.avanza(100, 30);
        assertTrue(scorrimento.isArrivato());
        assertEquals(100 + 20 + 50, scorrimento.getFondo());
        assertFalse(scorrimento.isUscito());
    }

    @Test
    void aManoNonSiStaccaDallaCimaNeDalFondo() {
        ScorrimentoVerticale scorrimento = new ScorrimentoVerticale(immagine(1000), AREA);
        assertFalse(scorrimento.haAltroSopra());
        assertTrue(scorrimento.haAltroSotto());
        scorrimento.scorri(-50);
        assertFalse(scorrimento.haAltroSopra(), "non si va sopra la cima");
        scorrimento.scorri(10_000);
        assertTrue(scorrimento.haAltroSopra());
        assertFalse(scorrimento.haAltroSotto(), "il fondo arriva al fondo dell'area, non oltre");
        assertEquals(100 + 300, scorrimento.getFondo());

        // Un'immagine più bassa dell'area non si sposta
        ScorrimentoVerticale corta = new ScorrimentoVerticale(immagine(100), AREA);
        corta.scorri(50);
        assertEquals(100 + 100, corta.getFondo());
        assertFalse(corta.haAltroSopra() || corta.haAltroSotto());
    }

    private static BufferedImage immagine(int altezza) {
        return new BufferedImage(200, altezza, BufferedImage.TYPE_INT_ARGB);
    }

    @Test
    void trascinandoLImmagineSegueIlPuntatoreEIlClickCheSegueNonVale() {
        ScorrimentoVerticale scorrimento = new ScorrimentoVerticale(immagine(1000), AREA);
        scorrimento.scorri(0);
        scorrimento.premi(300);
        // Sotto la soglia è un tocco
        scorrimento.trascina(297);
        assertEquals(0, scorrimento.getScorrimento());
        scorrimento.rilascia();
        assertFalse(scorrimento.isGestoTrascinato());
        // Oltre la soglia l'immagine segue il puntatore (verso l'alto: scorre verso il basso)
        scorrimento.premi(300);
        scorrimento.trascina(290);
        scorrimento.trascina(250);
        assertEquals(40, scorrimento.getScorrimento());
        scorrimento.rilascia();
        assertTrue(scorrimento.isGestoTrascinato());
        // Una nuova pressione azzera il ricordo del trascinamento
        scorrimento.premi(300);
        assertFalse(scorrimento.isGestoTrascinato());
    }

    @Test
    void trascinandoOltreLaCimaSiTiraEPoiTorna() {
        ScorrimentoVerticale scorrimento = new ScorrimentoVerticale(immagine(1000), AREA);
        scorrimento.premi(300);
        scorrimento.trascina(310);
        scorrimento.trascina(380);
        // Oltre la cima: l'immagine è tirata verso il basso, poco
        assertTrue(scorrimento.getScorrimento() < 0);
        assertTrue(scorrimento.getScorrimento() >= -(int) ScorrimentoSuUnAsse.MASSIMO_ELASTICO);
        scorrimento.rilascia();
        assertTrue(scorrimento.isInMovimento());
    }

    @Test
    void lAutomaticoNonSiTrascina() {
        ScorrimentoVerticale scorrimento = ScorrimentoVerticale.dalBasso(immagine(1000), AREA, 40);
        scorrimento.premi(300);
        scorrimento.trascina(100);
        scorrimento.rilascia();
        assertFalse(scorrimento.isGestoTrascinato());
        assertFalse(scorrimento.isInMovimento());
    }
}
