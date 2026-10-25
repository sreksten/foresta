package com.threeamigos.foresta.modellodati;


import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class RegistroArtefattiMDTest {

    @Test
    void salvaERicaricaGliArtefattiSmarriti() throws IOException {
        // Given
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        ArtefattoMD nelTempio = ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "il pugnale di Worr", "il cui potere è nel combattimento");
        nelTempio.addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "Forza");
        registro.addArtefattoInLocazione(nelTempio, new CoordinateMD(3, 4));
        // When
        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        // Then
        ArtefattoMD tempioRiletto = ricaricato.getArtefattoInLocazione(new CoordinateMD(3, 4));
        assertEquals("il pugnale di Worr", tempioRiletto.getNome());
        assertTrue(tempioRiletto.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "Forza")));
    }

    @Test
    void unArtefattoRimossoDallaLocazioneNonVieneSalvato() throws IOException {
        // Given
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        CoordinateMD tempio = new CoordinateMD(3, 4);
        registro.addArtefattoInLocazione(ArtefattiDiTest.creaArtefatto(TipoArtefatto.ELMO, "l'elmo di Gorgor", "che protegge poco"), tempio);
        // When
        registro.rimuoviArtefattoInLocazione(tempio);
        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        // Then
        assertNull(ricaricato.getArtefattoInLocazione(tempio));
    }

    @Test
    void gliArtefattiDaDistribuireSiPrendonoUnoAllaVoltaENonSiSalvano() throws IOException {
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        ArtefattoMD primo = ArtefattiDiTest.creaArtefatto(TipoArtefatto.ANELLO, "l'anello", "che brilla");
        ArtefattoMD secondo = ArtefattiDiTest.creaArtefatto(TipoArtefatto.TALISMANO, "il talismano", "che protegge");
        registro.aggiungiArtefatto(primo);
        registro.aggiungiArtefatto(secondo);
        assertEquals(2, registro.getNumeroDisponibili());
        assertSame(secondo, registro.rimuoviDisponibile(1));
        assertEquals(1, registro.getNumeroDisponibili());

        assertEquals(0, salvaERileggi(registro).getNumeroDisponibili(), "servono solo mentre si costruisce la foresta");
    }

    @Test
    void leLocalizzazioniConosciuteSiSalvanoESiDimenticanoConLArtefatto() throws IOException {
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        CoordinateMD palude = new CoordinateMD(5, 6);
        CoordinateMD grotta = new CoordinateMD(9, 2);
        registro.addArtefattoInLocazione(ArtefattiDiTest.creaArtefatto(TipoArtefatto.ANELLO, "l'anello", "che brilla"), palude);
        registro.addArtefattoInLocazione(ArtefattiDiTest.creaArtefatto(TipoArtefatto.ELMO, "l'elmo", "che protegge"), grotta);
        registro.segnaLocalizzazioneConosciuta(palude);
        assertTrue(registro.isLocalizzazioneConosciuta(palude));
        assertFalse(registro.isLocalizzazioneConosciuta(grotta));
        assertEquals(new HashSet<>(Arrays.asList(palude, grotta)), registro.getUbicazioniArtefattiSmarriti());
        assertThrows(UnsupportedOperationException.class, () -> registro.getLocalizzazioniConosciute().clear());
        assertThrows(UnsupportedOperationException.class, () -> registro.getUbicazioniArtefattiSmarriti().clear());

        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        assertEquals(Collections.singleton(palude), ricaricato.getLocalizzazioniConosciute());

        ricaricato.rimuoviArtefattoInLocazione(palude);
        assertFalse(ricaricato.isLocalizzazioneConosciuta(palude), "recuperato l'artefatto, la mappa non lo segnala più");
    }

    @Test
    void reimpostareDimenticaTutto() {
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        CoordinateMD casella = new CoordinateMD(2, 2);
        registro.aggiungiArtefatto(ArtefattiDiTest.creaArtefatto(TipoArtefatto.ANELLO, "l'anello", "che brilla"));
        registro.addArtefattoInLocazione(ArtefattiDiTest.creaArtefatto(TipoArtefatto.ELMO, "l'elmo", "che protegge"), casella);
        registro.segnaLocalizzazioneConosciuta(casella);

        registro.reimposta();

        assertEquals(0, registro.getNumeroDisponibili());
        assertTrue(registro.getUbicazioniArtefattiSmarriti().isEmpty());
        assertTrue(registro.getLocalizzazioniConosciute().isEmpty());
    }

    @Test
    void unArtefattoSiPortaDietroLaSuaUbicazione() {
        ArtefattoMD anello = ArtefattiDiTest.creaArtefatto(TipoArtefatto.ANELLO, "l'anello", "che brilla");
        CoordinateMD palude = new CoordinateMD(5, 6);
        RegistroArtefattiMD.ArtefattoESuaUbicazione informazione = new RegistroArtefattiMD.ArtefattoESuaUbicazione(anello, palude);
        assertSame(anello, informazione.getArtefattoMD());
        assertSame(palude, informazione.getCoordinate());
    }

    private static RegistroArtefattiMD salvaERileggi(RegistroArtefattiMD registro) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        registro.salva(printWriter);
        printWriter.flush();
        RegistroArtefattiMD ricaricato = new RegistroArtefattiMD();
        ricaricato.leggi(new BufferedReader(new StringReader(stringWriter.toString())));
        return ricaricato;
    }
}
