package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.LaSfidaDeiCampioni;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.threeamigos.foresta.tipi.MossaCartaForbiciSasso.*;
import static org.junit.jupiter.api.Assertions.*;

class PartitaCartaForbiciSassoTest {

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    @Test
    void cartaBatteSassoSassoBatteForbiciForbiciBattonoCarta() {
        assertTrue(CARTA.batte(SASSO));
        assertTrue(SASSO.batte(FORBICE));
        assertTrue(FORBICE.batte(CARTA));
        for (MossaCartaForbiciSasso mossa : values()) {
            assertFalse(mossa.batte(mossa));
        }
        assertFalse(SASSO.batte(CARTA));
        assertFalse(FORBICE.batte(SASSO));
        assertFalse(CARTA.batte(FORBICE));
    }

    @Test
    void l_ombrafiammaESegretaENonPartecipa() {
        assertFalse(MossaCartaForbiciSasso.puoGiocare(TipoPersonaggio.OMBRAFIAMMA));
        assertTrue(MossaCartaForbiciSasso.puoGiocare(TipoPersonaggio.GUERRIERO));
        assertFalse(LaSfidaDeiCampioni.classiAmichevoli().contains(TipoPersonaggio.OMBRAFIAMMA));
        assertFalse(LaSfidaDeiCampioni.classiAmichevoli().isEmpty());
    }

    @Test
    void ogniMossaHaIlSuoComando() {
        assertSame(CARTA, MossaCartaForbiciSasso.da(Comando.CARTA));
        assertSame(FORBICE, MossaCartaForbiciSasso.da(Comando.FORBICE));
        assertSame(SASSO, MossaCartaForbiciSasso.da(Comando.SASSO));
        assertNull(MossaCartaForbiciSasso.da(Comando.FUGA));
    }

    @Test
    void sivinceArrivandoATreMani() {
        // Il dado dà 1 = carta, 2 = forbici, 3 = sasso: il giocatore gioca sempre carta, l'avversario sempre sasso
        Dado.trucca(3, 3, 3);
        PartitaCartaForbiciSasso partita = new PartitaCartaForbiciSasso();
        assertFalse(partita.gioca(CARTA).isPareggio());
        assertFalse(partita.isFinita());
        partita.gioca(CARTA);
        assertEquals(2, partita.getVittorieDelGiocatore());
        assertFalse(partita.isFinita());
        partita.gioca(CARTA);
        assertTrue(partita.isFinita());
        assertTrue(partita.haVintoIlGiocatore());
        assertEquals(0, partita.getVittorieDellAvversario());
    }

    @Test
    void sePerdeLAvversarioArrivaATre() {
        Dado.trucca(2, 2, 2);
        PartitaCartaForbiciSasso partita = new PartitaCartaForbiciSasso();
        for (int i = 0; i < 3; i++) {
            partita.gioca(CARTA);
        }
        assertTrue(partita.isFinita());
        assertFalse(partita.haVintoIlGiocatore());
        assertEquals(3, partita.getVittorieDellAvversario());
    }

    @Test
    void ilPareggioNonContaESiRigioca() {
        Dado.trucca(1, 1, 3, 1, 3, 3);
        PartitaCartaForbiciSasso partita = new PartitaCartaForbiciSasso();
        assertTrue(partita.gioca(CARTA).isPareggio());
        assertTrue(partita.gioca(CARTA).isPareggio());
        assertEquals(0, partita.getVittorieDelGiocatore());
        assertEquals(0, partita.getVittorieDellAvversario());
        partita.gioca(CARTA);
        assertEquals(1, partita.getVittorieDelGiocatore());
    }

    @Test
    void dopoLaFineNonSiGiocaPiu() {
        Dado.trucca(3, 3, 3, 3);
        PartitaCartaForbiciSasso partita = new PartitaCartaForbiciSasso();
        for (int i = 0; i < 3; i++) {
            partita.gioca(CARTA);
        }
        assertThrows(IllegalStateException.class, () -> partita.gioca(CARTA));
    }
}
