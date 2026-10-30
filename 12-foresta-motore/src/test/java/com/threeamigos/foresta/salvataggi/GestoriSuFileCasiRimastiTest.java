package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.modellodati.BestiarioMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.TrofeiMD;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * I casi dei gestori su file che {@link GestoriSuFileAlLimiteTest} lascia fuori: la scrittura che fallisce per ogni gestore, la
 * sostituzione di un file quando il sistema non la lascia fare, i file che non si possono leggere.
 */
class GestoriSuFileCasiRimastiTest {

    @TempDir
    Path cartella;

    private final List<Object> eventi = new ArrayList<>();

    private static final class Gestore extends GestoreSuFile {
        Gestore() {
            super(new File("."));
        }

        static void scrivi(File f, Scrittura s) throws IOException {
            scriviSostituendo(f, s);
        }

        static void sostituisciFile(File a, File b) throws IOException {
            sostituisci(a, b);
        }
    }

    @BeforeEach
    void prepara() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        BusEventi.iscriviti(InternoException.class, eventi::add);
        ModelloDati.setIstanza(new ModelloDati());
        ModelloDati.installa();
    }

    @AfterEach
    void ripulisce() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
    }

    private File cartellaOccupata() throws IOException {
        File occupato = cartella.resolve("occupato").toFile();
        assertTrue(occupato.createNewFile());
        return occupato;
    }

    @Test
    void seLaCartellaEUnFileNiente_siSalvaPerNessunGestore() throws IOException {
        File occupato = cartellaOccupata();

        assertFalse(new GestoreTrofeiSuFile(occupato).salva(new TrofeiMD()));
        assertFalse(new GestoreBestiarioSuFile(occupato).salva(new BestiarioMD()));
        assertFalse(new GestorePunteggiSuFile(occupato).salva());

        assertTrue(eventi.size() >= 3);
    }

    @Test
    void laClassificaSenzaFileNonSiLeggeEnonSiMetteDaParte() throws IOException {
        GestorePunteggiSuFile classifica = new GestorePunteggiSuFile(cartella.toFile());
        // il file della classifica non c'è (la si scrive solo a un nuovo punteggio): leggerla fallisce, e non c'è niente da mettere da parte
        Files.delete(cartella.resolve("forestaHS"));
        assertFalse(classifica.carica());
        assertFalse(Files.exists(cartella.resolve("forestaHS.rovinato")));
        assertTrue(classifica.isPunteggioInClassifica(1, "una partita"));
    }

    @Test
    void laClassificaDellaHomeSiScriveNellaCartellaForesta() throws IOException {
        String home = System.getProperty("user.home");
        System.setProperty("user.home", cartella.toString());
        try {
            GestorePunteggiSuFile classifica = new GestorePunteggiSuFile();
            assertTrue(classifica.salva());
            assertTrue(Files.exists(cartella.resolve(".foresta").resolve("forestaHS")));
        } finally {
            System.setProperty("user.home", home);
        }
    }

    @Test
    void unSalvataggioNonLeggibileNonSiLegge() throws IOException {
        GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile(cartella.toFile());
        File file = cartella.resolve("NUMERO_1.TXT").toFile();
        Files.write(file.toPath(), "NUMERO_1|qualcosa".getBytes(StandardCharsets.UTF_8));
        file.setReadable(false);
        try {
            assumeFalse(file.canRead(), "con permessi da amministratore il file si legge comunque");
            assertTrue(gestore.getSalvataggiDisponibili().isEmpty());
            assertFalse(gestore.leggi(Comando.NUMERO_1));
        } finally {
            file.setReadable(true);
        }
    }

    @Test
    void sostituireConDestinazioneECopiaDaParteInsostituibili() throws IOException {
        // sia la destinazione sia il posto per la copia da parte sono cartelle piene: non si può fare niente
        Path destinazione = cartella.resolve("dest");
        Files.createDirectory(destinazione);
        Files.write(destinazione.resolve("x"), "x".getBytes(StandardCharsets.UTF_8));
        Path copia = cartella.resolve("dest.bak");
        Files.createDirectory(copia);
        Files.write(copia.resolve("y"), "y".getBytes(StandardCharsets.UTF_8));
        File temporaneo = cartella.resolve("nuovo.tmp").toFile();
        Files.write(temporaneo.toPath(), "n".getBytes(StandardCharsets.UTF_8));

        assertThrows(IOException.class, () -> Gestore.sostituisciFile(temporaneo, destinazione.toFile()));
        // scrivendo davvero si passa da un temporaneo che poi si butta
        assertThrows(IOException.class, () -> Gestore.scrivi(destinazione.toFile(), w -> w.println("dato")));
        assertFalse(Files.exists(cartella.resolve("dest.tmp")));
        assertTrue(Files.isDirectory(destinazione));
    }

    @Test
    void unaVecchiaCopiaDaParteSiButtaSePuo() throws IOException {
        // La destinazione è una cartella piena (il rename non riesce) e di una sostituzione precedente è rimasto un file .bak
        Path destinazione = cartella.resolve("dest");
        Files.createDirectory(destinazione);
        Files.write(destinazione.resolve("x"), "x".getBytes(StandardCharsets.UTF_8));
        Files.write(cartella.resolve("dest.bak"), "vecchio".getBytes(StandardCharsets.UTF_8));
        File temporaneo = cartella.resolve("nuovo.tmp").toFile();
        Files.write(temporaneo.toPath(), "n".getBytes(StandardCharsets.UTF_8));

        Gestore.sostituisciFile(temporaneo, destinazione.toFile());

        assertEquals("n", Files.readAllLines(destinazione).get(0));
    }

    @Test
    void seIlTemporaneoNonCEIlVecchioFileTornaAlSuoPosto() throws IOException {
        Path destinazione = cartella.resolve("dati");
        Files.write(destinazione, "vecchio".getBytes(StandardCharsets.UTF_8));

        assertThrows(IOException.class, () -> Gestore.sostituisciFile(cartella.resolve("manca.tmp").toFile(), destinazione.toFile()));

        assertEquals("vecchio", Files.readAllLines(destinazione).get(0));
        assertFalse(Files.exists(cartella.resolve("dati.bak")));
    }

    @Test
    void inUnaCartellaNonScrivibileIlVecchioFileNonSiMetteDaParte() throws IOException {
        Path sotto = Files.createDirectory(cartella.resolve("sola-lettura"));
        Path destinazione = sotto.resolve("dati");
        Files.write(destinazione, "vecchio".getBytes(StandardCharsets.UTF_8));
        File temporaneo = sotto.resolve("dati.tmp").toFile();
        Files.write(temporaneo.toPath(), "nuovo".getBytes(StandardCharsets.UTF_8));
        sotto.toFile().setWritable(false);
        try {
            assumeFalse(sotto.toFile().canWrite(), "con permessi da amministratore la cartella si scrive comunque");
            assertThrows(IOException.class, () -> Gestore.sostituisciFile(temporaneo, destinazione.toFile()));
        } finally {
            sotto.toFile().setWritable(true);
        }
        assertEquals("vecchio", Files.readAllLines(destinazione).get(0));
    }
}
