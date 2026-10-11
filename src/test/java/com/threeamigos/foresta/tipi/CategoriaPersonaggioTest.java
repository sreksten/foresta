package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le categorie di creatura delle classi dei personaggi.
 */
class CategoriaPersonaggioTest {

    @Test
    void ogniClasseHaAlmenoUnaCategoria() {
        for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
            assertFalse(tipo.getCategorie().isEmpty(), tipo.name());
        }
    }

    @Test
    void unaClassePuoEssereDiPiuCategorie() {
        assertEquals(EnumSet.of(CategoriaPersonaggio.NON_MORTO, CategoriaPersonaggio.VOLANTE), TipoPersonaggio.SPIRITO.getCategorie());
        assertEquals(EnumSet.of(CategoriaPersonaggio.NON_MORTO, CategoriaPersonaggio.VOLANTE), TipoPersonaggio.OMBRA_NERA.getCategorie());
        assertTrue(TipoPersonaggio.LICH.isDellaCategoria(CategoriaPersonaggio.NON_MORTO));
        assertTrue(TipoPersonaggio.MINOTAURO_GIGANTE.isDellaCategoria(CategoriaPersonaggio.IBRIDO));
        assertFalse(TipoPersonaggio.GOBLIN.isDellaCategoria(CategoriaPersonaggio.NON_MORTO));
    }

    @Test
    void leCategorieNonSiModificano() {
        assertThrows(UnsupportedOperationException.class,
                () -> TipoPersonaggio.GOBLIN.getCategorie().add(CategoriaPersonaggio.DRACONICO));
    }
}
