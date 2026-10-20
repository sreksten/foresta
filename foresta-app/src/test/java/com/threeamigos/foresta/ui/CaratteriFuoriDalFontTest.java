package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.DoomdarkFontSmall;
import com.threeamigos.foresta.ui.testo.DoomdarkTextProducer;
import com.threeamigos.foresta.ui.testo.FontTool;
import org.junit.jupiter.api.Test;

import java.awt.Image;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un nome con caratteri che il font non ha ("Jürgen", "Peña", "Bob_") non deve rompere il disegno del testo, e il Prompt non lo lascia passare.
 */
class CaratteriFuoriDalFontTest {

    @Test
    void ilTestoConCaratteriSconosciutiSiDisegnaSaltandoli() {
        for (DoomdarkFont font : new DoomdarkFont[]{DoomdarkFontSmall.getInstance(), DoomdarkFontMedium.getInstance()}) {
            for (String nome : new String[]{"Jürgen", "Peña", "Bob_", "ü"}) {
                Image semplice = DoomdarkTextProducer.getImage(nome, font);
                Image aRighe = DoomdarkTextProducer.getImage(nome + " " + nome, font, 200);
                assertNotNull(semplice, nome);
                assertNotNull(aRighe, nome);
                assertEquals(FontTool.getWidth(font, nome.replaceAll("[üñ_]", "")), FontTool.getWidth(font, nome), nome);
            }
        }
    }

    @Test
    void ilCarattereMancanteSiSegnalaUnaSolaVolta() {
        java.util.List<InternoErrore> errori = new java.util.ArrayList<>();
        BusEventi.iscriviti(InternoErrore.class, errori::add);
        DoomdarkFont font = DoomdarkFontSmall.getInstance();
        DoomdarkTextProducer.getImage("Zoë", font);
        DoomdarkTextProducer.getImage("Zoë", font);
        DoomdarkTextProducer.getImage("Zoë Zoë", font, 200);
        assertEquals(1, errori.size(), String.valueOf(errori.stream().map(InternoErrore::getMessaggio).collect(java.util.stream.Collectors.toList())));
        assertTrue(errori.get(0).getMessaggio().contains("ë"), errori.get(0).getMessaggio());
    }

    @Test
    void ilPromptTogliePerCaratteriCheIlFontNonHa() {
        assertEquals("Jrgen", Prompt.soloCaratteriDelFont("Jürgen"));
        assertEquals("Bob", Prompt.soloCaratteriDelFont("Bob_"));
        assertEquals("Chiara dell'Orto", Prompt.soloCaratteriDelFont("Chiara dell'Orto"));
        assertEquals("Perù", Prompt.soloCaratteriDelFont("Perù|"));
        assertNull(Prompt.soloCaratteriDelFont(null));
    }
}
