package com.threeamigos.foresta.modellodati;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le proprietà di locazioni e missioni nei salvataggi (la mappa "chiave§valore" è provata in foresta-serializzazione).
 */
class MappaProprietaTest {

    @Test
    void unValoreNullSiRifiutaEUnaProprietaSiTogliendoLa() {
        MissioneMD missione = new MissioneMD();
        LocazioneMD locazione = new LocazioneMD();

        IllegalArgumentException eccezioneMissione = assertThrows(IllegalArgumentException.class, () -> missione.aggiungiProprieta("TEMPO", null));
        assertTrue(eccezioneMissione.getMessage().contains("TEMPO"), eccezioneMissione.getMessage());
        assertThrows(IllegalArgumentException.class, () -> locazione.aggiungiProprieta("TEMPO", null));
        assertNull(missione.ottieniProprieta("TEMPO"));
        assertNull(locazione.ottieniProprieta("TEMPO"));

        // Il valore vuoto invece vale, e la pulizia dei campi resta
        missione.aggiungiProprieta("VUOTO", "");
        missione.aggiungiProprieta("PIPE", "a|b");
        assertEquals("", missione.ottieniProprieta("VUOTO"));
        assertEquals("ab", missione.ottieniProprieta("PIPE"));
    }
}
