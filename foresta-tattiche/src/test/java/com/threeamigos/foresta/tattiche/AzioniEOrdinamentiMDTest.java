package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le azioni, gli ordinamenti e i selettori delle tattiche: le fabbriche controllano i parametri, si confrontano, si descrivono
 * e si salvano; una riga incoerente non si rilegge.
 */
class AzioniEOrdinamentiMDTest {

    private static BufferedReader lettore(String testo) {
        return new BufferedReader(new StringReader(testo));
    }

    private static String salva(AzioneMD azione) {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            azione.salva(stream);
        }
        return scritto.toString();
    }

    private static String salva(SelettoreMD selettore) {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            selettore.salva(stream);
        }
        return scritto.toString();
    }

    @Test
    void ogniAzioneSiSalvaESiRilegge() throws IOException {
        List<AzioneMD> azioni = Arrays.asList(
                AzioneMD.di(TipoAzione.ATTACCO_FISICO),
                AzioneMD.di(TipoAzione.FUOCO).soloSePiuForteDellAttacco(),
                AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, true),
                AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.A_CASO, false),
                AzioneMD.cambiaArma(TipoDanno.CONTUNDENTE));
        for (AzioneMD azione : azioni) {
            assertEquals(azione, AzioneMD.leggi(lettore(salva(azione))), azione.toString());
        }
    }

    @Test
    void unAzioneTieneISuoiDatiESiDescrive() {
        AzioneMD incantesimo = AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, false);
        assertSame(TipoAzione.INCANTESIMO_DI_DANNO, incantesimo.getTipo());
        assertSame(CriterioIncantesimo.PIU_POTENTE, incantesimo.getCriterio());
        assertFalse(incantesimo.isSoloSePiuForteDellAttacco());
        assertSame(TipoDanno.CONTUNDENTE, AzioneMD.cambiaArma(TipoDanno.CONTUNDENTE).getArmaDi());
        assertEquals("ATTACCO_FISICO", AzioneMD.di(TipoAzione.ATTACCO_FISICO).toString());
        assertEquals("INCANTESIMO_DI_DANNO PIU_POTENTE SOLO_SE_PIU_FORTE_DELL_ATTACCO", incantesimo.soloSePiuForteDellAttacco().toString());
        assertEquals("INCANTESIMO_DEL_REPERTORIO A_CASO SOLO_SE_PIU_FORTE_DELL_ATTACCO",
                AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.A_CASO, false).soloSePiuForteDellAttacco().toString());
        assertEquals("CAMBIA_ARMA CONTUNDENTE", AzioneMD.cambiaArma(TipoDanno.CONTUNDENTE).toString());
    }

    @Test
    void leAzioniConParametriNonSiFannoSenza() {
        for (TipoAzione tipo : new TipoAzione[]{TipoAzione.INCANTESIMO_DI_DANNO, TipoAzione.INCANTESIMO_DEL_REPERTORIO, TipoAzione.CAMBIA_ARMA}) {
            assertThrows(IllegalArgumentException.class, () -> AzioneMD.di(tipo), tipo.name());
        }
    }

    @Test
    void soloLeAzioniCheFannoDannoSiConfrontanoConLAttacco() {
        assertTrue(AzioneMD.di(TipoAzione.DARDO_ARCANO).soloSePiuForteDellAttacco().isSoloSePiuForteDellAttacco());
        assertTrue(AzioneMD.di(TipoAzione.FUOCO).soloSePiuForteDellAttacco().isSoloSePiuForteDellAttacco());
        // L'attacco non è un incantesimo, la resurrezione non fa danno, silenzio e morte non si misurano col danno
        for (TipoAzione tipo : new TipoAzione[]{TipoAzione.ATTACCO_FISICO, TipoAzione.RESURREZIONE, TipoAzione.SILENZIO, TipoAzione.MORTE}) {
            AzioneMD azione = AzioneMD.di(tipo);
            assertThrows(IllegalArgumentException.class, azione::soloSePiuForteDellAttacco, tipo.name());
        }
    }

    @Test
    void dueAzioniSonoUgualiSoloConGliStessiDati() {
        AzioneMD incantesimo = AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, false);
        assertEquals(incantesimo, incantesimo);
        assertEquals(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, false), incantesimo);
        assertEquals(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, false).hashCode(), incantesimo.hashCode());
        assertNotEquals("INCANTESIMO_DI_DANNO", incantesimo);
        assertNotEquals(AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.PIU_POTENTE, false), incantesimo);
        assertNotEquals(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.A_CASO, false), incantesimo);
        assertNotEquals(incantesimo.soloSePiuForteDellAttacco(), incantesimo);
        assertNotEquals(AzioneMD.cambiaArma(TipoDanno.TAGLIENTE), AzioneMD.cambiaArma(TipoDanno.CONTUNDENTE));
    }

    @Test
    void unAzioneIncoerenteNonSiRilegge() {
        IOException senzaCriterio = assertThrows(IOException.class, () -> AzioneMD.leggi(lettore("AZIONE|INCANTESIMO_DI_DANNO||false|")));
        assertTrue(senzaCriterio.getMessage().contains("INCANTESIMO_DI_DANNO"), senzaCriterio.getMessage());
        assertThrows(IOException.class, () -> AzioneMD.leggi(lettore("AZIONE|ATTACCO_FISICO||true|")), "l'attacco non si confronta con sé stesso");
        assertThrows(IOException.class, () -> AzioneMD.leggi(lettore("VOCE|ATTACCO_FISICO||false|")), "un'altra etichetta");
    }

    @Test
    void unOrdinamentoVuoleIlTipoDiDannoSoloSeGliServe() {
        OrdinamentoMD primo = OrdinamentoMD.di(TipoOrdinamento.PRIMO);
        assertSame(TipoOrdinamento.PRIMO, primo.getTipoOrdinamento());
        assertNull(primo.getTipoDanno());
        assertEquals("PRIMO", primo.toString());
        OrdinamentoMD debole = OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.FUOCO);
        assertSame(TipoDanno.FUOCO, debole.getTipoDanno());
        assertEquals("PIU_DEBOLE_A FUOCO", debole.toString());
        assertThrows(IllegalArgumentException.class, () -> OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A));
        assertThrows(IllegalArgumentException.class, () -> OrdinamentoMD.di(TipoOrdinamento.PRIMO, TipoDanno.FUOCO));
        assertThrows(NullPointerException.class, () -> OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, null));
    }

    @Test
    void dueOrdinamentiSonoUgualiSoloConGliStessiDati() {
        OrdinamentoMD debole = OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.FUOCO);
        assertEquals(OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.FUOCO), debole);
        assertEquals(OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.FUOCO).hashCode(), debole.hashCode());
        assertNotEquals("PIU_DEBOLE_A", debole);
        assertNotEquals(OrdinamentoMD.di(TipoOrdinamento.PIU_RESISTENTE_A, TipoDanno.FUOCO), debole);
        assertNotEquals(OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.GELO), debole);
    }

    @Test
    void unSelettoreSiSalvaESiRilegge() throws IOException {
        SelettoreMD conFiltri = new SelettoreMD(TipoRosa.AVVERSARI,
                Collections.singletonList(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 10)),
                OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.FUOCO));
        SelettoreMD semplice = new SelettoreMD(TipoRosa.SE_STESSO, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO));
        for (SelettoreMD selettore : Arrays.asList(conFiltri, semplice)) {
            assertEquals(selettore, SelettoreMD.leggi(lettore(salva(selettore))), selettore.toString());
        }
        assertEquals(1, conFiltri.getFiltri().size());
        assertSame(TipoOrdinamento.PIU_DEBOLE_A, conFiltri.getOrdinamento().getTipoOrdinamento());
        assertEquals("SU AVVERSARI [SALUTE MINORE 10] ordine PIU_DEBOLE_A FUOCO", conFiltri.toString());
        assertEquals("SU SE_STESSO ordine PRIMO", semplice.toString());
    }

    @Test
    void dueSelettoriSonoUgualiSoloConGliStessiDati() {
        SelettoreMD selettore = new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO));
        assertEquals(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)), selettore);
        assertEquals(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)).hashCode(), selettore.hashCode());
        assertNotEquals("AVVERSARI", selettore);
        assertNotEquals(new SelettoreMD(TipoRosa.ALLEATI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)), selettore);
        assertNotEquals(new SelettoreMD(TipoRosa.AVVERSARI, Collections.singletonList(FiltroMD.senzaParametro(TipoFiltro.MAGICO)),
                OrdinamentoMD.di(TipoOrdinamento.PRIMO)), selettore);
        assertNotEquals(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.A_CASO)), selettore);
    }

    @Test
    void unOrdinamentoIncoerenteNelSelettoreNonSiRilegge() {
        IOException errore = assertThrows(IOException.class, () -> SelettoreMD.leggi(lettore("SELETTORE|AVVERSARI|PRIMO|FUOCO|0")));
        assertTrue(errore.getMessage().contains("PRIMO"), errore.getMessage());
        assertThrows(IOException.class, () -> SelettoreMD.leggi(lettore("SELETTORE|AVVERSARI|PIU_DEBOLE_A||0")));
    }
}
