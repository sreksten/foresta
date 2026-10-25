package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoNegozio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Che cosa tratta ogni negozio.
 */
class TipoNegozioTest {

    @Test
    void ilVenditoreTrattaGliIngredientiLArmaioloIlRestoNessunoGliAggiornamentiELeRiprogrammazioni() {
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            boolean ingrediente = tipo.isIngrediente() && !tipo.isAggiornamento() && !tipo.isRiprogrammazione();
            boolean altro = !tipo.isIngrediente();
            assertEquals(ingrediente, TipoNegozio.VENDITORE_DI_PERGAMENE.tratta(tipo), tipo.name());
            assertEquals(altro, TipoNegozio.ARMAIOLO.tratta(tipo), tipo.name());
        }
    }

    @Test
    void ognunNegozioHaIlSuoNome() {
        for (TipoNegozio negozio : TipoNegozio.values()) {
            assertFalse(negozio.getNome().trim().isEmpty(), negozio.name());
        }
    }
}
