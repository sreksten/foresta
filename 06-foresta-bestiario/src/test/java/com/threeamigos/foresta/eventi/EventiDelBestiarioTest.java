package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.interni.InternoVoceDelBestiarioSbloccata;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaBestiario;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventiDelBestiarioTest {

    @Test
    void laVoceSbloccataPortaLaVoce() {
        InternoVoceDelBestiarioSbloccata evento = new InternoVoceDelBestiarioSbloccata(TipoPersonaggio.MAGO);
        assertEquals(TipoEvento.INTERNO_VOCE_DEL_BESTIARIO_SBLOCCATA, evento.getTipoEvento());
        assertEquals(TipoPersonaggio.MAGO, evento.getVoce());
    }

    @Test
    void laRichiestaDiAperturaOffreLeSueScelte() {
        RichiestaAperturaBestiario richiesta = new RichiestaAperturaBestiario(Collections.singletonList(Comando.ANNULLA));
        assertEquals(TipoEvento.RICHIESTA_APERTURA_BESTIARIO, richiesta.getTipoEvento());
        assertEquals(Collections.singletonList(Comando.ANNULLA), richiesta.getPossibilita());
    }
}
