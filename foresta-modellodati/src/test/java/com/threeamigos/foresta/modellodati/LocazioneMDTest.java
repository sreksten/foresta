package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il modello dati di una casella: tipo, identificativo, proprietà (nome compreso) e come si salvano.
 */
class LocazioneMDTest {

    private static String salva(LocazioneMD locazione) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            locazione.salva(stream);
        }
        return scritto.toString();
    }

    private static LocazioneMD rileggi(LocazioneMD destinazione, String salvato) throws IOException {
        destinazione.leggi(new BufferedReader(new StringReader(salvato)));
        return destinazione;
    }

    @Test
    void ogniCasellaHaIlSuoIdentificativoEIlSuoTipo() {
        LocazioneMD radura = new LocazioneMD(TipoLocazione.RADURA);
        LocazioneMD altraRadura = new LocazioneMD(TipoLocazione.RADURA);
        assertNotNull(radura.getUuid());
        assertNotEquals(radura.getUuid(), altraRadura.getUuid(), "due radure sono due caselle diverse");
        assertEquals(TipoLocazione.RADURA, radura.getTipo());
        radura.setTipo(TipoLocazione.ROVINE);
        assertEquals(TipoLocazione.ROVINE, radura.getTipo());
        assertNull(new LocazioneMD().getTipo());
    }

    @Test
    void leProprietaSiAggiungonoSiSostituisconoESiTolgono() {
        LocazioneMD locazione = new LocazioneMD(TipoLocazione.BOSCO);
        assertNull(locazione.ottieniProprieta(LocazioneMD.VISITATA));
        locazione.aggiungiProprieta(LocazioneMD.VISITATA, LocazioneMD.AFFERMATIVO);
        assertEquals(LocazioneMD.AFFERMATIVO, locazione.ottieniProprieta(LocazioneMD.VISITATA));
        locazione.aggiungiProprieta("NOTA", "prima");
        locazione.aggiungiProprieta("NOTA", "dopo|con il separatore");
        assertEquals("dopocon il separatore", locazione.ottieniProprieta("NOTA"), "il | si toglie dal valore");
        locazione.rimuoviProprieta(LocazioneMD.VISITATA);
        assertNull(locazione.ottieniProprieta(LocazioneMD.VISITATA));
        locazione.rimuoviProprieta("NON_C_E");
    }

    @Test
    void ilNomeEUnaProprietaECheSiTogliConNull() {
        LocazioneMD locanda = new LocazioneMD(TipoLocazione.LOCANDA);
        assertNull(locanda.getNome());
        locanda.setNome("Il Boccale|Rovesciato");
        assertEquals("Il BoccaleRovesciato", locanda.getNome());
        assertEquals("Il BoccaleRovesciato", locanda.ottieniProprieta(LocazioneMD.NOME));
        locanda.setNome(null);
        assertNull(locanda.getNome());
        assertNull(locanda.ottieniProprieta(LocazioneMD.NOME));
    }

    @Test
    void senzaProprietaLaRigaHaSoloIdentificativoETipo() throws IOException {
        LocazioneMD locazione = new LocazioneMD(TipoLocazione.PALUDE);
        String salvato = salva(locazione);
        assertEquals(locazione.getUuid() + "|PALUDE", salvato.trim());

        LocazioneMD riletta = rileggi(new LocazioneMD(), salvato);
        assertEquals(locazione.getUuid(), riletta.getUuid());
        assertEquals(TipoLocazione.PALUDE, riletta.getTipo());
        assertNull(riletta.getNome());
    }

    @Test
    void conLeProprietaSiRileggeTuttoESiDimenticaQuelCheCEraPrima() throws IOException {
        LocazioneMD locazione = new LocazioneMD(TipoLocazione.LOCANDA);
        locazione.setNome("Il Boccale Rovesciato");
        locazione.aggiungiProprieta(LocazioneMD.VISITATA, LocazioneMD.AFFERMATIVO);
        locazione.aggiungiProprieta(LocazioneMD.CONOSCIUTA, LocazioneMD.AFFERMATIVO);
        String salvato = salva(locazione);

        LocazioneMD destinazione = new LocazioneMD(TipoLocazione.RADURA);
        destinazione.aggiungiProprieta(LocazioneMD.COMPLETA, LocazioneMD.AFFERMATIVO);
        LocazioneMD riletta = rileggi(destinazione, salvato);

        assertEquals(locazione.getUuid(), riletta.getUuid());
        assertEquals(TipoLocazione.LOCANDA, riletta.getTipo());
        assertEquals("Il Boccale Rovesciato", riletta.getNome());
        assertEquals(LocazioneMD.AFFERMATIVO, riletta.ottieniProprieta(LocazioneMD.VISITATA));
        assertEquals(LocazioneMD.AFFERMATIVO, riletta.ottieniProprieta(LocazioneMD.CONOSCIUTA));
        assertNull(riletta.ottieniProprieta(LocazioneMD.COMPLETA), "le proprietà di prima si sostituiscono");
        assertEquals(salvato, salva(riletta));
    }
}
