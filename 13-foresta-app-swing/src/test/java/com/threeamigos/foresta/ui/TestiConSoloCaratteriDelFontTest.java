package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.SorgentiDelProgetto;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.DoomdarkFontSmall;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I testi del gioco (le grammatiche {@code .txt} e i file {@code .properties}) vanno a video con i font bitmap, che hanno le lettere, le cifre, la
 * punteggiatura comune, alcune accentate e le virgolette alte aperte (“): niente « » ” … e simili, che il font non sa disegnare (il testo si
 * disegna lo stesso, ma senza quel carattere, e nel registro compare un errore). Per le virgolette del dialogo si apre con “ e si chiude con ".
 * Le righe di commento ({@code //} e {@code #}) non vanno a video e non si controllano.
 */
class TestiConSoloCaratteriDelFontTest {

    @Test
    void iTestiNonHannoCaratteriCheIlFontNonSaDisegnare() throws IOException {
        List<String> fuori = new ArrayList<>();
        for (Path file : fileDeiTesti()) {
            List<String> righe = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < righe.size(); i++) {
                String riga = righe.get(i);
                String pulita = riga.trim();
                if (pulita.startsWith("//") || pulita.startsWith("#")) {
                    continue;
                }
                for (char c : riga.toCharArray()) {
                    if (c > 127 && !(DoomdarkFontMedium.getInstance().supportaIlCarattere(c) && DoomdarkFontSmall.getInstance().supportaIlCarattere(c))) {
                        fuori.add(file.getFileName() + ":" + (i + 1) + " «" + c + "» [" + (int) c + "]");
                    }
                }
            }
        }
        assertTrue(fuori.isEmpty(), "Caratteri che il font non ha: " + fuori);
    }

    private static List<Path> fileDeiTesti() throws IOException {
        List<Path> file = new ArrayList<>();
        try (DirectoryStream<Path> moduli = Files.newDirectoryStream(SorgentiDelProgetto.radice(), "[0-9][0-9]-foresta-*")) {
            for (Path modulo : moduli) {
                Path risorse = modulo.resolve("src/main/resources");
                if (!Files.isDirectory(risorse)) {
                    continue;
                }
                try (Stream<Path> percorsi = Files.walk(risorse)) {
                    file.addAll(percorsi.filter(p -> p.toString().endsWith(".txt") || p.toString().endsWith(".properties")).collect(Collectors.toList()));
                }
            }
        }
        return file;
    }
}
