package com.threeamigos.foresta.strumenti;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I contesti dei lanci che si possono truccare: ognuno ha un nome per la finestra di prova, e solo due scelgono fra elementi
 * invece di dare un numero.
 */
class ContestoLancioTest {

    @Test
    void ognunoHaUnaDescrizione() {
        for (ContestoLancio contesto : ContestoLancio.values()) {
            assertFalse(contesto.getDescrizione().trim().isEmpty(), contesto.name());
        }
        assertEquals("Contenuto del cofano", ContestoLancio.COFANO_ESITO.getDescrizione());
    }

    @Test
    void soloIlTipoDiMostroEIlCofanoOggettoScelgonoFraElementi() {
        for (ContestoLancio contesto : ContestoLancio.values()) {
            boolean attesa = contesto == ContestoLancio.TIPO_MOSTRO || contesto == ContestoLancio.COFANO_OGGETTO;
            assertEquals(attesa, contesto.isScelta(), contesto.name());
        }
    }
}
