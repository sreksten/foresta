package com.threeamigos.foresta.danni.tipi;

import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TipoInterazioneConEffettiDiStatoTest {

    @Test
    void ogniInterazioneHaPrecondizioniEInnescanti() {
        for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
            assertFalse(interazione.getPrecondizioni().isEmpty(), interazione.name());
            assertFalse(interazione.getInnescanti().isEmpty(), interazione.name());
            assertNotNull(interazione.getStatiRimossi(), interazione.name());
            assertNotNull(interazione.getStatiApplicati(), interazione.name());
            assertEquals(interazione == TipoInterazioneConEffettiDiStato.COLPO_DI_GRAZIA
                    || interazione == TipoInterazioneConEffettiDiStato.FRANTUMAZIONE_DEL_GHIACCIO, interazione.isCriticoAutomatico(), interazione.name());
        }
        assertTrue(TipoInterazioneConEffettiDiStato.CONGELAMENTO.getStatiApplicati().contains(TipoEffettoDiStato.CONGELATO));
        assertTrue(TipoInterazioneConEffettiDiStato.CONGELAMENTO.getStatiRimossi().contains(TipoEffettoDiStato.BAGNATO));
    }

    @Test
    void soloTreInterazioniInvestonoIVicini() {
        assertSame(TipoDanno.FUOCO, TipoInterazioneConEffettiDiStato.ESPLOSIONE_DI_GAS.getDannoAdArea());
        assertSame(TipoDanno.SONICO, TipoInterazioneConEffettiDiStato.RIGETTO.getDannoAdArea());
        assertSame(TipoDanno.FULMINE, TipoInterazioneConEffettiDiStato.ELETTROCUZIONE.getDannoAdArea());
        for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
            if (interazione != TipoInterazioneConEffettiDiStato.ESPLOSIONE_DI_GAS
                    && interazione != TipoInterazioneConEffettiDiStato.RIGETTO
                    && interazione != TipoInterazioneConEffettiDiStato.ELETTROCUZIONE) {
                assertNull(interazione.getDannoAdArea(), interazione.name());
            }
        }
    }

    @Test
    void ogniInterazioneHaLaDescrizioneEChiInvesteIViciniLaSuaFrase() {
        for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
            assertEquals(Testi.testoOCostante(interazione, "DESCRIZIONE"), interazione.getDescrizione(), interazione.name());
            assertNotEquals(interazione.name(), interazione.getDescrizione(), "manca la descrizione di " + interazione);
            String frase = interazione.getTestoAdArea();
            if (interazione.getDannoAdArea() != null) {
                assertNotEquals(interazione.name(), frase, "manca la frase di " + interazione);
            } else {
                assertEquals(interazione.name(), frase, interazione.name() + " non ha una frase ad area");
            }
        }
    }
}
