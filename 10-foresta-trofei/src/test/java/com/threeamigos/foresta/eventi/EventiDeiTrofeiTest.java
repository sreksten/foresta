package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.interni.InternoTrofeoAcquisito;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaTrofei;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventiDeiTrofeiTest {

    @Test
    void ilTrofeoAcquisitoPortaIlTrofeo() {
        InternoTrofeoAcquisito evento = new InternoTrofeoAcquisito(TipoTrofeo.SBEVAZZONE);
        assertEquals(TipoEvento.INTERNO_TROFEO_ACQUISITO, evento.getTipoEvento());
        assertEquals(TipoTrofeo.SBEVAZZONE, evento.getTrofeo());
    }

    @Test
    void laRichiestaDiAperturaOffreLeSueScelte() {
        RichiestaAperturaTrofei richiesta = new RichiestaAperturaTrofei(Collections.singletonList(Comando.ANNULLA));
        assertEquals(TipoEvento.RICHIESTA_APERTURA_TROFEI, richiesta.getTipoEvento());
        assertEquals(Collections.singletonList(Comando.ANNULLA), richiesta.getPossibilita());
    }
}
