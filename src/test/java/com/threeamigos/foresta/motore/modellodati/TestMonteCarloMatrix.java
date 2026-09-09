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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            writer.write("PG,MOSTRO,QUANTITA_MOSTRI,WIN_RATE,LOSE_RATE,STALLO_RATE,TURNI_MEDI,STANCHEZZA_MEDIA_FINALE\n");

            for (ClassePersonaggio classePg : CLASSI_GIOCABILI) {
                for (ClassePersonaggio classeMostro : CLASSI_MOSTRO) {
                    for (int quantita : quantitaMostri) {
                        RisultatoMatrice risultato = simulaConLoggerMuto(classePg, classeMostro, quantita, LIVELLO, iterazioni);
                        writer.write(String.format("%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                                classePg, classeMostro, quantita,
                                risultato.winRatePg, risultato.loseRatePg, risultato.stalloRate,
                                risultato.mediaTurni, risultato.mediaStanchezzaFinale));
                    }
                }
            }
        }
    }

    private static RisultatoMatrice simulaConLoggerMuto(ClassePersonaggio classePg, ClassePersonaggio classeMostro,
                                                          int quantitaMostri, int livello, int iterazioni) {
        PrintStream originale = System.out;
        System.setOut(NULL_STREAM);
        try {
            return CombatSimulatorMatrix.simulaScontroGruppo(classePg, classeMostro, quantitaMostri, livello, iterazioni);
        } finally {
            System.setOut(originale);
        }
    }
}
