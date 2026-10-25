package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.ComandiAssociati;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.Silenzio;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'incantesimo del Silenzio: mette SILENZIATO su un avversario. Su un gruppo sceglie il primo non ancora silenziato,
 * altrimenti quello il cui silenzio dura meno.
 */
class SilenzioTest {

    @BeforeEach
    void preparaModello() {
        com.threeamigos.foresta.modellodati.ModelloDati.setIstanza(new com.threeamigos.foresta.modellodati.ModelloDati());
    }

    @Test
    void ilComandoEIlPrezzoSonoQuelliDelSilenzio() {
        assertEquals(TipoIncantesimo.SILENZIO, ComandiAssociati.incantesimo(Comando.SILENZIO));
        assertEquals(5, TipoIncantesimo.SILENZIO.getCostoAcquisto());
        assertEquals(CostantiIncantesimi.INCANTESIMO_SILENZIO_COSTO_LANCIO, TipoIncantesimo.SILENZIO.getCostoLancio());
        assertEquals(TipoIncantesimo.SILENZIO, FabbricaIncantesimi.crea(TipoIncantesimo.SILENZIO, 1).getClasse());
    }

    @Test
    void suUnGruppoSiSceglieIlPrimoNonSilenziato() {
        Personaggio a = new Guerriero("A", 1);
        Personaggio b = new Guerriero("B", 1);
        Personaggio c = new Guerriero("C", 1);
        a.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 5, 0);
        assertSame(b, Silenzio.scegliBersaglio(Arrays.asList(a, b, c)));
    }

    @Test
    void seSonoTuttiSilenziatiSiSceglieChiHaIlSilenzioPiuCorto() {
        Personaggio a = new Guerriero("A", 1);
        Personaggio b = new Guerriero("B", 1);
        Personaggio c = new Guerriero("C", 1);
        a.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 5, 0);
        b.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 2, 0);
        c.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 4, 0);
        assertSame(b, Silenzio.scegliBersaglio(Arrays.asList(a, b, c)));
        assertNull(Silenzio.scegliBersaglio(Collections.emptyList()));
    }

    @Test
    void suUnPersonaggioSilenziaProprioLui() {
        Personaggio mago = new Mago("Merlino", 20);
        Personaggio bersaglio = new Guerriero("B", 1);
        for (int i = 0; i < 100 && !bersaglio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO); i++) {
            new Silenzio(20).formula(mago, bersaglio, null);
        }
        assertTrue(bersaglio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO));
    }
}
