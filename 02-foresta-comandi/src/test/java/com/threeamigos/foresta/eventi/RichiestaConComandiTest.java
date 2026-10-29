package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoEvento;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RichiestaConComandiTest {

    @Test
    void portaIlSuoTipoELeScelteCheOffre() {
        Collection<Comando> scelte = Arrays.asList(Comando.SI, Comando.NO);
        RichiestaConComandi richiesta = new RichiestaConComandi(TipoEvento.RICHIESTA_APERTURA_TROFEI, scelte);
        assertEquals(TipoEvento.RICHIESTA_APERTURA_TROFEI, richiesta.getTipoEvento());
        assertSame(scelte, richiesta.getPossibilita());
    }
}
