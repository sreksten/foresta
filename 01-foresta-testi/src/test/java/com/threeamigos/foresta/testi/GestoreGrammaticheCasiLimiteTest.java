package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.tipi.TipoModalitaProduzioneTesti;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.GestoreGrammatiche.GrammaticaMalformataException;
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
 * I casi limite di {@link GestoreGrammatiche}: gli errori delle risorse e delle inclusioni, le sezioni statiche condivise, gli escape
 * dentro i tratti letterali e i valori predefiniti di un elemento.
 */
class GestoreGrammaticheCasiLimiteTest {

    private static final String CARTELLA = "/com/threeamigos/foresta/testi/casi_limite/";

    @AfterEach
    void ripristina() {
        Logger.impostaDestinazione(null);
        GestoreGrammatiche.ripristinaLeSezioniStatiche();
    }

    private static InputStream flusso(String testo) {
        return new ByteArrayInputStream(testo.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * File in memoria, che si identificano con un nome unico per ogni istanza (un test non vede le sezioni di un altro).
     */
    private static final class Risolutore implements GestoreGrammatiche.RisolutoreDelleInclusioni {
        private final Map<String, String> file = new HashMap<>();
        private final String identita = UUID.randomUUID().toString();

        Risolutore con(String percorso, String testo) {
            file.put(percorso, testo);
            return this;
        }

        @Override
        public InputStream apri(String percorso) {
            String testo = file.get(percorso);
            return testo == null ? null : flusso(testo);
        }

        @Override
        public String identita(String percorso) {
            return identita + ":" + percorso;
        }
    }

    private static GestoreGrammatiche grammatica(String principale, GestoreGrammatiche.RisolutoreDelleInclusioni risolutore) throws Exception {
        return new GestoreGrammatiche(flusso(principale), null, risolutore);
    }

    private static String errore(String grammatica) {
        return assertThrows(GrammaticaMalformataException.class, () -> new GestoreGrammatiche(grammatica)).getMessage();
    }

    // ------------------------------------------------------------------------------------------------ risorse

    @Test
    void ilRisolutoreSenzaIdentitaNonNeDa() {
        GestoreGrammatiche.RisolutoreDelleInclusioni risolutore = percorso -> null;
        assertNull(risolutore.identita("nomi.txt"));
    }

    @Test
    void unaGrammaticaDaRisorsaPuoAvereLaPostproduzione() throws Exception {
        GestoreGrammatiche bean = GestoreGrammatiche.daRisorsa(GestoreGrammaticheCasiLimiteTest.class, CARTELLA + "radice.txt", CARTELLA + "post.txt");
        assertEquals("vai al castello", bean.produci().get(0));
    }

    @Test
    void unaPostproduzioneCheNonEsisteEUnErrore() {
        GrammaticaMalformataException e = assertThrows(GrammaticaMalformataException.class,
                () -> GestoreGrammatiche.daRisorsa(GestoreGrammaticheCasiLimiteTest.class, CARTELLA + "radice.txt", CARTELLA + "manca.txt"));
        assertTrue(e.getMessage().contains("Post-production resource not found"), e.getMessage());
    }

    @Test
    void unFileStaticoDiUnaRisorsaChePuoNonEsistereEUnErrore() {
        GrammaticaMalformataException e = assertThrows(GrammaticaMalformataException.class,
                () -> GestoreGrammatiche.daRisorsa(GestoreGrammaticheCasiLimiteTest.class, CARTELLA + "statica_mancante.txt", null));
        assertTrue(e.getMessage().contains("Included file not found"), e.getMessage());
    }

    @Test
    void laGrammaticaNonPuoEssereNull() {
        assertThrows(GrammaticaMalformataException.class, () -> new GestoreGrammatiche((InputStream) null, null));
        assertThrows(GrammaticaMalformataException.class, () -> new GestoreGrammatiche((InputStream) null, null, percorso -> null));
    }

    // ------------------------------------------------------------------------------------------------ inclusioni

    @Test
    void unNomeDiFileVuotoInUnaDirettivaEUnErrore() {
        GrammaticaMalformataException e = assertThrows(GrammaticaMalformataException.class,
                () -> grammatica("RADICE\n    ciao\n#include < >\n", new Risolutore()));
        assertTrue(e.getMessage().contains("Invalid file name"), e.getMessage());
    }

    @Test
    void unFileInclusoPuoCominciareConIlBom() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "﻿NOME\n    Aldo\n");

        GestoreGrammatiche bean = grammatica("RADICE\n    [NOME]\n#include <nomi.txt>\n", risolutore);

        assertEquals("Aldo", bean.produci().get(0));
    }

    @Test
    void unFileStaticoChePuoNonEsisteSeIlRisolutoreNonLoIdentifica() {
        GestoreGrammatiche.RisolutoreDelleInclusioni senzaIdentita = percorso -> null;
        GrammaticaMalformataException e = assertThrows(GrammaticaMalformataException.class,
                () -> grammatica("RADICE\n    ciao\n#include_static <nomi.txt>\n", senzaIdentita));
        assertTrue(e.getMessage().contains("Included file not found"), e.getMessage());
    }

    @Test
    void unaProduzioneStaticaNonPuoEssereDefinitaDaDueSezioni() {
        Risolutore risolutore = new Risolutore().con("a.txt", "NOME$\n    Aldo\n").con("b.txt", "NOME$\n    Bruno\n");
        GrammaticaMalformataException e = assertThrows(GrammaticaMalformataException.class,
                () -> grammatica("RADICE\n    [NOME]\n#include_static <a.txt>\n#include_static <b.txt>\n", risolutore));
        assertTrue(e.getMessage().contains("defined by two static sections"), e.getMessage());
    }

    @Test
    void unaProduzioneStaticaEsauritaSiPotaUnaVoltaSolaPerGrammatica() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "N1$\n    uno\nN2$\n    due\n");
        GestoreGrammatiche a = grammatica("X1\n    [N1]\nX2\n    [N2]\n#include_static <nomi.txt>\n", risolutore);
        GestoreGrammatiche b = grammatica("Y1\n    [N1]\nY2\n    [N2]\nY3\n    resto\n#include_static <nomi.txt>\n", risolutore);

        assertEquals("uno", a.produci("X1").get(0));
        assertEquals("resto", b.produci("Y3").get(0));
        assertFalse(b.puoProdurre("Y1"), "N1 è esaurita, e con lei ciò che ne dipende");
        assertEquals("due", a.produci("X2").get(0));
        assertEquals("resto", b.produci("Y3").get(0));
        assertFalse(b.puoProdurre("Y2"));
    }

    @Test
    void ilNumeroDiRigaFuoriDalSorgenteSiDiceSenzaOrigine() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "NOME\n    Aldo\n");
        GestoreGrammatiche conInclusioni = grammatica("RADICE\n    [NOME]\n#include <nomi.txt>\n", risolutore);

        assertEquals("Line 0", conInclusioni.posizione(0));
        assertEquals("Line 99", conInclusioni.posizione(99));
        assertTrue(conInclusioni.posizione(1).startsWith("Line 1 ("), conInclusioni.posizione(1));
        assertEquals("Line 1", new GestoreGrammatiche("RADICE\n    ciao\n").posizione(1), "senza inclusioni non ha origine");
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
        GestoreGrammatiche bean = new GestoreGrammatiche("NOME$\n    Aldo\n");
        assertEquals("Aldo", bean.produci().get(0));
    }

    @Test
    void unFileInclusoVuotoNonAggiungeNiente() throws Exception {
        Risolutore risolutore = new Risolutore().con("vuoto.txt", "");

        GestoreGrammatiche bean = grammatica("RADICE\n    ciao\n#include <vuoto.txt>\n", risolutore);

        assertEquals("ciao", bean.produci().get(0));
    }

    @Test
    void piuSezioniStaticheConProduzioniDiverseConvivono() throws Exception {
        Risolutore risolutore = new Risolutore().con("a.txt", "N1\n    uno\n").con("b.txt", "N2\n    due\n");

        GestoreGrammatiche bean = grammatica("RADICE\n    [N1] [N2]\n#include_static <a.txt>\n#include_static <b.txt>\n", risolutore);

        assertEquals("uno due", bean.produci().get(0));
    }

    @Test
    void unaProduzioneStaticaEsauritaDaUnAltraGrammaticaNonSiPuoPiuScegliere() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "N1$\n    uno\n");
        GestoreGrammatiche a = grammatica("X1\n    [N1]\n#include_static <nomi.txt>\n", risolutore);
        GestoreGrammatiche b = grammatica("Y1\n    [N1]\nY2\n    resto\n#include_static <nomi.txt>\n", risolutore);
        assertEquals("uno", a.produci("X1").get(0));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> b.produzioneStatica("N1"));

        assertTrue(e.getMessage().contains("is empty"), e.getMessage());
    }

    // ------------------------------------------------------------------------------------------------ escape

    @Test
    void ilTestoFraVirgoletteTieneGliEscapeDelleVirgoletteEDelleParentesi() throws Exception {
        GestoreGrammatiche bean = new GestoreGrammatiche("RADICE\n    \"dice \\\"ciao\\\" e \\[x\\] \\]y\\[\" {a|b}\n");

        assertTrue(bean.produci().get(0).matches("dice \"ciao\" e \\[x\\] \\]y\\[ [ab]"), "una virgoletta e le parentesi escape restano letterali");
    }

    @Test
    void unaVirgolettaEscapeDentroUnGruppoNonLoChiude() throws Exception {
        GestoreGrammatiche bean = new GestoreGrammatiche("RADICE\n    {\"a \\\" b\"|c}\n");
        bean.setModalita(TipoModalitaProduzioneTesti.PRIMO);

        assertEquals("a \" b", bean.produci().get(0));
    }

    @Test
    void ilSeparatoreDiAlternativeNonSiVedeFraVirgolette() throws Exception {
        GestoreGrammatiche bean = new GestoreGrammatiche("RADICE\n    a | \"b \\\" | c\"\n");

        bean.setModalita(TipoModalitaProduzioneTesti.ULTIMO);
        assertEquals("b \" | c", bean.produci().get(0));
        bean.setModalita(TipoModalitaProduzioneTesti.PRIMO);
        assertEquals("a", bean.produci().get(0));
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
        assertErroreContiene("[}NOME?]", "without a matching");
        assertErroreContiene("[\"a\\\"=b\"?]", "default covers the order");
        assertErroreContiene("[#Z?|niente]", "Production Z is not defined");
        assertErroreContiene("[ ? ]", "must be preceded by a node name");
        assertErroreContiene("[{a=b}?]", "has no '|'");
    }

    @Test
    void unAssegnazioneIlCuiValoreFinisceConIlPuntoInterrogativoRestaUnaAssegnazione() throws Exception {
        GestoreGrammatiche bean = new GestoreGrammatiche("RADICE\n    [K=ciao?][#K]\n");

        assertEquals("ciao?", bean.produci().get(0));
    }

    @Test
    void unElementoConUnSegnoUgualeFraVirgoletteOParentesiNonAssegnaNiente() {
        assertDoesNotThrow(() -> new GestoreGrammatiche("RADICE\n    [\"a=b\"?]\n"));
        // Una graffa di chiusura senza apertura è un errore di caricamento, anche dentro un riferimento
        assertErroreContiene("[}a=b?]", "without a matching");
        assertDoesNotThrow(() -> new GestoreGrammatiche("RADICE\n    [[NOME]=y?]\nNOME\n    aldo\n"));
    }

    @Test
    void ilValorePredefinitoSiUsaSoloSeLaChiaveNonHaUnValore() throws Exception {
        GestoreGrammatiche assegnata = new GestoreGrammatiche("RADICE\n    [K=ciao][#K? | niente]\n");
        assertEquals("ciao", assegnata.produci().get(0));

        GestoreGrammatiche senzaValore = new GestoreGrammatiche("RADICE\n    [#K? | niente]\nK\n    x\n");
        assertEquals("niente", senzaValore.produci().get(0));

        GestoreGrammatiche globale = new GestoreGrammatiche("RADICE\n    [#K? | niente]\nK\n    x\n");
        globale.fissaProduzione("K", "globale");
        assertEquals("globale", globale.produci().get(0));
    }

    @Test
    void unaChiaveAssegnataNonEUnaProduzioneQuindiIlRiferimentoFissatoUsaIlPredefinito() throws Exception {
        GestoreGrammatiche bean = new GestoreGrammatiche("RADICE\n    [K=ciao][!K? | niente]\n");

        assertEquals("niente", bean.produci().get(0));
    }

    @Test
    void ilValorePredefinitoVaneAncheSuUnaProduzioneStatica() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "N1\n    uno\n");

        GestoreGrammatiche bean = grammatica("RADICE\n    [N1? | niente]\n#include_static <nomi.txt>\n", risolutore);

        assertEquals("uno", bean.produci().get(0));
    }

    @Test
    void unaProduzioneFissataSiRisolveSeEsisteAltrimentiSiUsaIlPredefinito() throws Exception {
        GestoreGrammatiche esistente = new GestoreGrammatiche("RADICE\n    [*NOME? | niente] [!NOME? | niente]\nNOME\n    aldo\n");
        assertEquals("aldo aldo", esistente.produci().get(0));

        GestoreGrammatiche esaurita = new GestoreGrammatiche("RADICE\n    [N] [*N? | niente]\nN$\n    solo\n");
        assertEquals("solo niente", esaurita.produci().get(0), "N è esaurita: vale il predefinito");
    }

    @Test
    void unaProduzioneEsauritaNonSiPuoPiuProdurre() throws Exception {
        GestoreGrammatiche bean = new GestoreGrammatiche("RADICE\n    [N]\nN$\n    solo\n");
        assertTrue(bean.puoProdurre("N"));

        assertEquals("solo", bean.produci("RADICE").get(0));

        assertFalse(bean.puoProdurre("N"), "dopo averla prodotta non c'è più");
        assertFalse(bean.puoProdurre("NonEsiste"));
    }

    // ------------------------------------------------------------------------------------------------ escape e virgolette

    @Test
    void unEscapeEUnBackslashSeguitoDaUnAltroCarattere() {
        assertTrue(GestoreGrammatiche.eUnEscape("a\\b", 1));
        assertFalse(GestoreGrammatiche.eUnEscape("ab\\", 2), "come ultimo carattere non è un escape");
        assertFalse(GestoreGrammatiche.eUnEscape("ab", 0));
        assertTrue(GestoreGrammatiche.eLEscapeDi("\\\"", 0, '"'));
        assertFalse(GestoreGrammatiche.eLEscapeDi("\\x", 0, '"'));
        assertFalse(GestoreGrammatiche.eLEscapeDi("\"x", 0, '"'));
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
        assertDoesNotThrow(() -> new GestoreGrammatiche("RADICE\n    [\"a\\\"b\\\"=c\"?]\n"));
        assertDoesNotThrow(() -> new GestoreGrammatiche("RADICE\n    [\"a\\\"b\\\\c\\\"=d\"?]\n"));
    }

    @Test
    void unaGraffaDiChiusuraSenzaAperturaEUnErroreDiCaricamento() {
        // Prima la riga diventava una sola alternativa, "A } | B", senza dire niente
        assertErroreContiene("A } | B", "without a matching");
        assertDoesNotThrow(() -> new GestoreGrammatiche("RADICE\n    A \"}\" | B\n"), "fra virgolette è testo");
    }

    @Test
    void leProduzioniDerivateACatenaHannoTutteLeAlternativeInQualunqueOrdine() throws Exception {
        GestoreGrammatiche primaLaDerivata = new GestoreGrammatiche("A = B\nB = C\nC\n    c\n");
        assertEquals("c", primaLaDerivata.produci("A").get(0));
        GestoreGrammatiche primaLaSorgente = new GestoreGrammatiche("C\n    c\nB = C\nA = B\n");
        assertEquals("c", primaLaSorgente.produci("A").get(0));
        GestoreGrammatiche conAlternativeProprie = new GestoreGrammatiche("A = B\n    a\nB = C\n    b\nC\n    c\n");
        conAlternativeProprie.setModalita(TipoModalitaProduzioneTesti.ULTIMO);
        assertEquals("c", conAlternativeProprie.produci("A").get(0), "A ha a, poi b e c da B");
    }

    @Test
    void unCicloDiDerivazioniEUnErroreCheLoDescrive() {
        GestoreGrammatiche.GrammaticaMalformataException errore = assertThrows(GestoreGrammatiche.GrammaticaMalformataException.class,
                () -> new GestoreGrammatiche("A = B\nB = C\nC = A\n"));
        assertTrue(errore.getMessage().contains("A -> B -> C -> A"), errore.getMessage());
    }
}
