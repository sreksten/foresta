package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.artefatti.leggendari.SetLeggendario;
import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli oggetti leggendari, i set, i libri tecnici e le battute di leggendari_en.txt: stesse chiavi di quelle italiane, nomi e leggende in inglese.
 */
class LeggendariInIngleseTest {

    private static List<String> chiaviItaliane;
    private static List<String> chiaviDeiSetItaliane;

    @BeforeEach
    void inglese() {
        Testi.impostaLingua("en");
    }

    @AfterEach
    void italiano() {
        Testi.impostaLingua(null);
    }

    private static void memorizzaLeItaliane() {
        Testi.impostaLingua(null);
        chiaviItaliane = new ArrayList<>(new TreeSet<>(CatalogoLeggendari.getTuttiILeggendari().keySet()));
        chiaviItaliane.addAll(new TreeSet<>(CatalogoLeggendari.getTuttiIBottini().keySet()));
        chiaviDeiSetItaliane = new ArrayList<>(new TreeSet<>(CatalogoLeggendari.getTuttiISet().keySet()));
        Testi.impostaLingua("en");
    }

    @Test
    void stesseChiaviDelleItaliane() {
        memorizzaLeItaliane();
        List<String> inglesi = new ArrayList<>(new TreeSet<>(CatalogoLeggendari.getTuttiILeggendari().keySet()));
        inglesi.addAll(new TreeSet<>(CatalogoLeggendari.getTuttiIBottini().keySet()));
        assertEquals(chiaviItaliane, inglesi);
        assertEquals(chiaviDeiSetItaliane, new ArrayList<>(new TreeSet<>(CatalogoLeggendari.getTuttiISet().keySet())));
    }

    @Test
    void nomiELeggendeSonoInInglese() {
        for (Map<String, OggettoLeggendario> gruppo : java.util.Arrays.asList(CatalogoLeggendari.getTuttiILeggendari(), CatalogoLeggendari.getTuttiIBottini())) {
            for (OggettoLeggendario o : gruppo.values()) {
                assertTrue(o.getNome().startsWith("the ") || o.getNome().startsWith("The "), o.getNome());
                assertTrue(o.getNomeBreve().startsWith("the "), o.getNomeBreve());
                assertTrue(o.getLeggenda().size() >= 2, o.getChiave());
                for (String battuta : o.getLeggenda()) {
                    assertFalse(battuta.matches("(?s).*\\b(il|della|dello|degli|che|non|nel)\\b.*"), o.getChiave() + ": " + battuta);
                }
            }
        }
        for (SetLeggendario s : CatalogoLeggendari.getTuttiISet().values()) {
            assertTrue(s.getNome().startsWith("the "), s.getNome());
        }
    }

    @Test
    void leBattuteEIlibriTecnici() {
        for (String nomeBreve : new String[] {"the Fiscal Shield", "the Sword of Death"}) {
            for (int i = 0; i < 40; i++) {
                String introduzione = ProduttoreDiArtefattiCasuale.introduzioneDellaLeggenda(nomeBreve);
                assertTrue(introduzione.contains(nomeBreve), introduzione);
                assertFalse(introduzione.matches("(?s).*[\\[\\]{}|<>].*"), introduzione);
            }
        }
        assertFalse(ProduttoreDiArtefattiCasuale.tuttiILibriTecnici().isEmpty());
        for (int i = 0; i < 40; i++) {
            assertTrue(ProduttoreDiArtefattiCasuale.letturaDelLibroTecnico("X").contains("X"));
            assertTrue(ProduttoreDiArtefattiCasuale.conclusioneSulLibroTecnico("MANUALE_UNIX_SYSTEM_V").matches("(?s).*(Alchemist).*"));
            assertTrue(ProduttoreDiArtefattiCasuale.conclusioneSulLibroTecnico("THE_C_PROGRAMMING_LANGUAGE").matches("(?s).*(Enchanter).*"));
            assertFalse(ProduttoreDiArtefattiCasuale.erroreDiRiprogrammazione().isEmpty());
            assertTrue(ProduttoreDiArtefattiCasuale.nomeAggiornamento().startsWith("the "), "aggiornamento");
        }
    }
}
