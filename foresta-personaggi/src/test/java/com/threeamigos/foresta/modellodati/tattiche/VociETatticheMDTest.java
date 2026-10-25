package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.tattiche.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le voci e le tattiche: il selettore di base per ogni portata, i problemi per i due lati, le tattiche disabilitate, il
 * confronto e il salvataggio; una tattica incoerente si rilegge disabilitata, una illeggibile non si rilegge.
 */
class VociETatticheMDTest {

    private static final SelettoreMD AVVERSARI_PIU_DEBOLI = new SelettoreMD(TipoRosa.AVVERSARI,
            Collections.singletonList(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 10)),
            OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE));

    private static BufferedReader lettore(String testo) {
        return new BufferedReader(new StringReader(testo));
    }

    private static String salva(TatticaMD tattica) {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            tattica.salva(stream);
        }
        return scritto.toString();
    }

    private static VoceMD voceCompleta() {
        return new VoceMD(AVVERSARI_PIU_DEBOLI,
                Collections.singletonList(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.MAGGIORE, 1)),
                Collections.singletonList(AzioneMD.di(TipoAzione.ATTACCO_FISICO)), true, " Finisci i feriti ");
    }

    @Test
    void unaVoceTieneISuoiDati() {
        VoceMD voce = voceCompleta();
        assertSame(AVVERSARI_PIU_DEBOLI, voce.getSelettore());
        assertSame(AVVERSARI_PIU_DEBOLI, voce.selettoreEffettivo());
        assertEquals(1, voce.getCondizioni().size());
        assertEquals(1, voce.getAzioni().size());
        assertTrue(voce.isAttiva());
        assertEquals("Finisci i feriti", voce.getNome(), "il nome si ripulisce dagli spazi");
        assertFalse(voce.disattivata().isAttiva());
        assertSame(PortataAzione.AVVERSARIO, voce.getPortata());
        assertNull(new VoceMD(null, Collections.emptyList(), Collections.singletonList(AzioneMD.di(TipoAzione.ATTACCO_FISICO)), true, "  ").getNome());
        assertNull(VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO)).getNome());
    }

    @Test
    void unaVoceHaAlmenoUnAzione() {
        assertThrows(IllegalArgumentException.class, () -> new VoceMD(null, Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    void senzaSelettoreSiUsaQuelloDiBaseDellaPortata() {
        assertSame(TipoRosa.AVVERSARI, rosaDiBase(TipoAzione.ATTACCO_FISICO));
        assertSame(TipoRosa.AVVERSARI, rosaDiBase(TipoAzione.CANTO_IPNOTICO));
        assertSame(TipoRosa.ALLEATI_CADUTI, rosaDiBase(TipoAzione.RESURREZIONE));
        assertSame(TipoRosa.ALLEATI, rosaDiBase(TipoAzione.ALBA_SACRA));
        assertSame(TipoRosa.SE_STESSO, rosaDiBase(TipoAzione.POZIONE_SALUTE));
        assertSame(TipoRosa.SE_STESSO, VoceMD.su(null, AzioneMD.cambiaArma(TipoDanno.CONTUNDENTE)).selettoreEffettivo().getTipoRosa());
        assertSame(TipoOrdinamento.PRIMO, VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO)).selettoreEffettivo().getOrdinamento().getTipoOrdinamento());
    }

    private static TipoRosa rosaDiBase(TipoAzione tipo) {
        return VoceMD.su(null, AzioneMD.di(tipo)).selettoreEffettivo().getTipoRosa();
    }

    @Test
    void iProblemiDiUnaVoceDipendonoDalLato() {
        assertTrue(voceCompleta().problemi(true).isEmpty());
        assertTrue(voceCompleta().problemi(false).isEmpty());

        VoceMD miste = new VoceMD(null, Collections.emptyList(),
                Arrays.asList(AzioneMD.di(TipoAzione.ATTACCO_FISICO), AzioneMD.di(TipoAzione.POZIONE_SALUTE)));
        assertTrue(miste.problemi(true).stream().anyMatch(p -> p.contains("stessa portata")), String.valueOf(miste.problemi(true)));

        // Gli alleati e gli ospiti valgono solo per i miei personaggi, e un attacco non pesca fra loro
        VoceMD fraGliOspiti = VoceMD.su(new SelettoreMD(TipoRosa.ALLEATI_E_OSPITI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)),
                AzioneMD.di(TipoAzione.ATTACCO_FISICO));
        List<String> perIMostri = fraGliOspiti.problemi(false);
        assertTrue(perIMostri.stream().anyMatch(p -> p.contains("non vale per i mostri")), String.valueOf(perIMostri));
        assertTrue(fraGliOspiti.problemi(true).stream().anyMatch(p -> p.contains("non accetta la rosa")), String.valueOf(fraGliOspiti.problemi(true)));
        VoceMD fraOspitiEAvversari = VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI_E_OSPITI, Collections.emptyList(),
                OrdinamentoMD.di(TipoOrdinamento.PRIMO)), AzioneMD.di(TipoAzione.ATTACCO_FISICO));
        assertTrue(fraOspitiEAvversari.problemi(true).stream().anyMatch(p -> p.contains("non vale per i personaggi del giocatore")));
    }

    @Test
    void unaVoceSiDescrive() {
        assertEquals("SU AVVERSARI [SALUTE MINORE 10] ordine MENO_SALUTE SE [ROUND MAGGIORE 1] → [ATTACCO_FISICO] \"Finisci i feriti\"",
                voceCompleta().toString());
        assertEquals("→ [ATTACCO_FISICO] (disattivata)", VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO)).disattivata().toString());
    }

    @Test
    void dueVociSonoUgualiSoloConGliStessiDati() {
        VoceMD voce = voceCompleta();
        assertEquals(voceCompleta(), voce);
        assertEquals(voceCompleta().hashCode(), voce.hashCode());
        assertNotEquals("voce", voce);
        List<AzioneMD> attacco = Collections.singletonList(AzioneMD.di(TipoAzione.ATTACCO_FISICO));
        List<PredicatoMD> condizioni = voce.getCondizioni();
        assertNotEquals(voce.disattivata(), voce);
        assertNotEquals(new VoceMD(null, condizioni, attacco, true, "Finisci i feriti"), voce);
        assertNotEquals(new VoceMD(AVVERSARI_PIU_DEBOLI, Collections.emptyList(), attacco, true, "Finisci i feriti"), voce);
        assertNotEquals(new VoceMD(AVVERSARI_PIU_DEBOLI, condizioni, Collections.singletonList(AzioneMD.di(TipoAzione.DARDO_ARCANO)), true,
                "Finisci i feriti"), voce);
        assertNotEquals(new VoceMD(AVVERSARI_PIU_DEBOLI, condizioni, attacco, true, "Un altro nome"), voce);
    }

    @Test
    void unaTatticaSiSalvaESiRilegge() throws IOException {
        TatticaMD tattica = TatticaMD.di(voceCompleta(), VoceMD.su(null, AzioneMD.di(TipoAzione.DARDO_ARCANO)).disattivata());
        assertEquals(tattica, TatticaMD.leggi(lettore(salva(tattica))));
        TatticaMD disabilitata = tattica.disabilitata();
        TatticaMD riletta = TatticaMD.leggi(lettore(salva(disabilitata)));
        assertTrue(riletta.isDisabilitata());
        assertEquals(disabilitata, riletta);
        assertSame(disabilitata, disabilitata.disabilitata(), "disabilitarla di nuovo non cambia niente");
        assertTrue(TatticaMD.VUOTA.getVoci().isEmpty());
    }

    @Test
    void iProblemiDiUnaTatticaHannoIlNumeroDellaVoce() {
        VoceMD miste = new VoceMD(null, Collections.emptyList(),
                Arrays.asList(AzioneMD.di(TipoAzione.ATTACCO_FISICO), AzioneMD.di(TipoAzione.POZIONE_SALUTE)));
        TatticaMD tattica = TatticaMD.di(voceCompleta(), miste);
        assertTrue(tattica.problemi(true).stream().allMatch(p -> p.startsWith("voce 2: ")), String.valueOf(tattica.problemi(true)));
        assertFalse(tattica.problemiPerQualsiasiLato().isEmpty(), "con problemi per tutti e due i lati");

        // Una rosa che vale solo per i mostri: per i miei è un problema, per un mostro no
        TatticaMD deiMostri = TatticaMD.di(VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI_E_OSPITI, Collections.emptyList(),
                OrdinamentoMD.di(TipoOrdinamento.PRIMO)), AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
        assertFalse(deiMostri.problemi(true).isEmpty());
        assertTrue(deiMostri.problemiPerQualsiasiLato().isEmpty());
        assertTrue(TatticaMD.di(voceCompleta()).problemiPerQualsiasiLato().isEmpty());
    }

    @Test
    void unaTatticaIncoerenteSiRileggeDisabilitata() throws IOException {
        TatticaMD incoerente = TatticaMD.di(new VoceMD(null, Collections.emptyList(),
                Arrays.asList(AzioneMD.di(TipoAzione.ATTACCO_FISICO), AzioneMD.di(TipoAzione.POZIONE_SALUTE))));
        TatticaMD riletta = TatticaMD.leggi(lettore(salva(incoerente)));
        assertTrue(riletta.isDisabilitata(), "il giocatore la corregge nella schermata delle tattiche");
        assertEquals(incoerente.getVoci(), riletta.getVoci());
    }

    @Test
    void unaTatticaSiDescriveESiConfronta() {
        TatticaMD tattica = TatticaMD.di(VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
        assertEquals("[→ [ATTACCO_FISICO]]", tattica.toString());
        assertEquals("(disabilitata) [→ [ATTACCO_FISICO]]", tattica.disabilitata().toString());
        assertEquals(TatticaMD.di(VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO))).hashCode(), tattica.hashCode());
        assertNotEquals(tattica.disabilitata(), tattica);
        assertNotEquals(tattica.disabilitata().hashCode(), tattica.hashCode());
        assertNotEquals(TatticaMD.VUOTA, tattica);
        assertNotEquals("tattica", tattica);
    }

    @Test
    void unaTatticaIlleggibileDaUnErroreChiaro() {
        IOException nomeSconosciuto = assertThrows(IOException.class,
                () -> TatticaMD.leggi(lettore("TATTICA|1|0\nVOCE|true|false|0|1|\nAZIONE|NON_ESISTE||false|\n")));
        assertTrue(nomeSconosciuto.getMessage().contains("NON_ESISTE"), nomeSconosciuto.getMessage());
        IOException etichetta = assertThrows(IOException.class, () -> TatticaMD.leggi(lettore("VOCE|1|0\n")));
        assertTrue(etichetta.getMessage().contains("atteso TATTICA"), etichetta.getMessage());
        IOException senzaAzioni = assertThrows(IOException.class, () -> TatticaMD.leggi(lettore("TATTICA|1|0\nVOCE|true|false|0|0|\n")));
        assertTrue(senzaAzioni.getMessage().contains("senza azioni"), senzaAzioni.getMessage());
    }
}
