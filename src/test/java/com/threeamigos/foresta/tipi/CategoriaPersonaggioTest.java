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

    @Test
    void ilDannoSubitoSiLeggeDallaClasseEDallaCategoria() {
        // I non morti: il doppio dal SACRO, immuni al VELENO
        assertEquals(2.0d, TipoPersonaggio.SCHELETRO.getMoltiplicatoreDannoSubito(TipoDanno.SACRO), 1e-9);
        assertEquals(0.0d, TipoPersonaggio.LICH.getMoltiplicatoreDannoSubito(TipoDanno.VELENO), 1e-9);
        // I fantasmi in più: immuni ad ARIA e TERRA, per la loro classe
        assertEquals(0.0d, TipoPersonaggio.SPIRITO.getMoltiplicatoreDannoSubito(TipoDanno.ARIA), 1e-9);
        assertEquals(1.0d, TipoPersonaggio.SCHELETRO.getMoltiplicatoreDannoSubito(TipoDanno.ARIA), 1e-9);
        // Il gargoyle subisce solo il danno fisico
        assertEquals(1.0d, TipoPersonaggio.GARGOYLE.getMoltiplicatoreDannoSubito(TipoDanno.TAGLIENTE), 1e-9);
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            if (tipoDanno.getSuperTipo() != SupertipoDanno.FISICO) {
                assertEquals(0.0d, TipoPersonaggio.GARGOYLE.getMoltiplicatoreDannoSubito(tipoDanno), 1e-9, tipoDanno.name());
            }
        }
        // Chi non ha niente di speciale subisce il danno normale
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            assertEquals(1.0d, TipoPersonaggio.GOBLIN.getMoltiplicatoreDannoSubito(tipoDanno), 1e-9, tipoDanno.name());
        }
    }
}
