package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.artefatti.tipi.TipoNegozio;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class RegistroArtefattiMDTest {

    @Test
    void salvaERicaricaArtefattiSmarritiEInventariDelleLocazioni() throws IOException {
        // Given
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        ArtefattoMD nelTempio = ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "il pugnale di Worr", "il cui potere è nel combattimento");
        nelTempio.addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "Forza");
        registro.addArtefattoInLocazione(nelTempio, new CoordinateMD(3, 4));
        ArtefattoMD dallArmaiolo = ArtefattiDiTest.creaArtefatto(TipoArtefatto.SCUDO, "lo scudo fiscale", "che si fa fare sconti");
        dallArmaiolo.addModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Parata");
        dallArmaiolo.addIncantamento("La battuta del cavolo", TipoDanno.GELO, 10, 0.5);
        registro.aggiungiAMagazzino(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO, dallArmaiolo);
        // When
        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        // Then
        ArtefattoMD tempioRiletto = ricaricato.getArtefattoInLocazione(new CoordinateMD(3, 4));
        assertEquals("il pugnale di Worr", tempioRiletto.getNome());
        assertTrue(tempioRiletto.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "Forza")));
        Collection<ArtefattoMD> armaiolo = ricaricato.getMagazzino(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO);
        assertEquals(1, armaiolo.size());
        ArtefattoMD scudo = armaiolo.iterator().next();
        assertEquals("lo scudo fiscale", scudo.getNome());
        assertTrue(scudo.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Parata")));
        assertEquals(1, scudo.getIncantamenti().size());
    }

    @Test
    void iNegoziDellaStessaCittaHannoMagazziniSeparati() throws IOException {
        // Given
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        CoordinateMD citta = new CoordinateMD(7, 1);
        registro.aggiungiAMagazzino(citta, TipoNegozio.ARMAIOLO,
                ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada d'argento", "che luccica"));
        ArtefattoMD pergamena = ArtefattiDiTest.creaArtefatto(TipoArtefatto.PERGAMENA, "una pergamena del fuoco", "che scotta");
        pergamena.addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        registro.aggiungiAMagazzino(citta, TipoNegozio.VENDITORE_DI_PERGAMENE, pergamena);
        registro.aggiungiAMagazzino(citta, TipoNegozio.VENDITORE_DI_PERGAMENE,
                ArtefattiDiTest.creaArtefatto(TipoArtefatto.PERGAMENA, "una pergamena del gelo", "che gela"));
        // When
        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        // Then
        Collection<ArtefattoMD> armaiolo = ricaricato.getMagazzino(citta, TipoNegozio.ARMAIOLO);
        assertEquals(1, armaiolo.size());
        assertEquals("la spada d'argento", armaiolo.iterator().next().getNome());
        Collection<ArtefattoMD> pergamene = ricaricato.getMagazzino(citta, TipoNegozio.VENDITORE_DI_PERGAMENE);
        assertEquals(2, pergamene.size());
        assertTrue(pergamene.stream().allMatch(a -> a.getTipo() == TipoArtefatto.PERGAMENA));
        assertTrue(pergamene.stream().anyMatch(a -> a.getIncantamenti().size() == 1));
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
    void iMagazziniSiCreanoScrivendoliNonLeggendoli() {
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        CoordinateMD citta = new CoordinateMD(7, 1);
        ArtefattoMD spada = ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia");
        ArtefattoMD scudo = ArtefattiDiTest.creaArtefatto(TipoArtefatto.SCUDO, "lo scudo", "che para");

        assertTrue(registro.getMagazzino(citta, TipoNegozio.ARMAIOLO).isEmpty());
        assertTrue(registro.getCoordinateMagazzini(TipoNegozio.ARMAIOLO).isEmpty(), "leggere un magazzino non lo crea");
        assertFalse(registro.rimuoviDaMagazzino(citta, TipoNegozio.ARMAIOLO, spada));
        registro.tieniInMagazzino(citta, TipoNegozio.ARMAIOLO, a -> true);

        registro.aggiungiAMagazzino(citta, TipoNegozio.ARMAIOLO, spada);
        registro.aggiungiAMagazzino(citta, TipoNegozio.ARMAIOLO, scudo);
        assertEquals(Collections.singleton(citta), registro.getCoordinateMagazzini(TipoNegozio.ARMAIOLO));
        assertThrows(UnsupportedOperationException.class, () -> registro.getMagazzino(citta, TipoNegozio.ARMAIOLO).clear());

        registro.tieniInMagazzino(citta, TipoNegozio.ARMAIOLO, a -> a.getTipo() == TipoArtefatto.SCUDO);
        assertEquals(Collections.singletonList(scudo), registro.getMagazzino(citta, TipoNegozio.ARMAIOLO));
        assertTrue(registro.rimuoviDaMagazzino(citta, TipoNegozio.ARMAIOLO, scudo));
        assertFalse(registro.rimuoviDaMagazzino(new CoordinateMD(1, 1), TipoNegozio.ARMAIOLO, scudo), "un'altra città non ha magazzino");
    }

    @Test
    void reimpostareDimenticaTutto() {
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        CoordinateMD casella = new CoordinateMD(2, 2);
        registro.aggiungiArtefatto(ArtefattiDiTest.creaArtefatto(TipoArtefatto.ANELLO, "l'anello", "che brilla"));
        registro.addArtefattoInLocazione(ArtefattiDiTest.creaArtefatto(TipoArtefatto.ELMO, "l'elmo", "che protegge"), casella);
        registro.segnaLocalizzazioneConosciuta(casella);
        registro.aggiungiAMagazzino(casella, TipoNegozio.ARMAIOLO, ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia"));

        registro.reimposta();

        assertEquals(0, registro.getNumeroDisponibili());
        assertTrue(registro.getUbicazioniArtefattiSmarriti().isEmpty());
        assertTrue(registro.getLocalizzazioniConosciute().isEmpty());
        assertTrue(registro.getCoordinateMagazzini(TipoNegozio.ARMAIOLO).isEmpty());
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
