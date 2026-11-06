package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.ui.testo.*;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un nome con caratteri che il font non ha ("Bob_", "a|b") non deve rompere il disegno del testo, e il Prompt non lo lascia passare.
 */
class CaratteriFuoriDalFontTest {

    @Test
    void ilTestoConCaratteriSconosciutiSiDisegnaSaltandoli() {
        for (DoomdarkFont font : new DoomdarkFont[]{DoomdarkFont.piccolo(), DoomdarkFont.medio()}) {
            for (String nome : new String[]{"Bob_", "a|b", "_", "x@y"}) {
                Image semplice = DoomdarkTextProducer.getImage(nome, font);
                Image aRighe = DoomdarkTextProducer.getImage(nome + " " + nome, font, 200);
                assertNotNull(semplice, nome);
                assertNotNull(aRighe, nome);
                assertEquals(FontTool.getWidth(font, nome.replaceAll("[_|@]", "")), FontTool.getWidth(font, nome), nome);
            }
        }
    }

    @Test
    void ilCarattereMancanteSiSegnalaUnaSolaVolta() {
        java.util.List<InternoErrore> errori = new java.util.ArrayList<>();
        BusEventi.iscriviti(InternoErrore.class, errori::add);
        DoomdarkFont font = DoomdarkFont.piccolo();
        DoomdarkTextProducer.getImage("Zo#", font);
        DoomdarkTextProducer.getImage("Zo#", font);
        DoomdarkTextProducer.getImage("Zo# Zo#", font, 200);
        assertEquals(1, errori.size(), String.valueOf(errori.stream().map(InternoErrore::getMessaggio).collect(java.util.stream.Collectors.toList())));
        assertTrue(errori.get(0).getMessaggio().contains("#"), errori.get(0).getMessaggio());
    }

    @Test
    void ilPromptTogliePerCaratteriCheIlFontNonHa() {
        assertEquals("Jurgen", CampoDiTesto.soloCaratteriDelFont("Ju|rgen"));
        assertEquals("Bob", CampoDiTesto.soloCaratteriDelFont("Bob_"));
        assertEquals("Chiara dell'Orto", CampoDiTesto.soloCaratteriDelFont("Chiara dell'Orto"));
        assertEquals("Perù", CampoDiTesto.soloCaratteriDelFont("Perù|"));
        assertNull(CampoDiTesto.soloCaratteriDelFont(null));
    }
}
