package com.threeamigos.foresta.punteggi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassificaTest {

    @Test
    void ilPipeSparisceDaiNomi() {
        assertEquals("Pippo", Classifica.pulisciNome("Pip|po"));
        assertEquals("Pippo #1", Classifica.pulisciNome("Pippo #1"));
        assertEquals("nessun nome", Classifica.pulisciNome("  "));
        assertEquals("nessun nome", Classifica.pulisciNome("|"));
        assertEquals("nessun nome", Classifica.pulisciNome(null));
    }

    private static Classifica predefinita() {
        Classifica classifica = new Classifica();
        classifica.ripristinaLaPredefinita();
        return classifica;
    }

    private static String nomi(Classifica classifica) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classifica.getConteggio(); i++) {
            sb.append(classifica.getPunteggio(i).getNome()).append(':').append(classifica.getPunteggio(i).getPunteggio()).append(' ');
        }
        return sb.toString().trim();
    }

    @Test
    void unaPartitaNuovaEntraESpingeFuoriLUltimo() {
        Classifica classifica = predefinita();
        assertTrue(classifica.isPunteggioInClassifica(5500, "partita-1"));
        assertTrue(classifica.inserisci("Arsenio", 5500, "partita-1"));
        assertEquals("Stefano:10000 Johan:9000 Alessandra:8000 Peter Porker:7000 Judah:6000 Arsenio:5500 Jona:5000 "
                + "Jamaikan:2000 Cosimo:1000 Oreste:500", nomi(classifica));
        assertEquals("partita-1", classifica.getPunteggio(5).getIdPartita());
    }

    @Test
    void unPunteggioTroppoBassoNonEntra() {
        Classifica classifica = predefinita();
        assertFalse(classifica.isPunteggioInClassifica(0, "partita-1"));
        assertFalse(classifica.inserisci("Arsenio", 0, "partita-1"));
        assertEquals("Axel e il sERCIO", classifica.getPunteggio(9).getNome());
    }

    @Test
    void laStessaPartitaEntraSoloMigliorandosiESostituisceIlSuoPunteggio() {
        Classifica classifica = predefinita();
        classifica.inserisci("Arsenio", 5500, "partita-1");

        // Ricaricata e rifinita peggio: non entra, e non si chiede il nome
        assertFalse(classifica.isPunteggioInClassifica(3000, "partita-1"));
        assertFalse(classifica.isPunteggioInClassifica(5500, "partita-1"));
        classifica.inserisci("Arsenio", 3000, "partita-1");
        assertEquals("Arsenio:5500", classifica.getPunteggio(5).getNome() + ":" + classifica.getPunteggio(5).getPunteggio());

        // Rifinita meglio: il suo punteggio sale, gli altri restano tutti
        assertTrue(classifica.isPunteggioInClassifica(8500, "partita-1"));
        classifica.inserisci("Arsenio", 8500, "partita-1");
        assertEquals("Stefano:10000 Johan:9000 Arsenio:8500 Alessandra:8000 Peter Porker:7000 Judah:6000 Jona:5000 "
                + "Jamaikan:2000 Cosimo:1000 Oreste:500", nomi(classifica));

        // Un'altra partita con un punteggio più basso del suo entra comunque al suo posto
        classifica.inserisci("Brunilde", 5200, "partita-2");
        assertEquals("Stefano:10000 Johan:9000 Arsenio:8500 Alessandra:8000 Peter Porker:7000 Judah:6000 Brunilde:5200 "
                + "Jona:5000 Jamaikan:2000 Cosimo:1000", nomi(classifica));
    }
}
