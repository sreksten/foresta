package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoCanto;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CantiImparatiMDTest {

    private static String salva(CantiImparatiMD canti) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            canti.salva(stream);
        }
        return scritto.toString();
    }

    @Test
    void iCantiImparatiSiSalvanoESiAzzeranoConUnaPartitaNuova() throws IOException {
        CantiImparatiMD canti = new CantiImparatiMD();
        assertTrue(canti.getCanti().isEmpty());
        canti.aggiungi(TipoCanto.URLO_DELLA_BANSHEE);

        CantiImparatiMD riletti = new CantiImparatiMD();
        riletti.leggi(new BufferedReader(new StringReader(salva(canti))));
        assertEquals(Collections.singleton(TipoCanto.URLO_DELLA_BANSHEE), riletti.getCanti());
        // Rileggere sostituisce quel che c'era
        riletti.aggiungi(TipoCanto.CURATIVO);
        riletti.leggi(new BufferedReader(new StringReader(salva(canti))));
        assertEquals(Collections.singleton(TipoCanto.URLO_DELLA_BANSHEE), riletti.getCanti());

        canti.reimposta();
        assertTrue(canti.getCanti().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> canti.getCanti().add(TipoCanto.CURATIVO));
    }
}
