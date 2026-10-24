package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il testo che dice a quale tipo di danno un personaggio è debole, resiste o è immune.
 */
class TestoReazioneDiClasseTest {

    @Test
    void ilTestoDiceAQualeDanno() {
        assertEquals("Debole a Sacro", TestoReazioneDiClasse.di(TipoDanno.SACRO, 2.0d));
        assertEquals("Resiste a Fuoco", TestoReazioneDiClasse.di(TipoDanno.FUOCO, 0.5d));
        assertEquals("Immune a Aria", TestoReazioneDiClasse.di(TipoDanno.ARIA, 0.0d));
    }

    /** Quanto un colore è lontano dal grigio neutro (la somma delle differenze dei canali) */
    private static int distanzaDalNeutro(Color colore) {
        Color neutro = TestoReazioneDiClasse.colore(1.0d);
        return Math.abs(colore.getRed() - neutro.getRed()) + Math.abs(colore.getGreen() - neutro.getGreen())
                + Math.abs(colore.getBlue() - neutro.getBlue());
    }

    @Test
    void ilColoreVaDalGrigioAlVerdeConLaDebolezzaEDalGrigioAlRossoConLaResistenza() {
        // Debolezza: dal grigio al verde pieno, che si raggiunge a ×2
        Color debole = TestoReazioneDiClasse.colore(1.5d);
        assertTrue(debole.getGreen() > debole.getRed(), String.valueOf(debole));
        assertEquals(new Color(0, 255, 0), TestoReazioneDiClasse.colore(2.0d));
        assertEquals(new Color(0, 255, 0), TestoReazioneDiClasse.colore(3.0d));
        // Resistenza: dal grigio al rosso, che è pieno solo per l'immunità
        Color resistente = TestoReazioneDiClasse.colore(0.5d);
        assertTrue(resistente.getRed() > resistente.getGreen(), String.valueOf(resistente));
        assertEquals(new Color(255, 0, 0), TestoReazioneDiClasse.colore(0.0d));
        assertNotEquals(new Color(255, 0, 0), TestoReazioneDiClasse.colore(0.01d));
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
        java.util.Set<Color> verdi = new java.util.HashSet<>();
        java.util.Set<Color> rossi = new java.util.HashSet<>();
        for (int i = 1; i <= 100; i++) {
            verdi.add(TestoReazioneDiClasse.colore(1.0d + i / 100.0d));
            rossi.add(TestoReazioneDiClasse.colore(1.0d - i / 100.0d));
        }
        assertEquals(TestoReazioneDiClasse.SFUMATURE, verdi.size());
        // Le sfumature di rosso sono dieci, l'ultima è l'immunità
        assertEquals(TestoReazioneDiClasse.SFUMATURE, rossi.size());
    }
}
