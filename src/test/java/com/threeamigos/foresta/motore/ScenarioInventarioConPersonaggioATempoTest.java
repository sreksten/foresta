package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Gigante;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Nell'inventario si scelgono solo i personaggi del gruppo: chi è a tempo (un gigante che accompagna il gruppo per un
 * po') non ha un inventario, e il suo comando non viene nemmeno offerto.
 */
class ScenarioInventarioConPersonaggioATempoTest {

    private static Gigante giganteATempo() {
        Gigante gigante = new Gigante(1);
        gigante.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
        gigante.setTempo(3);
        return gigante;
    }

    @Test
    void ilPersonaggioATempoNonEnellInventario() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(231)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            for (int i = 1; i < 5; i++) {
                partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno" + i, 1));
            }
            partita.gruppo().aggiungiPersonaggio(giganteATempo());
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.comando(Comando.INVENTARIO);

            assertEquals(Arrays.asList(Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2, Comando.PERSONAGGIO_3,
                    Comando.PERSONAGGIO_4, Comando.PERSONAGGIO_5, Comando.MOSTRA_TROFEI, Comando.ANNULLA),
                    partita.comandiDisponibili());
        }
    }

    @Test
    void ilComandoDiUnPersonaggioDelGruppoDopoUnoATempoRestaQuelloDellaSuaPosizione() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(232)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.gruppo().aggiungiPersonaggio(giganteATempo());
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Dopo", 1));
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.comando(Comando.INVENTARIO);

            List<Comando> attesi = Arrays.asList(Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_3, Comando.MOSTRA_TROFEI, Comando.ANNULLA);
            assertEquals(attesi, partita.comandiDisponibili());
            partita.comando(Comando.PERSONAGGIO_3);
            partita.assertStato(Stato.INVENTARIO);
        }
    }
}
