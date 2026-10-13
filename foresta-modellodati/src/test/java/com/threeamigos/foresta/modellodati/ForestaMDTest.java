package com.threeamigos.foresta.modellodati;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * La mappa a video si ricostruisce quando cambia la versione della mappa: una partita nuova (o caricata) non deve
 * poter avere la stessa versione di quella disegnata prima, altrimenti si vedrebbe la mappa della partita precedente.
 */
class ForestaMDTest {

    @Test
    void unaPartitaNuovaNonRipeteLaVersioneDellaMappaPrecedente() {
        ForestaMD primaPartita = new ForestaMD();
        primaPartita.reimposta(3, 3);
        ForestaMD secondaPartita = new ForestaMD();
        secondaPartita.reimposta(3, 3);
        assertNotEquals(primaPartita.getVersioneMappa(), secondaPartita.getVersioneMappa());
        int versione = secondaPartita.getVersioneMappa();
        secondaPartita.reimposta(3, 3);
        assertNotEquals(versione, secondaPartita.getVersioneMappa());
    }
}
