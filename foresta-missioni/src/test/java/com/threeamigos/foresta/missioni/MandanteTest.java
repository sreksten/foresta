package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.Mandante;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MandanteTest {

    @Test
    void ilCapitanoSiPresentaConIlSuoNome() {
        assertTrue(Mandante.CAPITANO.getApertura("Rodolfo").endsWith("Sono il capitano delle guardie, Rodolfo, e ho bisogno di prove."),
                Mandante.CAPITANO.getApertura("Rodolfo"));
    }

    @Test
    void senzaNomeLaBattutaEQuellaDiSempre() {
        assertTrue(Mandante.CAPITANO.getApertura(null).endsWith("Sono il capitano delle guardie, e ho bisogno di prove."),
                Mandante.CAPITANO.getApertura(null));
    }

    @Test
    void gliAltriMandantiNonCambiano() {
        assertEquals("Buongiorno, viaggiatori. Non è che andate in giro per la foresta?", Mandante.ALCHIMISTA.getApertura("Ferruccio"));
    }
}
