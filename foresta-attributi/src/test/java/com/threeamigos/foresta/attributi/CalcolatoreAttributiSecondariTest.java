package com.threeamigos.foresta.attributi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le formule degli attributi secondari: la somma pesata delle radici dei due primari, per il moltiplicatore della classe,
 * troncata a intero. Con Destrezza 16 e Fortuna o Intelligenza 4 le radici valgono 4 e 2.
 */
class CalcolatoreAttributiSecondariTest {

    @Test
    void ilCriticoPesaLaDestrezzaPiuDellaFortunaEHaUnTetto() {
        // 2,0 * 5 + 0,8 * 5 = 14
        assertEquals(14, CalcolatoreAttributiSecondari.critico(25, 25, 1.0));
        assertEquals(7, CalcolatoreAttributiSecondari.critico(25, 25, 0.5));
        // 2,0 * 100 + 0,8 * 100 = 280, ma il critico non supera il 100%
        assertEquals(CalcolatoreAttributiSecondari.CRITICO_MASSIMO, CalcolatoreAttributiSecondari.critico(10000, 10000, 1.0));
        assertEquals(100, CalcolatoreAttributiSecondari.CRITICO_MASSIMO);
    }

    @Test
    void ilRisultatoSiTroncaAIntero() {
        // 0,8 * 4 + 0,2 * 2 = 3,6
        assertEquals(3, CalcolatoreAttributiSecondari.precisione(16, 4, 1.0));
        // 3,6 * 2 = 7,2
        assertEquals(7, CalcolatoreAttributiSecondari.precisione(16, 4, 2.0));
    }

    @Test
    void ognunoDeiSecondariUsaIPesiDeiSuoiDuePrimari() {
        // Radici 4 e 2: ogni valore è la somma pesata (la riga dice il calcolo)
        assertEquals(3, CalcolatoreAttributiSecondari.velocita(16, 4, 1.0));            // 0,85*4 + 0,15*2 = 3,7
        assertEquals(3, CalcolatoreAttributiSecondari.furtivita(16, 4, 1.0));           // 0,75*4 + 0,25*2 = 3,5
        assertEquals(7, CalcolatoreAttributiSecondari.furtivita(16, 4, 2.0));           // 7,0
        assertEquals(3, CalcolatoreAttributiSecondari.parata(16, 4, 1.0));              // 0,70*4 + 0,30*2 = 3,4
        assertEquals(6, CalcolatoreAttributiSecondari.parata(16, 4, 2.0));              // 6,8
        assertEquals(3, CalcolatoreAttributiSecondari.resistenzaMagica(16, 4, 1.0));    // 3,4
        assertEquals(3, CalcolatoreAttributiSecondari.percezione(16, 4, 1.0));          // 3,5
        assertEquals(3, CalcolatoreAttributiSecondari.soggezione(16, 4, 1.0));          // 3,4
        assertEquals(3, CalcolatoreAttributiSecondari.furia(16, 4, 1.0));               // 3,5
        assertEquals(3, CalcolatoreAttributiSecondari.coraggio(16, 4, 1.0));            // 3,5
        assertEquals(3, CalcolatoreAttributiSecondari.valore(16, 4, 1.0));              // 3,4
        assertEquals(3, CalcolatoreAttributiSecondari.contrattazione(16, 4, 1.0));      // 0,60*4 + 0,40*2 = 3,2
    }

    @Test
    void ilPrimarioPrincipalePesaPiuDelSecondario() {
        // Scambiando i due primari il risultato cambia: pesa di più il primo
        assertTrue(CalcolatoreAttributiSecondari.parata(100, 1, 10.0) > CalcolatoreAttributiSecondari.parata(1, 100, 10.0));
        assertTrue(CalcolatoreAttributiSecondari.soggezione(100, 1, 10.0) > CalcolatoreAttributiSecondari.soggezione(1, 100, 10.0));
    }

    @Test
    void chiNonTrattaConIMercantiNonHaContrattazione() {
        assertEquals(0, CalcolatoreAttributiSecondari.contrattazione(100, 100, 0.0));
    }

    @Test
    void iBersagliSonoAlmenoUnoEUsanoIDuePrimariScelti() {
        // 0,70 * 10 + 0,30 * 10 = 10
        assertEquals(10, CalcolatoreAttributiSecondari.numeroBersagli(100, 100, 1.0));
        // (0,70 * 4 + 0,30 * 2) * 2 = 6,8: si arrotonda per difetto
        assertEquals(6, CalcolatoreAttributiSecondari.numeroBersagli(16, 4, 2.0));
        assertEquals(1, CalcolatoreAttributiSecondari.numeroBersagli(0, 0, 1.0), "almeno un bersaglio");
        assertEquals(1, CalcolatoreAttributiSecondari.numeroBersagli(16, 4, 0.1), "anche con un moltiplicatore piccolo");
    }

    @Test
    void ilCaricoSommaForzaECostituzioneEFaPortareNienteAChiHaMoltiplicatoreZero() {
        // 12 * 4 + 6 * 2 = 60
        assertEquals(60.0, CalcolatoreAttributiSecondari.potenzialeDiCarico(16, 4, 1.0), 1e-9);
        assertEquals(90.0, CalcolatoreAttributiSecondari.potenzialeDiCarico(16, 4, 1.5), 1e-9);
        assertEquals(0.0, CalcolatoreAttributiSecondari.potenzialeDiCarico(100, 100, 0.0), "i fantasmi non portano niente");
    }

    @Test
    void unSecondoCasoConAltriValoriConfermaIPesiDiOgniSecondario() {
        // Primari 81 e 25 (radici 9 e 5), moltiplicatore 1,3. Il primo primario pesa di più: scambiandoli il risultato
        // scenderebbe (per esempio la precisione da 10 a 7), quindi un peso invertito non passa inosservato.
        assertEquals(28, CalcolatoreAttributiSecondari.critico(81, 25, 1.3));             // (2,0*9 + 0,8*5) * 1,3 = 28,6
        assertEquals(22, CalcolatoreAttributiSecondari.critico(25, 81, 1.3));             // (2,0*5 + 0,8*9) * 1,3 = 22,36
        assertEquals(10, CalcolatoreAttributiSecondari.precisione(81, 25, 1.3));          // (0,8*9 + 0,2*5) * 1,3 = 10,66
        assertEquals(7, CalcolatoreAttributiSecondari.precisione(25, 81, 1.3));           // 7,54
        assertEquals(10, CalcolatoreAttributiSecondari.velocita(81, 25, 1.3));            // (0,85*9 + 0,15*5) * 1,3 = 10,92
        assertEquals(7, CalcolatoreAttributiSecondari.velocita(25, 81, 1.3));             // 7,28
        assertEquals(10, CalcolatoreAttributiSecondari.furtivita(81, 25, 1.3));           // (0,75*9 + 0,25*5) * 1,3 = 10,4
        assertEquals(7, CalcolatoreAttributiSecondari.furtivita(25, 81, 1.3));            // 7,8
        assertEquals(10, CalcolatoreAttributiSecondari.parata(81, 25, 1.3));              // (0,70*9 + 0,30*5) * 1,3 = 10,14
        assertEquals(8, CalcolatoreAttributiSecondari.parata(25, 81, 1.3));               // 8,06
        assertEquals(10, CalcolatoreAttributiSecondari.resistenzaMagica(81, 25, 1.3));    // 10,14
        assertEquals(8, CalcolatoreAttributiSecondari.resistenzaMagica(25, 81, 1.3));     // 8,06
        assertEquals(10, CalcolatoreAttributiSecondari.percezione(81, 25, 1.3));          // (0,75*9 + 0,25*5) * 1,3 = 10,4
        assertEquals(7, CalcolatoreAttributiSecondari.percezione(25, 81, 1.3));           // 7,8
        assertEquals(10, CalcolatoreAttributiSecondari.soggezione(81, 25, 1.3));          // 10,14
        assertEquals(8, CalcolatoreAttributiSecondari.soggezione(25, 81, 1.3));           // 8,06
        assertEquals(10, CalcolatoreAttributiSecondari.furia(81, 25, 1.3));               // 10,4
        assertEquals(7, CalcolatoreAttributiSecondari.furia(25, 81, 1.3));                // 7,8
        assertEquals(10, CalcolatoreAttributiSecondari.coraggio(81, 25, 1.3));            // 10,4
        assertEquals(7, CalcolatoreAttributiSecondari.coraggio(25, 81, 1.3));             // 7,8
        assertEquals(9, CalcolatoreAttributiSecondari.contrattazione(81, 25, 1.3));       // (0,60*9 + 0,40*5) * 1,3 = 9,62
        assertEquals(8, CalcolatoreAttributiSecondari.contrattazione(25, 81, 1.3));       // 8,58
        assertEquals(10, CalcolatoreAttributiSecondari.valore(81, 25, 1.3));              // 10,14
        assertEquals(8, CalcolatoreAttributiSecondari.valore(25, 81, 1.3));               // 8,06
        assertEquals(10, CalcolatoreAttributiSecondari.numeroBersagli(81, 25, 1.3));      // (0,70*9 + 0,30*5) * 1,3 = 10,14
        assertEquals(8, CalcolatoreAttributiSecondari.numeroBersagli(25, 81, 1.3));       // 8,06
        assertEquals(179.4, CalcolatoreAttributiSecondari.potenzialeDiCarico(81, 25, 1.3), 1e-9);  // (12*9 + 6*5) * 1,3
        assertEquals(148.2, CalcolatoreAttributiSecondari.potenzialeDiCarico(25, 81, 1.3), 1e-9);  // (12*5 + 6*9) * 1,3
    }
}
