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

    @Test
    void siScrivonoSoloIMostriUccisiConIlNomeDellaClasse() throws Exception {
        StatisticheMD statistiche = new StatisticheMD();
        statistiche.addMostroUcciso(TipoPersonaggio.GOBLIN);
        statistiche.addMostroUcciso(TipoPersonaggio.GOBLIN);
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            statistiche.salva(stream);
        }
        String[] righe = scritto.toString().split("\\R");
        assertEquals("GOBLIN|2", righe[1]);
    }

    @Test
    void unSalvataggioSenzaMostriOSenzaLeClassiNuoveSiLegge() throws Exception {
        StatisticheMD senzaMostri = new StatisticheMD();
        senzaMostri.leggi(new BufferedReader(new StringReader("1|0|0|0|id\n\n")));
        assertEquals(0, senzaMostri.getMostriUccisi(TipoPersonaggio.GOBLIN));
        // Un elenco più corto di quello di oggi (scritto prima che esistessero chierico e druido)
        StatisticheMD vecchia = new StatisticheMD();
        vecchia.leggi(new BufferedReader(new StringReader("1|0|0|0|id\nARPIA|0|GOBLIN|3|SACERDOTESSA|1\n")));
        assertEquals(3, vecchia.getMostriUccisi(TipoPersonaggio.GOBLIN));
        assertEquals(1, vecchia.getMostriUccisi(TipoPersonaggio.SACERDOTESSA));
        assertEquals(0, vecchia.getMostriUccisi(TipoPersonaggio.DRUIDO));
    }

    @Test
    void unaClasseCheNonEsistePiuSiSalta() throws Exception {
        StatisticheMD statistiche = new StatisticheMD();
        statistiche.leggi(new BufferedReader(new StringReader("1|0|0|0|id\nGOBLIN|3|BASILISCO|7|TROLL|1\n")));
        assertEquals(3, statistiche.getMostriUccisi(TipoPersonaggio.GOBLIN));
        assertEquals(1, statistiche.getMostriUccisi(TipoPersonaggio.TROLL));
    }
}
