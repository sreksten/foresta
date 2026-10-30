package com.threeamigos.foresta.economia;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaMonete;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPreziosi;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.CassaMD;
import com.threeamigos.foresta.tipi.CostantiEconomia;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CassaTest {

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(10, 8);
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    @Test
    void unaPartitaNuovaParteConLeMonetePrevisteELaUILeLegge() {
        Cassa.reimposta();
        assertEquals(CostantiEconomia.MONETE_INIZIALI, Cassa.getMonete());
        assertEquals(CostantiEconomia.PREZIOSI_INIZIALI, Cassa.getPreziosi());
        assertEquals(Cassa.getMonete(), Cassa.VISTA.getMonete());
        assertEquals(Cassa.getPreziosi(), Cassa.VISTA.getPreziosi());
    }

    @Test
    void ognVariazioneSiAnnunciaAllaUI() {
        List<NotificaVariazioneDisponibilitaMonete> monete = new ArrayList<>();
        List<NotificaVariazioneDisponibilitaPreziosi> preziosi = new ArrayList<>();
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaMonete.class, monete::add);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPreziosi.class, preziosi::add);
        Cassa.addMonete(30);
        Cassa.subMonete(10);
        Cassa.addPreziosi(5);
        Cassa.subPreziosi(2);
        assertEquals(20, Cassa.getMonete());
        assertEquals(3, Cassa.getPreziosi());
        assertEquals(2, monete.size());
        assertEquals(0, monete.get(0).getValorePrecedente());
        assertEquals(30, monete.get(0).getNuovoValore());
        assertEquals(2, preziosi.size());
        assertEquals(5, preziosi.get(1).getValorePrecedente());
        assertEquals(3, preziosi.get(1).getNuovoValore());
    }

    @Test
    void iPreziosiSiVendonoPerLeMoneteCalcolateDaChiChiama() {
        Cassa.addPreziosi(4);
        Cassa.vendiPreziosiPer(40);
        assertEquals(40, Cassa.getMonete());
        assertEquals(0, Cassa.getPreziosi());
    }

    @Test
    void siCompraSoloSeLeMoneteBastano() {
        Artefatto spada = MondoDiProva.artefatto(TipoArtefatto.SPADA, 2);
        Cassa.addMonete(10);
        assertFalse(Cassa.compra(spada, 11));
        assertEquals(10, Cassa.getMonete());
        assertTrue(Cassa.compra(spada, 10));
        assertEquals(0, Cassa.getMonete());
        assertEquals(1, GruppoGiocatore.getIstanza().getInventario().size());
    }

    @Test
    void nellaFugaSiPerdeUnaPercentualeArrotondataPerDifetto() {
        Cassa.addMonete(101);
        Cassa.addPreziosi(7);
        Cassa.perdeNellaFuga();
        assertTrue(Cassa.getMonete() < 101);
        assertTrue(Cassa.getMonete() > 0);
        assertTrue(Cassa.getPreziosi() <= 7);
    }

    @Test
    void senzaIDatiLaCassaNonVa() {
        Cassa.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, Cassa::getMonete);
    }

    @Test
    void laCassaSiSalvaESiRilegge() throws Exception {
        CassaMD md = new CassaMD();
        md.setMonete(12);
        md.setPreziosi(3);
        StringWriter scritto = new StringWriter();
        PrintWriter stream = new PrintWriter(scritto);
        md.salva(stream);
        stream.flush();
        CassaMD letta = new CassaMD();
        letta.leggi(new BufferedReader(new StringReader(scritto.toString())));
        assertEquals(12, letta.getMonete());
        assertEquals(3, letta.getPreziosi());
        letta.reimposta();
        assertEquals(0, letta.getMonete());
        assertEquals(0, letta.getPreziosi());
    }
}
