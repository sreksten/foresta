package com.threeamigos.foresta.modellodati;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il tempo della partita: ora, giorno, evento da mostrare e gioco finito, e come si salvano.
 */
class LineaTemporaleMDTest {

    private static String salva(LineaTemporaleMD linea) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            linea.salva(stream);
        }
        return scritto.toString();
    }

    private static LineaTemporaleMD rileggi(String salvato) throws IOException {
        LineaTemporaleMD riletta = new LineaTemporaleMD();
        riletta.leggi(new BufferedReader(new StringReader(salvato)));
        return riletta;
    }

    @Test
    void unaPartitaNuovaCominciaAlPrimoGiornoDiMattina() {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.setOra(22);
        linea.setGiorno(17);
        linea.setGiocoFinito(true);
        linea.setEvento("Un evento");

        linea.reimposta();

        assertEquals(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO, linea.getOra());
        assertEquals(1, linea.getGiorno());
        assertFalse(linea.isGiocoFinito());
        assertNull(linea.getEvento());
    }

    @Test
    void siSalvanoOraGiornoEdEventoMaNonIlGiocoFinito() throws IOException {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();
        linea.setOra(18);
        linea.setGiorno(29);
        linea.setEvento("Gwendolyn vede levarsi una colonna di fumo a nord");
        linea.setGiocoFinito(true);

        String salvato = salva(linea);
        assertEquals("18|29|Gwendolyn vede levarsi una colonna di fumo a nord", salvato.trim());

        LineaTemporaleMD riletta = rileggi(salvato);
        assertEquals(18, riletta.getOra());
        assertEquals(29, riletta.getGiorno());
        assertEquals("Gwendolyn vede levarsi una colonna di fumo a nord", riletta.getEvento());
        assertFalse(riletta.isGiocoFinito(), "una partita finita non si salva: alla rilettura non lo è");
    }

    @Test
    void senzaEventoLaRigaHaSoloIlCampoVuoto() throws IOException {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();

        String salvato = salva(linea);
        assertEquals("8|1|", salvato.trim());

        LineaTemporaleMD riletta = rileggi(salvato);
        assertNull(riletta.getEvento());
    }

    @Test
    void unEventoConIlSeparatoreOGliACapoNonRovinaLaRiga() throws IOException {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();
        linea.setEvento("Fumo|a nord\ne a sud");

        LineaTemporaleMD riletta = rileggi(salva(linea));

        assertEquals("Fumoa nord e a sud", riletta.getEvento());
    }

    @Test
    void rileggendoIlGiocoNonEFinito() throws IOException {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();
        String salvato = salva(linea);

        LineaTemporaleMD riletta = new LineaTemporaleMD();
        riletta.setGiocoFinito(true);
        riletta.leggi(new BufferedReader(new StringReader(salvato)));

        assertFalse(riletta.isGiocoFinito());
    }
}
