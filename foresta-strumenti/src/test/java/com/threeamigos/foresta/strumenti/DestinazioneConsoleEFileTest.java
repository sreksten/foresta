package com.threeamigos.foresta.strumenti;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La destinazione del desktop: scrive sempre sulla console e, se si indica una cartella, anche su un file per sessione.
 */
class DestinazioneConsoleEFileTest {

    private final PrintStream outOriginale = System.out;
    private final PrintStream errOriginale = System.err;
    private final ByteArrayOutputStream console = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errori = new ByteArrayOutputStream();

    @AfterEach
    void ripristinaLaConsole() {
        System.setOut(outOriginale);
        System.setErr(errOriginale);
        System.clearProperty(Logger.PROPRIETA_CARTELLA_DI_LOG);
    }

    private void catturaLaConsole() throws IOException {
        System.setOut(new PrintStream(console, true, "UTF-8"));
        System.setErr(new PrintStream(errori, true, "UTF-8"));
    }

    private static String contenutoDelFile(Path cartella) throws IOException {
        File[] file = cartella.toFile().listFiles((dir, nome) -> nome.endsWith(".log"));
        assertNotNull(file);
        assertEquals(1, file.length, "un file per sessione");
        return new String(Files.readAllBytes(file[0].toPath()), StandardCharsets.UTF_8);
    }

    @Test
    void senzaCartellaScriveSoloSullaConsole() throws IOException {
        catturaLaConsole();
        for (String cartella : new String[]{null, ""}) {
            DestinazioneConsoleEFile destinazione = new DestinazioneConsoleEFile(cartella);

            destinazione.scrivi("ciao");
            destinazione.scrivi(new IllegalStateException("rotto"));
        }

        assertTrue(console.toString("UTF-8").contains("ciao"));
        assertTrue(errori.toString("UTF-8").contains("IllegalStateException: rotto"));
    }

    @Test
    void conUnaCartellaScriveAncheSuFile(@TempDir Path cartella) throws IOException {
        catturaLaConsole();
        DestinazioneConsoleEFile destinazione = new DestinazioneConsoleEFile(cartella.resolve("log").toString());

        destinazione.scrivi("messaggio d'esempio: più giù");
        destinazione.scrivi(new IllegalStateException("eccezione d'esempio"));

        String file = contenutoDelFile(cartella.resolve("log"));
        assertTrue(file.contains("messaggio d'esempio: più giù"), file);
        assertTrue(file.contains("IllegalStateException: eccezione d'esempio"), file);
        assertTrue(console.toString("UTF-8").contains("messaggio d'esempio: più giù"));
    }

    @Test
    void laCartellaSiLeggeDallaProprietaDiSistema(@TempDir Path cartella) throws IOException {
        catturaLaConsole();
        System.setProperty(Logger.PROPRIETA_CARTELLA_DI_LOG, cartella.toString());

        new DestinazioneConsoleEFile().scrivi("dalla proprietà");

        assertTrue(contenutoDelFile(cartella).contains("dalla proprietà"));
    }

    @Test
    void unaCartellaCheNonSiPuoCreareLasciaSoloLaConsole(@TempDir Path cartella) throws IOException {
        catturaLaConsole();
        Path unFile = Files.createFile(cartella.resolve("sono-un-file"));
        DestinazioneConsoleEFile destinazione = new DestinazioneConsoleEFile(unFile.resolve("sotto").toString());

        destinazione.scrivi("solo console");

        assertTrue(console.toString("UTF-8").contains("solo console"));
        assertFalse(errori.toString("UTF-8").isEmpty(), "l'errore di apertura si segnala");
    }
}
