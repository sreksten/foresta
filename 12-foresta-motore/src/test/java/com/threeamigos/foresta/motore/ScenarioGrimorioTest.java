package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il grimorio, aperto dall'inventario: come il bestiario ha il solo comando ANNULLA, che torna all'inventario.
 */
class ScenarioGrimorioTest {

    private static void apriLInventario(PartitaDiTest partita) {
        partita.iniziaCon("Arsenia", Comando.FEMMINA, Comando.MAGA, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        partita.comando(Comando.ESCI_DA_CITTA);
        partita.comando(Comando.INVENTARIO);
        partita.assertStato(Stato.INVENTARIO);
    }

    @Test
    void dallInventarioAlGrimorioEIndietro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(921)) {
            apriLInventario(partita);
            partita.assertComandoDisponibile(Comando.MOSTRA_GRIMORIO);

            partita.comando(Comando.MOSTRA_GRIMORIO);
            partita.assertStato(Stato.GRIMORIO);
            assertEquals(Collections.singletonList(Comando.ANNULLA), partita.comandiDisponibili());

            partita.comando(Comando.ANNULLA);
            partita.assertStato(Stato.INVENTARIO);
            partita.assertComandoDisponibile(Comando.MOSTRA_GRIMORIO);
        }
    }

    @Test
    void unComandoDiversoDaAnnullaNelGrimorioNonSiAccetta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(922)) {
            apriLInventario(partita);
            partita.comando(Comando.MOSTRA_GRIMORIO);

            AssertionError errore = assertThrows(AssertionError.class, () -> partita.comando(Comando.MOSTRA_BESTIARIO));

            assertTrue(errore.getMessage().contains("errori"), errore.getMessage());
            partita.assertStato(Stato.GRIMORIO);
        }
    }
}
