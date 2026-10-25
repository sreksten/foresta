package com.threeamigos.foresta.modificatori;

import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un modificatore che sta agendo, con la fonte da cui viene.
 */
class ModificatoreAttivoTest {

    private static final ModificatoreAttributoMD FORZA = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2);

    @Test
    void portaIlModificatoreEDaDoveViene() {
        ModificatoreAttivo attivo = new ModificatoreAttivo(FORZA, "la spada del drago");
        assertSame(FORZA, attivo.getModificatore());
        assertEquals("la spada del drago", attivo.getFonte());
    }

    @Test
    void unaFonteMancanteEVuota() {
        assertEquals("", new ModificatoreAttivo(FORZA, null).getFonte());
    }
}
