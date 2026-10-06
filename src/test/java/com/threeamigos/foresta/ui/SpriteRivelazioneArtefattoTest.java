package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                md.addModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_FISSO, 1, "");
            }
        }
        return md;
    }
}
