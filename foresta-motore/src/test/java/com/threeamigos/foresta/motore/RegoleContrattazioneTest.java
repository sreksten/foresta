package com.threeamigos.foresta.motore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegoleContrattazioneTest {

    @Test
    void senzaContrattazionePrezzoPienoEVenditaAMeta() {
        assertEquals(100, RegoleContrattazione.prezzoAcquisto(100, 0));
        assertEquals(50, RegoleContrattazione.prezzoVendita(100, 0));
    }

    @Test
    void conLoScudoFiscaleSiArrivaAiLimiti() {
        // Ladro (4) + Scudo Fiscale (+16)
        assertEquals(80, RegoleContrattazione.prezzoAcquisto(100, 20));
        assertEquals(75, RegoleContrattazione.prezzoVendita(100, 20));
    }

    @Test
    void iLimitiNonSiSuperanoMai() {
        for (int contrattazione = 0; contrattazione <= 1000; contrattazione++) {
            assertTrue(RegoleContrattazione.sconto(contrattazione) <= Costanti.CONTRATTAZIONE_SCONTO_MASSIMO);
            assertTrue(RegoleContrattazione.quotaVendita(contrattazione) <= Costanti.CONTRATTAZIONE_QUOTA_VENDITA_MASSIMA);
        }
    }

    @Test
    void comprareERivendereNonFaGuadagnare() {
        for (int costo = 1; costo <= 500; costo++) {
            for (int contrattazione = 0; contrattazione <= 100; contrattazione++) {
                assertTrue(RegoleContrattazione.prezzoVendita(costo, contrattazione)
                        < RegoleContrattazione.prezzoAcquisto(costo, contrattazione), costo + "/" + contrattazione);
            }
        }
    }

    @Test
    void piuContrattazioneNonPeggioraIPrezzi() {
        for (int contrattazione = 0; contrattazione < 100; contrattazione++) {
            assertTrue(RegoleContrattazione.prezzoAcquisto(1000, contrattazione + 1) <= RegoleContrattazione.prezzoAcquisto(1000, contrattazione));
            assertTrue(RegoleContrattazione.prezzoVendita(1000, contrattazione + 1) >= RegoleContrattazione.prezzoVendita(1000, contrattazione));
        }
    }
}
