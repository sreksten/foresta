package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.tipi.GradoIncantamento;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il generatore con le sole tabelle, con le grammatiche e senza una delle due, e il listino dei prezzi.
 */
class GenerazioneEListinoTest {

    private static void generaTutto(GeneratoreArtefattiTabelle generatore) {
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            for (int livello = 0; livello <= 12; livello++) {
                Artefatto artefatto = generatore.generaArtefatto(tipo, livello);
                assertEquals(tipo, artefatto.getTipo());
                assertTrue(artefatto.getLivello() >= 1);
                assertTrue(artefatto.getCostoAcquisto() >= 0, tipo + " " + livello);
            }
        }
    }

    @Test
    void conLeSoleTabelleSiGeneraOgniTipo() {
        for (int seme = 0; seme < 20; seme++) {
            generaTutto(new GeneratoreArtefattiTabelle(new Random(seme)));
        }
    }

    @Test
    void conLeGrammaticheSiGeneraOgniTipo() {
        GrammaticaArtefatti artefatti = GrammaticaArtefatti.caricaOppureNull();
        GrammaticaArtefatti ingredienti = GrammaticaArtefatti.caricaIngredientiOppureNull();
        assertNotNull(artefatti);
        assertNotNull(ingredienti);
        for (int seme = 0; seme < 40; seme++) {
            generaTutto(new GeneratoreArtefattiTabelle(new Random(seme), artefatti, ingredienti));
        }
    }

    @Test
    void senzaLaGrammaticaDegliIngredientiSiUsanoLeTabelle() {
        GrammaticaArtefatti artefatti = GrammaticaArtefatti.caricaOppureNull();
        GeneratoreArtefattiTabelle generatore = new GeneratoreArtefattiTabelle(new Random(3), artefatti, null);
        for (int livello = 1; livello <= 12; livello++) {
            assertTrue(generatore.generaIngrediente(livello).getTipo().isIngrediente());
        }
    }

    @Test
    void conUnaGrammaticaCheNonConosceIlTipoSiUsanoLeTabelle() {
        // La grammatica degli artefatti non conosce gli ingredienti
        GrammaticaArtefatti artefatti = GrammaticaArtefatti.caricaOppureNull();
        GeneratoreArtefattiTabelle generatore = new GeneratoreArtefattiTabelle(new Random(3), null, artefatti);
        assertTrue(generatore.generaArtefatto(TipoArtefatto.GEMMA, 5).getTipo().isIngrediente());
    }

    @Test
    void ilGradoSiScriveDopoIlNomeComune() {
        assertEquals("la gemma del Drago", GeneratoreArtefattiTabelle.conGrado("la gemma del Drago", TipoArtefatto.GEMMA, GradoIncantamento.MEDIO));
        assertEquals("la gemma minore del Drago", GeneratoreArtefattiTabelle.conGrado("la gemma del Drago", TipoArtefatto.GEMMA, GradoIncantamento.MINORE));
        assertEquals("la pietra del Drago maggiore", GeneratoreArtefattiTabelle.conGrado("la pietra del Drago", TipoArtefatto.GEMMA, GradoIncantamento.MAGGIORE));
    }

    @Test
    void ilGeneratoreCasualeNonDaIngredienti() {
        GeneratoreArtefattiTabelle generatore = new GeneratoreArtefattiTabelle(new Random(8));
        for (int i = 0; i < 100; i++) {
            assertFalse(generatore.generaArtefattoCasuale(4).getTipo().isIngrediente());
        }
    }

    private static ModificatoreAttributoMD modificatore(TipoModificatore tipo, double quantita, UnitaDurataModificatore unita, int durata) {
        return new ModificatoreAttributoMD(TipoAttributo.FORZA, tipo, quantita, "", unita, durata, BersaglioModificatore.PORTATORE);
    }

    @Test
    void ilPrezzoDipendeDalTipoEDallaDurata() {
        double permanente = ListinoPergamene.prezzo(modificatore(TipoModificatore.VARIAZIONE_PERCENTUALE, 10, UnitaDurataModificatore.PERMANENTE, 0));
        double aTurni = ListinoPergamene.prezzo(modificatore(TipoModificatore.VARIAZIONE_PERCENTUALE, 10, UnitaDurataModificatore.TURNI, 2));
        double aLocazioni = ListinoPergamene.prezzo(modificatore(TipoModificatore.VARIAZIONE_PERCENTUALE, 10, UnitaDurataModificatore.LOCAZIONI, 1));
        double moltoLungo = ListinoPergamene.prezzo(modificatore(TipoModificatore.VARIAZIONE_PERCENTUALE, 10, UnitaDurataModificatore.TURNI, 100));
        assertEquals(permanente * CostantiArtefatti.PERGAMENA_COEFFICIENTE_PREZZO_PER_TURNO * 2, aTurni, 0.0001);
        // Una locazione vale 5 turni, e 5 turni arrivano già al tetto
        assertEquals(permanente * CostantiArtefatti.PERGAMENA_MOLTIPLICATORE_PREZZO_TEMPO_MASSIMO, aLocazioni, 0.0001);
        assertEquals(permanente * CostantiArtefatti.PERGAMENA_MOLTIPLICATORE_PREZZO_TEMPO_MASSIMO, moltoLungo, 0.0001);
        assertTrue(ListinoPergamene.prezzo(modificatore(TipoModificatore.VARIAZIONE_FISSA, 2, UnitaDurataModificatore.PERMANENTE, 0)) > 0);
        assertEquals(CostantiArtefatti.PERGAMENA_PREZZO_PORTA_A_ZERO,
                ListinoPergamene.prezzo(modificatore(TipoModificatore.QUANTITA_ASSOLUTA, 0, UnitaDurataModificatore.PERMANENTE, 0)), 0.0001);
        assertEquals(CostantiArtefatti.PERGAMENA_PREZZO_PER_PUNTO_ASSOLUTO * 3,
                ListinoPergamene.prezzo(modificatore(TipoModificatore.QUANTITA_ASSOLUTA, 3, UnitaDurataModificatore.PERMANENTE, 0)), 0.0001);
    }

    @Test
    void ilPrezzoDiUnIncantamentoCresceConIlLivello() {
        IncantamentoMD incantamento = new IncantamentoMD("Fiamma", TipoDanno.FUOCO, 5, 0.1);
        assertTrue(ListinoPergamene.prezzo(incantamento, 8) >= ListinoPergamene.prezzo(incantamento, 1));
    }
}
