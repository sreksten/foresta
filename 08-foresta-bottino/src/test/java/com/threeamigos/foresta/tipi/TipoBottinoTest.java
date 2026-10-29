package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TipoBottinoTest {

    @Test
    void gliOggettiGenerabiliSonoTuttiTranneArtefattiEOggettiDiMissione() {
        for (TipoBottino oggetto : TipoBottino.values()) {
            boolean generabile = oggetto != TipoBottino.ARTEFATTO && oggetto != TipoBottino.OGGETTO_MISSIONE;
            assertEquals(generabile, oggetto.isGenerabile(), oggetto.name());
            assertTrue(oggetto.getQuantitaMassima() >= 1, oggetto.name());
            assertTrue(oggetto.getValore() > 0, oggetto.name());
        }
        assertEquals(150, TipoBottino.COFANO.getValore());
        assertEquals(2, TipoBottino.MONETA.getQuantitaMassima());
    }
}
