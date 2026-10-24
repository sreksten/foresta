package com.threeamigos.foresta.testi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticoliTest {

    @Test
    void lePreposizioniSiFondonoConLArticolo() {
        assertEquals("nel Tempio del Sole", Articoli.conPreposizione("in", "il Tempio del Sole"));
        assertEquals("nello Ziggurat della Luna", Articoli.conPreposizione("in", "lo Ziggurat della Luna"));
        assertEquals("nella Pieve del Silenzio", Articoli.conPreposizione("in", "la Pieve del Silenzio"));
        assertEquals("nell'Eremo del Fine Settimana", Articoli.conPreposizione("in", "l'Eremo del Fine Settimana"));
        assertEquals("al Santuario", Articoli.conPreposizione("a", "il Santuario"));
        assertEquals("all'Abbazia", Articoli.conPreposizione("a", "l'Abbazia"));
        assertEquals("allo Ziggurat", Articoli.conPreposizione("a", "lo Ziggurat"));
        assertEquals("della Cappella", Articoli.conPreposizione("di", "la Cappella"));
        assertEquals("dai monti", Articoli.conPreposizione("da", "i monti"));
        assertEquals("negli gnomi", Articoli.conPreposizione("in", "gli gnomi"));
        assertEquals("delle stelle", Articoli.conPreposizione("di", "le stelle"));
        assertEquals("a Ruuna", Articoli.conPreposizione("a", "Ruuna"));
    }

    @Test
    void inizialeMaiuscola() {
        assertEquals("Il Tempio del Sole", Articoli.inizialeMaiuscola("il Tempio del Sole"));
        assertEquals("", Articoli.inizialeMaiuscola(""));
    }

    @Test
    void unaPreposizioneCheNonSiArticolaEUnErrore() {
        IllegalArgumentException errore = assertThrows(IllegalArgumentException.class, () -> Articoli.conPreposizione("su", "il monte"));
        assertTrue(errore.getMessage().contains("su"), errore.getMessage());
    }

    @Test
    void gliArticoliHannoLoSpazioFinaleTrannoQuelliElisi() {
        assertEquals("il ", Articoli.IL);
        assertEquals("lo ", Articoli.LO);
        assertEquals("la ", Articoli.LA);
        assertEquals("l'", Articoli.L_APOSTROFO);
        assertEquals("un ", Articoli.UN);
        assertEquals("un'", Articoli.UN_APOSTROFO);
        assertEquals("dell'", Articoli.DELL_APOSTROFO);
        assertEquals("da un'", Articoli.DA_UN_APOSTROFO);
        assertEquals("Egli", Articoli.EGLI);
    }
}
