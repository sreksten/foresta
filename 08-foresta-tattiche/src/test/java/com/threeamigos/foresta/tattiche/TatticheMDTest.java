package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.TatticheMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoOrdinamento;
import com.threeamigos.foresta.tipi.TipoRosa;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le tattiche dei personaggi: legate al personaggio dal suo identificativo, si salvano e si rileggono.
 */
class TatticheMDTest {

    private static TatticaMD unaTattica() {
        return TatticaMD.di(VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(),
                OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE)), AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
    }

    private static String salva(TatticheMD tattiche) throws IOException {
        StringWriter scritto = new StringWriter();
        PrintWriter stream = new PrintWriter(scritto);
        tattiche.salva(stream);
        stream.flush();
        return scritto.toString();
    }

    @Test
    void chiNonHaUnaTatticaNonLaTrova() {
        assertNull(new TatticheMD().get("sconosciuto"));
    }

    @Test
    void laTatticaSiDaESiToglie() {
        TatticheMD tattiche = new TatticheMD();
        TatticaMD tattica = unaTattica();

        tattiche.imposta("a", tattica);
        assertSame(tattica, tattiche.get("a"));
        assertEquals(1, tattiche.getTattiche().size());

        tattiche.imposta("a", null);
        assertNull(tattiche.get("a"));
        assertTrue(tattiche.getTattiche().isEmpty());
    }

    @Test
    void sonoInSolaLettura() {
        assertThrows(UnsupportedOperationException.class, () -> new TatticheMD().getTattiche().put("a", unaTattica()));
    }

    @Test
    void reimpostaLeTogliTutte() {
        TatticheMD tattiche = new TatticheMD();
        tattiche.imposta("a", unaTattica());
        tattiche.reimposta();
        assertTrue(tattiche.getTattiche().isEmpty());
    }

    @Test
    void siSalvanoESiRileggonoSostituendoQuelleDiPrima() throws IOException {
        TatticheMD tattiche = new TatticheMD();
        tattiche.imposta("a", unaTattica());
        tattiche.imposta("b", TatticaMD.VUOTA);
        String salvato = salva(tattiche);

        TatticheMD riletto = new TatticheMD();
        riletto.imposta("vecchio", unaTattica());
        riletto.leggi(new BufferedReader(new StringReader(salvato)));

        assertNull(riletto.get("vecchio"), "quelle di prima si sostituiscono");
        assertEquals(unaTattica(), riletto.get("a"));
        assertEquals(TatticaMD.VUOTA, riletto.get("b"));
        assertEquals(salvato, salva(riletto));
    }

    @Test
    void senzaTattichePartitaVuota() throws IOException {
        TatticheMD riletto = new TatticheMD();
        riletto.imposta("a", unaTattica());
        riletto.leggi(new BufferedReader(new StringReader(salva(new TatticheMD()))));
        assertTrue(riletto.getTattiche().isEmpty());
    }
}
