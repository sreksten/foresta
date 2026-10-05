package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Monte Carlo Matrix Test per il bilanciamento dei combattimenti. Vedi piano_montecarlo_matrix.md.
 */
public class TestMonteCarloMatrix {

    private static final int LIVELLO = 1;

    private static final List<TipoPersonaggio> CLASSI_GIOCABILI = Arrays.asList(
            TipoPersonaggio.BARDO, TipoPersonaggio.CANTASTORIE, TipoPersonaggio.ELFA,
            TipoPersonaggio.ELFO, TipoPersonaggio.GUERRIERA, TipoPersonaggio.GUERRIERO,
            TipoPersonaggio.LADRA, TipoPersonaggio.LADRO, TipoPersonaggio.MAGA,
            TipoPersonaggio.MAGO, TipoPersonaggio.OMBRAFIAMMA
    );

    private static final List<TipoPersonaggio> CLASSI_MOSTRO = Arrays.asList(
            TipoPersonaggio.ARPIA, TipoPersonaggio.CENTAURO, TipoPersonaggio.CHIMERA,
            TipoPersonaggio.CHIMERA_DRAGO, TipoPersonaggio.DRAGO, TipoPersonaggio.EREMITA,
            TipoPersonaggio.FANTASMA, TipoPersonaggio.FOLLETTO, TipoPersonaggio.GARGOYLE,
            TipoPersonaggio.GIGANTE, TipoPersonaggio.GOBLIN, TipoPersonaggio.HOBGOBLIN,
            TipoPersonaggio.IDRA, TipoPersonaggio.LICH, TipoPersonaggio.MINOTAURO,
            TipoPersonaggio.MINOTAURO_GIGANTE, TipoPersonaggio.OMBRA_NERA, TipoPersonaggio.SCHELETRO,
            TipoPersonaggio.SPETTRO, TipoPersonaggio.SPIRITO, TipoPersonaggio.STREGA,
            TipoPersonaggio.TITANO, TipoPersonaggio.TROLL, TipoPersonaggio.VIVERNA
    );

    /**
     * Gli equipaggiamenti su cui gira la matrice completa: togline qualcuno per farla più in fretta
     * (ogni equipaggiamento in più moltiplica il tempo)
     */
    private static final List<Equipaggiamento> EQUIPAGGIAMENTI_MATRICE = Equipaggiamento.TUTTI;

    // Per il confronto fra equipaggiamenti: i PG che li usano in modo diverso (due armi sì o no, magia),
    // e mostri fisici, forti e con il morso velenoso
    private static final int LIVELLO_CONFRONTO = 5;
    private static final List<TipoPersonaggio> CLASSI_CONFRONTO = Arrays.asList(
            TipoPersonaggio.LADRO, TipoPersonaggio.ELFA, TipoPersonaggio.GUERRIERO, TipoPersonaggio.MAGO);
    private static final List<TipoPersonaggio> MOSTRI_CONFRONTO = Arrays.asList(
            TipoPersonaggio.GOBLIN, TipoPersonaggio.TROLL, TipoPersonaggio.MINOTAURO, TipoPersonaggio.VIVERNA);

    // Per il confronto fra classi: ogni classe con le sue dotazioni tipiche e scorte di pergamene diverse
    private static final List<TipoPersonaggio> CLASSI_TIPICHE = Arrays.asList(TipoPersonaggio.GUERRIERO,
            TipoPersonaggio.LADRO, TipoPersonaggio.ELFO, TipoPersonaggio.BARDO, TipoPersonaggio.MAGO);
    private static final List<ScortaDiPergamene> SCORTE_CONFRONTO = Arrays.asList(ScortaDiPergamene.NESSUNA,
            ScortaDiPergamene.di(ClasseIncantesimo.FUOCO, 2), ScortaDiPergamene.di(ClasseIncantesimo.FUOCO, 5));
    private static final int[] LIVELLI_CONFRONTO_CLASSI = {1, 5, 10};
    // Gli scenari del confronto fra classi: {quantità di mostri, livelli dei mostri sopra il PG}. Uno contro uno dal
    // livello 5 quasi tutti vincono: per distinguere le classi servono gruppi di mostri e mostri più forti.
    private static final int[][] SCENARI_CONFRONTO_CLASSI = {{1, 0}, {2, 0}, {3, 0}, {1, 1}};

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
            // scarta l'output, serve solo a mutare Logger durante la simulazione
        }
    });

    @Test
    void testSingoloScontroLadroVsGoblin() {
        RisultatoMatrice risultato = simulaConLoggerMuto(TipoPersonaggio.LADRO, TipoPersonaggio.GOBLIN, 1, LIVELLO, 10_000);

        System.out.printf("LADRO vs 1 GOBLIN -> win=%.2f%% lose=%.2f%% stallo=%.2f%% turni medi=%.2f stanchezza finale media=%.2f%n",
                risultato.winRatePg, risultato.loseRatePg, risultato.stalloRate, risultato.mediaTurni, risultato.mediaStanchezzaFinale);
        System.out.printf("Percentuale di colpire -> LADRO=%.2f%% GOBLIN=%.2f%%%n",
                risultato.tassoColpirePg, risultato.tassoColpireMostro);
        System.out.printf("Danno medio per colpo -> LADRO=%.2f GOBLIN=%.2f%n",
                risultato.dannoMedioPg, risultato.dannoMedioMostro);

        // Non verifichiamo win/lose rate: questo test genera dati per l'analisi del bilanciamento,
        // non verifica che il bilanciamento sia corretto. L'unico invariante e' l'assenza di stalli (9.1).
        assertEquals(0, risultato.stalloRate, "Non dovrebbero esistere stalli, sono un difetto di bilanciamento");
    }

    @Test
    void testSingoloScontroGuerrieroVsGoblin() {
        RisultatoMatrice risultato = simulaConLoggerMuto(TipoPersonaggio.GUERRIERO, TipoPersonaggio.GOBLIN, 1, LIVELLO, 10_000);

        System.out.printf("GUERRIERO vs 1 GOBLIN -> win=%.2f%% lose=%.2f%% stallo=%.2f%% turni medi=%.2f stanchezza finale media=%.2f%n",
                risultato.winRatePg, risultato.loseRatePg, risultato.stalloRate, risultato.mediaTurni, risultato.mediaStanchezzaFinale);
        System.out.printf("Percentuale di colpire -> GUERRIERO=%.2f%% GOBLIN=%.2f%%%n",
                risultato.tassoColpirePg, risultato.tassoColpireMostro);
        System.out.printf("Danno medio per colpo -> GUERRIERO=%.2f GOBLIN=%.2f%n",
                risultato.dannoMedioPg, risultato.dannoMedioMostro);

        // Non verifichiamo win/lose rate: questo test genera dati per l'analisi del bilanciamento,
        // non verifica che il bilanciamento sia corretto. L'unico invariante e' l'assenza di stalli (9.1).
        assertEquals(0, risultato.stalloRate, "Non dovrebbero esistere stalli, sono un difetto di bilanciamento");
    }

    @Test
    void testSingoloScontroLadroConDueSpadeVsGoblin() {
        RisultatoMatrice conDueSpade = simulaConLoggerMuto(TipoPersonaggio.LADRO, Equipaggiamento.DUE_SPADE,
                TipoPersonaggio.GOBLIN, 1, LIVELLO, 5_000);
        RisultatoMatrice conUnaSpada = simulaConLoggerMuto(TipoPersonaggio.LADRO, Equipaggiamento.SPADA,
                TipoPersonaggio.GOBLIN, 1, LIVELLO, 5_000);

        System.out.printf("LADRO con DUE_SPADE vs 1 GOBLIN -> win=%.2f%% lose=%.2f%% stallo=%.2f%% turni medi=%.2f (con una spada: win=%.2f%% turni medi=%.2f)%n",
                conDueSpade.winRatePg, conDueSpade.loseRatePg, conDueSpade.stalloRate, conDueSpade.mediaTurni,
                conUnaSpada.winRatePg, conUnaSpada.mediaTurni);

        assertEquals(0, conDueSpade.stalloRate, "Non dovrebbero esistere stalli, sono un difetto di bilanciamento");
        // Due fasi di attacco a turno: lo scontro si chiude prima che con una spada sola
        assertTrue(conDueSpade.mediaTurni < conUnaSpada.mediaTurni,
                "con due spade " + conDueSpade.mediaTurni + " turni, con una " + conUnaSpada.mediaTurni);
    }

    @Test
    void unaClasseCheNonPuoPortareLEquipaggiamentoVieneRifiutata() {
        // Il Guerriero non sa combattere con due armi
        assertTrue(Equipaggiamento.DUE_SPADE.motivoRifiuto(TipoPersonaggio.GUERRIERO, LIVELLO).isPresent());
        assertThrows(IllegalArgumentException.class, () -> simulaConLoggerMuto(TipoPersonaggio.GUERRIERO,
                Equipaggiamento.DUE_SPADE, TipoPersonaggio.GOBLIN, 1, LIVELLO, 1));
    }

    @Test
    void conLePergameneIlMagoVincePiuSpesso() {
        // Contro tre troll: contro uno solo il Mago (con il budget degli eroi) vince praticamente sempre anche senza
        // pergamene, e il confronto non misurerebbe piu' nulla
        RisultatoMatrice senza = simulaConLoggerMuto(TipoPersonaggio.MAGO, Equipaggiamento.BASTONE_LIBRO_E_VESTE,
                ScortaDiPergamene.NESSUNA, TipoPersonaggio.TROLL, 3, 5, 2_000);
        RisultatoMatrice con = simulaConLoggerMuto(TipoPersonaggio.MAGO, Equipaggiamento.BASTONE_LIBRO_E_VESTE,
                ScortaDiPergamene.di(ClasseIncantesimo.FUOCO, 5), TipoPersonaggio.TROLL, 3, 5, 2_000);
        System.out.printf("MAGO vs 3 TROLL, livello 5 -> senza pergamene win=%.2f%%, con 5 di fuoco win=%.2f%%%n",
                senza.winRatePg, con.winRatePg);
        assertTrue(con.winRatePg > senza.winRatePg, "con " + con.winRatePg + ", senza " + senza.winRatePg);
    }

    @Test
    void ogniClassePuoPortareLeSueDotazioniTipiche() {
        for (TipoPersonaggio classe : CLASSI_TIPICHE) {
            for (Equipaggiamento equipaggiamento : Equipaggiamento.tipiciPer(classe)) {
                // Il Guerriero di livello 1 ha già la FORZA per l'armatura
                assertEquals(Optional.empty(), equipaggiamento.motivoRifiuto(classe, 1), classe + " con " + equipaggiamento);
            }
        }
    }

    /**
     * Confronta le classi, ognuna con le sue dotazioni tipiche e con scorte di pergamene diverse, a più livelli e
     * negli scenari di SCENARI_CONFRONTO_CLASSI (quantità e livello dei mostri), e scrive
     * REPORT_BILANCIAMENTO_CLASSI.csv. Una quantità oltre il massimo per locazione di un mostro si salta.
     */
    @Disabled("Da eseguire manualmente per confrontare le classi")
    @Test
    void testConfrontoClassi() throws IOException {
        int iterazioni = 1_000;
        try (FileWriter writer = new FileWriter("REPORT_BILANCIAMENTO_CLASSI.csv")) {
            writer.write("PG,EQUIPAGGIAMENTO,PERGAMENE,MOSTRO,QUANTITA_MOSTRI,LIVELLO,LIVELLO_MOSTRI,WIN_RATE,LOSE_RATE,"
                    + "STALLO_RATE,TURNI_MEDI,TASSO_COLPIRE_PG,TASSO_COLPIRE_MOSTRO,DANNO_MEDIO_PG,DANNO_MEDIO_MOSTRO\n");
            for (int livello : LIVELLI_CONFRONTO_CLASSI) {
                for (TipoPersonaggio classePg : CLASSI_TIPICHE) {
                    for (Equipaggiamento equipaggiamento : Equipaggiamento.tipiciPer(classePg)) {
                        for (ScortaDiPergamene pergamene : SCORTE_CONFRONTO) {
                            for (TipoPersonaggio classeMostro : MOSTRI_CONFRONTO) {
                                for (int[] scenario : SCENARI_CONFRONTO_CLASSI) {
                                    int quantita = scenario[0];
                                    int livelloMostri = livello + scenario[1];
                                    if (quantita > quantitaMassima(classeMostro)) {
                                        continue;
                                    }
                                    RisultatoMatrice r = simulaConLoggerMuto(classePg, equipaggiamento, pergamene,
                                            classeMostro, quantita, livello, livelloMostri, iterazioni);
                                    System.out.printf("L%-2d %-10s %-22s %-8s vs %dx%-10s L%-2d win=%6.2f%% turni=%5.2f%n",
                                            livello, classePg, equipaggiamento, pergamene, quantita, classeMostro,
                                            livelloMostri, r.winRatePg, r.mediaTurni);
                                    writer.write(String.format("%s,%s,%s,%s,%d,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                                            classePg, equipaggiamento, pergamene, classeMostro, quantita, livello,
                                            livelloMostri, r.winRatePg, r.loseRatePg, r.stalloRate, r.mediaTurni,
                                            r.tassoColpirePg, r.tassoColpireMostro, r.dannoMedioPg, r.dannoMedioMostro));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Confronta tutti gli equipaggiamenti per alcune classi contro alcuni mostri, 1 contro 1, e scrive
     * REPORT_BILANCIAMENTO_EQUIPAGGIAMENTI.csv. Gli equipaggiamenti che una classe non può portare si saltano,
     * con un avviso sulla console.
     */
    @Disabled("Da eseguire manualmente per confrontare armi, scudi e armature")
    @Test
    void testConfrontoEquipaggiamenti() throws IOException {
        int iterazioni = 3_000;
        try (FileWriter writer = new FileWriter("REPORT_BILANCIAMENTO_EQUIPAGGIAMENTI.csv")) {
            writer.write("PG,EQUIPAGGIAMENTO,MOSTRO,LIVELLO,WIN_RATE,LOSE_RATE,STALLO_RATE,TURNI_MEDI,"
                    + "TASSO_COLPIRE_PG,TASSO_COLPIRE_MOSTRO,DANNO_MEDIO_PG,DANNO_MEDIO_MOSTRO\n");
            for (TipoPersonaggio classePg : CLASSI_CONFRONTO) {
                for (Equipaggiamento equipaggiamento : Equipaggiamento.TUTTI) {
                    Optional<String> rifiuto = equipaggiamento.motivoRifiuto(classePg, LIVELLO_CONFRONTO);
                    if (rifiuto.isPresent()) {
                        System.out.printf("%s non può portare %s (%s): saltato%n", classePg, equipaggiamento, rifiuto.get());
                        continue;
                    }
                    for (TipoPersonaggio classeMostro : MOSTRI_CONFRONTO) {
                        RisultatoMatrice r = simulaConLoggerMuto(classePg, equipaggiamento, classeMostro, 1,
                                LIVELLO_CONFRONTO, iterazioni);
                        System.out.printf("%-10s %-24s vs %-10s win=%6.2f%% lose=%6.2f%% turni=%5.2f colpire=%5.1f%%/%5.1f%% danno=%6.2f/%6.2f%n",
                                classePg, equipaggiamento, classeMostro, r.winRatePg, r.loseRatePg, r.mediaTurni,
                                r.tassoColpirePg, r.tassoColpireMostro, r.dannoMedioPg, r.dannoMedioMostro);
                        writer.write(String.format("%s,%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                                classePg, equipaggiamento, classeMostro, LIVELLO_CONFRONTO,
                                r.winRatePg, r.loseRatePg, r.stalloRate, r.mediaTurni,
                                r.tassoColpirePg, r.tassoColpireMostro, r.dannoMedioPg, r.dannoMedioMostro));
                    }
                }
            }
        }
    }

    @Disabled("Da eseguire manualmente per analizzare l'effetto della quantità di mostri")
    @Test
    void testScalataQuantitaMostriLadroVsGoblin() {
        int[] quantita = {1, 2, 3, 4, 5};
        for (int q : quantita) {
            RisultatoMatrice risultato = simulaConLoggerMuto(TipoPersonaggio.LADRO, TipoPersonaggio.GOBLIN, q, LIVELLO, 5_000);
            System.out.printf("LADRO vs %d GOBLIN -> win=%.2f%% lose=%.2f%% stallo=%.2f%% turni medi=%.2f stanchezza finale media=%.2f%n",
                    q, risultato.winRatePg, risultato.loseRatePg, risultato.stalloRate, risultato.mediaTurni, risultato.mediaStanchezzaFinale);
        }
    }

    @Disabled("Da eseguire manualmente per analizzare l'effetto della quantità di mostri")
    @Test
    void testScalataQuantitaMostriGuerrieroVsGoblin() {
        int[] quantita = {1, 2, 3, 4, 5};
        for (int q : quantita) {
            RisultatoMatrice risultato = simulaConLoggerMuto(TipoPersonaggio.GUERRIERO, TipoPersonaggio.GOBLIN, q, 5, 5_000);
            System.out.printf("GUERRIERO vs %d GOBLIN -> win=%.2f%% lose=%.2f%% stallo=%.2f%% turni medi=%.2f stanchezza finale media=%.2f%n",
                    q, risultato.winRatePg, risultato.loseRatePg, risultato.stalloRate, risultato.mediaTurni, risultato.mediaStanchezzaFinale);
        }
    }

    @Disabled("Test pesante: genera la matrice completa PG x MOSTRO x QUANTITA. Da eseguire manualmente da IntelliJ.")
    @Test
    void testMatriceCompletaBilanciamento() throws IOException {
        int iterazioni = 1_000;
        int[] quantitaMostri = {1, 2, 3, 4, 5};

        try (FileWriter writer = new FileWriter("REPORT_BILANCIAMENTO_MATRICE.csv")) {
            writer.write("PG,EQUIPAGGIAMENTO,MOSTRO,QUANTITA_MOSTRI,WIN_RATE,LOSE_RATE,STALLO_RATE,TURNI_MEDI,STANCHEZZA_MEDIA_FINALE\n");

            for (TipoPersonaggio classePg : CLASSI_GIOCABILI) {
                for (Equipaggiamento equipaggiamento : EQUIPAGGIAMENTI_MATRICE) {
                    // Chi non può portarlo (due armi, troppo peso) non compare nel report
                    if (equipaggiamento.motivoRifiuto(classePg, LIVELLO).isPresent()) {
                        continue;
                    }
                    for (TipoPersonaggio classeMostro : CLASSI_MOSTRO) {
                        for (int quantita : quantitaMostri) {
                            RisultatoMatrice risultato = simulaConLoggerMuto(classePg, equipaggiamento, classeMostro,
                                    quantita, LIVELLO, iterazioni);
                            writer.write(String.format("%s,%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                                    classePg, equipaggiamento, classeMostro, quantita,
                                    risultato.winRatePg, risultato.loseRatePg, risultato.stalloRate,
                                    risultato.mediaTurni, risultato.mediaStanchezzaFinale));
                        }
                    }
                }
            }
        }
    }

    /**
     * Quanti mostri di quella classe possono stare in una locazione. Il valore lo imposta il costruttore del
     * mostro, quindi prima ne serve un'istanza (vedi piano_montecarlo_matrix.md, §6.3).
     */
    private static int quantitaMassima(TipoPersonaggio classeMostro) {
        PrintStream originale = System.out;
        System.setOut(NULL_STREAM);
        try {
            FabbricaPersonaggi.crea(classeMostro, 1);
        } finally {
            System.setOut(originale);
        }
        return FabbricaPersonaggi.quantitaMassima(classeMostro);
    }

    private static RisultatoMatrice simulaConLoggerMuto(TipoPersonaggio classePg, TipoPersonaggio classeMostro,
                                                          int quantitaMostri, int livello, int iterazioni) {
        return simulaConLoggerMuto(classePg, Equipaggiamento.NESSUNO, classeMostro, quantitaMostri, livello, iterazioni);
    }

    private static RisultatoMatrice simulaConLoggerMuto(TipoPersonaggio classePg, Equipaggiamento equipaggiamento,
                                                          TipoPersonaggio classeMostro, int quantitaMostri,
                                                          int livello, int iterazioni) {
        return simulaConLoggerMuto(classePg, equipaggiamento, ScortaDiPergamene.NESSUNA, classeMostro, quantitaMostri,
                livello, iterazioni);
    }

    private static RisultatoMatrice simulaConLoggerMuto(TipoPersonaggio classePg, Equipaggiamento equipaggiamento,
                                                          ScortaDiPergamene pergamene, TipoPersonaggio classeMostro,
                                                          int quantitaMostri, int livello, int iterazioni) {
        return simulaConLoggerMuto(classePg, equipaggiamento, pergamene, classeMostro, quantitaMostri, livello, livello,
                iterazioni);
    }

    private static RisultatoMatrice simulaConLoggerMuto(TipoPersonaggio classePg, Equipaggiamento equipaggiamento,
                                                          ScortaDiPergamene pergamene, TipoPersonaggio classeMostro,
                                                          int quantitaMostri, int livello, int livelloMostri,
                                                          int iterazioni) {
        PrintStream originale = System.out;
        System.setOut(NULL_STREAM);
        try {
            return CombatSimulatorMatrix.simulaScontroGruppo(classePg, equipaggiamento, pergamene, classeMostro,
                    quantitaMostri, livello, livelloMostri, iterazioni);
        } finally {
            System.setOut(originale);
        }
    }
}
