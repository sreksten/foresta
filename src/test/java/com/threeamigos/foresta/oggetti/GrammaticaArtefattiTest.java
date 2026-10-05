package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.SupertipoDanno;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoModificatore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GrammaticaArtefattiTest {

    private static final int GIRI = 2000;
    private static final int EFFETTI_MASSIMI_SPADA = 3;
    /**
     * I tipi che la grammatica conosce, con il numero massimo di effetti e l'inizio del nome senza prefisso
     */
    private static final Object[][] TIPI = {
            { TipoArtefatto.SPADA, 3, "la spada" },
            { TipoArtefatto.ARMATURA, 3, "l'armatura" },
            { TipoArtefatto.VESTE, 3, "la veste" },
            { TipoArtefatto.ELMO, 3, "l'elmo" },
            { TipoArtefatto.SCUDO, 3, "lo scudo" },
            { TipoArtefatto.SCHINIERI, 3, "gli schinieri" },
    };

    private static GrammaticaArtefatti grammatica;

    @BeforeAll
    static void carica() {
        grammatica = GrammaticaArtefatti.caricaOppureNull();
        assertNotNull(grammatica, "artefatti2.txt non si carica");
    }

    @Test
    void conosceSoloITipiChePrevede() {
        for (Object[] tipo : TIPI) {
            assertTrue(grammatica.supporta((TipoArtefatto) tipo[0]), tipo[0].toString());
        }
        assertFalse(grammatica.supporta(TipoArtefatto.ANELLO));
        assertThrows(IllegalArgumentException.class, () -> grammatica.genera(TipoArtefatto.ANELLO, 1));
    }

    @Test
    void ogniTipoHaIlSuoArticoloEIlNumeroDiEffettiChiesto() {
        for (Object[] riga : TIPI) {
            TipoArtefatto tipo = (TipoArtefatto) riga[0];
            int massimo = (Integer) riga[1];
            String nome = ((String) riga[2]).replaceFirst("^(la |l'|lo |gli )", "");
            for (int effetti = 0; effetti <= massimo; effetti++) {
                for (int i = 0; i < GIRI / 4; i++) {
                    GrammaticaArtefatti.Risultato risultato = grammatica.genera(tipo, effetti);
                    String testo = risultato.getNome();
                    assertEquals(effetti, risultato.getEffetti(), testo);
                    assertTrue(testo.matches("^(la |l'|il |lo |i |gli ).*\\b" + nome + "\\b.*"), testo);
                    assertFalse(testo.matches(".*[@\\[\\]{}|<>\"].*"), testo);
                    // Senza effetti non c'è prefisso, quindi il nome comincia con articolo e nome
                    if (effetti == 0) {
                        assertTrue(testo.startsWith((String) riga[2]), testo);
                    }
                }
            }
            assertEquals(massimo, grammatica.genera(tipo, 10).getEffetti());
        }
    }

    @Test
    void gliArticoliMaschiliSiAccordanoConLaParolaCheSegue() {
        for (int i = 0; i < GIRI; i++) {
            String elmo = grammatica.genera(TipoArtefatto.ELMO, 1).getNome();
            assertTrue(elmo.matches("^(l'[aeiou]|lo (s[^aeiou]|z|gn|ps|pn|x|y)|il [^aeiou]).*"), elmo);
            assertFalse(elmo.matches("^il (s[^aeiou]|z|gn|ps).*"), elmo);
            String schinieri = grammatica.genera(TipoArtefatto.SCHINIERI, 1).getNome();
            assertTrue(schinieri.matches("^(gli ([aeiou]|s[^aeiou]|z|gn|ps|pn|x|y)|i [^aeiou]).*"), schinieri);
            assertFalse(schinieri.matches("^i (s[^aeiou]|z|gn|ps).*"), schinieri);
        }
    }

    @Test
    void gliSchinieriHannoLeDescrizioniAlPlurale() {
        int elementali = 0;
        for (int i = 0; i < GIRI; i++) {
            GrammaticaArtefatti.Risultato risultato = grammatica.genera(TipoArtefatto.SCHINIERI, 2);
            if (!risultato.getDanni().isEmpty()) {
                elementali++;
                // "che respingono", "che non si lasciano": il verbo dopo "che", "non" e "si" finisce in -no
                String verbo = risultato.getDescrizione().replaceFirst("^che (non )?(si )?", "").split(" ")[0];
                assertTrue(verbo.endsWith("no"), risultato.getNome() + ", " + risultato.getDescrizione());
            }
        }
        assertTrue(elementali > GIRI / 5, "Elementali: " + elementali);
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
                    assertNotSame(danno.getSuperTipo(), SupertipoDanno.FISICO, nome);
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
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(12), grammatica, null);
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

    @Test
    void leProtezioniDallaGrammaticaHannoLaLoroDifesaDiBase() {
        GeneratoreArtefatti generatore = new GeneratoreArtefattiTabelle(new Random(7), grammatica, null);
        for (TipoArtefatto tipo : new TipoArtefatto[] { TipoArtefatto.ARMATURA, TipoArtefatto.VESTE, TipoArtefatto.ELMO,
                TipoArtefatto.SCUDO, TipoArtefatto.SCHINIERI }) {
            TipoAttributo difesa = tipo == TipoArtefatto.VESTE ? TipoAttributo.RESISTENZA_MAGICA : TipoAttributo.PARATA;
            for (int i = 0; i < GIRI / 4; i++) {
                Artefatto artefatto = generatore.generaArtefatto(tipo, 1 + i % 10);
                ArtefattoMD md = artefatto.getModelloDati();
                assertTrue(md.getModificatori().stream().anyMatch(m -> m.getTipoAttributo() == difesa
                        && m.getTipoModificatoreAttributo() == TipoModificatore.AUMENTO_PERCENTUALE), md.getNome());
                assertNotNull(md.getDescrizione(), md.getNome());
                // La difesa di base c'è anche quando l'artefatto non ha posti (comune di livello 1)
                int effetti = md.getModificatori().size() - 1 + md.getIncantamenti().size();
                assertTrue(effetti == 0 || effetti < artefatto.getEffettiMassimi(), md.getNome());
            }
        }
    }
}
