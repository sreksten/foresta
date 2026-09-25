package com.threeamigos.foresta.motore.modellodati;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
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
        StringWriter scrittura = new StringWriter();
        try (PrintWriter writer = new PrintWriter(scrittura)) {
            originale.salva(writer);
        }

        // When
        IntermezziMD riletto = new IntermezziMD();
        riletto.aggiungiScattato("DA_DIMENTICARE");
        riletto.leggi(new BufferedReader(new StringReader(scrittura.toString())));

        // Then
        assertTrue(riletto.isScattato("PRIMO"));
        assertTrue(riletto.isScattato("SECONDO"));
        assertFalse(riletto.isScattato("DA_DIMENTICARE"));
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
