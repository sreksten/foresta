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
        assertEquals(0.75d, TipoPersonaggio.GARGOYLE.getMoltiplicatoreDannoSubito(TipoDanno.TAGLIENTE), 1e-9);
        assertEquals(1.5d, TipoPersonaggio.GARGOYLE.getMoltiplicatoreDannoSubito(TipoDanno.CONTUNDENTE), 1e-9);
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            if (tipoDanno.getCategoriaDanno() != CategoriaDanno.FISICO) {
                assertEquals(0.0d, TipoPersonaggio.GARGOYLE.getMoltiplicatoreDannoSubito(tipoDanno), 1e-9, tipoDanno.name());
            }
        }
        // Chi non ha niente di speciale subisce il danno normale
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            assertEquals(1.0d, TipoPersonaggio.GOBLIN.getMoltiplicatoreDannoSubito(tipoDanno), 1e-9, tipoDanno.name());
        }
    }

    @Test
    void laTabellaDelleDebolezzeEResistenzeStaNeiLimiti() {
        // Nessun valore oltre il doppio del danno (la debolezza massima) né sotto lo zero
        for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
            for (TipoDanno tipoDanno : TipoDanno.values()) {
                double moltiplicatore = tipo.getMoltiplicatoreDannoSubito(tipoDanno);
                assertTrue(moltiplicatore >= 0.0d && moltiplicatore <= 2.0d, tipo + " " + tipoDanno + ": " + moltiplicatore);
            }
        }
        // I giocanti e i non combattenti subiscono il danno normale
        for (TipoPersonaggio tipo : new TipoPersonaggio[] {TipoPersonaggio.GUERRIERO, TipoPersonaggio.MAGO, TipoPersonaggio.LOCANDIERE,
                TipoPersonaggio.VIANDANTE}) {
            for (TipoDanno tipoDanno : TipoDanno.values()) {
                assertEquals(1.0d, tipo.getMoltiplicatoreDannoSubito(tipoDanno), 1e-9, tipo + " " + tipoDanno);
            }
        }
    }

    @Test
    void alcuniEsempiDellaTabella() {
        // Lo scheletro si spezza a bastonate e resiste a punte e lame
        assertEquals(1.5d, TipoPersonaggio.SCHELETRO.getMoltiplicatoreDannoSubito(TipoDanno.CONTUNDENTE), 1e-9);
        assertEquals(0.5d, TipoPersonaggio.SCHELETRO.getMoltiplicatoreDannoSubito(TipoDanno.PERFORANTE), 1e-9);
        // Il fuoco ferisce troll e idra, il gelo il drago
        assertEquals(1.5d, TipoPersonaggio.TROLL.getMoltiplicatoreDannoSubito(TipoDanno.FUOCO), 1e-9);
        assertEquals(1.5d, TipoPersonaggio.IDRA.getMoltiplicatoreDannoSubito(TipoDanno.ACIDO), 1e-9);
        assertEquals(1.5d, TipoPersonaggio.DRAGO.getMoltiplicatoreDannoSubito(TipoDanno.GELO), 1e-9);
        assertEquals(0.0d, TipoPersonaggio.DRAGO.getMoltiplicatoreDannoSubito(TipoDanno.FUOCO), 1e-9);
        // Il fantasma è un non morto e un volante: il sacro lo ferisce doppio, il necrotico non lo tocca, la terra non lo raggiunge
        assertEquals(2.0d, TipoPersonaggio.FANTASMA.getMoltiplicatoreDannoSubito(TipoDanno.SACRO), 1e-9);
        assertEquals(0.0d, TipoPersonaggio.FANTASMA.getMoltiplicatoreDannoSubito(TipoDanno.NECROTICO), 1e-9);
        assertEquals(0.0d, TipoPersonaggio.FANTASMA.getMoltiplicatoreDannoSubito(TipoDanno.TERRA), 1e-9);
        // Chi vola e non è altro (l'arpia) resiste solo a ciò che scuote il terreno
        assertEquals(0.5d, TipoPersonaggio.ARPIA.getMoltiplicatoreDannoSubito(TipoDanno.TERRA), 1e-9);
        assertEquals(1.0d, TipoPersonaggio.ARPIA.getMoltiplicatoreDannoSubito(TipoDanno.FUOCO), 1e-9);
    }
}
