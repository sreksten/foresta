package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RecuperaIlMedaglione;
import com.threeamigos.foresta.missioni.RecuperaLeDerrateAlimentari;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le due missioni di recupero sui passi (gestione_missioni.md, §5): l'intermezzo del mandante in città, la
 * missione che si attiva solo dopo, e il ringraziamento al ritorno.
 */
class ScenarioMissioniDiRecuperoTest {

    @Test
    void aFleenaPrimaLIntermezzoDelMedaglionePoiLaNuovaMissione() {
        verificaIncarico(TipoLocazione.CITTA_FLEENA, RecuperaIlMedaglione.class, "medaglione di famiglia",
                "Recupera il medaglione", TipoLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE);
    }

    @Test
    void aRuunaPrimaLIntermezzoDelBorgomastroPoiLaNuovaMissione() {
        verificaIncarico(TipoLocazione.CITTA_RUUNA, RecuperaLeDerrateAlimentari.class, "derrate per l'inverno",
                "Recupera le derrate alimentari", TipoLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI);
    }

    @Test
    void alRitornoIlRingraziamentoPoiLaRicompensa() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            MissioneAPassi medaglione = (MissioneAPassi) trova(RecuperaIlMedaglione.class);
            assertEquals("RECUPERO", medaglione.getPassoCorrente());

            // Come se il medaglione fosse appena stato recuperato nella grotta, e il gruppo fosse tornato a Fleena
            medaglione.aggiungiProprieta("PASSO_CORRENTE", "RITORNO");
            int monete = partita.gruppo().getMonete();
            medaglione.controllaPreLocazione();
            assertEquals("RICOMPENSA", medaglione.getPassoCorrente());
            assertEquals("RITORNO", medaglione.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertEquals(monete, partita.gruppo().getMonete(), "le monete arrivano dopo l'intermezzo");
            assertFalse(medaglione.isCompleta());

            medaglione.controllaInLocazione();
            assertEquals(monete + 20, partita.gruppo().getMonete());
            assertTrue(medaglione.isCompleta());
        }
    }

    private static void verificaIncarico(TipoLocazione citta, Class<? extends Missione> tipo, String battutaDelMandante,
                                         String nomeMissione, TipoLocazione covo) {
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
                partita.spostaGruppoIn(citta);
                partita.eventi().ascolta(NotificaPaginaIntermezzo.class, NotificaAggiornamentoStatoMissione.class);
            });
            Missione missione = trova(tipo);
            assertTrue(missione.isAttiva());
            assertNotNull(Foresta.getCoordinateLocazioneUnica(covo), "il covo compare con l'incarico");

            List<Object> eventi = partita.eventi().inOrdine(NotificaPaginaIntermezzo.class, NotificaAggiornamentoStatoMissione.class);
            int pagina = -1;
            int nuovaMissione = -1;
            for (int i = 0; i < eventi.size(); i++) {
                Object evento = eventi.get(i);
                if (pagina < 0 && evento instanceof NotificaPaginaIntermezzo
                        && battute(((NotificaPaginaIntermezzo) evento).getPagina()).contains(battutaDelMandante)) {
                    pagina = i;
                }
                if (evento instanceof NotificaAggiornamentoStatoMissione
                        && "NUOVA MISSIONE".equals(((NotificaAggiornamentoStatoMissione) evento).getEtichetta())
                        && nomeMissione.equals(((NotificaAggiornamentoStatoMissione) evento).getDescrizione())) {
                    nuovaMissione = i;
                }
            }
            assertTrue(pagina >= 0, "l'intermezzo del mandante deve comparire");
            assertTrue(nuovaMissione > pagina, "l'avviso di nuova missione arriva dopo l'intermezzo");
        }
    }

    private static String battute(PaginaIntermezzo pagina) {
        return pagina.getBattuteProgrammate().stream().map(BattutaProgrammata::getBattuta)
                .map(b -> b.getTesto()).collect(Collectors.joining(" "));
    }

    private static Missione trova(Class<? extends Missione> tipo) {
        return RegistroMissioni.getMissioniNonCompletate().stream().filter(tipo::isInstance).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }

    @Test
    void ilCovoDeiLadriRicordaIlMedaglioneAMissioneFinita() {
        verificaRicordoDelCovo(TipoLocazione.CITTA_FLEENA, RecuperaIlMedaglione.class, TipoLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE,
                TipoLocazione.GROTTA, "In questa grotta i ladri nascondevano il medaglione rubato.");
    }

    @Test
    void ilNascondiglioDeiTrollRicordaLeDerrateAMissioneFinita() {
        verificaRicordoDelCovo(TipoLocazione.CITTA_RUUNA, RecuperaLeDerrateAlimentari.class, TipoLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI,
                TipoLocazione.ROVINE, "Fra queste rovine i Troll nascondevano le derrate di Ruuna.");
    }

    private static void verificaRicordoDelCovo(TipoLocazione citta, Class<? extends Missione> tipo, TipoLocazione covo,
                                               TipoLocazione covoRipulito, String ricordo) {
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(citta));
            MissioneAPassi missione = (MissioneAPassi) trova(tipo);
            CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(covo);
            assertSame(missione, RegistroMissioni.getMissioneCheHaOccupato(coordinate).orElse(null), "il covo è della missione");

            // Il covo ripulito torna una locazione qualsiasi, ma a missione in corso non si ricorda ancora niente
            Foresta.getLocazioneMD(coordinate).aggiungiProprieta(LocazioneMD.COMPLETA, LocazioneMD.AFFERMATIVO);
            Foresta.costruisciIstanza(coordinate).azzeraLocazione(partita.gruppo());
            assertEquals(covoRipulito, Foresta.getLocazione(coordinate));
            assertEquals(Optional.empty(), RegistroMissioni.getRicordo(coordinate));

            missione.completaMissione();
            assertEquals(Optional.of(ricordo), RegistroMissioni.getRicordo(coordinate));
        }
    }
}
