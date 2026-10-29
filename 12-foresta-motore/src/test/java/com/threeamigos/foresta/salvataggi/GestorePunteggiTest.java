package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GestorePunteggiTest {

    @Test
    void unaRigaDellaClassificaSiLegge() throws IOException {
        LettoreCampi campi = new LettoreCampi("Pippo|1234");
        assertEquals("Pippo", campi.testo());
        assertEquals(1234, campi.intero());
    }

    /**
     * Una classifica che parte da quella predefinita e conta quante volte si salva.
     */
    private static final class ClassificaCheContaISalvataggi extends GestorePunteggiBase {
        int salvataggi;

        @Override
        public boolean carica() {
            return false;
        }

        @Override
        public boolean salva() {
            salvataggi++;
            return true;
        }
    }

    @Test
    void siSalvaSoloQuandoUnPunteggioEntra() {
        ClassificaCheContaISalvataggi classifica = new ClassificaCheContaISalvataggi();
        assertEquals(1, classifica.salvataggi, "la classifica predefinita si scrive subito");

        classifica.addPunteggio("Arsenio", 5500, "partita-1");
        assertEquals(2, classifica.salvataggi);
        assertEquals("Arsenio", classifica.getPunteggio(5).getNome());

        classifica.addPunteggio("Arsenio", 3000, "partita-1");
        assertEquals(2, classifica.salvataggi, "un punteggio che non entra non riscrive niente");
    }

    @Test
    void unaClassificaRovinataSiMetteDaParteEPartiDaQuellaPredefinita(@org.junit.jupiter.api.io.TempDir java.nio.file.Path cartella) throws IOException {
        java.nio.file.Files.write(cartella.resolve("forestaHS"), java.util.Arrays.asList("p1|Stefano|10000", "p2|Johan|non un numero"),
                java.nio.charset.StandardCharsets.UTF_8);

        // La classifica si legge già nel costruttore: non riuscendoci riparte da quella predefinita, e la scrive
        GestorePunteggiSuFile classifica = new GestorePunteggiSuFile(cartella.toFile());

        assertEquals(10, classifica.getConteggio());
        assertTrue(java.nio.file.Files.readAllLines(cartella.resolve("forestaHS.rovinato"), java.nio.charset.StandardCharsets.UTF_8)
                .contains("p1|Stefano|10000"), "il file rovinato si conserva");
        assertTrue(java.nio.file.Files.isRegularFile(cartella.resolve("forestaHS")), "e la classifica predefinita si scrive a parte");
        assertTrue(new GestorePunteggiSuFile(cartella.toFile()).carica());
    }
}
