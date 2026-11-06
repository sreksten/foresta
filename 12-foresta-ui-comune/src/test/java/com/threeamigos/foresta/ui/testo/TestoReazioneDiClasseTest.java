package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.tipi.TipoDanno;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static com.threeamigos.foresta.ui.testo.TestoReazioneDiClasse.blu;
import static com.threeamigos.foresta.ui.testo.TestoReazioneDiClasse.rosso;
import static com.threeamigos.foresta.ui.testo.TestoReazioneDiClasse.verde;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Il testo che dice a quale tipo di danno un personaggio è debole, resiste o è immune.
 */
class TestoReazioneDiClasseTest {

    private static final int VERDE_PIENO = 0xFF00FF00;
    private static final int ROSSO_PIENO = 0xFFFF0000;

    @Test
    void ilTestoDiceAQualeDanno() {
        assertEquals("Debole a Sacro", TestoReazioneDiClasse.di(TipoDanno.SACRO, 2.0d));
        assertEquals("Resiste a Fuoco", TestoReazioneDiClasse.di(TipoDanno.FUOCO, 0.5d));
        assertEquals("Immune a Aria", TestoReazioneDiClasse.di(TipoDanno.ARIA, 0.0d));
    }

    /** Quanto un colore è lontano dal grigio neutro (la somma delle differenze dei canali) */
    private static int distanzaDalNeutro(int colore) {
        int neutro = TestoReazioneDiClasse.colore(1.0d);
        return Math.abs(rosso(colore) - rosso(neutro)) + Math.abs(verde(colore) - verde(neutro))
                + Math.abs(blu(colore) - blu(neutro));
    }

    @Test
    void ilColoreVaDalGrigioAlVerdeConLaDebolezzaEDalGrigioAlRossoConLaResistenza() {
        // Debolezza: dal grigio al verde pieno, che si raggiunge a ×2
        int debole = TestoReazioneDiClasse.colore(1.5d);
        assertTrue(verde(debole) > rosso(debole), Integer.toHexString(debole));
        assertEquals(VERDE_PIENO, TestoReazioneDiClasse.colore(2.0d));
        assertEquals(VERDE_PIENO, TestoReazioneDiClasse.colore(3.0d));
        // Resistenza: dal grigio al rosso, che è pieno solo per l'immunità
        int resistente = TestoReazioneDiClasse.colore(0.5d);
        assertTrue(rosso(resistente) > verde(resistente), Integer.toHexString(resistente));
        assertEquals(ROSSO_PIENO, TestoReazioneDiClasse.colore(0.0d));
        assertNotEquals(ROSSO_PIENO, TestoReazioneDiClasse.colore(0.01d));
    }

    @Test
    void iColoriSonoOpachi() {
        for (double moltiplicatore : new double[]{0.0d, 0.3d, 1.0d, 1.4d, 2.5d}) {
            assertEquals(0xFF, TestoReazioneDiClasse.colore(moltiplicatore) >>> 24, "×" + moltiplicatore);
        }
    }

    @Test
    void piuUnPersonaggioEDebolePiuIlColoreSiAllontanaDalGrigio() {
        int precedente = 0;
        for (double moltiplicatore = 1.1d; moltiplicatore <= 2.0d; moltiplicatore += 0.1d) {
            int distanza = distanzaDalNeutro(TestoReazioneDiClasse.colore(moltiplicatore));
            assertTrue(distanza >= precedente, "×" + moltiplicatore);
            precedente = distanza;
        }
        precedente = 0;
        for (double moltiplicatore = 0.9d; moltiplicatore >= 0.0d; moltiplicatore -= 0.1d) {
            int distanza = distanzaDalNeutro(TestoReazioneDiClasse.colore(Math.max(0.0d, moltiplicatore)));
            assertTrue(distanza >= precedente, "×" + moltiplicatore);
            precedente = distanza;
        }
    }

    @Test
    void cisonoDieciSfumaturePerParte() {
        Set<Integer> verdi = new HashSet<>();
        Set<Integer> rossi = new HashSet<>();
        for (int i = 1; i <= 100; i++) {
            verdi.add(TestoReazioneDiClasse.colore(1.0d + i / 100.0d));
            rossi.add(TestoReazioneDiClasse.colore(1.0d - i / 100.0d));
        }
        assertEquals(TestoReazioneDiClasse.SFUMATURE, verdi.size());
        // Le sfumature di rosso sono dieci, l'ultima è l'immunità
        assertEquals(TestoReazioneDiClasse.SFUMATURE, rossi.size());
    }
}
