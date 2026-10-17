package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il tempo della partita: ora, giorno, città distrutte, evento da mostrare e gioco finito, e come si salvano.
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
        linea.addCittaDistrutta(TipoLocazione.CITTA_RUUNA);

        linea.reimposta();

        assertEquals(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO, linea.getOra());
        assertEquals(1, linea.getGiorno());
        assertFalse(linea.isGiocoFinito());
        assertNull(linea.getEvento());
        assertTrue(linea.getCittaDistrutte().isEmpty());
    }

    @Test
    void leCittaDistrutteSiRicordanoNellOrdineESoloInLettura() {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();
        linea.addCittaDistrutta(TipoLocazione.CITTA_RUUNA);
        linea.addCittaDistrutta(TipoLocazione.CITTA_NYENA);

        assertEquals(Arrays.asList(TipoLocazione.CITTA_RUUNA, TipoLocazione.CITTA_NYENA), linea.getCittaDistrutte());
        assertTrue(linea.isCittaDistrutta(TipoLocazione.CITTA_NYENA));
        assertFalse(linea.isCittaDistrutta(TipoLocazione.CITTA_FLEENA));
        assertThrows(UnsupportedOperationException.class, () -> linea.getCittaDistrutte().add(TipoLocazione.CITTA_FLEENA));
    }

    @Test
    void siSalvanoOraGiornoEventoECittaDistrutteMaNonIlGiocoFinito() throws IOException {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();
        linea.setOra(18);
        linea.setGiorno(29);
        linea.setEvento("Gwendolyn vede levarsi una colonna di fumo a nord");
        linea.addCittaDistrutta(TipoLocazione.CITTA_RUUNA);
        linea.addCittaDistrutta(TipoLocazione.CITTA_NYENA);
        linea.setGiocoFinito(true);

        String salvato = salva(linea);
        assertEquals("18|29|Gwendolyn vede levarsi una colonna di fumo a nord|CITTA_RUUNA|CITTA_NYENA", salvato.trim());

        LineaTemporaleMD riletta = rileggi(salvato);
        assertEquals(18, riletta.getOra());
        assertEquals(29, riletta.getGiorno());
        assertEquals("Gwendolyn vede levarsi una colonna di fumo a nord", riletta.getEvento());
        assertEquals(Arrays.asList(TipoLocazione.CITTA_RUUNA, TipoLocazione.CITTA_NYENA), riletta.getCittaDistrutte());
        assertFalse(riletta.isGiocoFinito(), "una partita finita non si salva: alla rilettura non lo è");
    }

    @Test
    void senzaEventoNeCittaDistrutteLaRigaHaSoloIlCampoVuoto() throws IOException {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();

        String salvato = salva(linea);
        assertEquals("8|1|", salvato.trim());

        LineaTemporaleMD riletta = rileggi(salvato);
        assertNull(riletta.getEvento());
        assertEquals(Collections.emptyList(), riletta.getCittaDistrutte());
    }

    @Test
    void unEventoConIlSeparatoreOGliACapoNonRovinaLaRiga() throws IOException {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();
        linea.setEvento("Fumo|a nord\ne a sud");
        linea.addCittaDistrutta(TipoLocazione.CITTA_FLEENA);

        LineaTemporaleMD riletta = rileggi(salva(linea));

        assertEquals("Fumoa nord e a sud", riletta.getEvento());
        assertEquals(Collections.singletonList(TipoLocazione.CITTA_FLEENA), riletta.getCittaDistrutte());
    }

    @Test
    void rileggendoSiDimenticanoLeCittaDistrutteDiPrima() throws IOException {
        LineaTemporaleMD linea = new LineaTemporaleMD();
        linea.reimposta();
        String senzaCitta = salva(linea);

        LineaTemporaleMD riletta = new LineaTemporaleMD();
        riletta.addCittaDistrutta(TipoLocazione.CITTA_MALGAARD);
        riletta.setGiocoFinito(true);
        riletta.leggi(new BufferedReader(new StringReader(senzaCitta)));

        assertTrue(riletta.getCittaDistrutte().isEmpty());
        assertFalse(riletta.isGiocoFinito());
    }
}
