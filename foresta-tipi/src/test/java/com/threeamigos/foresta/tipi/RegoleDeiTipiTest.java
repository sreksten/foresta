package com.threeamigos.foresta.tipi;


import com.threeamigos.foresta.artefatti.CostantiArtefatti;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le piccole regole che le enum del modulo portano con sé: chi batte chi a carta, forbici e sasso, il grado di un incantamento, che
 * cosa tratta un negozio, il ruolo di una classe, quanti effetti regge un artefatto...
 */
class RegoleDeiTipiTest {

    @Test
    void cartaBatteSassoSassoBatteForbiceForbiceBatteCarta() {
        for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
            for (MossaCartaForbiciSasso altra : MossaCartaForbiciSasso.values()) {
                boolean attesa = (mossa == MossaCartaForbiciSasso.CARTA && altra == MossaCartaForbiciSasso.SASSO)
                        || (mossa == MossaCartaForbiciSasso.SASSO && altra == MossaCartaForbiciSasso.FORBICE)
                        || (mossa == MossaCartaForbiciSasso.FORBICE && altra == MossaCartaForbiciSasso.CARTA);
                assertEquals(attesa, mossa.batte(altra), mossa + " contro " + altra);
            }
        }
    }

    @Test
    void ilGradoDiUnIncantamentoCresceConIlLivello() {
        assertSame(GradoIncantamento.MINORE, GradoIncantamento.perLivello(CostantiArtefatti.GRADO_INCANTAMENTO_MEDIO_DAL_LIVELLO - 1));
        assertSame(GradoIncantamento.MEDIO, GradoIncantamento.perLivello(CostantiArtefatti.GRADO_INCANTAMENTO_MEDIO_DAL_LIVELLO));
        assertSame(GradoIncantamento.MEDIO, GradoIncantamento.perLivello(CostantiArtefatti.GRADO_INCANTAMENTO_MAGGIORE_DAL_LIVELLO - 1));
        assertSame(GradoIncantamento.MAGGIORE, GradoIncantamento.perLivello(CostantiArtefatti.GRADO_INCANTAMENTO_MAGGIORE_DAL_LIVELLO));

        assertSame(GradoIncantamento.MINORE, GradoIncantamento.perIngrediente(CostantiArtefatti.INGREDIENTE_GRADO_MEDIO_DAL_LIVELLO - 1));
        assertSame(GradoIncantamento.MEDIO, GradoIncantamento.perIngrediente(CostantiArtefatti.INGREDIENTE_GRADO_MEDIO_DAL_LIVELLO));
        assertSame(GradoIncantamento.MEDIO, GradoIncantamento.perIngrediente(CostantiArtefatti.INGREDIENTE_GRADO_MAGGIORE_DAL_LIVELLO - 1));
        assertSame(GradoIncantamento.MAGGIORE, GradoIncantamento.perIngrediente(CostantiArtefatti.INGREDIENTE_GRADO_MAGGIORE_DAL_LIVELLO));

        // Un grado più alto dà di più, e i gradini contano da 1
        GradoIncantamento precedente = null;
        for (GradoIncantamento grado : GradoIncantamento.values()) {
            assertEquals(grado.ordinal() + 1, grado.getGradino());
            if (precedente != null) {
                assertTrue(grado.getBonusFisso() > precedente.getBonusFisso(), grado.name());
                assertTrue(grado.getCoefficiente() > precedente.getCoefficiente(), grado.name());
            }
            precedente = grado;
        }
    }

    @Test
    void unArmaHaSempreUnTipoDiDannoIlRestoMai() {
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            assertEquals(tipo.getCategoria() == CategoriaArtefatto.ARMA, tipo.getTipoDanno() != null, tipo.name());
            assertNotNull(tipo.getCardinalita(), tipo.name());
            assertNotNull(tipo.getSlotArtefatto(), tipo.name());
        }
        assertSame(TipoDanno.TAGLIENTE, TipoArtefatto.SPADA.getTipoDanno());
        assertSame(TipoSlotArtefatto.ENTRAMBE_LE_MANI, TipoArtefatto.SPADONE.getSlotArtefatto());
        assertSame(TipoCardinalitaArtefatto.PAIO, TipoArtefatto.SCHINIERI.getCardinalita());
    }

    @Test
    void gliIngredientiSonoGliIncantamentiEDiQuestiLAggiornamentoELaRiprogrammazioneUnoSolo() {
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            assertEquals(tipo.getCategoria() == CategoriaArtefatto.INCANTAMENTO, tipo.isIngrediente(), tipo.name());
            assertEquals(tipo == TipoArtefatto.AGGIORNAMENTO, tipo.isAggiornamento(), tipo.name());
            assertEquals(tipo == TipoArtefatto.RIPROGRAMMAZIONE, tipo.isRiprogrammazione(), tipo.name());
        }
        assertTrue(TipoArtefatto.RIPROGRAMMAZIONE.isIngrediente(), "il libro che riprogramma si mette sul banco come gli altri ingredienti");
        assertTrue(TipoArtefatto.AGGIORNAMENTO.isIngrediente(), "l'aggiornamento si mette sul banco come gli altri ingredienti");
    }


    @Test
    void gliEffettiDiUnArtefattoSalgonoColLivelloFinoAlTettoDellaRarita() {
        assertEquals(0, TipoRaritaArtefatto.COMUNE.getEffettiMassimi(0), "mai meno di zero");
        assertEquals(1, TipoRaritaArtefatto.COMUNE.getEffettiMassimi(2));
        assertEquals(3, TipoRaritaArtefatto.COMUNE.getEffettiMassimi(20), "il tetto dei comuni");
        assertEquals(2, TipoRaritaArtefatto.RARO.getEffettiMassimi(2));
        assertEquals(4, TipoRaritaArtefatto.RARO.getEffettiMassimi(20));
        assertEquals(3, TipoRaritaArtefatto.LEGGENDARIO.getEffettiMassimi(2));
        assertEquals(5, TipoRaritaArtefatto.LEGGENDARIO.getEffettiMassimi(20));
    }
}
