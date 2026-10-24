package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpriteRivelazioneArtefattoTest {

    @Test
    void loSplendoreCresceConEffettiRaritaELivelloSopraIlMondo() {
        // Spoglio, al livello del mondo
        assertEquals(0, SpriteRivelazioneArtefatto.splendore(Artefatto.di(artefatto(3, 0, TipoRaritaArtefatto.COMUNE)), 3));
        // Un effetto, o un livello sopra il mondo
        assertEquals(1, SpriteRivelazioneArtefatto.splendore(Artefatto.di(artefatto(3, 1, TipoRaritaArtefatto.COMUNE)), 3));
        assertEquals(1, SpriteRivelazioneArtefatto.splendore(Artefatto.di(artefatto(4, 0, TipoRaritaArtefatto.COMUNE)), 3));
        // Raro con un effetto: 3 punti
        assertEquals(2, SpriteRivelazioneArtefatto.splendore(Artefatto.di(artefatto(3, 1, TipoRaritaArtefatto.RARO)), 3));
        // Leggendario con un effetto: 5 punti
        assertEquals(3, SpriteRivelazioneArtefatto.splendore(Artefatto.di(artefatto(3, 1, TipoRaritaArtefatto.LEGGENDARIO)), 3));
        // Livelli sotto il mondo non tolgono niente
        assertEquals(1, SpriteRivelazioneArtefatto.splendore(Artefatto.di(artefatto(1, 2, TipoRaritaArtefatto.COMUNE)), 5));
    }

    private static SpriteRivelazioneArtefatto sprite(long[] ora) {
        return new SpriteRivelazioneArtefatto(Artefatto.di(artefatto(3, 1, TipoRaritaArtefatto.COMUNE)), 3,
                new Rectangle(0, 0, 640, 480), new Point(10, 10), () -> ora[0]);
    }

    private static void disegna(SpriteRivelazioneArtefatto sprite) {
        BufferedImage schermo = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = schermo.createGraphics();
        try {
            sprite.anima(g);
        } finally {
            g.dispose();
        }
    }

    @Test
    void ilTempoPassaInSecondiVeriENonADognDisegno() {
        long[] ora = {0};
        SpriteRivelazioneArtefatto sprite = sprite(ora);

        // Cento disegni ravvicinati (il mouse che si muove sopra la finestra), un millisecondo l'uno
        for (int i = 0; i < 100; i++) {
            disegna(sprite);
            ora[0] += 1_000_000L;
        }

        assertEquals(0.099f, sprite.getSecondi(), 0.001f, "cento disegni in un decimo di secondo non fanno passare più tempo");
        assertTrue(sprite.isAttivo());
    }

    @Test
    void laRivelazioneFinisceDopoLaSuaDurataQualunqueSiaIlRitmoDeiDisegni() {
        long[] ora = {0};
        SpriteRivelazioneArtefatto sprite = sprite(ora);

        // Un disegno ogni mezzo secondo: dopo cinque secondi e mezzo ha finito
        for (int i = 0; i < 12 && sprite.isAttivo(); i++) {
            disegna(sprite);
            ora[0] += 500_000_000L;
        }

        assertFalse(sprite.isAttivo());
    }

    private static ArtefattoMD artefatto(int livello, int effetti, TipoRaritaArtefatto rarita) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(TipoArtefatto.SPADA);
        md.setNome("la spada di prova");
        md.setLivello(livello);
        md.setRarita(rarita);
        for (int i = 0; i < effetti; i++) {
            if (i % 2 == 0) {
                md.addIncantamento("Fuoco minore", TipoDanno.FUOCO, 5, 0.05);
            } else {
                md.addModificatore(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1, "");
            }
        }
        return md;
    }
}
