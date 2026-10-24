package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La mappa della Foresta: le caselle, le informazioni che ne derivano (locazioni uniche, limiti dell'area conosciuta,
 * versione della mappa) e il salvataggio, che le ricostruisce alla rilettura.
 */
class ForestaMDTest {

    /**
     * Una mappa 4 x 3 tutta di radure, con la città di Nyena in (2, 1).
     */
    private static ForestaMD mappa() {
        ForestaMD foresta = new ForestaMD();
        foresta.reimposta(4, 3);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 4; x++) {
                foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.RADURA);
            }
        }
        foresta.impostaLocazione(new CoordinateMD(2, 1), TipoLocazione.CITTA_NYENA);
        foresta.aggiungiLocazioneUnica(TipoLocazione.CITTA_NYENA, new CoordinateMD(2, 1));
        return foresta;
    }

    @Test
    void unaPartitaNuovaNonRipeteLaVersioneDellaMappaPrecedente() {
        ForestaMD primaPartita = new ForestaMD();
        primaPartita.reimposta(3, 3);
        ForestaMD secondaPartita = new ForestaMD();
        secondaPartita.reimposta(3, 3);
        assertNotEquals(primaPartita.getVersioneMappa(), secondaPartita.getVersioneMappa());
        int versione = secondaPartita.getVersioneMappa();
        secondaPartita.reimposta(3, 3);
        assertNotEquals(versione, secondaPartita.getVersioneMappa());
    }

    @Test
    void unaMappaNuovaNonHaCaselleNeLocazioniUnicheNeAreaConosciuta() {
        ForestaMD foresta = new ForestaMD();
        foresta.reimposta(4, 3);
        assertEquals(4, foresta.getDimensioneX());
        assertEquals(3, foresta.getDimensioneY());
        assertNull(foresta.ottieniLocazioneMD(new CoordinateMD(1, 1)));
        assertNull(foresta.ottieniTipoLocazione(new CoordinateMD(1, 1)));
        assertNull(foresta.ottieniCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        assertEquals(-1, foresta.getMinXConosciuta());
        assertEquals(-1, foresta.getMaxXConosciuta());
        assertEquals(-1, foresta.getMinYConosciuta());
        assertEquals(-1, foresta.getMaxYConosciuta());
    }

    @Test
    void ogniCasellaHaLaSuaLocazione() {
        ForestaMD foresta = mappa();
        assertEquals(TipoLocazione.CITTA_NYENA, foresta.ottieniTipoLocazione(new CoordinateMD(2, 1)));
        assertEquals(TipoLocazione.CITTA_NYENA, foresta.ottieniTipoLocazione(2, 1));
        assertEquals(TipoLocazione.RADURA, foresta.ottieniTipoLocazione(1, 2));
        assertNotSame(foresta.ottieniLocazioneMD(new CoordinateMD(0, 0)), foresta.ottieniLocazioneMD(new CoordinateMD(1, 0)),
                "due radure sono due radure, non la stessa");
    }

    @Test
    void unaLocazioneNuovaTieneSoloVisitataEConosciutaDellaCasella() {
        ForestaMD foresta = mappa();
        CoordinateMD casella = new CoordinateMD(1, 1);
        foresta.impostaLocazioneVisitata(casella);
        foresta.impostaLocazioneConosciuta(casella);
        foresta.ottieniLocazioneMD(casella).setNome("La radura dei funghi");

        foresta.impostaLocazione(casella, TipoLocazione.BOSCO);

        assertEquals(TipoLocazione.BOSCO, foresta.ottieniTipoLocazione(casella));
        assertTrue(foresta.isLocazioneVisitata(casella));
        assertTrue(foresta.isLocazioneConosciuta(casella));
        assertNotEquals("La radura dei funghi", foresta.ottieniLocazioneMD(casella).getNome(), "il nome era della radura");

        // Su una casella né visitata né conosciuta non c'è niente da tenere
        CoordinateMD altra = new CoordinateMD(3, 2);
        foresta.impostaLocazione(altra, TipoLocazione.PALUDE);
        assertFalse(foresta.isLocazioneVisitata(altra));
        assertFalse(foresta.isLocazioneConosciuta(altra));
    }

    @Test
    void leLocazioniUnicheSiAggiungonoESiDimenticano() {
        ForestaMD foresta = mappa();
        assertEquals(new CoordinateMD(2, 1), foresta.ottieniCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        foresta.rimuoviLocazioneUnica(TipoLocazione.CITTA_NYENA);
        assertNull(foresta.ottieniCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
    }

    @Test
    void visitareUnaCasellaETogliereIlSegnoCambiaLaVersioneDellaMappa() {
        ForestaMD foresta = mappa();
        CoordinateMD casella = new CoordinateMD(0, 2);
        int versione = foresta.getVersioneMappa();

        foresta.impostaLocazioneVisitata(casella);
        assertTrue(foresta.isLocazioneVisitata(casella));
        assertNotEquals(versione, foresta.getVersioneMappa());

        versione = foresta.getVersioneMappa();
        foresta.impostaLocazioneVisitata(casella, false);
        assertFalse(foresta.isLocazioneVisitata(casella));
        assertNotEquals(versione, foresta.getVersioneMappa());
    }

    @Test
    void leCaselleConosciuteAllarganoLAreaConosciuta() {
        ForestaMD foresta = mappa();
        int versione = foresta.getVersioneMappa();

        foresta.impostaLocazioneConosciuta(new CoordinateMD(2, 1));
        assertTrue(foresta.isLocazioneConosciuta(new CoordinateMD(2, 1)));
        assertFalse(foresta.isLocazioneConosciuta(new CoordinateMD(1, 1)));
        assertNotEquals(versione, foresta.getVersioneMappa());
        assertArea(foresta, 2, 2, 1, 1);

        // Più in basso a sinistra, poi più in alto a destra: i limiti si allargano, e una casella interna non li cambia
        foresta.impostaLocazioneConosciuta(new CoordinateMD(1, 2));
        assertArea(foresta, 1, 2, 1, 2);
        foresta.impostaLocazioneConosciuta(new CoordinateMD(3, 0));
        assertArea(foresta, 1, 3, 0, 2);
        foresta.impostaLocazioneConosciuta(new CoordinateMD(2, 1));
        assertArea(foresta, 1, 3, 0, 2);
    }

    @Test
    void conLaMappaCompletaTutteLeCaselleSonoConosciute() {
        ForestaMD foresta = mappa();
        foresta.ottieniMappa();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 4; x++) {
                assertTrue(foresta.isLocazioneConosciuta(new CoordinateMD(x, y)), x + "," + y);
            }
        }
        assertArea(foresta, 0, 3, 0, 2);
    }

    @Test
    void ilSalvataggioRicostruisceCaselleLocazioniUnicheEAreaConosciuta() throws IOException {
        ForestaMD foresta = mappa();
        foresta.impostaLocazioneConosciuta(new CoordinateMD(1, 2));
        foresta.impostaLocazioneConosciuta(new CoordinateMD(3, 0));
        foresta.impostaLocazioneVisitata(new CoordinateMD(1, 2));

        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            foresta.salva(stream);
        }
        assertEquals(1 + 4 * 3, scritto.toString().split("\n").length, "le dimensioni e una riga per casella");

        ForestaMD riletta = new ForestaMD();
        riletta.leggi(new BufferedReader(new StringReader(scritto.toString())));

        assertEquals(4, riletta.getDimensioneX());
        assertEquals(3, riletta.getDimensioneY());
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 4; x++) {
                CoordinateMD casella = new CoordinateMD(x, y);
                assertEquals(foresta.ottieniTipoLocazione(casella), riletta.ottieniTipoLocazione(casella));
                assertEquals(foresta.isLocazioneConosciuta(casella), riletta.isLocazioneConosciuta(casella));
                assertEquals(foresta.isLocazioneVisitata(casella), riletta.isLocazioneVisitata(casella));
            }
        }
        // Le locazioni uniche e i limiti dell'area conosciuta non si salvano: si ricavano dalle caselle
        assertEquals(new CoordinateMD(2, 1), riletta.ottieniCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        assertArea(riletta, 1, 3, 0, 2);
    }

    @Test
    void unaPosizioneFuoriDallaMappaEUnErrore() {
        ForestaMD foresta = mappa();
        // Senza il controllo (4, 0) sarebbe la casella (0, 1) e (-1, 1) la (3, 0)
        for (int[] fuori : new int[][]{{4, 0}, {-1, 1}, {0, 3}, {0, -1}}) {
            CoordinateMD coordinate = new CoordinateMD(fuori[0], fuori[1]);
            assertThrows(IllegalArgumentException.class, () -> foresta.ottieniLocazioneMD(coordinate));
            assertThrows(IllegalArgumentException.class, () -> foresta.ottieniTipoLocazione(coordinate));
            assertThrows(IllegalArgumentException.class, () -> foresta.impostaLocazione(coordinate, TipoLocazione.RADURA));
        }
    }

    @Test
    void unaMappaNonAncoraPreparataEVuotaENonDaNullPointerException() {
        ForestaMD foresta = new ForestaMD();
        assertNull(foresta.ottieniCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        assertArea(foresta, -1, -1, -1, -1);
        assertThrows(IllegalArgumentException.class, () -> foresta.ottieniTipoLocazione(0, 0));
        foresta.aggiungiLocazioneUnica(TipoLocazione.CITTA_NYENA, new CoordinateMD(0, 0));
        assertEquals(new CoordinateMD(0, 0), foresta.ottieniCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
    }

    @Test
    void dimensioniNonValideNelSalvataggioSonoUnErrore() {
        assertThrows(IOException.class, () -> new ForestaMD().leggi(new BufferedReader(new StringReader("0|3\n"))));
        assertThrows(IOException.class, () -> new ForestaMD().leggi(new BufferedReader(new StringReader("-4|3\n"))));
    }

    private static void assertArea(ForestaMD foresta, int minX, int maxX, int minY, int maxY) {
        assertEquals(minX, foresta.getMinXConosciuta(), "minX");
        assertEquals(maxX, foresta.getMaxXConosciuta(), "maxX");
        assertEquals(minY, foresta.getMinYConosciuta(), "minY");
        assertEquals(maxY, foresta.getMaxYConosciuta(), "maxY");
    }
}
