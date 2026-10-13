package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.GrammarBean.InvalidGrammarException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I commenti con {@code //} e le sezioni statiche, {@code #include_static <file>} (vedi GrammarBean.md, §4.11 e §4.12).
 */
class GrammarBeanStaticTest {

    @BeforeEach
    @AfterEach
    void azzeraLeProduzioniStatiche() {
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
        private final List<String> aperti = new ArrayList<>();
        private boolean conIdentita = true;

        Risolutore con(String percorso, String testo) {
            file.put(percorso, testo);
            return this;
        }

        @Override
        public synchronized InputStream open(String percorso) {
            aperti.add(percorso);
            String testo = file.get(percorso);
            return testo == null ? null : flusso(testo);
        }

        @Override
        public String identity(String percorso) {
            return conIdentita ? identita + ":" + percorso : null;
        }
    }

    private static GrammarBean grammatica(String principale, Risolutore risolutore) throws Exception {
        return new GrammarBean(flusso(principale), null, risolutore);
    }

    private static Set<String> prodotti(GrammarBean bean, String produzione, int volte) {
        Set<String> prodotti = new TreeSet<>();
        for (int i = 0; i < volte; i++) {
            prodotti.add(bean.produce(produzione).get(0));
        }
        return prodotti;
    }

    private static String errore(String principale, Risolutore risolutore) {
        return assertThrows(InvalidGrammarException.class, () -> grammatica(principale, risolutore)).getMessage();
    }

    // ---------------------------------------------------------------------------------------------- commenti

    @Test
    void ICommentiIniziaranoConDoppiaBarra() throws Exception {
        GrammarBean bean = new GrammarBean("// un commento\nRADICE\n// un altro\n    ciao\n// e un ultimo\n");
        assertEquals("ciao", bean.produce("RADICE").get(0));
    }

    @Test
    void SoloLaRigaIntieraACOlonna0EUnCommento() throws Exception {
        // Indentata, la doppia barra è il testo di un'alternativa; a metà riga è testo
        GrammarBean bean = new GrammarBean("RADICE\n    // non un commento\n    a // b\n");
        assertEquals(new TreeSet<>(Arrays.asList("// non un commento", "a // b")), prodotti(bean, "RADICE", 200));
    }

    @Test
    void IlCancelettoNonEUnCommento() throws Exception {
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class,
                () -> new GrammarBean("# vecchio commento\nRADICE\n    ciao\n"));
        assertTrue(e.getMessage().contains("Unknown directive") && e.getMessage().contains("//"), e.getMessage());
        // I commenti // e le direttive restano a posto
        Risolutore risolutore = new Risolutore().con("a.txt", "A\n    x\n");
        GrammarBean bean = grammatica("// commento\nRADICE\n    [A]\n#include <a.txt>\n", risolutore);
        assertEquals("x", bean.produce("RADICE").get(0));
    }

    @Test
    void IlCancelettoDentroUnaContinuazioneETesto() throws Exception {
        GrammarBean bean = new GrammarBean("RADICE\n    uno \\\n#due\n");
        assertEquals("uno #due", bean.produce("RADICE").get(0));
    }

    @Test
    void IlFileDiPostProduzioneAccettaICommentiConDoppiaBarra() throws Exception {
        GrammarBean bean = new GrammarBean(flusso("RADICE\n    vado a il [X]\nX\n    forno\n"), flusso("// regola\n a il : al \n"));
        assertEquals("vado al forno", bean.produce("RADICE").get(0));
    }

    // ----------------------------------------------------------------------------------------------- statiche

    private static final String NOMI = "// nomi\nNOME$\n    Aldo\n    Bruno\n    Carlo\n";

    @Test
    void LeProduzioniDiUnaSezioneStaticaSiUsanoNellaGrammatica() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        GrammarBean bean = grammatica("RADICE\n    ciao [NOME]\n\n#include_static <nomi.txt>\n", risolutore);
        assertTrue(bean.canProduce("NOME"));
        assertTrue(bean.produce("RADICE").get(0).startsWith("ciao "));
    }

    @Test
    void UnaProduzioneOneShotStaticaSiConsumaPerTutteLeGrammatiche() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        GrammarBean a = grammatica("A\n    [NOME]\n#include_static <nomi.txt>\n", risolutore);
        GrammarBean b = grammatica("B\n    [NOME]\n#include_static <nomi.txt>\n", risolutore);

        Set<String> usciti = new HashSet<>();
        usciti.add(a.produce("A").get(0));
        usciti.add(b.produce("B").get(0));
        usciti.add(a.produce("A").get(0));
        // Tre nomi, tre pescate fra due grammatiche: uno diverso dall'altro
        assertEquals(new HashSet<>(Arrays.asList("Aldo", "Bruno", "Carlo")), usciti);
        assertFalse(a.canProduce("NOME"));
        assertFalse(b.canProduce("NOME"), "è finito anche per l'altra grammatica");
        assertEquals(1, risolutore.aperti.stream().filter("nomi.txt"::equals).count(), "il file è letto una volta sola");
    }

    @Test
    void NonCEUnaRicaricaAutomaticaEChiDipendeDaUnaProduzioneEsauritaSiPotaAncheNelleAltre() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        GrammarBean a = grammatica("A\n    [NOME]\n#include_static <nomi.txt>\n", risolutore);
        GrammarBean b = grammatica("B\n    il signor [NOME]\n    nessuno\n#include_static <nomi.txt>\n", risolutore);
        for (int i = 0; i < 3; i++) {
            a.produce("A");
        }
        // In B l'alternativa che usava i nomi è stata tolta: resta solo l'altra
        assertEquals(Collections.singleton("nessuno"), prodotti(b, "B", 100));
        // E in A la produzione che dipendeva dai nomi non esiste più
        assertFalse(a.canProduce("A"));
        assertThrows(IllegalArgumentException.class, () -> a.produce("A"));
    }

    @Test
    void IlResetDelleStaticheRestituisceITuttiEILoroDipendenti() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        GrammarBean a = grammatica("A\n    [NOME]\n#include_static <nomi.txt>\n", risolutore);
        GrammarBean b = grammatica("B\n    il signor [NOME]\n    nessuno\n#include_static <nomi.txt>\n", risolutore);
        for (int i = 0; i < 3; i++) {
            a.produce("A");
        }
        assertEquals(Collections.singleton("nessuno"), prodotti(b, "B", 100));

        GrammarBean.resetStaticProductions();
        assertTrue(a.canProduce("NOME"));
        assertTrue(a.canProduce("A"));
        assertTrue(prodotti(b, "B", 200).stream().anyMatch(t -> t.startsWith("il signor ")), "B ha di nuovo l'alternativa con i nomi");
    }

    @Test
    void IlResetNormaleDiUnaGrammaticaNonRestituisceLeStatiche() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        GrammarBean a = grammatica("A\n    [NOME]\n#include_static <nomi.txt>\n", risolutore);
        for (int i = 0; i < 3; i++) {
            a.produce("A");
        }
        a.reset();
        assertFalse(a.canProduce("NOME"), "reset() è della grammatica: le sezioni statiche si restituiscono con resetStaticProductions()");
        assertFalse(a.canProduce("A"));
    }

    @Test
    void ConLeStaticheEsauriteUnaGrammaticaNuovaNonVedeIPotati() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        GrammarBean a = grammatica("A\n    [NOME]\n#include_static <nomi.txt>\n", risolutore);
        for (int i = 0; i < 3; i++) {
            a.produce("A");
        }
        GrammarBean c = grammatica("C\n    [NOME]\n    altro\n#include_static <nomi.txt>\n", risolutore);
        assertEquals(Collections.singleton("altro"), prodotti(c, "C", 100));
    }

    @Test
    void UnaStaticaChePassaDaUnaStaticaSiEsaurisceACascata() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt",
                "NOME_COMPLETO\n    [NOME] [COGNOME]\n\nNOME$\n    Aldo\n\nCOGNOME\n    Rossi\n");
        GrammarBean a = grammatica("A\n    [NOME_COMPLETO]\n    nessuno\n#include_static <nomi.txt>\n", risolutore);
        // Il primo nome esce (Aldo Rossi) o "nessuno"; poi il nome è finito e con lui NOME_COMPLETO
        Set<String> usciti = prodotti(a, "A", 300);
        assertTrue(usciti.contains("nessuno"));
        assertFalse(a.canProduce("NOME_COMPLETO"));
        assertFalse(a.canProduce("NOME"));
        assertTrue(a.canProduce("COGNOME"), "una produzione che non dipende dal nome resta");
    }

    @Test
    void UnaProduzioneStaticaNonOneShotNonSiConsuma() throws Exception {
        Risolutore risolutore = new Risolutore().con("c.txt", "COGNOME\n    Rossi\n    Verdi\n");
        GrammarBean a = grammatica("A\n    [COGNOME]\n#include_static <c.txt>\n", risolutore);
        GrammarBean b = grammatica("B\n    [COGNOME]\n#include_static <c.txt>\n", risolutore);
        assertEquals(new TreeSet<>(Arrays.asList("Rossi", "Verdi")), prodotti(a, "A", 300));
        assertEquals(new TreeSet<>(Arrays.asList("Rossi", "Verdi")), prodotti(b, "B", 300));
    }

    @Test
    void IlGruppoInlineDiUnaStaticaENonSiConfondeConQuelloDellaPrincipale() throws Exception {
        Risolutore risolutore = new Risolutore().con("s.txt", "S\n    {uno|due}\n");
        GrammarBean a = grammatica("A\n    {rosso|verde} [S]\n#include_static <s.txt>\n", risolutore);
        assertEquals(new TreeSet<>(Arrays.asList("rosso uno", "rosso due", "verde uno", "verde due")), prodotti(a, "A", 400));
    }

    @Test
    void UnaProduzioneChePortaLoStessoNomeDiUnaStaticaEUnErrore() {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        String messaggio = errore("NOME\n    mio\n#include_static <nomi.txt>\n", risolutore);
        assertTrue(messaggio.contains("NOME") && messaggio.contains("static section"), messaggio);
    }

    @Test
    void DueStaticheCheDefinisconoLoStessoNomeSiEscludono() {
        Risolutore risolutore = new Risolutore().con("a.txt", "X\n    uno\n").con("b.txt", "X\n    due\n");
        String messaggio = errore("RADICE\n    [X]\n#include_static <a.txt>\n#include_static <b.txt>\n", risolutore);
        assertTrue(messaggio.contains("two static sections"), messaggio);
    }

    @Test
    void UnaStaticaDeveEssereAutonomaENonPuoIncludereAltreStatiche() {
        Risolutore risolutore = new Risolutore()
                .con("a.txt", "A\n    [B]\n")   // B è della principale
                .con("c.txt", "C\n    x\n#include_static <a.txt>\n");
        String messaggio = errore("RADICE\n    [A]\nB\n    y\n#include_static <a.txt>\n", risolutore);
        assertTrue(messaggio.contains("not defined"), messaggio);
        String annidata = errore("RADICE\n    [C]\n#include_static <c.txt>\n", risolutore);
        assertTrue(annidata.contains("cannot be used inside a static section"), annidata);
    }

    @Test
    void UnaStaticaPuoUsareGliIncludeNormaliNellaPropriaCartella() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi/nomi.txt", "NOME$\n    Aldo\n\n#include <cognomi.txt>\n")
                .con("nomi/cognomi.txt", "COGNOME\n    Rossi\n");
        GrammarBean a = grammatica("A\n    [NOME] [COGNOME]\n#include_static <nomi/nomi.txt>\n", risolutore);
        assertEquals("Aldo Rossi", a.produce("A").get(0));
    }

    @Test
    void ErroriDellaStatica_DiconoDoveELaDirettiva() {
        Risolutore risolutore = new Risolutore().con("rotta.txt", "RADICE\n    [manca]\n");
        String messaggio = errore("PRINCIPALE\n    x\n#include_static <rotta.txt>\n", risolutore);
        assertTrue(messaggio.contains("source:3") && messaggio.contains("rotta.txt"), messaggio);
        String mancante = errore("PRINCIPALE\n    x\n#include_static <non_c_e.txt>\n", risolutore);
        assertTrue(mancante.contains("not found"), mancante);
    }

    @Test
    void ServeUnRisolutoreCheIdentificaIFile() {
        Risolutore senzaIdentita = new Risolutore().con("nomi.txt", NOMI);
        senzaIdentita.conIdentita = false;
        String messaggio = errore("RADICE\n    [NOME]\n#include_static <nomi.txt>\n", senzaIdentita);
        assertTrue(messaggio.contains("identifies"), messaggio);
        InvalidGrammarException e = assertThrows(InvalidGrammarException.class,
                () -> new GrammarBean("RADICE\n    [NOME]\n#include_static <nomi.txt>\n"));
        assertTrue(e.getMessage().contains("IncludeResolver"), e.getMessage());
    }

    @Test
    void UnaStaticaIncludaDueVolteNellaStessaGrammaticaVaBene() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        GrammarBean a = grammatica("A\n    [NOME]\n#include_static <nomi.txt>\n#include_static <nomi.txt>\n", risolutore);
        assertTrue(a.canProduce("NOME"));
    }

    @Test
    void SiPuoPartireDaUnaProduzioneStatica() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", NOMI);
        GrammarBean a = grammatica("A\n    x\n#include_static <nomi.txt>\n", risolutore);
        a.setRootNode("NOME");
        assertTrue(new TreeSet<>(Arrays.asList("Aldo", "Bruno", "Carlo")).contains(a.produce().get(0)));
        assertEquals("NOME", a.getRootNode());
    }

    @Test
    void UnaProduzioneFissataPassaPerOgniGrammaticaPerConto() throws Exception {
        Risolutore risolutore = new Risolutore().con("nomi.txt", "NOME\n    Aldo\n    Bruno\n");
        GrammarBean a = grammatica("A\n    [*NOME] e [*NOME]\n#include_static <nomi.txt>\n", risolutore);
        a.addFixedProduction("NOME", "Zeno");
        assertEquals("Zeno e Zeno", a.produce("A").get(0));
        GrammarBean b = grammatica("B\n    [*NOME] e [*NOME]\n#include_static <nomi.txt>\n", risolutore);
        assertFalse(prodotti(b, "B", 200).stream().anyMatch(t -> t.contains("Zeno")));
    }

    @Test
    void PiuThreadCheConsumanoLoStessoPoolNonRipetonoNiente() throws Exception {
        StringBuilder nomi = new StringBuilder("NOME$\n");
        int totale = 300;
        for (int i = 0; i < totale; i++) {
            nomi.append("    nome").append(i).append('\n');
        }
        Risolutore risolutore = new Risolutore().con("nomi.txt", nomi.toString());
        int numeroThread = 6;
        List<String> usciti = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errori = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch via = new CountDownLatch(1);
        List<Thread> thread = new ArrayList<>();
        for (int t = 0; t < numeroThread; t++) {
            GrammarBean bean = grammatica("R" + t + "\n    [NOME]\n#include_static <nomi.txt>\n", risolutore);
            String radice = "R" + t;
            Thread thread1 = new Thread(() -> {
                try {
                    via.await();
                    while (true) {
                        try {
                            usciti.add(bean.produce(radice).get(0));
                        } catch (IllegalArgumentException finito) {
                            return;
                        }
                    }
                } catch (Throwable e) {
                    errori.add(e);
                }
            });
            thread.add(thread1);
            thread1.start();
        }
        via.countDown();
        for (Thread t : thread) {
            t.join(20_000);
        }
        assertTrue(errori.isEmpty(), errori.toString());
        assertEquals(totale, usciti.size(), "ogni nome esce una volta sola");
        assertEquals(totale, new HashSet<>(usciti).size(), "nessun nome si ripete fra i thread");
    }

    @Test
    void LeGrammaticheDaRisorsaCondividonoLaStaticaPerURL() throws Exception {
        GrammarBean a = GrammarBean.fromResource(GrammarBeanStaticTest.class,
                "/com/threeamigos/foresta/motore/include_prova/statica_a.txt", null);
        GrammarBean b = GrammarBean.fromResource(GrammarBeanStaticTest.class,
                "/com/threeamigos/foresta/motore/include_prova/statica_b.txt", null);
        Set<String> usciti = new HashSet<>();
        usciti.add(a.produce("RADICE").get(0));
        usciti.add(b.produce("RADICE").get(0).replace("il signor ", ""));
        usciti.add(a.produce("RADICE").get(0));
        assertEquals(new HashSet<>(Arrays.asList("Aldo", "Bruno", "Carlo")), usciti);
        assertFalse(b.canProduce("RADICE"));
        GrammarBean.resetStaticProductions();
        assertTrue(b.canProduce("RADICE"));
    }
}
