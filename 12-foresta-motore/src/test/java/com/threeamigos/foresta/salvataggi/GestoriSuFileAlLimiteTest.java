package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaErroreCaricamento;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.interfacce.GestoreSalvataggi;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
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

/**
 * I file di salvataggio quando le cose vanno storte: file illeggibili o non scrivibili, cartelle al posto dei file, scritture che falliscono,
 * una home che non esiste.
 */
class GestoriSuFileAlLimiteTest {

    @TempDir
    Path cartella;

    private final List<Object> eventi = new ArrayList<>();

    @BeforeEach
    void prepara() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        BusEventi.iscriviti(InternoException.class, eventi::add);
        BusEventi.iscriviti(InternoMessaggio.class, eventi::add);
        BusEventi.iscriviti(NotificaErroreCaricamento.class, eventi::add);
        ModelloDati.setIstanza(new ModelloDati());
        ModelloDati.installa();
        com.threeamigos.foresta.mappa.Foresta.azzera(6, 5);
        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 5; y++) {
                com.threeamigos.foresta.mappa.Foresta.impostaLocazione(new com.threeamigos.foresta.modellodati.CoordinateMD(x, y), com.threeamigos.foresta.tipi.TipoLocazione.BOSCO);
            }
        }
        Guerriero capo = new Guerriero("Arsenio", 3);
        capo.getModelloDati().setPng(false);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(capo);
    }

    @AfterEach
    void ripulisce() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(null);
        ModelloDati.setIstanza(new ModelloDati());
    }

    private static final class Gestore extends GestoreSuFile {
        Gestore(File directory) {
            super(directory);
        }

        static void scrivi(File f, Scrittura s) throws IOException {
            scriviSostituendo(f, s);
        }

        static void sostituisciFile(File a, File b) throws IOException {
            sostituisci(a, b);
        }

        static void rovinato(File f) {
            mettiDaParteIlFileRovinato(f);
        }

        File directory() {
            return recuperaDirectory();
        }
    }

    private String spiega() {
        StringBuilder sb = new StringBuilder();
        for (Object e : eventi) {
            if (e instanceof InternoException || e instanceof NotificaErroreCaricamento) {
                Throwable t = e instanceof InternoException ? ((InternoException) e).getException() : ((NotificaErroreCaricamento) e).getException();
                sb.append(t).append(' ').append(java.util.Arrays.asList(t.getStackTrace()).subList(0, Math.min(6, t.getStackTrace().length)));
            }
        }
        return sb.toString();
    }

    @Test
    void ilSalvataggioSiScriveSiLeggeEsiElenca() {
        GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile(cartella.toFile());
        assertTrue(gestore.getSalvataggiDisponibili().isEmpty());
        assertTrue(gestore.salva(Comando.NUMERO_2), spiega());
        assertTrue(gestore.salva(Comando.NUMERO_2), "si può riscrivere sopra");
        assertEquals(1, gestore.getSalvataggiDisponibili().size());
        assertTrue(gestore.leggi(Comando.NUMERO_2), spiega());
        gestore.elimina(Comando.NUMERO_2);
        assertTrue(gestore.getSalvataggiDisponibili().isEmpty());
        gestore.elimina(Comando.NUMERO_2);
    }

    @Test
    void ilGestorePredefinitoUsaLaCartellaForestaDellaHome() {
        String home = System.getProperty("user.home");
        System.setProperty("user.home", cartella.toString());
        try {
            GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile();
            assertTrue(gestore.salva(Comando.NUMERO_1));
            assertTrue(Files.exists(cartella.resolve(".foresta").resolve("NUMERO_1.TXT")));
            System.setProperty("user.home", cartella.resolve("non-esiste").toString());
            assertThrows(IllegalArgumentException.class, () -> new GestoreSalvataggiSuFile().getSalvataggiDisponibili());
        } finally {
            System.setProperty("user.home", home);
        }
    }

    @Test
    void unFileRovinatoSiSegnalaENonSiLegge() throws IOException {
        GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile(cartella.toFile());
        Files.write(cartella.resolve("NUMERO_1.TXT"), "NUMERO_1|roba senza senso\nnon è un salvataggio".getBytes(StandardCharsets.UTF_8));
        assertTrue(gestore.getSalvataggiDisponibili().isEmpty());
        assertFalse(gestore.leggi(Comando.NUMERO_1));
        assertFalse(eventi.isEmpty());
        assertFalse(gestore.leggi(Comando.NUMERO_3), "un file che non c'è non si legge");
    }

    @Test
    void unFileNonScrivibileNonSiSovrascrive() throws IOException {
        GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile(cartella.toFile());
        assertTrue(gestore.salva(Comando.NUMERO_1));
        File file = cartella.resolve("NUMERO_1.TXT").toFile();
        assertTrue(file.setWritable(false));
        try {
            if (!file.canWrite()) {
                assertFalse(gestore.salva(Comando.NUMERO_1));
            }
        } finally {
            file.setWritable(true);
        }
    }

    @Test
    void conUnaCartellaAlPostoDelFileNonSiCancellaNeSiScrive() throws IOException {
        GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile(cartella.toFile());
        Path finto = cartella.resolve("NUMERO_4.TXT");
        Files.createDirectory(finto);
        Files.write(finto.resolve("dentro"), "x".getBytes(StandardCharsets.UTF_8));
        gestore.elimina(Comando.NUMERO_4);
        assertTrue(eventi.stream().anyMatch(e -> e instanceof InternoMessaggio));
        // la cartella non si legge come file
        gestore.getSalvataggiDisponibili();
        gestore.leggi(Comando.NUMERO_4);
    }

    @Test
    void seLaCartellaDeiSalvataggiEUnFileNonSiScrive() throws IOException {
        File occupato = cartella.resolve("occupato").toFile();
        assertTrue(occupato.createNewFile());
        GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile(occupato);
        assertFalse(gestore.salva(Comando.NUMERO_1));
        assertTrue(eventi.stream().anyMatch(e -> e instanceof InternoException));
    }

    @Test
    void laScritturaSostituisceIlFileSoloSeVaBene() throws IOException {
        File destinazione = cartella.resolve("dati").toFile();
        Gestore.scrivi(destinazione, w -> w.println("uno"));
        assertEquals("uno", Files.readAllLines(destinazione.toPath()).get(0));
        Gestore.scrivi(destinazione, w -> w.println("due"));
        assertEquals("due", Files.readAllLines(destinazione.toPath()).get(0));
        assertThrows(IOException.class, () -> Gestore.scrivi(destinazione, w -> {
            throw new IOException("niente da fare");
        }));
        assertThrows(IllegalStateException.class, () -> Gestore.scrivi(destinazione, w -> {
            throw new IllegalStateException("peggio");
        }));
        assertThrows(IOException.class, () -> Gestore.scrivi(destinazione, w -> {
            w.close();
            w.println("troppo tardi");
        }));
        assertEquals("due", Files.readAllLines(destinazione.toPath()).get(0), "il vecchio file resta");
        assertFalse(Files.exists(cartella.resolve("dati.tmp")));
    }

    @Test
    void sostituireUnFileChePuoNonEsistere() throws IOException {
        File manca = cartella.resolve("manca.tmp").toFile();
        assertThrows(IOException.class, () -> Gestore.sostituisciFile(manca, cartella.resolve("nuovo").toFile()));
        // la destinazione è una cartella piena: il vecchio si mette da parte
        Path piena = cartella.resolve("piena");
        Files.createDirectory(piena);
        Files.write(piena.resolve("x"), "x".getBytes(StandardCharsets.UTF_8));
        File nuovo = cartella.resolve("nuovo.tmp").toFile();
        Files.write(nuovo.toPath(), "n".getBytes(StandardCharsets.UTF_8));
        Gestore.sostituisciFile(nuovo, piena.toFile());
        assertTrue(Files.isRegularFile(piena));
        // il posto per il vecchio è occupato da una cartella piena
        Path daParte = cartella.resolve("fermo.bak");
        Files.createDirectory(daParte);
        Files.write(daParte.resolve("x"), "x".getBytes(StandardCharsets.UTF_8));
        Path fermo = cartella.resolve("fermo");
        Files.write(fermo, "v".getBytes(StandardCharsets.UTF_8));
        File altro = cartella.resolve("altro.tmp").toFile();
        Files.write(altro.toPath(), "a".getBytes(StandardCharsets.UTF_8));
        Gestore.sostituisciFile(altro, fermo.toFile());
    }

    @Test
    void ilFileRovinatoSiMetteDaParte() throws IOException {
        File file = cartella.resolve("trofei").toFile();
        Files.write(file.toPath(), "x".getBytes(StandardCharsets.UTF_8));
        Gestore.rovinato(file);
        assertTrue(Files.exists(cartella.resolve("trofei.rovinato")));
        Files.write(file.toPath(), "y".getBytes(StandardCharsets.UTF_8));
        Gestore.rovinato(file);
        assertEquals("y", Files.readAllLines(cartella.resolve("trofei.rovinato")).get(0));
        Gestore.rovinato(cartella.resolve("non-c-e").toFile());
        assertEquals(cartella.resolve("sotto").toFile(), new Gestore(cartella.resolve("sotto").toFile()).directory());
    }

    @Test
    void ilSalvataggioInMemoria() {
        SalvataggioInMemoria memoria = new SalvataggioInMemoria();
        assertFalse(memoria.isPresente());
        assertNull(memoria.getTestata());
        assertFalse(memoria.leggi());
        memoria.salva();
        assertTrue(memoria.isPresente());
        assertNotNull(memoria.getTestata());
        assertTrue(memoria.leggi(), spiega());
        memoria.elimina();
        assertFalse(memoria.isPresente());
        // con i dati che mancano non si salva e lo dice
        ModelloDati.setIstanza(null);
        eventi.clear();
        memoria.salva();
        assertFalse(memoria.isPresente());
        assertTrue(eventi.stream().anyMatch(e -> e instanceof InternoException));
    }
}
