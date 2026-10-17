package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le statistiche della partita: livello, esperienza, punti, turni, identificativo e mostri uccisi per classe.
 */
class StatisticheMDTest {

    @Test
    void leStatisticheSiContanoESiSalvano() throws IOException {
        StatisticheMD statistiche = new StatisticheMD();
        statistiche.reimposta();
        String idPartita = statistiche.getIdPartita();
        assertNotNull(idPartita);
        statistiche.setLivello(4);
        statistiche.setPuntiEsperienza(320);
        statistiche.setPunti(1500);
        statistiche.setTurniGiocati(88);
        statistiche.addMostroUcciso(TipoPersonaggio.GOBLIN);
        statistiche.addMostroUcciso(TipoPersonaggio.GOBLIN);
        statistiche.addMostroUcciso(TipoPersonaggio.TROLL);
        assertEquals(2, statistiche.getMostriUccisi(TipoPersonaggio.GOBLIN));
        assertEquals(0, statistiche.getMostriUccisi(TipoPersonaggio.DRAGO));

        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            statistiche.salva(stream);
        }
        StatisticheMD riletta = new StatisticheMD();
        riletta.leggi(new BufferedReader(new StringReader(scritto.toString())));

        assertEquals(4, riletta.getLivello());
        assertEquals(320, riletta.getPuntiEsperienza());
        assertEquals(1500, riletta.getPunti());
        assertEquals(88, riletta.getTurniGiocati());
        assertEquals(idPartita, riletta.getIdPartita());
        assertEquals(2, riletta.getMostriUccisi(TipoPersonaggio.GOBLIN));
        assertEquals(1, riletta.getMostriUccisi(TipoPersonaggio.TROLL));
        assertEquals(0, riletta.getMostriUccisi(TipoPersonaggio.DRAGO));
    }
}
