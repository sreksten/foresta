package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.tipi.TipoLocazione;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.eventi.interni.InternoAmiciziaStretta;
import com.threeamigos.foresta.eventi.interni.InternoSfidaCartaForbiciSasso;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.oggetti.FabbricaOggetti;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La sfida a carta, forbici e sasso (vedi carta_forbici_sasso.md): chi stringe amicizia viene a volte sfidato invece di
 * ricevere un'offerta, si gioca a tre mani vinte con i soli comandi carta, forbici e sasso, e alla terza sfida vinta
 * dalla partita parte la missione La sfida dei campioni.
 */
class ScenarioCartaForbiciSassoTest {

    private static final Set<Comando> LE_TRE_MOSSE = new HashSet<>(Arrays.asList(Comando.CARTA, Comando.FORBICE, Comando.SASSO));

    /**
     * Una partita in una locazione dove si può stringere amicizia (come in TrofeiTest), con il carisma che basta e il
     * 33% che scatta: l'avversario sfida.
     */
    private static PartitaDiTest sfidataDaLIdra(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
        partita.assertStato(Stato.IN_LOCAZIONE);
        partita.eventi().ascolta(InternoSfidaCartaForbiciSasso.class);
        partita.eventi().ascolta(InternoAmiciziaStretta.class);
        return partita;
    }

    @Test
    void conIlTrentatrePerCentoChiStringeAmiciziaVieneSfidatoESiGiocaATreMani() {
        try (PartitaDiTest partita = sfidataDaLIdra(25)) {
            // L'amicizia riesce (1 su un dado a 12) e il dado delle sfide dice 1: l'avversario sfida, qualunque sia la percentuale
            Dado.trucca(1, 1);
            partita.comando(Comando.AMICIZIA);

            assertEquals(1, partita.eventi().tutti(InternoAmiciziaStretta.class).size());
            assertEquals(1, partita.eventi().tutti(InternoSfidaCartaForbiciSasso.class).size(), "la sfida si apre");
            InternoSfidaCartaForbiciSasso apertura = partita.eventi().tutti(InternoSfidaCartaForbiciSasso.class).get(0);
            assertTrue(apertura.isInizio());
            assertFalse(apertura.isFinale());
            assertEquals(MossaCartaForbiciSasso.SASSO, apertura.getMossaDelGiocatore());
            assertEquals(MossaCartaForbiciSasso.SASSO, apertura.getMossaDellAvversario());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("dice: \"") && t.toLowerCase().contains("carta, forbici e sasso")),
                    String.valueOf(partita.testi()));
            // Niente altro che le tre mosse
            partita.assertStato(Stato.IN_LOCAZIONE);
            assertEquals(LE_TRE_MOSSE, new HashSet<>(partita.comandiDisponibili()));

            // Il giocatore gioca sempre carta, l'avversario sempre sasso (3): la carta batte il sasso, a tre mani
            Dado.trucca(3, 3);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);
            assertEquals(LE_TRE_MOSSE, new HashSet<>(partita.comandiDisponibili()), "la sfida non è finita");
            InternoSfidaCartaForbiciSasso mano = lastEvent(partita);
            assertFalse(mano.isInizio());
            assertEquals(2, mano.getPunteggioDelGiocatore());
            assertEquals(0, mano.getPunteggioDellAvversario());
            assertEquals(MossaCartaForbiciSasso.CARTA, mano.getMossaDelGiocatore());
            assertEquals(MossaCartaForbiciSasso.SASSO, mano.getMossaDellAvversario());

            assertFalse(lastEvent(partita).isFinale(), "con due mani a zero non è finita");
            Dado.trucca(3);
            partita.comando(Comando.CARTA);
            assertTrue(lastEvent(partita).isFinale(), "l'ultima mano è l'ultima");
            assertNotEquals(new HashSet<>(partita.comandiDisponibili()), LE_TRE_MOSSE, "la sfida è finita");
            assertEquals(1, partita.gruppo().getSfideVinte());
            // Una delle frasi di GIOCATORE_VINCE (vedi sfide.txt), scelta a caso
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("L'Idra dice: \"Poffarre") || t.startsWith("L'Idra dice: \"Incredibile")),
                    String.valueOf(partita.testi()));
            assertFalse(RegistroMissioni.isPartita(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI), "una sfida sola non basta");
        }
    }

    @Test
    void conUnAmiciziaRiuscitaLOggettoNonPresoSparisceSubito() {
        try (PartitaDiTest partita = sfidataDaLIdra(25)) {
            LocazioneCorrente.get().collocaOggettoMissione(FabbricaOggetti.crea(TipoBottino.MONETA));
            assertEquals(TipoBottino.MONETA, LocazioneCorrente.getTipoBottino());
            int monete = Cassa.getMonete();

            // L'amicizia riesce e l'avversario sfida: la sfida è ancora in corso, e l'oggetto è già sparito
            Dado.trucca(1, 1);
            partita.comando(Comando.AMICIZIA);
            assertEquals(LE_TRE_MOSSE, new HashSet<>(partita.comandiDisponibili()), "la sfida è in corso");
            assertNull(LocazioneCorrente.getTipoBottino());
            assertEquals(monete, Cassa.getMonete());
        }
    }

    @Test
    void ancheDopoUnaCorruzioneRiuscitaLOggettoNonPresoSparisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(25)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
            partita.assertStato(Stato.IN_LOCAZIONE);
            LocazioneCorrente.get().collocaOggettoMissione(FabbricaOggetti.crea(TipoBottino.MONETA));
            assertEquals(TipoBottino.MONETA, LocazioneCorrente.getTipoBottino());
            int monete = Cassa.getMonete();

            Dado.trucca(10);
            partita.comando(Comando.CORRUZIONE);

            assertNull(LocazioneCorrente.getTipoBottino(), "con la corruzione l'oggetto non si prende e non resta lì");
            assertTrue(Cassa.getMonete() <= monete, "e non è stato preso");
        }
    }

    @Test
    void finitaLaSfidaVintaOPersaArrivaComunqueLOffertaDiAmicizia() {
        // L'offerta dell'avversario si sceglie a caso: fra più partite almeno una chiede se accettare, vinta o persa che sia
        boolean dopoUnaVittoria = false;
        boolean dopoUnaSconfitta = false;
        for (long seme = 1; seme <= 40 && !(dopoUnaVittoria && dopoUnaSconfitta); seme++) {
            for (boolean vince : new boolean[]{true, false}) {
                try (PartitaDiTest partita = sfidataDaLIdra(seme)) {
                    // L'Idra non ha niente da offrire: al suo posto un eremita, che sì
                    GruppoAvversario avversari = GruppoAvversario.getIstanza();
                    avversari.reimposta();
                    avversari.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.EREMITA, 1));
                    Dado.trucca(1, 1);
                    partita.comando(Comando.AMICIZIA);
                    if (!new HashSet<>(partita.comandiDisponibili()).equals(LE_TRE_MOSSE)) {
                        continue;
                    }
                    // Il giocatore gioca sempre carta, l'avversario sempre sasso (vince lui) o sempre carta... forbici (perde lui)
                    Dado.trucca(vince ? 3 : 2, vince ? 3 : 2, vince ? 3 : 2);
                    partita.comando(Comando.CARTA);
                    partita.comando(Comando.CARTA);
                    partita.comando(Comando.CARTA);
                    assertEquals(vince ? 1 : 0, partita.gruppo().getSfideVinte());
                    if (partita.comandiDisponibili().contains(Comando.SI)) {
                        partita.assertStato(Stato.IN_LOCAZIONE);
                        if (vince) {
                            dopoUnaVittoria = true;
                        } else {
                            dopoUnaSconfitta = true;
                        }
                        partita.comando(Comando.NO);
                        assertNotEquals(Stato.IN_LOCAZIONE, partita.stato(), "rifiutata l'offerta la locazione è finita");
                    }
                }
            }
        }
        assertTrue(dopoUnaVittoria, "dopo una sfida vinta l'avversario fa la sua offerta");
        assertTrue(dopoUnaSconfitta, "e anche dopo una persa");
    }

    @Test
    void ilPareggioNonContaEPerdereDaIlRisultatoDellAvversario() {
        try (PartitaDiTest partita = sfidataDaLIdra(25)) {
            Dado.trucca(1, 1);
            partita.comando(Comando.AMICIZIA);
            // Pareggio (sasso contro sasso, 3), poi tre mani perse (forbici, 2... il sasso rompe le forbici)
            Dado.trucca(3, 3, 3, 3);
            partita.comando(Comando.SASSO);
            assertEquals(0, lastEvent(partita).getPunteggioDelGiocatore());
            assertEquals(0, lastEvent(partita).getPunteggioDellAvversario());
            partita.comando(Comando.FORBICE);
            partita.comando(Comando.FORBICE);
            partita.comando(Comando.FORBICE);
            assertEquals(0, partita.gruppo().getSfideVinte());
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("L'Idra dice: \"") && !t.contains("Poffarre") && !t.contains("Incredibile")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void allaTerzaSfidaVintaPartonoICampioni() {
        try (PartitaDiTest partita = sfidataDaLIdra(25)) {
            partita.gruppo().addSfidaVinta();
            partita.gruppo().addSfidaVinta();
            assertFalse(RegistroMissioni.isPartita(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI));
            Dado.trucca(1, 1);
            partita.comando(Comando.AMICIZIA);
            Dado.trucca(3, 3, 3);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);

            assertEquals(3, partita.gruppo().getSfideVinte());
            assertTrue(RegistroMissioni.isPartita(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI));
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("L'Idra dice: \"") && t.contains("torneo")),
                    String.valueOf(partita.testi()));
        }
    }

    @Test
    void conLaMissioneGiaPartitaChiStringeAmiciziaNonVieneSfidatoPiu() {
        try (PartitaDiTest partita = sfidataDaLIdra(25)) {
            RegistroMissioni.avvia(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI);
            // Il dado delle sfide non viene nemmeno tirato: l'amicizia va per l'offerta di sempre
            Dado.trucca(1);
            partita.comando(Comando.AMICIZIA);
            assertEquals(1, partita.eventi().tutti(InternoAmiciziaStretta.class).size());
            assertEquals(0, partita.eventi().tutti(InternoSfidaCartaForbiciSasso.class).size());
            assertEquals(0, Dado.trucchiRimasti());
        }
    }

    private static InternoSfidaCartaForbiciSasso lastEvent(PartitaDiTest partita) {
        java.util.List<InternoSfidaCartaForbiciSasso> eventi = partita.eventi().tutti(InternoSfidaCartaForbiciSasso.class);
        return eventi.get(eventi.size() - 1);
    }
}
