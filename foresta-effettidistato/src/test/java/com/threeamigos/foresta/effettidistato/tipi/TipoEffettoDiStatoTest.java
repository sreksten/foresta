package com.threeamigos.foresta.effettidistato.tipi;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TipoEffettoDiStatoTest {

    @Test
    void gliStatiBeneficiSonoLIspirazioneEIlBerserk() {
        for (TipoEffettoDiStato stato : TipoEffettoDiStato.values()) {
            assertEquals(stato == TipoEffettoDiStato.ISPIRATO || stato == TipoEffettoDiStato.BERSERK
					|| stato == TipoEffettoDiStato.RIGENERAZIONE, stato.isBenefico(), stato.name());
        }
    }

    @Test
    void ogniStatoHaLaDescrizioneDalFileDeiTesti() {
        for (TipoEffettoDiStato stato : TipoEffettoDiStato.values()) {
            assertEquals(Testi.testoOCostante(stato, "DESCRIZIONE"), stato.getDescrizione(), stato.name());
            assertNotEquals(stato.name(), stato.getDescrizione(), "manca la descrizione di " + stato);
        }
    }
}
