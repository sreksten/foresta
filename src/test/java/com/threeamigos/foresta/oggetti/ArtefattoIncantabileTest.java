package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.RaritaArtefatto;
import com.threeamigos.foresta.motore.modellodati.SlotArtefatto;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtefattoIncantabileTest {

    @Test
    void armiScudiElmiEArmatureSonoIncantabili() {
        assertTrue(artefatto(TipoArtefatto.SPADA, 3).isIncantabile());
        assertTrue(artefatto(TipoArtefatto.SPADONE, 3).isIncantabile());
        assertTrue(artefatto(TipoArtefatto.BASTONE_MAGICO, 3).isIncantabile());
        assertTrue(artefatto(TipoArtefatto.SCUDO, 3).isIncantabile());
        assertTrue(artefatto(TipoArtefatto.ELMO, 3).isIncantabile());
        assertTrue(artefatto(TipoArtefatto.ARMATURA, 3).isIncantabile());
        assertTrue(artefatto(TipoArtefatto.VESTE, 3).isIncantabile());
    }

    @Test
    void accessoriLibroEPergameneNonSonoIncantabili() {
        assertFalse(artefatto(TipoArtefatto.ANELLO, 3).isIncantabile());
        assertFalse(artefatto(TipoArtefatto.TALISMANO, 3).isIncantabile());
        assertFalse(artefatto(TipoArtefatto.NINNOLO, 3).isIncantabile());
        assertFalse(artefatto(TipoArtefatto.LIBRO_MAGICO, 3).isIncantabile());
        assertFalse(artefatto(TipoArtefatto.INCANTAMENTO, 3).isIncantabile());
        assertEquals(0, artefatto(TipoArtefatto.ANELLO, 6).getEffettiMassimi());
    }

    @Test
    void effettiMassimiCresconoConIlLivelloFinoATre() {
        assertEquals(0, artefatto(TipoArtefatto.SPADA, 1).getEffettiMassimi());
        assertEquals(1, artefatto(TipoArtefatto.SPADA, 2).getEffettiMassimi());
        assertEquals(2, artefatto(TipoArtefatto.SPADA, 3).getEffettiMassimi());
        assertEquals(3, artefatto(TipoArtefatto.SPADA, 4).getEffettiMassimi());
        assertEquals(3, artefatto(TipoArtefatto.SPADA, 20).getEffettiMassimi());
    }

    @Test
    void iPostiDipendonoDallaRarita() {
        assertEquals(2, artefatto(TipoArtefatto.SPADA, 3, RaritaArtefatto.COMUNE).getEffettiMassimi());
        assertEquals(3, artefatto(TipoArtefatto.SPADA, 3, RaritaArtefatto.RARO).getEffettiMassimi());
        assertEquals(4, artefatto(TipoArtefatto.SPADA, 3, RaritaArtefatto.LEGGENDARIO).getEffettiMassimi());
        // A livello 1 un raro ha già un posto, un leggendario due
        assertEquals(0, artefatto(TipoArtefatto.SPADA, 1, RaritaArtefatto.COMUNE).getEffettiMassimi());
        assertEquals(1, artefatto(TipoArtefatto.SPADA, 1, RaritaArtefatto.RARO).getEffettiMassimi());
        assertEquals(2, artefatto(TipoArtefatto.SPADA, 1, RaritaArtefatto.LEGGENDARIO).getEffettiMassimi());
        // Tetti: 3, 4, 5
        assertEquals(3, artefatto(TipoArtefatto.SPADA, 20, RaritaArtefatto.COMUNE).getEffettiMassimi());
        assertEquals(4, artefatto(TipoArtefatto.SPADA, 20, RaritaArtefatto.RARO).getEffettiMassimi());
        assertEquals(5, artefatto(TipoArtefatto.SPADA, 20, RaritaArtefatto.LEGGENDARIO).getEffettiMassimi());
        // Un accessorio non si incanta, qualunque sia la rarità
        assertEquals(0, artefatto(TipoArtefatto.ANELLO, 5, RaritaArtefatto.LEGGENDARIO).getEffettiMassimi());
    }

    @Test
    void spadoneEArmaADueMani() {
        assertEquals(SupertipoArtefatto.ARMA, TipoArtefatto.SPADONE.getSupertipo());
        assertEquals(SlotArtefatto.ENTRAMBE_LE_MANI, TipoArtefatto.SPADONE.getSlotArtefatto());
        assertEquals(TipoDanno.TAGLIENTE, TipoArtefatto.SPADONE.getTipoDanno());
        assertTrue(Artefatto.di(artefatto(TipoArtefatto.SPADONE, 3).getModelloDati()) instanceof ArmaFisica);
    }

    @Test
    void nomeCompletoDaArtefatto() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 3);
        assertFalse(spada.getNomeProprio().isPresent());
        spada.getModelloDati().setNomeProprio("Diavolina");
        assertEquals("Diavolina", spada.getNomeProprio().orElse(null));
        assertEquals("Diavolina, la spada di fuoco, che brucia i nemici", spada.getNomeCompleto());
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello, RaritaArtefatto rarita) {
        Artefatto artefatto = artefatto(tipo, livello);
        artefatto.getModelloDati().setRarita(rarita);
        return artefatto;
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("la spada di fuoco");
        md.setDescrizione("che brucia i nemici");
        md.setLivello(livello);
        md.setPeso(1);
        return Artefatto.di(md);
    }
}
