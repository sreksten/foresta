package com.threeamigos.foresta.modellodati;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un messaggio del pannello di testo: se apriva un paragrafo o continuava il precedente, il testo, e come si salva.
 */
class MessaggioMDTest {

    private static String salva(MessaggioMD messaggio) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            messaggio.salva(stream);
        }
        return scritto.toString();
    }

    private static MessaggioMD rileggi(String salvato) throws IOException {
        MessaggioMD riletto = new MessaggioMD();
        riletto.leggi(new BufferedReader(new StringReader(salvato)));
        return riletto;
    }

    @Test
    void unParagrafoEUnaFraseSiSalvanoESiRileggono() throws IOException {
        MessaggioMD paragrafo = new MessaggioMD("Un paragrafo", true);
        MessaggioMD frase = new MessaggioMD("Una frase", false);
        assertNotEquals(paragrafo.getUuid(), frase.getUuid());

        String salvato = salva(paragrafo);
        assertEquals(paragrafo.getUuid() + "|P|Un paragrafo", salvato.trim());
        MessaggioMD paragrafoRiletto = rileggi(salvato);
        assertEquals(paragrafo.getUuid(), paragrafoRiletto.getUuid());
        assertTrue(paragrafoRiletto.isParagrafo());
        assertEquals("Un paragrafo", paragrafoRiletto.getTesto());

        MessaggioMD fraseRiletta = rileggi(salva(frase));
        assertFalse(fraseRiletta.isParagrafo());
        assertEquals("Una frase", fraseRiletta.getTesto());
    }

    @Test
    void ilTestoPuoContenereIlSeparatoreEGliACapoDiventanoSpazi() throws IOException {
        MessaggioMD riletto = rileggi(salva(new MessaggioMD("Prima | dopo\ne ancora", false)));
        assertEquals("Prima | dopo e ancora", riletto.getTesto());
    }

    @Test
    void unTestoCheMancaSiRileggeVuotoENonComeLaParolaNull() throws IOException {
        MessaggioMD senzaTesto = new MessaggioMD(null, true);
        String salvato = salva(senzaTesto);
        assertFalse(salvato.contains("null"), salvato);
        assertEquals("", rileggi(salvato).getTesto());
    }

    @Test
    void unTipoCheNonEPoFNonSiLegge() {
        for (String tipo : new String[]{"X", "PF", ""}) {
            IOException errore = assertThrows(IOException.class, () -> rileggi("uuid|" + tipo + "|testo"), tipo);
            assertTrue(errore.getMessage().contains("Tipo di messaggio non valido"), errore.getMessage());
        }
    }

    @Test
    void unFlussoFinitoNonSiLegge() {
        assertThrows(IOException.class, () -> rileggi(""));
    }
}
