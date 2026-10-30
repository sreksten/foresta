package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi del gestore delle botteghe che il gioco non provoca: comandi dati senza essere in una bottega o a una bottega che non c'è.
 */
class GestoreBottegheCasiRimastiTest {

    @Test
    void soloQuattroComandiSonoIngressiDiBottega() {
        assertTrue(GestoreBotteghe.isComandoDiIngresso(Comando.NEGOZIO_ALCHIMISTA));
        assertTrue(GestoreBotteghe.isComandoDiIngresso(Comando.NEGOZIO_ARMAIOLO));
        assertTrue(GestoreBotteghe.isComandoDiIngresso(Comando.NEGOZIO_VENDITORE_DI_PERGAMENE));
        assertTrue(GestoreBotteghe.isComandoDiIngresso(Comando.NEGOZIO_INCANTATORE));
        assertFalse(GestoreBotteghe.isComandoDiIngresso(Comando.LOCANDA));
    }

    @Test
    void unComandoCheNonEUnIngressoNonApreNessunaBottega() {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        GestoreBotteghe gestore = new GestoreBotteghe();

        assertThrows(IllegalArgumentException.class, () -> gestore.apri(new GruppoGiocatore(), Comando.LOCANDA));
        assertFalse(gestore.isAperta());
    }

    @Test
    void senzaUnaBottegaApertaNienteSiChiudeENienteSiRiceve() {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        GestoreBotteghe gestore = new GestoreBotteghe();

        assertFalse(gestore.gestisci(new GruppoGiocatore(), Comando.ANNULLA));
        gestore.riceviTesto("un nome che nessuno ha chiesto");
        assertFalse(gestore.isAperta());
    }
}
