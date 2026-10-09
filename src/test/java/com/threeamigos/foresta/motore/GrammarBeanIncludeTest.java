package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.GrammarBean.InvalidGrammarException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La direttiva {@code #include <file>} di GrammarBean (vedi GrammarBean.md, §4.11).
 */
class GrammarBeanIncludeTest {

    private static InputStream flusso(String testo) {
        return new ByteArrayInputStream(testo.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Una grammatica principale e i file che può includere, tutti in memoria; ricorda quali file sono stati chiesti.
     */
    private static GrammarBean grammatica(String principale, Map<String, String> file, List<String> richiesti) throws Exception {
        return new GrammarBean(flusso(principale), null, percorso -> {
            richiesti.add(percorso);
            String testo = file.get(percorso);
            return testo == null ? null : flusso(testo);
        });
    }

    private static GrammarBean grammatica(String principale, Map<String, String> file) throws Exception {
        return grammatica(principale, file, new ArrayList<>());
    }

    private static Map<String, String> file(String... coppie) {
        Map<String, String> file = new HashMap<>();
        for (int i = 0; i < coppie.length; i += 2) {
            file.put(coppie[i], coppie[i + 1]);
        }
        return file;
    }

    private static String errore(String principale, Map<String, String> file) {
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class, () -> grammatica(principale, file));
        return e.getMessage();
    }

    @Test
    void includeUnFileNellaStessaCartella() throws Exception {
        GrammarBean bean = grammatica("RADICE\n    ciao [NOME]\n\n#include <nomi.txt>\n", file("nomi.txt", "NOME\n    Aldo\n"));
        assertEquals("ciao Aldo", bean.produce("RADICE").get(0));
    }

    @Test
    void laRadiceDiDefaultELaPrimaProduzioneDopoLEspansione() throws Exception {
        // Con l'include in cima, la prima produzione è quella del file incluso: lo dice il manuale
        GrammarBean bean = grammatica("#include <nomi.txt>\nRADICE\n    ciao [NOME]\n", file("nomi.txt", "NOME\n    Aldo\n"));
        assertEquals("NOME", bean.getRootNode());
        bean.setRootNode("RADICE");
        assertEquals("ciao Aldo", bean.produce().get(0));
    }

    @Test
    void ilNomeEARelativoAllaCartellaDiChiInclude() throws Exception {
        List<String> richiesti = new ArrayList<>();
        GrammarBean bean = grammatica("RADICE\n    [NOME] [COGNOME]\n\n#include <nomi/nomi.txt>\n",
                file("nomi/nomi.txt", "NOME\n    Aldo\n\n#include <cognomi.txt>\n", "nomi/cognomi.txt", "COGNOME\n    Rossi\n"), richiesti);
        assertEquals("Aldo Rossi", bean.produce("RADICE").get(0));
        assertEquals(java.util.Arrays.asList("nomi/nomi.txt", "nomi/cognomi.txt"), richiesti);
    }

    @Test
    void unFileSiIncludeUnaVoltaSola() throws Exception {
        List<String> richiesti = new ArrayList<>();
        // Incluso due volte dalla principale e una da un altro incluso: niente produzioni ripetute
        GrammarBean bean = grammatica("RADICE\n    [NOME] [COGNOME]\n#include <nomi.txt>\n#include <nomi.txt>\n#include <altro.txt>\n",
                file("nomi.txt", "NOME\n    Aldo\n", "altro.txt", "COGNOME\n    Rossi\n#include <nomi.txt>\n"), richiesti);
        assertEquals("Aldo Rossi", bean.produce("RADICE").get(0));
        assertEquals(java.util.Arrays.asList("nomi.txt", "altro.txt"), richiesti);
    }

    @Test
    void dueFileCheSiIncludonoVicendevolmenteNonGirano() throws Exception {
        GrammarBean bean = grammatica("RADICE\n    [A] [B]\n#include <a.txt>\n",
                file("a.txt", "A\n    uno\n#include <b.txt>\n", "b.txt", "B\n    due\n#include <a.txt>\n"));
        assertEquals("uno due", bean.produce("RADICE").get(0));
    }

    @Test
    void ilFileNonTrovatoELErrore() {
        String messaggio = errore("RADICE\n    ciao\n#include <manca.txt>\n", file());
        assertTrue(messaggio.contains("manca.txt") && messaggio.contains("not found"), messaggio);
        assertTrue(messaggio.contains("source:3"), messaggio);
    }

    @Test
    void ISoliNomiRelativiSenzaPuntiSonoAmmessi() {
        for (String nome : new String[]{"../x.txt", "/x.txt", "a//b.txt", "./x.txt", "a/../x.txt", "a\\b.txt", "a/"}) {
            String messaggio = errore("RADICE\n    ciao\n#include <" + nome + ">\n", file("x.txt", "X\n    y\n"));
            assertTrue(messaggio.contains("Invalid file name"), nome + ": " + messaggio);
        }
    }

    @Test
    void laDirettivaDeveAvereLaFormaCStyle() {
        for (String riga : new String[]{"#include nomi.txt", "#include <nomi.txt", "#include nomi.txt>", "#include <>", "#include", "#include \"nomi.txt\""}) {
            String messaggio = errore("RADICE\n    ciao\n" + riga + "\n", file("nomi.txt", "X\n    y\n"));
            assertTrue(messaggio.contains("Malformed"), riga + ": " + messaggio);
        }
    }

    @Test
    void senzaUnResolverLInclusioneNonSiPuoRisolvere() throws Exception {
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class,
                () -> new GrammarBean("RADICE\n    ciao\n#include <nomi.txt>\n"));
        assertTrue(e.getMessage().contains("IncludeResolver"), e.getMessage());
    }

    @Test
    void gliErroriDiUnFileIncluso_DiconoQualeFileEQualeRiga() {
        String messaggio = errore("RADICE\n    [NOME]\n#include <nomi.txt>\n",
                file("nomi.txt", "NOME\n    Aldo\n\n   [peso non chiuso=\n\nNOME2\n"));
        // La riga 3 dell'espansione... ma quel che conta è che si nomini il file di provenienza
        assertTrue(messaggio.contains("nomi.txt:"), messaggio);
    }

    @Test
    void gliErroriDellaPrincipaleIndicanoLaRigaOriginale() {
        // La produzione ripetuta è a riga 5 della principale, dopo un file di 4 righe incluso a riga 3
        String messaggio = errore("RADICE\n    ciao\n#include <nomi.txt>\nRADICE\n    ancora\n",
                file("nomi.txt", "NOME\n    uno\n    due\n    tre\n"));
        assertTrue(messaggio.contains("source:4"), messaggio);
        assertTrue(messaggio.contains("repeated"), messaggio);
    }

    @Test
    void unaRigaDiContinuazioneNonEMaiUnaDirettiva() throws Exception {
        List<String> richiesti = new ArrayList<>();
        GrammarBean bean = grammatica("RADICE\n    uno \\\n#include <nomi.txt>\n", file("nomi.txt", "NOME\n    Aldo\n"), richiesti);
        assertEquals("uno #include <nomi.txt>", bean.produce("RADICE").get(0));
        assertTrue(richiesti.isEmpty());
    }

    @Test
    void unaContinuazioneNonPassaDaUnFileAllAltro() {
        String messaggio = errore("RADICE\n    uno\n#include <nomi.txt>\n", file("nomi.txt", "NOME\n    Aldo \\\n"));
        assertTrue(messaggio.contains("nomi.txt") && messaggio.contains("continuation"), messaggio);
    }

    @Test
    void UnCommentoConLeBarreNonEUnaDirettiva() throws Exception {
        GrammarBean bean = grammatica("// #include <non-un-file>\n//  commento\nRADICE\n    ciao\n", file());
        assertEquals("ciao", bean.produce("RADICE").get(0));
    }

    @Test
    void UnCancellettoCheNonEUnaDirettivaEErrore() {
        String messaggio = errore("# commento\nRADICE\n    ciao\n", new HashMap<>());
        assertTrue(messaggio.contains("Unknown directive"), messaggio);
    }

    @Test
    void limitaLaProfonditaDegliInclude() {
        Map<String, String> catena = new HashMap<>();
        for (int i = 0; i < 15; i++) {
            catena.put("f" + i + ".txt", "P" + i + "\n    x\n#include <f" + (i + 1) + ".txt>\n");
        }
        String messaggio = errore("RADICE\n    x\n#include <f0.txt>\n", catena);
        assertTrue(messaggio.contains("Too many nested"), messaggio);
    }

    @Test
    void daRisorsaLeggeIFileDellaStessaCartellaEDelleSottocartelle() throws Exception {
        GrammarBean bean = GrammarBean.fromResource(GrammarBeanIncludeTest.class,
                "/com/threeamigos/foresta/motore/include_prova/principale.txt", null);
        assertEquals("Aldo Rossi", bean.produce("RADICE").get(0));
    }

    @Test
    void daRisorsaDiceSeLaRisorsaManca() {
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class, () -> GrammarBean.fromResource(
                GrammarBeanIncludeTest.class, "/com/threeamigos/foresta/motore/include_prova/non_esiste.txt", null));
        assertTrue(e.getMessage().contains("not found"), e.getMessage());
    }

    @Test
    void ilFileDiPostProduzioneNonSiToccaEFunzionaConGliInclude() throws Exception {
        GrammarBean bean = new GrammarBean(flusso("RADICE\n    vado a il [NOME]\n#include <nomi.txt>\n"), flusso(" a il : al \n"),
                percorso -> flusso("NOME\n    forno\n"));
        assertEquals("vado al forno", bean.produce("RADICE").get(0).trim());
    }

    private static java.util.Set<String> prodotti(GrammarBean bean, String produzione) {
        java.util.Set<String> prodotti = new java.util.TreeSet<>();
        for (int i = 0; i < 300; i++) {
            prodotti.add(bean.produce(produzione).get(0));
        }
        return prodotti;
    }

    @Test
    void sipuoUsarePiuDiUnaDirettivaNelloStessoFile() throws Exception {
        List<String> richiesti = new ArrayList<>();
        GrammarBean bean = grammatica("RADICE\n    [NOME] [COGNOME] [CITTA]\n#include <nomi.txt>\n#include <cognomi.txt>\n#include <sub/citta.txt>\n",
                file("nomi.txt", "NOME\n    Aldo\n", "cognomi.txt", "COGNOME\n    Rossi\n", "sub/citta.txt", "CITTA\n    Roma\n"), richiesti);
        assertEquals("Aldo Rossi Roma", bean.produce("RADICE").get(0));
        assertEquals(java.util.Arrays.asList("nomi.txt", "cognomi.txt", "sub/citta.txt"), richiesti);
    }

    @Test
    void unIncludeSenzaIntestazioneAggiungeAlternativeALaProduzioneCheLoPrecede() throws Exception {
        GrammarBean bean = grammatica("RADICE\n    uno\n#include <altre.txt>\n", file("altre.txt", "    due\n    tre\n"));
        assertEquals(new java.util.TreeSet<>(java.util.Arrays.asList("uno", "due", "tre")), prodotti(bean, "RADICE"));
    }

    @Test
    void unIncludeNelMezzoDiUnaProduzioneLaSpezza_LeRigheDopoAppartengonoAllUltimaProduzioneDelFileIncluso() throws Exception {
        GrammarBean bean = grammatica("RADICE\n    uno\n#include <altro.txt>\n    due\n", file("altro.txt", "ALTRA\n    alfa\n"));
        assertEquals(java.util.Collections.singleton("uno"), prodotti(bean, "RADICE"));
        assertEquals(new java.util.TreeSet<>(java.util.Arrays.asList("alfa", "due")), prodotti(bean, "ALTRA"));
    }

    @Test
    void unIncludeIndentatoNonEUnaDirettiva() throws Exception {
        List<String> richiesti = new ArrayList<>();
        GrammarBean bean = grammatica("RADICE\n    #include <altro.txt>\n", file("altro.txt", "ALTRA\n    alfa\n"), richiesti);
        assertEquals("#include <altro.txt>", bean.produce("RADICE").get(0));
        assertTrue(richiesti.isEmpty());
    }

    @Test
    void dueGrammaticheCheIncludonoLoStessoFileHannoProduzioniIndipendenti() throws Exception {
        Map<String, String> comune = file("nomi.txt", "NOME$\n    Aldo\n    Bruno\n");
        GrammarBean a = grammatica("A\n    [NOME]\n#include <nomi.txt>\n", comune);
        GrammarBean b = grammatica("B\n    [NOME]\n#include <nomi.txt>\n", comune);

        // Una produzione one-shot si consuma in A...
        java.util.Set<String> usciti = new java.util.TreeSet<>();
        usciti.add(a.produce("NOME").get(0));
        usciti.add(a.produce("NOME").get(0));
        assertEquals(new java.util.TreeSet<>(java.util.Arrays.asList("Aldo", "Bruno")), usciti);
        assertFalse(a.canProduce("NOME"), "in A i nomi sono finiti");
        // ...ma in B è intatta: gli stessi due nomi, per intero
        assertTrue(b.canProduce("NOME"));
        java.util.Set<String> inB = new java.util.TreeSet<>();
        inB.add(b.produce("NOME").get(0));
        inB.add(b.produce("NOME").get(0));
        assertEquals(usciti, inB);
        assertFalse(b.canProduce("NOME"));

        // Il reset di una non tocca l'altra
        a.reset();
        assertTrue(a.canProduce("NOME"));
        assertFalse(b.canProduce("NOME"));
    }

    @Test
    void leProduzioniFissateNonPassanoDaUnaGrammaticaAllAltra() throws Exception {
        Map<String, String> comune = file("nomi.txt", "NOME\n    Aldo\n    Bruno\n");
        GrammarBean a = grammatica("A\n    [*NOME] e [*NOME]\n#include <nomi.txt>\n", comune);
        GrammarBean b = grammatica("B\n    [*NOME] e [*NOME]\n#include <nomi.txt>\n", comune);
        a.addFixedProduction("NOME", "Zeno");
        assertEquals("Zeno e Zeno", a.produce("A").get(0));
        java.util.Set<String> inB = prodotti(b, "B");
        assertFalse(inB.stream().anyMatch(t -> t.contains("Zeno")), inB.toString());
        assertTrue(inB.contains("Aldo e Aldo") && inB.contains("Bruno e Bruno"), inB.toString());
    }
}
