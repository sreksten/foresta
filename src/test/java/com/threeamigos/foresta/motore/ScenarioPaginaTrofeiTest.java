package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.tipi.TipoTrofeo;
import com.threeamigos.foresta.trofei.ClasseTrofeo;
import com.threeamigos.foresta.trofei.Trofeo;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La pagina dei trofei: si apre dall'inventario con MOSTRA_TROFEI, offre solo ANNULLA e con quello si torna
 * all'inventario. Ogni trofeo sa quanto serve per vincerlo e a che punto è.
 */
class ScenarioPaginaTrofeiTest {

    @Test
    void dallInventarioAllaPaginaDeiTrofeiEIndietro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(231)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.assertComandoDisponibile(Comando.INVENTARIO);
            partita.comando(Comando.INVENTARIO);
            partita.assertStato(Stato.INVENTARIO);
            partita.assertComandoDisponibile(Comando.MOSTRA_TROFEI);

            partita.comando(Comando.MOSTRA_TROFEI);
            partita.assertStato(Stato.TROFEI);
            assertEquals(Collections.singletonList(Comando.ANNULLA), partita.comandiDisponibili());

            partita.comando(Comando.ANNULLA);
            partita.assertStato(Stato.INVENTARIO);
            partita.assertComandoDisponibile(Comando.MOSTRA_TROFEI);
        }
    }

    @Test
    void ogniTrofeoSaQuantoServeEAChePuntoE() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(232)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            for (TipoTrofeo tipo : TipoTrofeo.values()) {
                Trofeo trofeo = ClasseTrofeo.di(tipo);
                assertEquals(tipo, trofeo.getTipo());
                assertTrue(trofeo.getObiettivo() >= 1, tipo.name());
                assertTrue(trofeo.getProgresso() >= 0 && trofeo.getProgresso() <= trofeo.getObiettivo(), tipo.name());
            }
            assertEquals(TipoTrofeo.values().length - 1, ClasseTrofeo.di(TipoTrofeo.PERDIGIORNO).getObiettivo());
            assertEquals(1, ClasseTrofeo.di(TipoTrofeo.UCCIDI_IL_DRAGO).getObiettivo());
        }
    }
}
