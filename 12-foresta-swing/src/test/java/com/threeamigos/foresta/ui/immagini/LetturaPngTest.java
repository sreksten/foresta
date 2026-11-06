package com.threeamigos.foresta.ui.immagini;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Il lettore di PNG del modulo comune deve dare gli stessi pixel di ImageIO per tutte le immagini del gioco.
 */
class LetturaPngTest {

    private static final Path IMMAGINI = Paths.get("..", "12-foresta-ui-comune", "src", "main", "resources", "com", "threeamigos", "foresta", "img");

    @Test
    void tuttiIPngDelGiocoHannoGliStessiPixelDiImageIO() throws IOException {
        List<Path> file;
        try (Stream<Path> tutti = Files.walk(IMMAGINI)) {
            file = tutti.filter(p -> p.toString().endsWith(".png")).collect(Collectors.toList());
        }
        assertFalse(file.isEmpty());
        for (Path png : file) {
            BufferedImage atteso = ImageIO.read(png.toFile());
            ImmagineDiPixel letto;
            try (InputStream in = Files.newInputStream(png)) {
                letto = LetturaPng.leggi(in);
            }
            assertEquals(atteso.getWidth(), letto.getLarghezza(), png.toString());
            assertEquals(atteso.getHeight(), letto.getAltezza(), png.toString());
            for (int y = 0; y < atteso.getHeight(); y++) {
                for (int x = 0; x < atteso.getWidth(); x++) {
                    int a = atteso.getRGB(x, y);
                    int b = letto.getArgb(x, y);
                    // Un pixel trasparente non ha un colore che conti
                    if ((a >>> 24) == 0 && (b >>> 24) == 0) {
                        continue;
                    }
                    assertEquals(Integer.toHexString(a), Integer.toHexString(b), png + " in " + x + "," + y);
                }
            }
        }
    }
}
