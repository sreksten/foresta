package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Ogni personaggio che può comparire in un gruppo, ospiti compresi, ha la sua immagine e la sua icona.
 */
class ClassePersonaggioImmagineTest {

    @Test
    void ilViandanteHaLeSueImmaginiEIlSuoComandoIcona() {
        assertNotNull(ClassePersonaggioImmagine.getImmagine(TipoPersonaggio.VIANDANTE));
        assertNotNull(ClassePersonaggioImmagine.getIcona(TipoPersonaggio.VIANDANTE));
        assertNotSame(ClassePersonaggioImmagine.getImmagine(TipoPersonaggio.BARDO),
                ClassePersonaggioImmagine.getImmagine(TipoPersonaggio.VIANDANTE), "non più l'immagine del bardo");
        // Come per ogni altro personaggio, anche la barra delle icone lo sa disegnare
        assertNotNull(ClasseIcona.ofClasse(TipoPersonaggio.VIANDANTE).getIcona());
    }
}
