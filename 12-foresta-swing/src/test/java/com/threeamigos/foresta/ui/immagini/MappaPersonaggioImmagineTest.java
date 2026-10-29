package com.threeamigos.foresta.ui.immagini;

import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoIcona;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Ogni personaggio che può comparire in un gruppo, ospiti compresi, ha la sua immagine e la sua icona.
 */
class MappaPersonaggioImmagineTest {

    @Test
    void ilViandanteHaLeSueImmaginiEIlSuoComandoIcona() {
        assertNotNull(MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.VIANDANTE));
        assertNotNull(MappaPersonaggioImmagine.getIcona(TipoPersonaggio.VIANDANTE));
        assertNotSame(MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.BARDO),
                MappaPersonaggioImmagine.getImmagine(TipoPersonaggio.VIANDANTE), "non più l'immagine del bardo");
        // Come per ogni altro personaggio, anche la barra delle icone lo sa disegnare
        assertNotNull(TipoIcona.ofClasse(TipoPersonaggio.VIANDANTE).getIcona());
    }
}
