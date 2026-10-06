package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class RegistroArtefattiMDTest {

    @Test
    void salvaERicaricaArtefattiSmarritiEInventariDelleLocazioni() throws IOException {
        // Given
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        ArtefattoMD nelTempio = ArtefattoMDTest.creaArtefatto(TipoArtefatto.SPADA, "il pugnale di Worr", "il cui potere è nel combattimento");
        nelTempio.addModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 2, "Forza");
        registro.addArtefattoInLocazione(nelTempio, new CoordinateMD(3, 4));
        ArtefattoMD dallArmaiolo = ArtefattoMDTest.creaArtefatto(TipoArtefatto.SCUDO, "lo scudo fiscale", "che si fa fare sconti");
        dallArmaiolo.addModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 10, "Parata");
        dallArmaiolo.addIncantamento("La battuta del cavolo", TipoDanno.GELO, 10, 0.5);
        registro.getMagazzino(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO).add(dallArmaiolo);
        // When
        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        // Then
        ArtefattoMD tempioRiletto = ricaricato.getArtefattoInLocazione(new CoordinateMD(3, 4));
        assertEquals("il pugnale di Worr", tempioRiletto.getNome());
        assertTrue(tempioRiletto.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 2, "Forza")));
        Collection<ArtefattoMD> armaiolo = ricaricato.getMagazzino(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO);
        assertEquals(1, armaiolo.size());
        ArtefattoMD scudo = armaiolo.iterator().next();
        assertEquals("lo scudo fiscale", scudo.getNome());
        assertTrue(scudo.getModificatori().contains(new ModificatoreAttributoMD(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 10, "Parata")));
        assertEquals(1, scudo.getIncantamenti().size());
    }

    @Test
    void iNegoziDellaStessaCittaHannoMagazziniSeparati() throws IOException {
        // Given
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        CoordinateMD citta = new CoordinateMD(7, 1);
        registro.getMagazzino(citta, TipoNegozio.ARMAIOLO)
                .add(ArtefattoMDTest.creaArtefatto(TipoArtefatto.SPADA, "la spada d'argento", "che luccica"));
        ArtefattoMD pergamena = ArtefattoMDTest.creaArtefatto(TipoArtefatto.PERGAMENA, "una pergamena del fuoco", "che scotta");
        pergamena.addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        Collection<ArtefattoMD> venditore = registro.getMagazzino(citta, TipoNegozio.VENDITORE_DI_PERGAMENE);
        venditore.add(pergamena);
        venditore.add(ArtefattoMDTest.creaArtefatto(TipoArtefatto.PERGAMENA, "una pergamena del gelo", "che gela"));
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
        registro.addArtefattoInLocazione(ArtefattoMDTest.creaArtefatto(TipoArtefatto.ELMO, "l'elmo di Gorgor", "che protegge poco"), tempio);
        // When
        registro.rimuoviArtefattoInLocazione(tempio);
        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        // Then
        assertNull(ricaricato.getArtefattoInLocazione(tempio));
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
