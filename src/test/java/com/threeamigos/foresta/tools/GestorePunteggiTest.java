package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.modellodati.LettoreCampi;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestorePunteggiTest {

    @Test
    void ilPipeSparisceDaiNomi() {
        assertEquals("Pippo", GestorePunteggiBase.pulisciNome("Pip|po"));
        assertEquals("Pippo #1", GestorePunteggiBase.pulisciNome("Pippo #1"));
        assertEquals("nessun nome", GestorePunteggiBase.pulisciNome("  "));
        assertEquals("nessun nome", GestorePunteggiBase.pulisciNome("|"));
        assertEquals("nessun nome", GestorePunteggiBase.pulisciNome(null));
    }

    @Test
    void unaRigaDellaClassificaSiLegge() throws IOException {
        LettoreCampi campi = new LettoreCampi("Pippo|1234");
        assertEquals("Pippo", campi.testo());
        assertEquals(1234, campi.intero());
    }

    /**
     * Una classifica che parte da quella predefinita e non scrive niente su disco.
     */
    private static final class ClassificaInMemoria extends GestorePunteggiBase {
        @Override
        public boolean carica() {
            return false;
        }

        @Override
        public boolean salva() {
            return true;
        }
    }

    private static String nomi(GestorePunteggiBase classifica) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classifica.getConteggio(); i++) {
            sb.append(classifica.getPunteggio(i).getNome()).append(':').append(classifica.getPunteggio(i).getPunteggio()).append(' ');
        }
        return sb.toString().trim();
    }

    @Test
    void unaPartitaNuovaEntraESpingeFuoriLUltimo() {
        ClassificaInMemoria classifica = new ClassificaInMemoria();
        assertTrue(classifica.isPunteggioInClassifica(5500, "partita-1"));
        classifica.addPunteggio("Arsenio", 5500, "partita-1");
        assertEquals("Stefano:10000 Johan:9000 Alessandra:8000 Peter Porker:7000 Judah:6000 Arsenio:5500 Jona:5000 "
                + "Jamaikan:2000 Cosimo:1000 Oreste:500", nomi(classifica));
        assertEquals("partita-1", classifica.getPunteggio(5).getIdPartita());
    }

    @Test
    void laStessaPartitaEntraSoloMigliorandosiESostituisceIlSuoPunteggio() {
        ClassificaInMemoria classifica = new ClassificaInMemoria();
        classifica.addPunteggio("Arsenio", 5500, "partita-1");

        // Ricaricata e rifinita peggio: non entra, e non si chiede il nome
        assertFalse(classifica.isPunteggioInClassifica(3000, "partita-1"));
        assertFalse(classifica.isPunteggioInClassifica(5500, "partita-1"));
        classifica.addPunteggio("Arsenio", 3000, "partita-1");
        assertEquals("Arsenio:5500", classifica.getPunteggio(5).getNome() + ":" + classifica.getPunteggio(5).getPunteggio());

        // Rifinita meglio: il suo punteggio sale, gli altri restano tutti
        assertTrue(classifica.isPunteggioInClassifica(8500, "partita-1"));
        classifica.addPunteggio("Arsenio", 8500, "partita-1");
        assertEquals("Stefano:10000 Johan:9000 Arsenio:8500 Alessandra:8000 Peter Porker:7000 Judah:6000 Jona:5000 "
                + "Jamaikan:2000 Cosimo:1000 Oreste:500", nomi(classifica));

        // Un'altra partita con un punteggio più basso del suo entra comunque al suo posto
        classifica.addPunteggio("Brunilde", 5200, "partita-2");
        assertEquals("Stefano:10000 Johan:9000 Arsenio:8500 Alessandra:8000 Peter Porker:7000 Judah:6000 Brunilde:5200 "
                + "Jona:5000 Jamaikan:2000 Cosimo:1000", nomi(classifica));
    }
}
