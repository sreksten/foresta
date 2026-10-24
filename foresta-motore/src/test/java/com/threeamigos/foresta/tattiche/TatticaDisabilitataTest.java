package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.*;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Una tattica incoerente letta da un salvataggio non fa fallire il caricamento: si disabilita (non si esegue) e resta com'è, perché il giocatore la
 * corregga nella schermata delle tattiche.
 */
class TatticaDisabilitataTest {

    private static VoceMD voce(TipoRosa rosa, TipoAzione azione) {
        return VoceMD.su(new SelettoreMD(rosa, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)), AzioneMD.di(azione));
    }

    private static TatticaMD rileggi(TatticaMD tattica) throws Exception {
        StringWriter scritto = new StringWriter();
        PrintWriter stream = new PrintWriter(scritto);
        tattica.salva(stream);
        stream.flush();
        return TatticaMD.leggi(new BufferedReader(new StringReader(scritto.toString())));
    }

    @Test
    void unaTatticaCoerenteSiRileggeAbilitata() throws Exception {
        TatticaMD tattica = TatticaMD.di(voce(TipoRosa.AVVERSARI, TipoAzione.ATTACCO_FISICO));
        TatticaMD riletta = rileggi(tattica);
        assertFalse(riletta.isDisabilitata());
        assertEquals(tattica, riletta);
    }

    @Test
    void unaTatticaIncoerenteSiRileggeDisabilitataEConservaLeSueVoci() throws Exception {
        // Un attacco sugli alleati: né per i miei né per i mostri
        TatticaMD incoerente = TatticaMD.di(voce(TipoRosa.AVVERSARI, TipoAzione.ATTACCO_FISICO), voce(TipoRosa.ALLEATI, TipoAzione.ATTACCO_FISICO));
        assertFalse(incoerente.isDisabilitata(), "il costruttore non controlla: lo fa la rilettura");
        assertFalse(incoerente.problemiPerQualsiasiLato().isEmpty());
        TatticaMD riletta = rileggi(incoerente);
        assertTrue(riletta.isDisabilitata());
        assertEquals(incoerente.getVoci(), riletta.getVoci());
        // Il flag si salva: riletta di nuovo resta disabilitata
        assertTrue(rileggi(riletta).isDisabilitata());
        assertNotEquals(incoerente, riletta);
    }

    @Test
    void unaRosaSoloDeiMostriNonEUnErrorePerUnMostro() {
        TatticaMD deiMostri = TatticaMD.di(voce(TipoRosa.AVVERSARI_E_OSPITI, TipoAzione.ATTACCO_FISICO));
        assertFalse(deiMostri.problemi(true).isEmpty(), "per un mio personaggio non vale");
        assertTrue(deiMostri.problemiPerQualsiasiLato().isEmpty(), "ma per un mostro sì");
    }

    @Test
    void laTatticaDiOggiEQuelleBaseNonSiDisabilitano() throws Exception {
        assertFalse(rileggi(RegistroTattiche.tatticaDiOggi()).isDisabilitata());
        for (com.threeamigos.foresta.tipi.TipoPersonaggio classe : TatticheBase.classiDeiMostri()) {
            assertFalse(rileggi(TatticheBase.deiMostri(classe)).isDisabilitata(), classe.name());
        }
    }
}
