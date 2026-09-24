package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.SupertipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrammaticaArtefattiTest {

    private static final int GIRI = 2000;
    private static final int EFFETTI_MASSIMI_SPADA = 3;

    private static GrammaticaArtefatti grammatica;

    @BeforeAll
    static void carica() {
        grammatica = GrammaticaArtefatti.caricaOppureNull();
        assertNotNull(grammatica, "artefatti2.txt non si carica");
    }

    @Test
    void conosceLaSpadaESoloLei() {
        assertTrue(grammatica.supporta(TipoArtefatto.SPADA));
        assertFalse(grammatica.supporta(TipoArtefatto.SCUDO));
        assertThrows(IllegalArgumentException.class, () -> grammatica.genera(TipoArtefatto.SCUDO, 1));
    }

    @Test
    void ogniSpadaHaTantiEffettiQuantiNeChiedeENomePulito() {
        for (int effetti = 0; effetti <= EFFETTI_MASSIMI_SPADA; effetti++) {
            for (int i = 0; i < GIRI; i++) {
                GrammaticaArtefatti.Risultato risultato = grammatica.genera(TipoArtefatto.SPADA, effetti);
                String nome = risultato.getNome();
                assertEquals(effetti, risultato.getEffetti(), nome);
                assertTrue(nome.startsWith("la ") || nome.startsWith("l'"), nome);
                assertFalse(nome.matches(".*[@\\[\\]{}|<>\"].*"), nome);
                assertFalse(nome.contains("  "), nome);
                for (TipoDanno danno : risultato.getDanni()) {
                    assertTrue(danno.getSuperTipo() != SupertipoDanno.FISICO, nome);
                }
                for (GrammaticaArtefatti.Modificatore modificatore : risultato.getModificatori()) {
                    assertTrue(modificatore.getIntensita() != 0 && Math.abs(modificatore.getIntensita()) <= 3, nome);
                }
                // Il soprannome e la descrizione vengono solo dalle parti elementali
                if (risultato.getDanni().isEmpty()) {
                    assertNull(risultato.getSoprannome(), nome);
                    assertNull(risultato.getDescrizione(), nome);
                } else {
                    assertNotNull(risultato.getDescrizione(), nome);
                }
            }
        }
    }

    @Test
    void chiedereTroppiEffettiDaIlMassimoEPocoNessuno() {
        assertEquals(EFFETTI_MASSIMI_SPADA, grammatica.genera(TipoArtefatto.SPADA, 10).getEffetti());
        assertEquals(0, grammatica.genera(TipoArtefatto.SPADA, -2).getEffetti());
    }

    @Test
    void leSpadeElementaliHannoSpessoUnSoprannome() {
        int elementali = 0;
        int conSoprannome = 0;
        for (int i = 0; i < GIRI; i++) {
            GrammaticaArtefatti.Risultato risultato = grammatica.genera(TipoArtefatto.SPADA, 2);
            if (!risultato.getDanni().isEmpty()) {
                elementali++;
                if (risultato.getSoprannome() != null) {
                    conSoprannome++;
                }
            }
        }
        assertTrue(elementali > GIRI / 5, "Elementali: " + elementali);
        double quota = (double) conSoprannome / elementali;
        assertTrue(quota > 0.5 && quota < 0.8, "Quota con soprannome: " + quota);
    }

    @Test
    void interpretaIMarcatori() {
        GrammaticaArtefatti.Risultato risultato = GrammaticaArtefatti.interpreta(
                "la spada ardente<danno:FUOCO><descrizione:che brucia i nemici><soprannome:Barbecue>"
                        + " del Monaco Distratto<mod:SAGGEZZA-1> (con Manuale)<mod:INTELLIGENZA+2><soprannome:Secondo>");
        assertEquals("la spada ardente del Monaco Distratto (con Manuale)", risultato.getNome());
        assertEquals("Barbecue", risultato.getSoprannome());
        assertEquals("che brucia i nemici", risultato.getDescrizione());
        assertEquals(1, risultato.getDanni().size());
        assertEquals(TipoDanno.FUOCO, risultato.getDanni().get(0));
        assertEquals(2, risultato.getModificatori().size());
        assertEquals(TipoAttributo.SAGGEZZA, risultato.getModificatori().get(0).getAttributo());
        assertEquals(-1, risultato.getModificatori().get(0).getIntensita());
        assertEquals(2, risultato.getModificatori().get(1).getIntensita());
    }

    @Test
    void unMarcatoreSbagliatoEUnErrore() {
        assertThrows(IllegalArgumentException.class, () -> GrammaticaArtefatti.interpreta("la spada<boh:1>"));
        assertThrows(IllegalArgumentException.class, () -> GrammaticaArtefatti.interpreta("la spada<mod:CARICO+1>"));
        assertThrows(IllegalArgumentException.class, () -> GrammaticaArtefatti.interpreta("la spada<mod:FORZA>"));
        assertThrows(IllegalArgumentException.class, () -> GrammaticaArtefatti.interpreta("la spada<danno:TENEBRA>"));
        assertThrows(IllegalArgumentException.class, () -> GrammaticaArtefatti.interpreta("la spada<danno:FUOCO"));
    }

    @Test
    void ilGeneratoreUsaLaGrammaticaPerMetaDelleSpadeRispettandoIlLimite() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(12), grammatica);
        int conModificatori = 0;
        for (int i = 0; i < GIRI; i++) {
            int livello = 1 + i % 10;
            Artefatto spada = generatore.generaArtefatto(TipoArtefatto.SPADA, livello);
            ArtefattoMD md = spada.getModelloDati();
            int effetti = md.getModificatori().size() + md.getIncantamenti().size();
            // Resta sempre un posto libero per la fusione
            assertTrue(effetti == 0 || effetti < spada.getEffettiMassimi(), md.getNome());
            assertTrue(md.getDanni() > 0);
            assertTrue(spada.getCostoAcquisto() >= (5 + 5 * livello) / 2, md.getNome());
            // Dalle tabelle una spada non ha mai modificatori: questi vengono dalla grammatica
            if (!md.getModificatori().isEmpty()) {
                conModificatori++;
            }
        }
        assertTrue(conModificatori > GIRI / 5, "Spade con modificatori: " + conModificatori);
        // Al livello 1 un artefatto comune non ha posto per effetti
        for (int i = 0; i < 200; i++) {
            Artefatto spada = generatore.generaArtefatto(TipoArtefatto.SPADA, 1);
            assertTrue(spada.getModelloDati().getModificatori().isEmpty());
            assertTrue(spada.getIncantamenti().isEmpty());
        }
    }
}
