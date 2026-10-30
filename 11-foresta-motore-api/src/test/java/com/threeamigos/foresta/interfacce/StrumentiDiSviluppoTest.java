package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.DovePartono;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.OggettoSpeciale;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.Scorta;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.VoceMissione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrumentiDiSviluppoTest {

    @Test
    void ogniVoceHaLaSuaDescrizione() {
        for (DovePartono dove : DovePartono.values()) {
            assertFalse(dove.getDescrizione().isEmpty());
        }
        for (Scorta scorta : Scorta.values()) {
            assertFalse(scorta.getDescrizione().isEmpty());
        }
    }

    @Test
    void laVoceDiMissionePortaIDatiCheLeSonoStatiDati() {
        MissionePredefinita classe = MissionePredefinita.values()[0];
        VoceMissione voce = new VoceMissione(classe, "nome", "descrizione", "stato", true);
        assertSame(classe, voce.getClasse());
        assertEquals("nome", voce.getNome());
        assertEquals("descrizione", voce.getDescrizione());
        assertEquals("stato", voce.getStato());
        assertTrue(voce.isForzabile());
        assertFalse(new VoceMissione(classe, "n", "d", "s", false).isForzabile());
    }

    @Test
    void gliOggettiSpecialiSiConfrontanoPerCodice() {
        OggettoSpeciale spada = new OggettoSpeciale("S", "Spada");
        assertEquals("S", spada.getCodice());
        assertEquals("Spada", spada.toString());
        assertEquals(spada, spada);
        assertEquals(spada, new OggettoSpeciale("S", "Altro nome"));
        assertEquals(spada.hashCode(), new OggettoSpeciale("S", "Altro nome").hashCode());
        assertNotEquals(spada, new OggettoSpeciale("A", "Spada"));
        assertNotEquals(spada, "S");
        assertNotEquals(spada, null);
    }
}
