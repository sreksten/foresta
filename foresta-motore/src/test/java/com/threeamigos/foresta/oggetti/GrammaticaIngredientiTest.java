package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoDanno;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GrammaticaIngredientiTest {

    private static final int GIRI = 1000;
    private static final Object[][] INGREDIENTI = {
            { TipoArtefatto.PERGAMENA, "la pergamena " },
            { TipoArtefatto.GEMMA, "la gemma " },
            { TipoArtefatto.MONILE, "il monile " },
            { TipoArtefatto.GINGILLO, "il gingillo " },
            { TipoArtefatto.SIGILLO, "il sigillo " },
    };

    private static GrammaticaArtefatti grammatica;

    @BeforeAll
    static void carica() {
        grammatica = GrammaticaArtefatti.caricaIngredientiOppureNull();
        assertNotNull(grammatica, "ingredienti.txt non si carica");
    }

    @Test
    void ogniIngredienteHaEffettiDiversiDellaSuaSpecialita() {
        for (Object[] riga : INGREDIENTI) {
            TipoArtefatto tipo = (TipoArtefatto) riga[0];
            assertTrue(grammatica.supporta(tipo), tipo.toString());
            for (int effetti = 1; effetti <= 3; effetti++) {
                for (int i = 0; i < GIRI; i++) {
                    GrammaticaArtefatti.Risultato risultato = grammatica.genera(tipo, effetti);
                    String nome = risultato.getNome();
                    assertTrue(nome.startsWith((String) riga[1]), nome);
                    assertFalse(nome.matches(".*[@\\[\\]{}|<>\"].*"), nome);
                    assertFalse(nome.contains(" ,") || nome.contains("  "), nome);
                    assertEquals(effetti, risultato.getEffetti(), nome);
                    Set<Object> visti = new HashSet<>();
                    for (GrammaticaArtefatti.Modificatore modificatore : risultato.getModificatori()) {
                        assertTrue(GeneratoreArtefattiTabelle.attributiDi(tipo).contains(modificatore.getAttributo()), nome);
                        assertEquals(1, modificatore.getIntensita(), nome);
                        assertTrue(visti.add(modificatore.getAttributo()), nome);
                    }
                    for (TipoDanno danno : risultato.getDanni()) {
                        assertTrue(GeneratoreArtefattiTabelle.danniDi(tipo).contains(danno), nome);
                        assertTrue(visti.add(danno), nome);
                    }
                    assertNull(risultato.getSoprannome(), nome);
                }
            }
        }
    }

    @Test
    void laGrammaticaCopreTuttaLaSpecialitaDiOgniIngrediente() {
        for (Object[] riga : INGREDIENTI) {
            TipoArtefatto tipo = (TipoArtefatto) riga[0];
            Set<Object> visti = new HashSet<>();
            for (int i = 0; i < GIRI; i++) {
                GrammaticaArtefatti.Risultato risultato = grammatica.genera(tipo, 3);
                risultato.getModificatori().forEach(m -> visti.add(m.getAttributo()));
                visti.addAll(risultato.getDanni());
            }
            Set<Object> attesi = new HashSet<>(GeneratoreArtefattiTabelle.attributiDi(tipo));
            attesi.addAll(GeneratoreArtefattiTabelle.danniDi(tipo));
            assertEquals(attesi, visti, tipo.toString());
        }
    }

    @Test
    void ilGeneratoreMetteIlGradoDopoIlNomeComune() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(3), null, grammatica);
        for (int i = 0; i < GIRI; i++) {
            int livello = 1 + i % 6;
            ArtefattoMD md = generatore.generaIngrediente(livello).getModelloDati();
            String nome = md.getNome();
            String grado = livello <= 2 ? " minore " : livello == 3 ? " " : " maggiore ";
            assertTrue(nome.matches("^(la|il) [a-z]+" + grado + "(del|della|dello|dell'|delle).*"), nome);
            assertEquals(Math.min(3, livello), md.getModificatori().size() + md.getIncantamenti().size(), nome);
        }
    }

}
