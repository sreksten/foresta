package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.ui.immagini.SfondoARiempimento;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lo sfondo delle icone in ricarica: la cornice si riempie dal basso, dal rosso scuro al verde scuro passando per il giallo scuro, sul grigio scuro.
 */
class SfondoARiempimentoTest {

    private static final int BORDO = 0xFFAA5500;

    /**
     * Una cornice 12x12: bordo di un pixel e interno trasparente, con gli angoli fuori dal bordo trasparenti.
     */
    private static BufferedImage cornice() {
        BufferedImage cornice = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        for (int i = 1; i < 11; i++) {
            cornice.setRGB(i, 1, BORDO);
            cornice.setRGB(i, 10, BORDO);
            cornice.setRGB(1, i, BORDO);
            cornice.setRGB(10, i, BORDO);
        }
        return cornice;
    }

    private static int righeRiempite(BufferedImage sfondo, int colore) {
        int righe = 0;
        for (int y = 2; y < 10; y++) {
            if ((sfondo.getRGB(5, y) & 0xFFFFFF) == colore) {
                righe++;
            }
        }
        return righe;
    }

    @Test
    void ilBordoResta() {
        BufferedImage sfondo = SfondoARiempimento.crea(cornice(), 1, 3);
        assertEquals(BORDO, sfondo.getRGB(5, 1));
        assertEquals(BORDO, sfondo.getRGB(1, 5));
    }

    @Test
    void gliAngoliFuoriDalBordoRestanoTrasparenti() {
        BufferedImage sfondo = SfondoARiempimento.crea(cornice(), 2, 3);
        assertEquals(0, sfondo.getRGB(0, 0) >>> 24);
        assertEquals(0, sfondo.getRGB(11, 11) >>> 24);
    }

    @Test
    void conTreTurniDiRicaricaSiRiempieUnQuartoMetaETreQuarti() {
        // Le righe interne sono 8: un quarto sono 2, metà 4, tre quarti 6; il resto è nero
        for (int passati = 1; passati <= 3; passati++) {
            BufferedImage sfondo = SfondoARiempimento.crea(cornice(), passati, 3);
            int nere = righeRiempite(sfondo, 0x000000);
            assertEquals(8 - 2 * passati, nere, "nero dopo " + passati + " turni");
        }
    }

    @Test
    void ilRiempimentoSaleDalBasso() {
        BufferedImage sfondo = SfondoARiempimento.crea(cornice(), 2, 3);
        assertEquals(0x000000, sfondo.getRGB(5, 2) & 0xFFFFFF, "in alto è ancora nero");
        assertNotEquals(0x000000, sfondo.getRGB(5, 9) & 0xFFFFFF, "in basso è riempito");
    }

    @Test
    void aMetaDelCamminoIlColoreEGialloENonMarrone() {
        // Con una ricarica di un turno il solo passo è a metà: giallo scuro, rosso e verde alla pari
        int meta = SfondoARiempimento.crea(cornice(), 1, 1).getRGB(5, 9) & 0xFFFFFF;
        assertEquals(0x8C8C00, meta);
    }

    @Test
    void ilGradienteHaIlGialloFraRossoEVerde() {
        // Con 3 turni: un quarto (fra rosso e giallo, il verde sale), metà (giallo), tre quarti (fra giallo e verde, il rosso cala)
        int uno = SfondoARiempimento.crea(cornice(), 1, 3).getRGB(5, 9) & 0xFFFFFF;
        int due = SfondoARiempimento.crea(cornice(), 2, 3).getRGB(5, 9) & 0xFFFFFF;
        int tre = SfondoARiempimento.crea(cornice(), 3, 3).getRGB(5, 9) & 0xFFFFFF;
        assertEquals(0x8C, (uno >> 16) & 0xFF, "il rosso resta pieno fino al giallo");
        assertTrue(((uno >> 8) & 0xFF) > 0 && ((uno >> 8) & 0xFF) < 0x8C, "il verde sale");
        assertEquals(0x8C, (due >> 16) & 0xFF);
        assertTrue(((tre >> 16) & 0xFF) < 0x8C && ((tre >> 16) & 0xFF) > 0, "il rosso cala");
        assertTrue(((tre >> 8) & 0xFF) < 0x8C && ((tre >> 8) & 0xFF) > 0x52, "il verde scende al suo valore");
    }

    @Test
    void ilColoreVaDalRossoScuroAlVerdeScuro() {
        int primo = SfondoARiempimento.crea(cornice(), 1, 5).getRGB(5, 9) & 0xFFFFFF;
        int ultimo = SfondoARiempimento.crea(cornice(), 5, 5).getRGB(5, 9) & 0xFFFFFF;
        assertTrue(((primo >> 16) & 0xFF) > ((ultimo >> 16) & 0xFF), "il rosso cala");
        assertTrue(((primo >> 8) & 0xFF) < ((ultimo >> 8) & 0xFF), "il verde cresce");
        assertEquals(0, primo & 0xFF);
        assertEquals(0, ultimo & 0xFF);
    }
}
