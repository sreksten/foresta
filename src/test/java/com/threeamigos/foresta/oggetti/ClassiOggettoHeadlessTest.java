package com.threeamigos.foresta.oggetti;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il motore non deve caricare immagini: gli oggetti si devono poter usare senza schermo (le immagini stanno in
 * ui.ClassiOggettoImmagine). Nella JVM dei test AWT può essere già stato inizializzato con il display da altri
 * test, quindi la prova gira in una JVM a parte avviata con java.awt.headless=true.
 */
class ClassiOggettoHeadlessTest {

    @Test
    void gliOggettiSiUsanoSenzaSchermo() throws IOException, InterruptedException {
        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        Process processo = new ProcessBuilder(java, "-Djava.awt.headless=true",
                "-cp", System.getProperty("java.class.path"), Sonda.class.getName())
                .redirectErrorStream(true)
                .start();
        assertTrue(processo.waitFor(60, TimeUnit.SECONDS), "la JVM headless non ha finito in tempo");
        String uscita = new String(readAll(processo), StandardCharsets.UTF_8);
        assertEquals(0, processo.exitValue(), uscita);
        assertTrue(uscita.contains(Sonda.FATTO), uscita);
    }

    private static byte[] readAll(Process processo) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int letti;
        while ((letti = processo.getInputStream().read(buffer)) != -1) {
            out.write(buffer, 0, letti);
        }
        return out.toByteArray();
    }

    /**
     * Gira nella JVM headless: tocca ogni ClassiOggetto e ne crea un'istanza.
     */
    static final class Sonda {

        static final String FATTO = "SONDA HEADLESS COMPLETATA";

        public static void main(String[] args) {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                throw new IllegalStateException("la sonda deve girare headless");
            }
            for (ClassiOggetto classe : ClassiOggetto.values()) {
                if (classe != ClassiOggetto.ARTEFATTO) {
                    Oggetto oggetto = classe.getIstanza();
                    if (oggetto == null || oggetto.getClasse() != classe) {
                        throw new IllegalStateException("istanza sbagliata per " + classe);
                    }
                }
            }
            System.out.println(FATTO);
        }
    }
}
