package com.threeamigos.foresta.ui.componenti;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il trascinamento con soglia, l'inerzia e la destinazione, con un orologio fatto avanzare a mano.
 */
class ScorrimentoSuUnAsseTest {

    private static final long MILLISECONDO = 1_000_000L;

    private long orologio;
    private ScorrimentoSuUnAsse scorrimento;

    @BeforeEach
    void prepara() {
        orologio = 1_000_000_000L;
        scorrimento = new ScorrimentoSuUnAsse(() -> orologio);
        scorrimento.imposta(true, 500.0d);
    }

    private void passa(long millisecondi) {
        for (long t = 0; t < millisecondi; t += 16) {
            orologio += 16 * MILLISECONDO;
            scorrimento.aggiorna();
        }
    }

    @Test
    void sottoLaSogliaEUnTocco() {
        scorrimento.premi(100, true);
        scorrimento.trascina(100 + ScorrimentoSuUnAsse.SOGLIA_TRASCINAMENTO - 1);
        assertFalse(scorrimento.isTrascinando());
        assertEquals(0.0d, scorrimento.getScorrimento(), 0.001d);
        assertTrue(scorrimento.isPremuto());
    }

    @Test
    void oltreLaSogliaIlContenutoSeguePartendoDaDovEra() {
        scorrimento.premi(200, true);
        scorrimento.trascina(190);
        assertTrue(scorrimento.isTrascinando());
        // Appena superata la soglia non c'è lo scatto: si parte da dov'è il puntatore
        assertEquals(0.0d, scorrimento.getScorrimento(), 0.001d);
        scorrimento.trascina(150);
        // Il puntatore va verso l'inizio: il contenuto scorre verso la fine
        assertEquals(40.0d, scorrimento.getScorrimento(), 0.001d);
    }

    @Test
    void trascinandoOltreUnCapoIlContenutoSiTiraConResistenzaFinoAUnMassimo() {
        scorrimento.premi(200, true);
        scorrimento.trascina(100);
        // Oltre l'inizio: si tira, ma meno di quanto si sposta il puntatore
        scorrimento.trascina(290);
        assertTrue(scorrimento.getScorrimento() < 0.0d);
        assertTrue(scorrimento.getScorrimento() > -(290 - 100));
        // Tirando molto non si supera il massimo
        scorrimento.trascina(9000);
        assertTrue(scorrimento.getScorrimento() >= -ScorrimentoSuUnAsse.MASSIMO_ELASTICO);
        // Oltre la fine, lo stesso
        scorrimento.trascina(-9000);
        assertTrue(scorrimento.getScorrimento() > 500.0d);
        assertTrue(scorrimento.getScorrimento() <= 500.0d + ScorrimentoSuUnAsse.MASSIMO_ELASTICO);
    }

    @Test
    void ilLimiteImpostatoMentreSiTiraNonCancellaLElastico() {
        scorrimento.premi(200, true);
        scorrimento.trascina(100);
        scorrimento.trascina(290);
        double tirato = scorrimento.getScorrimento();
        scorrimento.imposta(true, 500.0d);
        assertEquals(tirato, scorrimento.getScorrimento(), 0.001d);
    }

    @Test
    void lasciandoOltreUnCapoIlContenutoTornaAlCapo() {
        scorrimento.premi(200, true);
        scorrimento.trascina(100);
        scorrimento.trascina(290);
        assertTrue(scorrimento.getScorrimento() < 0.0d);
        scorrimento.rilascia();
        assertTrue(scorrimento.isInMovimento());
        passa(2000);
        assertEquals(0.0d, scorrimento.getScorrimento(), 0.001d);
        assertFalse(scorrimento.isInMovimento());
    }

    @Test
    void lasciandoOltreLaFineTornaAllaFine() {
        scorrimento.scorriDi(500.0d);
        scorrimento.premi(200, true);
        scorrimento.trascina(300);
        scorrimento.trascina(150);
        assertTrue(scorrimento.getScorrimento() > 500.0d);
        scorrimento.rilascia();
        passa(2000);
        assertEquals(500.0d, scorrimento.getScorrimento(), 0.001d);
    }

    @Test
    void senzaTrascinamentoUnLimiteCheSiRiduceRiportaDentro() {
        scorrimento.scorriDi(400.0d);
        scorrimento.imposta(true, 100.0d);
        assertEquals(100.0d, scorrimento.getScorrimento(), 0.001d);
    }

    @Test
    void daUnPuntoDoveNonSiTrascinaNonSiTrascina() {
        scorrimento.premi(200, false);
        scorrimento.trascina(100);
        assertFalse(scorrimento.isTrascinando());
    }

    @Test
    void senzaNienteDaScorrereNonSiTrascina() {
        scorrimento.imposta(false, 0.0d);
        scorrimento.premi(200, true);
        scorrimento.trascina(100);
        assertFalse(scorrimento.isTrascinando());
    }

    @Test
    void lasciandoInFrettaSiProseguePerInerziaEPoiCiSiFerma() {
        scorrimento.premi(400, true);
        scorrimento.trascina(380);
        for (int i = 0; i < 5; i++) {
            orologio += 16 * MILLISECONDO;
            // 10 pixel ogni 16 ms: 625 pixel al secondo
            scorrimento.trascina(380 - (i + 1) * 10);
        }
        double allaFine = scorrimento.getScorrimento();
        scorrimento.rilascia();
        assertTrue(scorrimento.isInMovimento());
        passa(160);
        assertTrue(scorrimento.getScorrimento() > allaFine);
        passa(5000);
        assertFalse(scorrimento.isInMovimento());
        assertTrue(scorrimento.getScorrimento() <= 500.0d);
    }

    @Test
    void lasciandoDopoUnaPausaNonCEInerzia() {
        scorrimento.premi(400, true);
        scorrimento.trascina(380);
        orologio += 16 * MILLISECONDO;
        scorrimento.trascina(300);
        orologio += 500 * MILLISECONDO;
        scorrimento.rilascia();
        assertFalse(scorrimento.isInMovimento());
    }

    @Test
    void premendoMentreSiMuoveLoFermaESegnalaCheNonEUnTocco() {
        scorrimento.scorriFinoA(300.0d, 0.0d);
        passa(32);
        assertTrue(scorrimento.isInMovimento());
        assertTrue(scorrimento.premi(10, true));
        assertFalse(scorrimento.isInMovimento());
        assertFalse(scorrimento.premi(10, true));
    }

    @Test
    void laDestinazioneSiRaggiungeConUnaBreveAnimazione() {
        scorrimento.scorriFinoA(200.0d, 0.0d);
        assertEquals(200.0d, scorrimento.getPosizioneDiArrivo(), 0.001d);
        assertEquals(0.0d, scorrimento.getScorrimento(), 0.001d);
        passa(2000);
        assertEquals(200.0d, scorrimento.getScorrimento(), 0.001d);
        assertFalse(scorrimento.isInMovimento());
    }

    @Test
    void vicinoAUnCapoSiArrivaFinoAlCapo() {
        scorrimento.scorriFinoA(20.0d, 32.0d);
        assertEquals(0.0d, scorrimento.getPosizioneDiArrivo(), 0.001d);
        scorrimento.scorriFinoA(490.0d, 32.0d);
        assertEquals(500.0d, scorrimento.getPosizioneDiArrivo(), 0.001d);
    }

    @Test
    void ridurreIlMassimoTieneDentroLoScorrimentoELaDestinazione() {
        scorrimento.scorriFinoA(400.0d, 0.0d);
        scorrimento.imposta(true, 100.0d);
        assertEquals(100.0d, scorrimento.getPosizioneDiArrivo(), 0.001d);
    }

    @Test
    void reimpostaRiportaAllInizioEFermaTutto() {
        scorrimento.scorriFinoA(300.0d, 0.0d);
        passa(2000);
        scorrimento.reimposta();
        assertEquals(0.0d, scorrimento.getScorrimento(), 0.001d);
        assertFalse(scorrimento.isInMovimento());
    }

    @Test
    void scorriDiSpostaSubitoEStaDentroILimiti() {
        scorrimento.scorriDi(30.0d);
        assertEquals(30.0d, scorrimento.getScorrimento(), 0.001d);
        scorrimento.scorriDi(-100.0d);
        assertEquals(0.0d, scorrimento.getScorrimento(), 0.001d);
        scorrimento.scorriDi(9000.0d);
        assertEquals(500.0d, scorrimento.getScorrimento(), 0.001d);
    }

    @Test
    void scorriDiFermaLInerzia() {
        scorrimento.scorriFinoA(300.0d, 0.0d);
        scorrimento.scorriDi(10.0d);
        assertFalse(scorrimento.isInMovimento());
        passa(500);
        assertEquals(10.0d, scorrimento.getScorrimento(), 0.001d);
    }

    @Test
    void ilRilascioDiUnTrascinamentoSiRiconoscePerIlClickCheSegue() {
        scorrimento.premi(200, true);
        scorrimento.trascina(100);
        scorrimento.rilascia();
        assertTrue(scorrimento.isGestoTrascinato());
        // Una nuova pressione, anche senza trascinare, lo azzera
        scorrimento.premi(200, true);
        scorrimento.rilascia();
        assertFalse(scorrimento.isGestoTrascinato());
    }
}
