package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
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

    private static final List<ClassePersonaggio> CLASSI_GIOCABILI = Arrays.asList(
            ClassePersonaggio.BARDO, ClassePersonaggio.CANTASTORIE, ClassePersonaggio.ELFA,
            ClassePersonaggio.ELFO, ClassePersonaggio.GUERRIERA, ClassePersonaggio.GUERRIERO,
            ClassePersonaggio.LADRA, ClassePersonaggio.LADRO, ClassePersonaggio.MAGA,
            ClassePersonaggio.MAGO, ClassePersonaggio.OMBRAFIAMMA
    );

    private static final List<ClassePersonaggio> CLASSI_MOSTRO = Arrays.asList(
            ClassePersonaggio.ARPIA, ClassePersonaggio.CENTAURO, ClassePersonaggio.CHIMERA,
            ClassePersonaggio.CHIMERA_DRAGO, ClassePersonaggio.DRAGO, ClassePersonaggio.EREMITA,
            ClassePersonaggio.FANTASMA, ClassePersonaggio.FOLLETTO, ClassePersonaggio.GARGOYLE,
            ClassePersonaggio.GIGANTE, ClassePersonaggio.GOBLIN, ClassePersonaggio.HOBGOBLIN,
            ClassePersonaggio.IDRA, ClassePersonaggio.LICH, ClassePersonaggio.MINOTAURO,
            ClassePersonaggio.MINOTAURO_GIGANTE, ClassePersonaggio.OMBRA_NERA, ClassePersonaggio.SCHELETRO,
            ClassePersonaggio.SPETTRO, ClassePersonaggio.SPIRITO, ClassePersonaggio.STREGA,
            ClassePersonaggio.TITANO, ClassePersonaggio.TROLL, ClassePersonaggio.VIVERNA
    );

    /**
     * Gli equipaggiamenti su cui gira la matrice completa: togline qualcuno per farla più in fretta
     * (ogni equipaggiamento in più moltiplica il tempo)
     */
    private static final List<Equipaggiamento> EQUIPAGGIAMENTI_MATRICE = Equipaggiamento.TUTTI;

    // Per il confronto fra equipaggiamenti: i PG che li usano in modo diverso (due armi sì o no, magia),
    // e mostri fisici, forti e con il morso velenoso
    private static final int LIVELLO_CONFRONTO = 5;
    private static final List<ClassePersonaggio> CLASSI_CONFRONTO = Arrays.asList(
            ClassePersonaggio.LADRO, ClassePersonaggio.ELFA, ClassePersonaggio.GUERRIERO, ClassePersonaggio.MAGO);
    private static final List<ClassePersonaggio> MOSTRI_CONFRONTO = Arrays.asList(
            ClassePersonaggio.GOBLIN, ClassePersonaggio.TROLL, ClassePersonaggio.MINOTAURO, ClassePersonaggio.VIVERNA);

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
            // scarta l'output, serve solo a mutare Logger durante la simulazione
        }
    });

    @Test
    void testSingoloScontroLadroVsGoblin() {
        RisultatoMatrice risultato = simulaConLoggerMuto(ClassePersonaggio.LADRO, ClassePersonaggio.GOBLIN, 1, LIVELLO, 10_000);

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
        RisultatoMatrice risultato = simulaConLoggerMuto(ClassePersonaggio.GUERRIERO, ClassePersonaggio.GOBLIN, 1, LIVELLO, 10_000);

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
        RisultatoMatrice conDueSpade = simulaConLoggerMuto(ClassePersonaggio.LADRO, Equipaggiamento.DUE_SPADE,
                ClassePersonaggio.GOBLIN, 1, LIVELLO, 5_000);
        RisultatoMatrice conUnaSpada = simulaConLoggerMuto(ClassePersonaggio.LADRO, Equipaggiamento.SPADA,
                ClassePersonaggio.GOBLIN, 1, LIVELLO, 5_000);

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
        assertTrue(Equipaggiamento.DUE_SPADE.motivoRifiuto(ClassePersonaggio.GUERRIERO, LIVELLO).isPresent());
        assertThrows(IllegalArgumentException.class, () -> simulaConLoggerMuto(ClassePersonaggio.GUERRIERO,
                Equipaggiamento.DUE_SPADE, ClassePersonaggio.GOBLIN, 1, LIVELLO, 1));
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
            for (ClassePersonaggio classePg : CLASSI_CONFRONTO) {
                for (Equipaggiamento equipaggiamento : Equipaggiamento.TUTTI) {
                    Optional<String> rifiuto = equipaggiamento.motivoRifiuto(classePg, LIVELLO_CONFRONTO);
                    if (rifiuto.isPresent()) {
                        System.out.printf("%s non può portare %s (%s): saltato%n", classePg, equipaggiamento, rifiuto.get());
                        continue;
                    }
                    for (ClassePersonaggio classeMostro : MOSTRI_CONFRONTO) {
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
            RisultatoMatrice risultato = simulaConLoggerMuto(ClassePersonaggio.LADRO, ClassePersonaggio.GOBLIN, q, LIVELLO, 5_000);
            System.out.printf("LADRO vs %d GOBLIN -> win=%.2f%% lose=%.2f%% stallo=%.2f%% turni medi=%.2f stanchezza finale media=%.2f%n",
                    q, risultato.winRatePg, risultato.loseRatePg, risultato.stalloRate, risultato.mediaTurni, risultato.mediaStanchezzaFinale);
        }
    }

    @Disabled("Da eseguire manualmente per analizzare l'effetto della quantità di mostri")
    @Test
    void testScalataQuantitaMostriGuerrieroVsGoblin() {
        int[] quantita = {1, 2, 3, 4, 5};
        for (int q : quantita) {
            RisultatoMatrice risultato = simulaConLoggerMuto(ClassePersonaggio.GUERRIERO, ClassePersonaggio.GOBLIN, q, 5, 5_000);
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

            for (ClassePersonaggio classePg : CLASSI_GIOCABILI) {
                for (Equipaggiamento equipaggiamento : EQUIPAGGIAMENTI_MATRICE) {
                    // Chi non può portarlo (due armi, troppo peso) non compare nel report
                    if (equipaggiamento.motivoRifiuto(classePg, LIVELLO).isPresent()) {
                        continue;
                    }
                    for (ClassePersonaggio classeMostro : CLASSI_MOSTRO) {
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

    private static RisultatoMatrice simulaConLoggerMuto(ClassePersonaggio classePg, ClassePersonaggio classeMostro,
                                                          int quantitaMostri, int livello, int iterazioni) {
        return simulaConLoggerMuto(classePg, Equipaggiamento.NESSUNO, classeMostro, quantitaMostri, livello, iterazioni);
    }

    private static RisultatoMatrice simulaConLoggerMuto(ClassePersonaggio classePg, Equipaggiamento equipaggiamento,
                                                          ClassePersonaggio classeMostro, int quantitaMostri,
                                                          int livello, int iterazioni) {
        PrintStream originale = System.out;
        System.setOut(NULL_STREAM);
        try {
            return CombatSimulatorMatrix.simulaScontroGruppo(classePg, equipaggiamento, classeMostro, quantitaMostri,
                    livello, iterazioni);
        } finally {
            System.setOut(originale);
        }
    }
}
