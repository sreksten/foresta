package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.tipi.TipoNegozio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Il negoziante dice che cosa farà della merce al momento della vendita, a bottega aperta: all'uscita dalla città,
 * quando la smaltisce, la bottega non si vede più.
 */
class DisplayableCanvasCommercianteTest {

    @Test
    void allaVenditaArmaioloEVenditoreDiconoCheCosaFarannoDellaMerce() {
        DisplayableCanvasCommerciante commerciante = new DisplayableCanvasCommerciante(1024, 768);
        commerciante.impostaNegozio(TipoNegozio.ARMAIOLO);
        assertEquals("Con il materiale che mi hai fornito, farò altre meravigliose creazioni!", commerciante.fraseDopoLaVendita());
        commerciante.impostaNegozio(TipoNegozio.VENDITORE_DI_PERGAMENE);
        assertEquals("Ottime pergamene: le rivenderò a qualche mago di passaggio!", commerciante.fraseDopoLaVendita());
    }
}
