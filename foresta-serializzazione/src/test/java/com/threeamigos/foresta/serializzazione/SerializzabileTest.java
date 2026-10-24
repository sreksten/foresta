package com.threeamigos.foresta.serializzazione;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Come si scrive una riga di un salvataggio: i campi separati da "|", gli enum con il loro nome, i campi che possono
 * mancare vuoti.
 */
class SerializzabileTest {

    private enum Colore {
        ROSSO
    }

    private static String scritto(java.util.function.Consumer<PrintWriter> scrittura) {
        StringWriter testo = new StringWriter();
        PrintWriter stream = new PrintWriter(testo);
        scrittura.accept(stream);
        stream.flush();
        return testo.toString();
    }

    @Test
    void unEnumSiScriveConIlSuoNomeIlRestoComeIlTesto() {
        assertEquals("ROSSO", Serializzabile.campo(Colore.ROSSO));
        assertEquals("12", Serializzabile.campo(12));
        assertEquals("1.5", Serializzabile.campo(1.5d));
        assertEquals("uno", Serializzabile.campo("uno"));
        assertEquals("null", Serializzabile.campo(null), "un campo che può mancare passa da facoltativo");
    }

    @Test
    void unCampoFacoltativoCheMancaSiScriveVuoto() {
        assertEquals("", Serializzabile.facoltativo(null));
        assertEquals("ROSSO", Serializzabile.facoltativo(Colore.ROSSO));
        assertEquals("7", Serializzabile.facoltativo(7));
        assertEquals("testo", Serializzabile.facoltativo("testo"));
    }

    @Test
    void unaRigaHaICampiSeparatiDalPipeELaFineRiga() {
        String riga = scritto(stream -> Serializzabile.stampaRiga(stream, "a", 2, Colore.ROSSO));
        assertEquals("a|2|ROSSO" + System.lineSeparator(), riga);
        assertEquals(System.lineSeparator(), scritto(stream -> Serializzabile.stampaRiga(stream)), "senza valori è una riga vuota");
    }

    @Test
    void unaRigaSiScriveAncheDaUnaCollezione() {
        String riga = scritto(stream -> Serializzabile.stampaRiga(stream, Arrays.asList("x", 3, Colore.ROSSO)));
        assertEquals("x|3|ROSSO" + System.lineSeparator(), riga);
        assertEquals(System.lineSeparator(), scritto(stream -> Serializzabile.stampaRiga(stream, Collections.emptyList())));
    }

    @Test
    void piuValoriInUnCampoSiSeparanoConIlSeparatoreDeiSottocampi() throws IOException {
        String composto = Serializzabile.composto("FORZA", 12, Colore.ROSSO);
        assertEquals("FORZA§12§ROSSO", composto);
        LettoreCampi sottocampi = new LettoreCampi(composto).sottocampi(3, 3);
        assertEquals("FORZA", sottocampi.testo());
        assertEquals(12, sottocampi.intero());
        assertEquals(Colore.ROSSO, sottocampi.enumerato(Colore.class));
    }

    @Test
    void gliACapoSonoSostituitiDaUnoSpazioEIlPipeResta() {
        assertNull(Serializzabile.senzaACapo(null));
        assertEquals("a b c d|e", Serializzabile.senzaACapo("a\nb\r\nc\rd|e"));
        assertEquals("niente", Serializzabile.senzaACapo("niente"));
    }
}
