package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.missioni.IlCampione;
import com.threeamigos.foresta.missioni.LaSfidaDeiCampioni;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La missione La sfida dei campioni (vedi carta_forbici_sasso.md): quattro campioni, ognuno con il suo posto segnato sulla
 * mappa, da battere a carta, forbici e sasso; sul posto dell'ultimo, monete e un leggendario.
 */
class ScenarioSfidaDeiCampioniTest {

    private static final Set<Comando> LE_TRE_MOSSE = new HashSet<>(Arrays.asList(Comando.CARTA, Comando.FORBICE, Comando.SASSO));

    private static LaSfidaDeiCampioni avviaLaMissione(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        partita.assertStato(Stato.IN_LOCAZIONE);
        LaSfidaDeiCampioni missione = LaSfidaDeiCampioni.avvia();
        assertTrue(missione.isAttiva());
        // I quattro campioni si affidano a fine locazione, e ognuno trova il suo posto al controllo in locazione
        missione.controllaPostLocazione();
        assertEquals(LaSfidaDeiCampioni.CAMPIONI, missione.getCampioni().size());
        missione.getCampioni().forEach(Missione::controllaInLocazione);
        return missione;
    }

    @Test
    void appenaPartitaLaMissioneSiVedeLaNotificaEISegnaliniSonoGiaSullaMappa() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(7)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.eventi().ascolta(NotificaAggiornamentoStatoMissione.class);

            // Senza altri controlli di missione: tutto succede in avvia()
            LaSfidaDeiCampioni missione = LaSfidaDeiCampioni.avvia();

            assertTrue(partita.eventi().tutti(NotificaAggiornamentoStatoMissione.class).stream()
                    .anyMatch(n -> "NUOVA MISSIONE".equals(n.getEtichetta())), "la notifica di nuova missione");
            assertEquals(4, missione.getCampioni().size());
            for (Missione m : missione.getCampioni()) {
                CoordinateMD posto = ((IlCampione) m).getPosto();
                assertNotNull(posto);
                assertTrue(Foresta.getCoordinateDaSegnalare().contains(posto), "il segnalino c'è subito");
            }
        }
    }

    @Test
    void iQuattroCampioniHannoUnPostoSegnatoSullaMappaCiascuno() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(7)) {
            LaSfidaDeiCampioni missione = avviaLaMissione(partita);

            Set<TipoLocazione> tipi = new HashSet<>();
            Set<CoordinateMD> posti = new HashSet<>();
            for (Missione m : missione.getCampioni()) {
                IlCampione campione = (IlCampione) m;
                assertNotNull(campione.getPosto(), campione.getNome());
                assertEquals(campione.getLuogo(), Foresta.getLocazione(campione.getPosto()));
                assertTrue(Foresta.isLocazioneConosciuta(campione.getPosto()));
                assertTrue(Foresta.getCoordinateDaSegnalare().contains(campione.getPosto()), "lampeggia sulla mappa");
                assertTrue(LaSfidaDeiCampioni.classiAmichevoli().contains(campione.getClasseDelCampione()));
                assertFalse(campione.getCampione().isEmpty());
                tipi.add(campione.getLuogo());
                posti.add(campione.getPosto());
                assertTrue(campione.getDescrizione().contains(campione.ilCampione() + ", a carta, forbici e sasso"), campione.getDescrizione());
            }
            assertEquals(4, tipi.size(), "quattro tipi di posto diversi");
            assertEquals(4, posti.size(), "quattro posti diversi");
            assertTrue(missione.getDescrizione().contains("0 battuti su 4"), missione.getDescrizione());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains(", aspetta ")), "il posto si annuncia con la virgola: "
                    + partita.testi());
        }
    }

    @Test
    void arrivatiDalCampioneSiGiocaSubitoESoloACartaForbiciESasso() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(7)) {
            LaSfidaDeiCampioni missione = avviaLaMissione(partita);
            partita.comando(Comando.ESCI_DA_CITTA);
            IlCampione campione = (IlCampione) missione.getCampioni().get(0);
            vaiDa(partita, campione.getPosto());

            partita.assertStato(Stato.IN_LOCAZIONE);
            assertEquals(LE_TRE_MOSSE, new HashSet<>(partita.comandiDisponibili()));
            assertNull(partita.gruppo().getLocazioneCorrente().getOggetto(), "dal campione non ci sono oggetti");
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("dice: \"")), String.valueOf(partita.testi()));

            // Tre mani vinte: la carta batte il sasso (il dado dice 3)
            Dado.trucca(3, 3, 3);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);

            partita.assertStato(Stato.SCELTA_DIREZIONE);
            assertTrue(campione.isCompleta(), "il campione è battuto");
            assertFalse(Foresta.getCoordinateDaSegnalare().contains(campione.getPosto()), "il suo posto non lampeggia più");
            assertTrue(missione.getDescrizione().contains("1 battuti su 4"), missione.getDescrizione());
            assertFalse(missione.isCompleta());
            // La sfida dei campioni non conta come amicizia per la partita: nessuna sfida vinta in più per il torneo
            assertEquals(0, partita.gruppo().getSfideVinte());
        }
    }

    @Test
    void sePerdeIlCampioneRestaELaLocazioneNonSiCompleta() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(7)) {
            LaSfidaDeiCampioni missione = avviaLaMissione(partita);
            partita.comando(Comando.ESCI_DA_CITTA);
            IlCampione campione = (IlCampione) missione.getCampioni().get(0);
            vaiDa(partita, campione.getPosto());

            // Il sasso contro la carta (il dado dice 1): tre mani perse
            Dado.trucca(1, 1, 1);
            partita.comando(Comando.SASSO);
            partita.comando(Comando.SASSO);
            partita.comando(Comando.SASSO);

            partita.assertStato(Stato.SCELTA_DIREZIONE);
            assertFalse(campione.isCompleta());
            assertFalse(campione.isFallita());
            assertEquals(campione.getPosto(), com.threeamigos.foresta.motore.RegistroMissioni.getLocazioneOccupata(campione));
            assertTrue(Foresta.getCoordinateDaSegnalare().contains(campione.getPosto()), "il posto resta segnato: si può riprovare");
            assertFalse(Foresta.isLocazioneVisitata(campione.getPosto()), "la locazione resta non visitata");
        }
    }

    @Test
    void sulPostoDelQuartoCampioneMoneteELeggendario() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(7)) {
            LaSfidaDeiCampioni missione = avviaLaMissione(partita);
            int livello = Statistiche.getLivello();
            int monete = partita.gruppo().getMonete();
            int artefatti = partita.gruppo().getInventario().size();
            assertEquals(100 * livello, missione.getMonete());
            assertNotNull(missione.getLeggendario());

            List<Missione> campioni = missione.getCampioni();
            for (int i = 0; i < campioni.size() - 1; i++) {
                campioni.get(i).completaMissione();
                missione.controllaPostLocazione();
                assertFalse(missione.isCompleta(), "ne mancano ancora");
            }
            assertEquals(monete, partita.gruppo().getMonete());
            campioni.get(campioni.size() - 1).completaMissione();
            missione.controllaPostLocazione();

            assertTrue(missione.isCompleta());
            assertEquals(monete + 100 * livello, partita.gruppo().getMonete());
            assertEquals(artefatti + 1, partita.gruppo().getInventario().size(), "il leggendario è nell'inventario");

            // Il trofeo si conquista a fine locazione
            partita.pubblica(new InternoFineLocazione());
            assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.RE_DI_CARTA_FORBICI_E_SASSO));
        }
    }

    @Test
    void ilLeggendarioSiCostruisceAlVoloQuandoIlCatalogoEFinito() {
        OggettoLeggendario costruito = LaSfidaDeiCampioni.costruisciUnLeggendario(7);
        assertFalse(costruito.getNome().isEmpty());
        assertEquals(7, costruito.costruisci().getLivello());
        assertEquals(2, costruito.getLeggenda().size());
    }

    @Test
    void ilContoDelleSfideEIQuattroCampioniSopravvivonoAlSalvataggio() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(7)) {
            LaSfidaDeiCampioni missione = avviaLaMissione(partita);
            partita.gruppo().addSfidaVinta();
            partita.gruppo().addSfidaVinta();
            partita.gruppo().addSfidaVinta();
            String nomeDelPrimo = ((IlCampione) missione.getCampioni().get(0)).getCampione();
            CoordinateMD postoDelPrimo = ((IlCampione) missione.getCampioni().get(0)).getPosto();

            partita.salva(Comando.NUMERO_2);
            assertTrue(partita.leggi(Comando.NUMERO_2));

            assertEquals(3, partita.gruppo().getSfideVinte());
            assertTrue(LaSfidaDeiCampioni.isPartita());
            LaSfidaDeiCampioni riletta = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(LaSfidaDeiCampioni.class::isInstance).map(LaSfidaDeiCampioni.class::cast).findFirst()
                    .orElseThrow(AssertionError::new);
            assertEquals(4, riletta.getCampioni().size());
            IlCampione primo = (IlCampione) riletta.getCampioni().get(0);
            assertEquals(nomeDelPrimo, primo.getCampione());
            assertEquals(postoDelPrimo, primo.getPosto());
            assertNotNull(riletta.getLeggendario());
        }
    }

    /**
     * Mette il gruppo accanto a quella casella e ci arriva con un passo.
     */
    private static void vaiDa(PartitaDiTest partita, CoordinateMD posto) {
        int[][] direzioni = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        Comando[] comandi = {Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST};
        for (int i = 0; i < direzioni.length; i++) {
            int x = posto.getX() - direzioni[i][0];
            int y = posto.getY() - direzioni[i][1];
            if (x >= 0 && y >= 0 && x < Foresta.getDimensioneX() && y < Foresta.getDimensioneY()) {
                partita.gruppo().setCoordinate(new CoordinateMD(x, y));
                Foresta.aggiornaMappaCircostante(partita.gruppo());
                partita.comando(comandi[i]);
                if (partita.comandiDisponibili().contains(Comando.NUMERO_1)) {
                    partita.comando(Comando.NUMERO_1);
                }
                return;
            }
        }
        fail("Nessuna casella accanto a " + posto);
    }
}
