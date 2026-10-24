package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chi è stordito, congelato o atterrato non agisce neanche con i comandi manuali: non si propone fra chi può attaccare, formulare o bere, e se
 * nessuno può agire il combattimento procede da solo (con la fuga sempre fra i comandi) finché qualcuno si riprende.
 */
class ScenarioIncapaciDiAgireTest {

    private static PartitaDiTest partitaInCombattimento(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
        partita.assertStato(Stato.IN_LOCAZIONE);
        return partita;
    }

    @Test
    void chiEIncapaceNonSiPropone() {
        try (PartitaDiTest partita = partitaInCombattimento(296)) {
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Secondo", 1));
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Terzo", 1));
            partita.gruppo().getPersonaggi().get(1).addEffettoDiStato(TipoEffettoDiStato.STORDITO, 3, 0);

            partita.comando(Comando.SINGOLO_ATTACCO);

            Collection<Comando> scelta = partita.comandiDisponibili();
            assertTrue(scelta.contains(Comando.PERSONAGGIO_1), String.valueOf(scelta));
            assertFalse(scelta.contains(Comando.PERSONAGGIO_2), "lo stordito non si propone: " + scelta);
            assertTrue(scelta.contains(Comando.PERSONAGGIO_3), String.valueOf(scelta));
        }
    }

    @Test
    void seNessunoPuoAgireIlCombattimentoProcedeDaSoloELaFugaResta() {
        try (PartitaDiTest partita = partitaInCombattimento(297)) {
            Personaggio capo = partita.gruppo().getCapo();
            capo.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 2, 0);

            partita.comando(Comando.COMBATTIMENTO);

            partita.assertStato(Stato.IN_COMBATTIMENTO);
            Collection<Comando> comandi = partita.comandiDisponibili();
            assertTrue(comandi.contains(Comando.FUGA), String.valueOf(comandi));
            for (Comando azione : new Comando[]{Comando.SINGOLO_ATTACCO, Comando.COMBATTIMENTO, Comando.INCANTESIMO, Comando.POZIONE_SALUTE,
                    Comando.INTERRUZIONE_COMBATTIMENTO}) {
                assertFalse(comandi.contains(azione), azione + " non si può dare: " + comandi);
            }
            assertTrue(partita.testi().stream().anyMatch(t -> t.endsWith("non è in grado di agire.")), String.valueOf(partita.testi()));

            // Il battito passa i turni da solo finché lo stordimento finisce (o il capo cade)
            for (int i = 0; i < 60 && partita.stato() == Stato.IN_COMBATTIMENTO; i++) {
                partita.scatta();
            }
            assertNotEquals(Stato.IN_COMBATTIMENTO, partita.stato());
            if (partita.stato() == Stato.IN_LOCAZIONE) {
                assertFalse(capo.isIncapaceDiAgire());
                assertTrue(partita.comandiDisponibili().contains(Comando.SINGOLO_ATTACCO));
            }
        }
    }
}
