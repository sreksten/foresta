package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioGruppo;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoScambioArtefatto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpesaPuntoAbilita;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nell'inventario la UI vede solo una VistaScambio e cambia le cose solo con i comandi sul bus: sposta gli artefatti
 * con ComandoScambioArtefatto e spende i punti abilità con ComandoSpesaPuntoAbilita.
 */
class ScenarioScambiAEventiTest {

    @Test
    void nellInventarioSiSpostanoGliArtefattiESiSpendonoIPuntiConIComandi() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(231)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.eventi().ascolta(ComandoAperturaInventarioGruppo.class);
            partita.comando(Comando.INVENTARIO);
            ComandoAperturaInventarioGruppo apertura = partita.eventi().ultimo(ComandoAperturaInventarioGruppo.class);
            VistaScambio scambio = apertura.getScambio();
            Personaggio personaggio = apertura.getPersonaggio();
            assertSame(partita.gruppo().getCapo(), personaggio);

            // Dal personaggio al gruppo e ritorno
            Artefatto artefatto = new ArrayList<>(scambio.getInventarioParteAttiva()).get(0);
            partita.pubblica(new ComandoScambioArtefatto(scambio, ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, artefatto));
            assertTrue(PartitaDiTest.contiene(partita.gruppo().getInventario(), artefatto));
            assertFalse(PartitaDiTest.contiene(personaggio.getInventario(), artefatto));
            // Come la UI, si prende l'artefatto dall'elenco mostrato: il gruppo lo ricrea dal suo modello dati
            Artefatto nelGruppo = scambio.getInventarioParteRemota().stream()
                    .filter(a -> a.getModelloDati() == artefatto.getModelloDati()).findFirst().orElseThrow(AssertionError::new);
            partita.pubblica(new ComandoScambioArtefatto(scambio, ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA, nelGruppo));
            assertTrue(PartitaDiTest.contiene(personaggio.getInventario(), artefatto));
            assertFalse(PartitaDiTest.contiene(scambio.getInventarioParteRemota(), artefatto));

            // Un punto abilità sulla forza
            personaggio.getModelloDati().setPuntiAbilitaDisponibili(1);
            int forza = personaggio.getForza();
            partita.pubblica(new ComandoSpesaPuntoAbilita(personaggio, TipoAttributo.FORZA));
            assertEquals(0, personaggio.getPuntiAbilitaDisponibili());
            assertTrue(personaggio.getForza() > forza, "forza " + personaggio.getForza());
        }
    }
}
