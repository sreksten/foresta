package com.threeamigos.foresta.modellodati;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli intermezzi già mostrati e gli accampamenti della partita: come si contano, si dimenticano e si salvano.
 *
 * @author Stefano Reksten
 */
class IntermezziMDTest {

    @Test
    void salvaERileggeGliIntermezziScattati() throws IOException {
        // Given
        IntermezziMD originale = new IntermezziMD();
        originale.aggiungiScattato("PRIMO");
        originale.aggiungiScattato("SECONDO");
        originale.incrementaNumeroAccampamenti();
        originale.incrementaNumeroAccampamenti();
        StringWriter scrittura = new StringWriter();
        try (PrintWriter writer = new PrintWriter(scrittura)) {
            originale.salva(writer);
        }

        // When
        IntermezziMD riletto = new IntermezziMD();
        riletto.aggiungiScattato("DA_DIMENTICARE");
        riletto.incrementaNumeroAccampamenti();
        riletto.leggi(new BufferedReader(new StringReader(scrittura.toString())));

        // Then
        assertTrue(riletto.isScattato("PRIMO"));
        assertTrue(riletto.isScattato("SECONDO"));
        assertFalse(riletto.isScattato("DA_DIMENTICARE"));
        assertEquals(2, riletto.getNumeroAccampamenti(), "gli accampamenti si sostituiscono, non si sommano");
        assertEquals("2\nPRIMO\nSECONDO\n2", scrittura.toString().trim().replace("\r\n", "\n"));
    }

    @Test
    void unIntermezzoScattaUnaVoltaSolaEGliAccampamentiSiContano() {
        IntermezziMD intermezzi = new IntermezziMD();
        assertFalse(intermezzi.isScattato("PRIMO"));
        assertEquals(0, intermezzi.getNumeroAccampamenti());
        intermezzi.aggiungiScattato("PRIMO");
        intermezzi.aggiungiScattato("PRIMO");
        intermezzi.incrementaNumeroAccampamenti();
        assertTrue(intermezzi.isScattato("PRIMO"));
        assertEquals(1, intermezzi.getNumeroAccampamenti());
    }

    @Test
    void unaPartitaNuovaDimenticaIntermezziEAccampamenti() {
        IntermezziMD intermezzi = new IntermezziMD();
        intermezzi.aggiungiScattato("PRIMO");
        intermezzi.incrementaNumeroAccampamenti();
        intermezzi.reimposta();
        assertFalse(intermezzi.isScattato("PRIMO"));
        assertEquals(0, intermezzi.getNumeroAccampamenti());
    }

    @Test
    void unFileTroncatoNonSiLegge() {
        // Given: ne annuncia due ma ne contiene uno
        String troncato = "2\nPRIMO\n";
        // When / Then
        assertThrows(IOException.class,
                () -> new IntermezziMD().leggi(new BufferedReader(new StringReader(troncato))));
    }
}
