package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.oggetti.Artefatto;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        registro.getScambiatorePerNegozio(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO).addArtefatto(Artefatto.di(dallArmaiolo));
        // When
        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        // Then
        ArtefattoMD tempioRiletto = ricaricato.getArtefattoInLocazione(new CoordinateMD(3, 4));
        assertEquals("il pugnale di Worr", tempioRiletto.getNome());
        assertTrue(tempioRiletto.getModificatori().contains(new ModificatoreAttributo(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 2, "Forza")));
        Collection<Artefatto> armaiolo = ricaricato.getScambiatorePerNegozio(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO).getInventario();
        assertEquals(1, armaiolo.size());
        Artefatto scudo = armaiolo.iterator().next();
        assertEquals("lo scudo fiscale", scudo.getNome());
        assertTrue(scudo.getModificatori().contains(new ModificatoreAttributo(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 10, "Parata")));
        assertEquals(1, scudo.getIncantamenti().size());
    }

    @Test
    void iNegoziDellaStessaCittaHannoMagazziniSeparati() throws IOException {
        // Given
        RegistroArtefattiMD registro = new RegistroArtefattiMD();
        CoordinateMD citta = new CoordinateMD(7, 1);
        registro.getScambiatorePerNegozio(citta, TipoNegozio.ARMAIOLO)
                .addArtefatto(Artefatto.di(ArtefattoMDTest.creaArtefatto(TipoArtefatto.SPADA, "la spada d'argento", "che luccica")));
        ArtefattoMD pergamena = ArtefattoMDTest.creaArtefatto(TipoArtefatto.INCANTAMENTO, "una pergamena del fuoco", "che scotta");
        pergamena.addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        ScambiatoreArtefatti venditore = registro.getScambiatorePerNegozio(citta, TipoNegozio.VENDITORE_DI_PERGAMENE);
        venditore.addArtefatto(Artefatto.di(pergamena));
        venditore.addArtefatto(Artefatto.di(ArtefattoMDTest.creaArtefatto(TipoArtefatto.INCANTAMENTO, "una pergamena del gelo", "che gela")));
        // When
        RegistroArtefattiMD ricaricato = salvaERileggi(registro);
        // Then
        Collection<Artefatto> armaiolo = ricaricato.getScambiatorePerNegozio(citta, TipoNegozio.ARMAIOLO).getInventario();
        assertEquals(1, armaiolo.size());
        assertEquals("la spada d'argento", armaiolo.iterator().next().getNome());
        Collection<Artefatto> pergamene = ricaricato.getScambiatorePerNegozio(citta, TipoNegozio.VENDITORE_DI_PERGAMENE).getInventario();
        assertEquals(2, pergamene.size());
        assertTrue(pergamene.stream().allMatch(a -> a.getTipo() == TipoArtefatto.INCANTAMENTO));
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
