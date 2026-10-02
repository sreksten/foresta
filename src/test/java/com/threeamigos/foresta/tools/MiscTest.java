package com.threeamigos.foresta.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiscTest {

    @Test
    void lePreposizioniSiFondonoConLArticolo() {
        assertEquals("nel Tempio del Sole", Misc.conPreposizione("in", "il Tempio del Sole"));
        assertEquals("nello Ziggurat della Luna", Misc.conPreposizione("in", "lo Ziggurat della Luna"));
        assertEquals("nella Pieve del Silenzio", Misc.conPreposizione("in", "la Pieve del Silenzio"));
        assertEquals("nell'Eremo del Fine Settimana", Misc.conPreposizione("in", "l'Eremo del Fine Settimana"));
        assertEquals("al Santuario", Misc.conPreposizione("a", "il Santuario"));
        assertEquals("all'Abbazia", Misc.conPreposizione("a", "l'Abbazia"));
        assertEquals("allo Ziggurat", Misc.conPreposizione("a", "lo Ziggurat"));
        assertEquals("della Cappella", Misc.conPreposizione("di", "la Cappella"));
        assertEquals("dai monti", Misc.conPreposizione("da", "i monti"));
        assertEquals("negli gnomi", Misc.conPreposizione("in", "gli gnomi"));
        assertEquals("delle stelle", Misc.conPreposizione("di", "le stelle"));
        assertEquals("a Ruuna", Misc.conPreposizione("a", "Ruuna"));
    }

    @Test
    void inizialeMaiuscola() {
        assertEquals("Il Tempio del Sole", Misc.inizialeMaiuscola("il Tempio del Sole"));
        assertEquals("", Misc.inizialeMaiuscola(""));
    }
}
