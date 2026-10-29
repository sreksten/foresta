package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoNegozio;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class MagazziniMDTest {

    @Test
    void unMagazzinoSiSalvaESiRilegge() throws IOException {
        // Given
        MagazziniMD registro = new MagazziniMD();
        ArtefattoMD dallArmaiolo = ArtefattiDiTest.creaArtefatto(TipoArtefatto.SCUDO, "lo scudo fiscale", "che si fa fare sconti");
        dallArmaiolo.addModificatore(TipoAttributo.PARATA, TipoModificatore.VARIAZIONE_PERCENTUALE, 10, "Parata");
        dallArmaiolo.addIncantamento("La battuta del cavolo", TipoDanno.GELO, 10, 0.5);
        registro.aggiungiAMagazzino(new CoordinateMD(7, 1), TipoNegozio.ARMAIOLO, dallArmaiolo);
        // When
        MagazziniMD ricaricato = salvaERileggi(registro);
        // Then
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
        MagazziniMD registro = new MagazziniMD();
        CoordinateMD citta = new CoordinateMD(7, 1);
        registro.aggiungiAMagazzino(citta, TipoNegozio.ARMAIOLO,
                ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada d'argento", "che luccica"));
        ArtefattoMD pergamena = ArtefattiDiTest.creaArtefatto(TipoArtefatto.PERGAMENA, "una pergamena del fuoco", "che scotta");
        pergamena.addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
        registro.aggiungiAMagazzino(citta, TipoNegozio.VENDITORE_DI_PERGAMENE, pergamena);
        registro.aggiungiAMagazzino(citta, TipoNegozio.VENDITORE_DI_PERGAMENE,
                ArtefattiDiTest.creaArtefatto(TipoArtefatto.PERGAMENA, "una pergamena del gelo", "che gela"));
        // When
        MagazziniMD ricaricato = salvaERileggi(registro);
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
    void iMagazziniSiCreanoScrivendoliNonLeggendoli() {
        MagazziniMD registro = new MagazziniMD();
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
    void reimpostareSvuotaIMagazzini() {
        MagazziniMD registro = new MagazziniMD();
        registro.aggiungiAMagazzino(new CoordinateMD(2, 2), TipoNegozio.ARMAIOLO, ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia"));

        registro.reimposta();

        assertTrue(registro.getCoordinateMagazzini(TipoNegozio.ARMAIOLO).isEmpty());
    }

    private static MagazziniMD salvaERileggi(MagazziniMD registro) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        registro.salva(printWriter);
        printWriter.flush();
        MagazziniMD ricaricato = new MagazziniMD();
        ricaricato.leggi(new BufferedReader(new StringReader(stringWriter.toString())));
        return ricaricato;
    }
}
