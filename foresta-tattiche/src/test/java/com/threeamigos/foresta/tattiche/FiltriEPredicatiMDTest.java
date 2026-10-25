package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.PredicatoMD;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoConfronto;
import com.threeamigos.foresta.tipi.TipoFiltro;
import com.threeamigos.foresta.tipi.TipoPredicato;
import com.threeamigos.foresta.tipi.TipoRosa;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I filtri e i predicati delle tattiche: le fabbriche controllano i parametri, si negano, si confrontano, si descrivono e si
 * salvano; una riga incoerente non si rilegge.
 */
class FiltriEPredicatiMDTest {

    private static final CategoriaPersonaggio CATEGORIA = CategoriaPersonaggio.values()[0];

    private static List<FiltroMD> unFiltroPerOgniParametro() {
        return Arrays.asList(
                FiltroMD.senzaParametro(TipoFiltro.MAGICO),
                FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 10).negato(),
                FiltroMD.conConfronto(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoConfronto.MAGGIORE),
                FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BRUCIATO),
                FiltroMD.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.GOBLIN),
                FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CATEGORIA),
                FiltroMD.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.FUOCO));
    }

    private static String salva(FiltroMD filtro) {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            filtro.salva(stream);
        }
        return scritto.toString();
    }

    private static String salva(PredicatoMD predicato) {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            predicato.salva(stream);
        }
        return scritto.toString();
    }

    private static BufferedReader lettore(String testo) {
        return new BufferedReader(new StringReader(testo));
    }

    @Test
    void ogniFiltroSiSalvaESiRilegge() throws IOException {
        for (FiltroMD filtro : unFiltroPerOgniParametro()) {
            assertEquals(filtro, FiltroMD.leggi(lettore(salva(filtro))), filtro.toString());
        }
    }

    @Test
    void unFiltroTieneISuoiDati() {
        FiltroMD salute = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 10);
        assertSame(TipoFiltro.SALUTE, salute.getTipoFiltro());
        assertFalse(salute.isNegato());
        assertTrue(salute.negato().isNegato());
        assertFalse(salute.negato().negato().isNegato());
        assertSame(TipoConfronto.MINORE, salute.getConfronto());
        assertEquals(10, salute.getNumero());
        assertSame(TipoEffettoDiStato.BRUCIATO, FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BRUCIATO).getTipoEffettoDiStato());
        assertSame(TipoPersonaggio.GOBLIN, FiltroMD.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.GOBLIN).getTipoPersonaggio());
        assertSame(CATEGORIA, FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CATEGORIA).getCategoriaPersonaggio());
        assertSame(TipoDanno.FUOCO, FiltroMD.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.FUOCO).getTipoDanno());
    }

    @Test
    void unFiltroSiDescriveConIlSuoParametro() {
        List<FiltroMD> filtri = unFiltroPerOgniParametro();
        assertEquals("MAGICO", filtri.get(0).toString());
        assertEquals("NON SALUTE MINORE 10", filtri.get(1).toString());
        assertEquals("LIVELLO_RISPETTO_AL_MIO MAGGIORE", filtri.get(2).toString());
        assertEquals("STATO BRUCIATO", filtri.get(3).toString());
        assertEquals("CLASSE GOBLIN", filtri.get(4).toString());
        assertEquals("CATEGORIA " + CATEGORIA, filtri.get(5).toString());
        assertEquals("DEBOLE_A FUOCO", filtri.get(6).toString());
    }

    @Test
    void unaFabbricaRifiutaUnFiltroDallAltroParametro() {
        IllegalArgumentException errore = assertThrows(IllegalArgumentException.class,
                () -> FiltroMD.conStato(TipoFiltro.SALUTE, TipoEffettoDiStato.BRUCIATO));
        assertTrue(errore.getMessage().contains("SALUTE"), errore.getMessage());
        assertThrows(NullPointerException.class, () -> FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, null, 10));
    }

    @Test
    void dueFiltriSonoUgualiSoloConGliStessiDati() {
        FiltroMD salute = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 10);
        assertEquals(salute, salute);
        assertEquals(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 10), salute);
        assertEquals(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 10).hashCode(), salute.hashCode());
        assertNotEquals("SALUTE", salute);
        assertNotEquals(FiltroMD.conConfrontoENumero(TipoFiltro.MAGIA, TipoConfronto.MINORE, 10), salute);
        assertNotEquals(salute.negato(), salute);
        assertNotEquals(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MAGGIORE, 10), salute);
        assertNotEquals(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 20), salute);
        assertNotEquals(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BAGNATO), FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BRUCIATO));
        assertNotEquals(FiltroMD.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.TROLL), FiltroMD.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.GOBLIN));
        assertNotEquals(FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.values()[1]), FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CATEGORIA));
        assertNotEquals(FiltroMD.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.GELO), FiltroMD.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.FUOCO));
    }

    @Test
    void unFiltroIncoerenteNonSiRilegge() {
        IOException errore = assertThrows(IOException.class, () -> FiltroMD.leggi(lettore("FILTRO|SALUTE|false||10|||||")));
        assertTrue(errore.getMessage().contains("SALUTE"), errore.getMessage());
        assertThrows(IOException.class, () -> FiltroMD.leggi(lettore("PREDICATO|SALUTE|false|MINORE|10|||||")), "un'altra etichetta");
    }

    @Test
    void ogniPredicatoSiSalvaESiRilegge() throws IOException {
        List<PredicatoMD> predicati = Arrays.asList(
                PredicatoMD.quanti(TipoRosa.AVVERSARI, unFiltroPerOgniParametro(), TipoConfronto.MAGGIORE, 2),
                PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.UGUALE, 1).negato(),
                PredicatoMD.probabilita(30));
        for (PredicatoMD predicato : predicati) {
            assertEquals(predicato, PredicatoMD.leggi(lettore(salva(predicato))), predicato.toString());
        }
    }

    @Test
    void unPredicatoTieneISuoiDatiESiDescrive() {
        FiltroMD ferito = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 10);
        PredicatoMD quanti = PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.singletonList(ferito), TipoConfronto.MAGGIORE, 2);
        assertSame(TipoPredicato.QUANTI, quanti.getTipoPredicato());
        assertFalse(quanti.isNegato());
        assertSame(TipoConfronto.MAGGIORE, quanti.getTipoConfronto());
        assertEquals(2, quanti.getNumero());
        assertSame(TipoRosa.AVVERSARI, quanti.getTipoRosa());
        assertEquals(Collections.singletonList(ferito), quanti.getFiltri());
        assertEquals("NON QUANTI AVVERSARI [SALUTE MINORE 10] MAGGIORE 2", quanti.negato().toString());
        assertEquals("QUANTI AVVERSARI MAGGIORE 2", PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.emptyList(), TipoConfronto.MAGGIORE, 2).toString());
        assertEquals("PROBABILITA 30", PredicatoMD.probabilita(30).toString());
        assertFalse(quanti.negato().negato().isNegato());
    }

    @Test
    void unaFabbricaRifiutaUnPredicatoDallAltroParametro() {
        IllegalArgumentException errore = assertThrows(IllegalArgumentException.class,
                () -> PredicatoMD.conConfrontoENumero(TipoPredicato.QUANTI, TipoConfronto.MAGGIORE, 2));
        assertTrue(errore.getMessage().contains("QUANTI"), errore.getMessage());
    }

    @Test
    void duePredicatiSonoUgualiSoloConGliStessiDati() {
        PredicatoMD round = PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.UGUALE, 1);
        assertEquals(round, round);
        assertEquals(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.UGUALE, 1), round);
        assertEquals(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.UGUALE, 1).hashCode(), round.hashCode());
        assertNotEquals("ROUND", round);
        assertNotEquals(PredicatoMD.conConfrontoENumero(TipoPredicato.ASTUZIA, TipoConfronto.UGUALE, 1), round);
        assertNotEquals(round.negato(), round);
        assertNotEquals(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.MINORE, 1), round);
        assertNotEquals(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.UGUALE, 2), round);
        PredicatoMD quanti = PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.emptyList(), TipoConfronto.MAGGIORE, 2);
        assertNotEquals(PredicatoMD.quanti(TipoRosa.ALLEATI, Collections.emptyList(), TipoConfronto.MAGGIORE, 2), quanti);
        assertNotEquals(PredicatoMD.quanti(TipoRosa.AVVERSARI, unFiltroPerOgniParametro(), TipoConfronto.MAGGIORE, 2), quanti);
    }

    @Test
    void unPredicatoIncoerenteNonSiRilegge() {
        IOException errore = assertThrows(IOException.class, () -> PredicatoMD.leggi(lettore("PREDICATO|ROUND|false||1||0")));
        assertTrue(errore.getMessage().contains("ROUND"), errore.getMessage());
    }
}
