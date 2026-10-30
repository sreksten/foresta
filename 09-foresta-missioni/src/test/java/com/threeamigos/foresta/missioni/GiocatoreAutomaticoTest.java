package com.threeamigos.foresta.missioni;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tutte le missioni del gioco, giocate da un giocatore che non capisce niente (vedi GiocatoreAutomatico) in mondi diversi: nessuna deve rompersi
 * e parecchie devono arrivare in fondo.
 */
class GiocatoreAutomaticoTest {

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    /**
     * Gli errori che il giocatore automatico incontra senza che siano della missione: le classi inventate dei test che la fabbrica non conosce, le
     * locande di prova senza dialogo (di cui si occupano gli intermezzi) e i passi percorsi a forza, che a volte non hanno alle spalle quelli
     * che li preparano.
     */
    static boolean isRumoreNoto(String errore) {
        return errore.startsWith("avvia:") || errore.startsWith("intermezzo ") || errore.startsWith("percorso ");
    }

    @Test
    void gira() {
        Map<String, String> inattesi = new TreeMap<>();
        Map<String, Integer> esiti = new TreeMap<>();
        for (int seme = 1; seme <= 8; seme++) {
            MondoDiProva.nuovo(seme);
            RegistroMissioni.reimposta();
            RegistroMissioni.registrati();
            GiocatoreAutomatico g = new GiocatoreAutomatico(seme);
            g.gioca(250);
            g.getErrori().forEach((k, v) -> {
                if (!isRumoreNoto(k)) {
                    inattesi.put(k, v);
                }
            });
            g.getEsiti().forEach((k, v) -> esiti.merge(k, v, Integer::sum));
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
        long completate = esiti.entrySet().stream().filter(e -> e.getKey().endsWith(" completa")).count();
        assertTrue(completate >= 15, "le missioni arrivano in fondo: " + esiti);
    }
}
