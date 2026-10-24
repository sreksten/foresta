package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il salvataggio degli artefatti costruiti dal {@code CostruttoreArtefatto}, insieme agli altri casi di salvataggio
 * dell'{@code ArtefattoMD} ({@code ArtefattoMDTest}).
 */
class ArtefattoMDCostruitoTest {

    @Test
    void salvaERicaricaPergamenaConModificatoriEIncantamenti() throws IOException {
        // Given: una pergamena è un artefatto, quindi porta sia modificatori sia incantamenti
        ArtefattoMD pergamena = CostruttoreArtefatto.istanza()
                .setTipo(TipoArtefatto.PERGAMENA)
                .setNome("il Sigillo della Fiamma Eterna")
                .setDescrizione("che arde di magia")
                .setLivello(3)
                .setCostoAcquisto(50)
                .setPeso(0.1)
                .setModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2)
                .setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10)
                .setIncantamento("Fiamma", TipoDanno.FUOCO, 15, 0.2)
                .setIncantamento("Brina", TipoDanno.GELO, 5, 0.05)
                .costruisci()
                .getModelloDati();
        // When
        ArtefattoMD ricaricato = salvaERileggi(pergamena);
        // Then
        assertEquals(TipoArtefatto.PERGAMENA, ricaricato.getTipo());
        Collection<ModificatoreAttributoMD> modificatori = ricaricato.getModificatori();
        assertEquals(2, modificatori.size());
        assertTrue(modificatori.contains(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2)));
        assertTrue(modificatori.contains(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, 10)));
        List<IncantamentoMD> incantamenti = new ArrayList<>(ricaricato.getIncantamenti());
        assertEquals(2, incantamenti.size());
        verificaIncantamento(incantamenti, "Fiamma", TipoDanno.FUOCO, 15, 0.2);
        verificaIncantamento(incantamenti, "Brina", TipoDanno.GELO, 5, 0.05);
    }

    private static ArtefattoMD salvaERileggi(ArtefattoMD artefatto) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        artefatto.salva(printWriter);
        printWriter.flush();
        ArtefattoMD ricaricato = new ArtefattoMD();
        ricaricato.leggi(new BufferedReader(new StringReader(stringWriter.toString())));
        return ricaricato;
    }

    private static void verificaIncantamento(List<IncantamentoMD> incantamenti, String nome, TipoDanno tipoDanno,
                                             int dannoBonusFisso, double coefficienteScala) {
        IncantamentoMD incantamento = incantamenti.stream()
                .filter(i -> i.getNomeIncantamento().equals(nome))
                .findFirst()
                .orElseThrow(() -> new AssertionError("IncantamentoMD non riletto: " + nome));
        assertEquals(tipoDanno, incantamento.getTipoDannoElementale());
        assertEquals(dannoBonusFisso, incantamento.getDannoBonusFisso());
        assertEquals(coefficienteScala, incantamento.getCoefficienteScala(), 0.0001);
    }
}
