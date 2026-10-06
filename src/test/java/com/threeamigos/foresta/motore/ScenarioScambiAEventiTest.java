package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaInventarioGruppo;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoCommutazioneElenco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoScambioArtefatto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpesaPuntoAbilita;
import com.threeamigos.foresta.interfacce.VistaArtefatto;
import com.threeamigos.foresta.interfacce.VistaMissione;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaScambio;
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
            partita.eventi().ascolta(RichiestaAperturaInventarioGruppo.class);
            partita.comando(Comando.INVENTARIO);
            RichiestaAperturaInventarioGruppo apertura = partita.eventi().ultimo(RichiestaAperturaInventarioGruppo.class);
            VistaScambio scambio = apertura.getScambio();
            Personaggio personaggio = partita.gruppo().getCapo();
            assertSame(personaggio, apertura.getPersonaggio());

            // Dal personaggio al gruppo e ritorno
            VistaArtefatto artefatto = new ArrayList<>(scambio.getInventarioParteAttiva()).get(0);
            partita.pubblica(new ComandoScambioArtefatto(scambio.getId(), ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, artefatto.getUuid()));
            assertTrue(PartitaDiTest.contiene(partita.gruppo().getInventario(), artefatto));
            assertFalse(PartitaDiTest.contiene(personaggio.getInventario(), artefatto));
            // Come la UI, si prende l'artefatto dall'elenco mostrato: il gruppo lo ricrea dal suo modello dati
            VistaArtefatto nelGruppo = scambio.getInventarioParteRemota().stream()
                    .filter(a -> a.getUuid().equals(artefatto.getUuid())).findFirst().orElseThrow(AssertionError::new);
            partita.pubblica(new ComandoScambioArtefatto(scambio.getId(), ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA, nelGruppo.getUuid()));
            assertTrue(PartitaDiTest.contiene(personaggio.getInventario(), artefatto));
            assertFalse(PartitaDiTest.contiene(scambio.getInventarioParteRemota(), artefatto));

            // Un punto abilità sulla forza
            personaggio.getModelloDati().setPuntiAbilitaDisponibili(1);
            int forza = personaggio.getForza();
            partita.pubblica(new ComandoSpesaPuntoAbilita(personaggio.getUuid(), TipoAttributo.FORZA));
            assertEquals(0, personaggio.getPuntiAbilitaDisponibili());
            assertTrue(personaggio.getForza() > forza, "forza " + personaggio.getForza());
        }
    }

    @Test
    void elenchiDiArtefattiEMissioniSiApronoESiChiudonoConUnComando() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(231)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            VistaPartita vista = new VistaPartitaMotore();
            VistaScambio scambio = apriInventario(partita);

            VistaArtefatto artefatto = new ArrayList<>(scambio.getInventarioParteAttiva()).get(0);
            assertTrue(artefatto.isFigliVisibili());
            partita.pubblica(ComandoCommutazioneElenco.artefatto(scambio.getId(), artefatto.getUuid()));
            assertFalse(artefatto.isFigliVisibili());
            partita.pubblica(ComandoCommutazioneElenco.artefatto(scambio.getId(), artefatto.getUuid()));
            assertTrue(artefatto.isFigliVisibili());

            VistaMissione missione = vista.getMissioniAttive().get(0);
            assertTrue(missione.isDescrizioneVisibile());
            partita.pubblica(ComandoCommutazioneElenco.missione(missione.getId()));
            assertFalse(missione.isDescrizioneVisibile());
        }
    }

    @Test
    void unComandiConIdentificativiCheNonCorrispondonoANienteSiIgnorano() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(231)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            VistaScambio scambio = apriInventario(partita);
            Personaggio personaggio = partita.gruppo().getCapo();
            VistaArtefatto artefatto = new ArrayList<>(scambio.getInventarioParteAttiva()).get(0);
            int nelPersonaggio = personaggio.getInventario().size();
            int nelGruppo = partita.gruppo().getInventario().size();

            // Uno scambio che non c'è, un artefatto che non c'è, nessun identificativo
            partita.pubblica(new ComandoScambioArtefatto("non-esiste", ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, artefatto.getUuid()));
            partita.pubblica(new ComandoScambioArtefatto(scambio.getId(), ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, "non-esiste"));
            partita.pubblica(new ComandoScambioArtefatto(null, ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, null));
            // Un artefatto della parte sbagliata: sta nel personaggio, non nel gruppo
            partita.pubblica(new ComandoScambioArtefatto(scambio.getId(), ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA, artefatto.getUuid()));
            assertEquals(nelPersonaggio, personaggio.getInventario().size());
            assertEquals(nelGruppo, partita.gruppo().getInventario().size());

            partita.pubblica(ComandoCommutazioneElenco.artefatto("non-esiste", artefatto.getUuid()));
            partita.pubblica(ComandoCommutazioneElenco.artefatto(scambio.getId(), "non-esiste"));
            partita.pubblica(ComandoCommutazioneElenco.missione("non-esiste"));
            assertTrue(artefatto.isFigliVisibili());

            personaggio.getModelloDati().setPuntiAbilitaDisponibili(1);
            int forza = personaggio.getForza();
            partita.pubblica(new ComandoSpesaPuntoAbilita("non-esiste", TipoAttributo.FORZA));
            partita.pubblica(new ComandoSpesaPuntoAbilita(null, TipoAttributo.FORZA));
            assertEquals(1, personaggio.getPuntiAbilitaDisponibili());
            assertEquals(forza, personaggio.getForza());
        }
    }

    private static VistaScambio apriInventario(PartitaDiTest partita) {
        partita.comando(Comando.ESCI_DA_CITTA);
        partita.eventi().ascolta(RichiestaAperturaInventarioGruppo.class);
        partita.comando(Comando.INVENTARIO);
        return partita.eventi().ultimo(RichiestaAperturaInventarioGruppo.class).getScambio();
    }
}
