package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.IngredienteAlchemico;
import com.threeamigos.foresta.missioni.LAlchimista;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli ingredienti che chiede l'alchimista vengono da missioni.txt: ogni riga si legge, e i testi della missione si
 * accordano con l'ingrediente.
 */
class ScenarioIngredientiAlchemiciTest {

    private static final String ACONITO = "M;fiore di aconito;fiori di aconito;RADURA;Li raccogliamo con i guanti?;Se ci tenete alle dita, sì.";

    @Test
    void ogniIngredienteDellaGrammaticaSiLeggeEGliIngredientiSonoVari() {
        Set<String> plurali = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            IngredienteAlchemico ingrediente = IngredienteAlchemico.da(ProduttoreDiTestiCasuale.ingredienteAlchemico());
            assertFalse(ingrediente.getLuoghi().isEmpty());
            assertFalse(ingrediente.getBattutaDelCapo().isEmpty());
            assertFalse(ingrediente.getRispostaDellAlchimista().isEmpty());
            plurali.add(ingrediente.getPlurale());
        }
        assertTrue(plurali.size() >= 8, "ingredienti diversi: " + plurali);
        assertTrue(plurali.contains("radici di mandragola"));
        assertTrue(plurali.contains("bacche di belladonna"));
    }

    @Test
    void iTestiSiAccordanoConUnIngredienteMaschile() {
        IngredienteAlchemico aconito = IngredienteAlchemico.da(ACONITO);
        assertEquals("i fiori di aconito", aconito.getPluraleConArticolo());
        assertEquals("tre fiori di aconito", aconito.quanti(3));
        assertEquals("li", aconito.getPronome());
        assertEquals("nelle radure", aconito.getDoveSiTrova());
        assertEquals("nelle radure e nei boschi", IngredienteAlchemico.da(Alchimie.MANDRAGOLA).getDoveSiTrova());
        assertThrows(IllegalArgumentException.class, () -> IngredienteAlchemico.da("X;uno;due;BOSCO;a;b"));
    }

    @Test
    void lAlchimistaChiedeIFioriDiAconitoESiRipeteConUnAltroIngrediente() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(171)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            LAlchimista alchimista = Alchimie.conLaMandragola();
            alchimista.aggiungiProprieta("PARAMETRO_" + LAlchimista.INGREDIENTE, ACONITO);
            alchimista.aggiungiProprieta("PARAMETRO_QUANTITA", "3");
            alchimista.controllaPreLocazione();
            alchimista.segnaIntermezzoPassoMostrato("INCARICO");
            alchimista.controllaInLocazione();
            assertTrue(alchimista.isAttiva());
            assertEquals("L'alchimista e i fiori di aconito", alchimista.getNome());
            assertTrue(alchimista.getDescrizione().contains("ti ha chiesto tre fiori di aconito, che si trovano nelle radure"),
                    alchimista.getDescrizione());

            // I fiori si trovano solo nelle radure
            boolean nelBosco = false;
            for (int i = 0; i < 100; i++) {
                nelBosco |= alchimista.getOggettoInLocazione(new com.threeamigos.foresta.motore.modellodati.CoordinateMD(0, 0),
                        ClassiLocazione.BOSCO, false).isPresent();
            }
            assertFalse(nelBosco);

            new OggettoMissione(alchimista.getId(), LAlchimista.INGREDIENTE, alchimista.getIngredienti().getNome(), 3)
                    .prendi(partita.gruppo(), null);
            alchimista.controllaPostLocazione();
            int monete = partita.gruppo().getMonete();
            alchimista.controllaPreLocazione();
            alchimista.controllaInLocazione();
            assertEquals(monete + 20, partita.gruppo().getMonete());
            List<String> testi = partita.testi();
            assertTrue(testi.contains("I tre fiori di aconito passano all'alchimista, che li annusa soddisfatto."), String.valueOf(testi));
            assertTrue(testi.stream().anyMatch(t -> t.startsWith("I fiori di aconito ci sono tutti")), String.valueOf(testi));

            // Il nuovo alchimista chiederà un ingrediente suo, pescato quando si offrirà
            LAlchimista nuovo = RegistroMissioni.getTutteLeMissioni().stream().filter(LAlchimista.class::isInstance)
                    .map(LAlchimista.class::cast).filter(m -> m != alchimista).findFirst().orElseThrow(AssertionError::new);
            assertNull(nuovo.getParametro(LAlchimista.INGREDIENTE));
        }
    }
}
