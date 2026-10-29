package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il comando di ogni incantesimo, canto, abilità e mossa di carta, forbici e sasso, e il percorso inverso.
 */
class ComandiAssociatiTest {

    @Test
    void ognuniPoteriHaIlSuoComandoEViceversa() {
        for (TipoAbilita abilita : TipoAbilita.values()) {
            assertSame(abilita, ComandiAssociati.abilita(ComandiAssociati.di(abilita)));
        }
        assertNull(ComandiAssociati.abilita(Comando.FUGA));
        for (TipoCanto canto : TipoCanto.values()) {
            assertSame(canto, ComandiAssociati.canto(ComandiAssociati.di(canto)));
        }
        assertNull(ComandiAssociati.canto(Comando.FUGA));
        for (TipoIncantesimo incantesimo : TipoIncantesimo.values()) {
            assertSame(incantesimo, ComandiAssociati.incantesimo(ComandiAssociati.di(incantesimo)));
        }
        IllegalArgumentException errore = assertThrows(IllegalArgumentException.class, () -> ComandiAssociati.incantesimo(Comando.FUGA));
        assertTrue(errore.getMessage().contains("FUGA"), errore.getMessage());
        for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
            assertSame(mossa, ComandiAssociati.mossa(ComandiAssociati.di(mossa)));
        }
        assertNull(ComandiAssociati.mossa(Comando.FUGA));
    }
}
