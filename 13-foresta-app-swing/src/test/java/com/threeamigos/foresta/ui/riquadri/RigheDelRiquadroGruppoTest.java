package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.tipi.TipoAttributo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lo sprite di una variazione sta sulla riga in cui il riquadro del Gruppo scrive il valore: salute, magia e livello sulla seconda,
 * coraggio, valore, stanchezza e carisma sulla terza.
 */
class RigheDelRiquadroGruppoTest {

    @Test
    void saluteMagiaELivelloStannoSullaPrimaRigaDopoIlNome() {
        for (TipoAttributo attributo : new TipoAttributo[]{TipoAttributo.SALUTE, TipoAttributo.SALUTE_MASSIMA, TipoAttributo.MAGIA,
                TipoAttributo.MAGIA_MASSIMA, TipoAttributo.LIVELLO}) {
            assertEquals(1, DisplayableCanvasRiquadroGruppo.rigaDelValore(attributo), attributo.name());
        }
    }

    @Test
    void coraggioValoreStanchezzaECarismaStannoSullaRigaSeguente() {
        for (TipoAttributo attributo : new TipoAttributo[]{TipoAttributo.CORAGGIO, TipoAttributo.VALORE, TipoAttributo.STANCHEZZA,
                TipoAttributo.CARISMA}) {
            assertEquals(2, DisplayableCanvasRiquadroGruppo.rigaDelValore(attributo), attributo.name());
        }
    }

    @Test
    void unAttributoChePiuNonSiMostraSiRifiuta() {
        assertThrows(IllegalArgumentException.class, () -> DisplayableCanvasRiquadroGruppo.rigaDelValore(TipoAttributo.FORZA));
    }
}
