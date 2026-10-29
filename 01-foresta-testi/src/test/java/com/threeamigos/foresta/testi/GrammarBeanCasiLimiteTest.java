package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.tipi.TipoModalitaProduzioneTesti;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.GrammarBean.InvalidGrammarException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi limite di {@link GrammarBean}: gli errori delle risorse e delle inclusioni, le sezioni statiche condivise, gli escape
 * dentro i tratti letterali e i valori predefiniti di un elemento.
 */
class GrammarBeanCasiLimiteTest {

    private static final String CARTELLA = "/com/threeamigos/foresta/testi/casi_limite/";

    @AfterEach
    void ripristina() {
        Logger.impostaDestinazione(null);
        GrammarBean.resetStaticProductions();
    }

    private static InputStream flusso(String testo) {
        return new ByteArrayInputStream(testo.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * File in memoria, che si identificano con un nome unico per ogni istanza (un test non vede le sezioni di un altro).
     */
    private static final class Risolutore implements GrammarBean.IncludeResolver {
        private final Map<String, String> file = new HashMap<>();
        private final String identita = UUID.randomUUID().toString();

        Risolutore con(String percorso, String testo) {
            file.put(percorso, testo);
            return this;
        }

        @Override
        public InputStream open(String percorso) {
            String testo = file.get(percorso);
            return testo == null ? null : flusso(testo);
        }

        @Override
        public String identity(String percorso) {
            return identita + ":" + percorso;
        }
    }

    private static GrammarBean grammatica(String principale, GrammarBean.IncludeResolver risolutore) throws Exception {
        return new GrammarBean(flusso(principale), null, risolutore);
    }

    private static String errore(String grammatica) {
        return assertThrows(InvalidGrammarException.class, () -> new GrammarBean(grammatica)).getMessage();
    }

    // ------------------------------------------------------------------------------------------------ risorse

    @Test
    void ilRisolutoreSenzaIdentitaNonNeDa() {
        GrammarBean.IncludeResolver risolutore = percorso -> null;
        assertNull(risolutore.identity("nomi.txt"));
    }

    @Test
    void unaGrammaticaDaRisorsaPuoAvereLaPostproduzione() throws Exception {
        GrammarBean bean = GrammarBean.fromResource(GrammarBeanCasiLimiteTest.class, CARTELLA + "radice.txt", CARTELLA + "post.txt");
        assertEquals("vai al castello", bean.produce().get(0));
    }

    @Test
    void unaPostproduzioneCheNonEsisteEUnErrore() {
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class,
                () -> GrammarBean.fromResource(GrammarBeanCasiLimiteTest.class, CARTELLA + "radice.txt", CARTELLA + "manca.txt"));
        assertTrue(e.getMessage().contains("Post-production resource not found"), e.getMessage());
    }

    @Test
    void unFileStaticoDiUnaRisorsaChePuoNonEsistereEUnErrore() {
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class,
                () -> GrammarBean.fromResource(GrammarBeanCasiLimiteTest.class, CARTELLA + "statica_mancante.txt", null));
        assertTrue(e.getMessage().contains("Included file not found"), e.getMessage());
    }

    @Test
    void laGrammaticaNonPuoEssereNull() {
        assertThrows(InvalidGrammarException.class, () -> new GrammarBean((InputStream) null, null));
        assertThrows(InvalidGrammarException.class, () -> new GrammarBean((InputStream) null, null, percorso -> null));
    }

    // ------------------------------------------------------------------------------------------------ inclusioni

    @Test
    void unNomeDiFileVuotoInUnaDirettivaEUnErrore() {
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class,
                () -> grammatica("RADICE\n    ciao\n#include < >\n", new Risolutore()));
        assertTrue(e.getMessage().contains("Invalid file name"), e.getMessage());
    }

    @Test
    void unFileInclusoPuoCominciareConIlBom() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "﻿NOME\n    Aldo\n");

        GrammarBean bean = grammatica("RADICE\n    [NOME]\n#include <nomi.txt>\n", risolutore);

        assertEquals("Aldo", bean.produce().get(0));
    }

    @Test
    void unFileStaticoChePuoNonEsisteSeIlRisolutoreNonLoIdentifica() {
        GrammarBean.IncludeResolver senzaIdentita = percorso -> null;
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class,
                () -> grammatica("RADICE\n    ciao\n#include_static <nomi.txt>\n", senzaIdentita));
        assertTrue(e.getMessage().contains("Included file not found"), e.getMessage());
    }

    @Test
    void unaProduzioneStaticaNonPuoEssereDefinitaDaDueSezioni() {
        Risolutore risolutore = new Risolutore().con("a.txt", "NOME$\n    Aldo\n").con("b.txt", "NOME$\n    Bruno\n");
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class,
                () -> grammatica("RADICE\n    [NOME]\n#include_static <a.txt>\n#include_static <b.txt>\n", risolutore));
        assertTrue(e.getMessage().contains("defined by two static sections"), e.getMessage());
    }

    @Test
    void unaProduzioneStaticaEsauritaSiPotaUnaVoltaSolaPerGrammatica() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "N1$\n    uno\nN2$\n    due\n");
        GrammarBean a = grammatica("X1\n    [N1]\nX2\n    [N2]\n#include_static <nomi.txt>\n", risolutore);
        GrammarBean b = grammatica("Y1\n    [N1]\nY2\n    [N2]\nY3\n    resto\n#include_static <nomi.txt>\n", risolutore);

        assertEquals("uno", a.produce("X1").get(0));
        assertEquals("resto", b.produce("Y3").get(0));
        assertFalse(b.canProduce("Y1"), "N1 è esaurita, e con lei ciò che ne dipende");
        assertEquals("due", a.produce("X2").get(0));
        assertEquals("resto", b.produce("Y3").get(0));
        assertFalse(b.canProduce("Y2"));
    }

    @Test
    void ilNumeroDiRigaFuoriDalSorgenteSiDiceSenzaOrigine() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "NOME\n    Aldo\n");
        GrammarBean conInclusioni = grammatica("RADICE\n    [NOME]\n#include <nomi.txt>\n", risolutore);

        assertEquals("Line 0", conInclusioni.position(0));
        assertEquals("Line 99", conInclusioni.position(99));
        assertTrue(conInclusioni.position(1).startsWith("Line 1 ("), conInclusioni.position(1));
        assertEquals("Line 1", new GrammarBean("RADICE\n    ciao\n").position(1), "senza inclusioni non ha origine");
    }

    // ------------------------------------------------------------------------------------------------ sorgente

    @Test
    void ilSegnapostoDiChiusuraNonSiPuoScrivereNellaGrammatica() {
        assertTrue(errore("RADICE\n    ab\n").contains("Reserved placeholder"));
        assertTrue(errore("RADICE\n    ab\n").contains("Reserved placeholder"));
    }

    @Test
    void unaProduzioneMonousoSiSegnalaSoloSeIlRegistroELAttivo() throws Exception {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
        GrammarBean bean = new GrammarBean("NOME$\n    Aldo\n");
        assertEquals("Aldo", bean.produce().get(0));
    }

    @Test
    void unFileInclusoVuotoNonAggiungeNiente() throws Exception {
        Risolutore risolutore = new Risolutore().con("vuoto.txt", "");

        GrammarBean bean = grammatica("RADICE\n    ciao\n#include <vuoto.txt>\n", risolutore);

        assertEquals("ciao", bean.produce().get(0));
    }

    @Test
    void piuSezioniStaticheConProduzioniDiverseConvivono() throws Exception {
        Risolutore risolutore = new Risolutore().con("a.txt", "N1\n    uno\n").con("b.txt", "N2\n    due\n");

        GrammarBean bean = grammatica("RADICE\n    [N1] [N2]\n#include_static <a.txt>\n#include_static <b.txt>\n", risolutore);

        assertEquals("uno due", bean.produce().get(0));
    }

    @Test
    void unaProduzioneStaticaEsauritaDaUnAltraGrammaticaNonSiPuoPiuScegliere() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "N1$\n    uno\n");
        GrammarBean a = grammatica("X1\n    [N1]\n#include_static <nomi.txt>\n", risolutore);
        GrammarBean b = grammatica("Y1\n    [N1]\nY2\n    resto\n#include_static <nomi.txt>\n", risolutore);
        assertEquals("uno", a.produce("X1").get(0));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> b.getStaticProduction("N1"));

        assertTrue(e.getMessage().contains("is empty"), e.getMessage());
    }

    // ------------------------------------------------------------------------------------------------ escape

    @Test
    void ilTestoFraVirgoletteTieneGliEscapeDelleVirgoletteEDelleParentesi() throws Exception {
        GrammarBean bean = new GrammarBean("RADICE\n    \"dice \\\"ciao\\\" e \\[x\\] \\]y\\[\" {a|b}\n");

        assertTrue(bean.produce().get(0).matches("dice \"ciao\" e \\[x\\] \\]y\\[ [ab]"), "una virgoletta e le parentesi escape restano letterali");
    }

    @Test
    void unaVirgolettaEscapeDentroUnGruppoNonLoChiude() throws Exception {
        GrammarBean bean = new GrammarBean("RADICE\n    {\"a \\\" b\"|c}\n");
        bean.setProductionMode(TipoModalitaProduzioneTesti.PRIMO);

        assertEquals("a \" b", bean.produce().get(0));
    }

    @Test
    void ilSeparatoreDiAlternativeNonSiVedeFraVirgolette() throws Exception {
        GrammarBean bean = new GrammarBean("RADICE\n    a | \"b \\\" | c\"\n");

        bean.setProductionMode(TipoModalitaProduzioneTesti.ULTIMO);
        assertEquals("b \" | c", bean.produce().get(0));
        bean.setProductionMode(TipoModalitaProduzioneTesti.PRIMO);
        assertEquals("a", bean.produce().get(0));
    }

    // ------------------------------------------------------------------------------------------------ elementi

    private static void assertErroreContiene(String riga, String atteso) {
        String messaggio = errore("RADICE\n    " + riga + "\nNOME\n    aldo\n");
        assertTrue(messaggio.contains(atteso), riga + " -> " + messaggio);
    }

    @Test
    void unNomeConVirgoletteBraceOParentesiNonEUnaProduzione() {
        assertErroreContiene("[\"a|b\"]", "Production a|b is not defined");
        assertErroreContiene("[\"a\\\"b\"]", "is not defined");
        assertErroreContiene("[ |x]", "is not defined");
        assertErroreContiene("[NOME | x]", "is not defined");
    }

    @Test
    void unValorePredefinitoCheNomaUnaProduzioneInesistenteEUnErrore() {
        assertErroreContiene("[{a|b}?|z]", "default covers the order");
        assertErroreContiene("[ {a|b} ? | z ]", "default covers the order");
        assertErroreContiene("[[NOME]?]", "default covers the order");
        assertErroreContiene("[}NOME?]", "default covers the order");
        assertErroreContiene("[\"a\\\"=b\"?]", "default covers the order");
        assertErroreContiene("[#Z?|niente]", "Production Z is not defined");
        assertErroreContiene("[ ? ]", "must be preceded by a node name");
        assertErroreContiene("[{a=b}?]", "has no '|'");
    }

    @Test
    void unAssegnazioneIlCuiValoreFinisceConIlPuntoInterrogativoRestaUnaAssegnazione() throws Exception {
        GrammarBean bean = new GrammarBean("RADICE\n    [K=ciao?][#K]\n");

        assertEquals("ciao?", bean.produce().get(0));
    }

    @Test
    void unElementoConUnSegnoUgualeFraVirgoletteOParentesiNonAssegnaNiente() {
        assertDoesNotThrow(() -> new GrammarBean("RADICE\n    [\"a=b\"?]\n"));
        assertDoesNotThrow(() -> new GrammarBean("RADICE\n    [}a=b?]\n"));
        assertDoesNotThrow(() -> new GrammarBean("RADICE\n    [[NOME]=y?]\nNOME\n    aldo\n"));
    }

    @Test
    void ilValorePredefinitoSiUsaSoloSeLaChiaveNonHaUnValore() throws Exception {
        GrammarBean assegnata = new GrammarBean("RADICE\n    [K=ciao][#K? | niente]\n");
        assertEquals("ciao", assegnata.produce().get(0));

        GrammarBean senzaValore = new GrammarBean("RADICE\n    [#K? | niente]\nK\n    x\n");
        assertEquals("niente", senzaValore.produce().get(0));

        GrammarBean globale = new GrammarBean("RADICE\n    [#K? | niente]\nK\n    x\n");
        globale.addFixedProduction("K", "globale");
        assertEquals("globale", globale.produce().get(0));
    }

    @Test
    void unaChiaveAssegnataNonEUnaProduzioneQuindiIlRiferimentoFissatoUsaIlPredefinito() throws Exception {
        GrammarBean bean = new GrammarBean("RADICE\n    [K=ciao][!K? | niente]\n");

        assertEquals("niente", bean.produce().get(0));
    }

    @Test
    void ilValorePredefinitoVaneAncheSuUnaProduzioneStatica() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "N1\n    uno\n");

        GrammarBean bean = grammatica("RADICE\n    [N1? | niente]\n#include_static <nomi.txt>\n", risolutore);

        assertEquals("uno", bean.produce().get(0));
    }

    @Test
    void unaProduzioneFissataSiRisolveSeEsisteAltrimentiSiUsaIlPredefinito() throws Exception {
        GrammarBean esistente = new GrammarBean("RADICE\n    [*NOME? | niente] [!NOME? | niente]\nNOME\n    aldo\n");
        assertEquals("aldo aldo", esistente.produce().get(0));

        GrammarBean esaurita = new GrammarBean("RADICE\n    [N] [*N? | niente]\nN$\n    solo\n");
        assertEquals("solo niente", esaurita.produce().get(0), "N è esaurita: vale il predefinito");
    }

    @Test
    void unaProduzioneEsauritaNonSiPuoPiuProdurre() throws Exception {
        GrammarBean bean = new GrammarBean("RADICE\n    [N]\nN$\n    solo\n");
        assertTrue(bean.canProduce("N"));

        assertEquals("solo", bean.produce("RADICE").get(0));

        assertFalse(bean.canProduce("N"), "dopo averla prodotta non c'è più");
        assertFalse(bean.canProduce("NonEsiste"));
    }

    // ------------------------------------------------------------------------------------------------ escape e virgolette

    @Test
    void unEscapeEUnBackslashSeguitoDaUnAltroCarattere() {
        assertTrue(GrammarBean.isEscape("a\\b", 1));
        assertFalse(GrammarBean.isEscape("ab\\", 2), "come ultimo carattere non è un escape");
        assertFalse(GrammarBean.isEscape("ab", 0));
        assertTrue(GrammarBean.isEscapeOf("\\\"", 0, '"'));
        assertFalse(GrammarBean.isEscapeOf("\\x", 0, '"'));
        assertFalse(GrammarBean.isEscapeOf("\"x", 0, '"'));
    }

    @Test
    void unaVirgolettaLetteraleNelNomeDiUnElementoNonLoFaDiventareUnaProduzione() {
        assertErroreContiene("[\"a\\\"b\\\"c\"]", "is not defined");
        assertErroreContiene("[\"a\\\"b\\\\c\\\"d\"]", "is not defined");
        assertErroreContiene("[\"{\"NOME?|z]", "is not defined");
        assertErroreContiene("[\"{\"a=b?]", "default covers the order");
        assertErroreContiene("[[K=x]?]", "default covers the order");
        assertErroreContiene("[\"a\\\"b\\\\\"?]", "Missing closing");
    }

    @Test
    void unSegnoUgualeDopoUnaVirgolettaLetteraleEUnaAssegnazione() {
        assertDoesNotThrow(() -> new GrammarBean("RADICE\n    [\"a\\\"b\\\"=c\"?]\n"));
        assertDoesNotThrow(() -> new GrammarBean("RADICE\n    [\"a\\\"b\\\\c\\\"=d\"?]\n"));
    }
}
