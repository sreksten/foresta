package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAmiciziaStretta;
import com.threeamigos.foresta.eventi.interni.InternoSfidaCartaForbiciSasso;
import com.threeamigos.foresta.missioni.LaSfidaDeiCampioni;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.oggetti.FabbricaOggetti;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoOggetto;
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
            assertFalse(new HashSet<>(partita.comandiDisponibili()).equals(LE_TRE_MOSSE), "la sfida è finita");
            assertEquals(1, partita.gruppo().getSfideVinte());
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("L'Idra dice: \"Poffarre")), String.valueOf(partita.testi()));
            assertFalse(LaSfidaDeiCampioni.isPartita(), "una sfida sola non basta");
        }
    }

    @Test
    void conUnAmiciziaRiuscitaLOggettoNonPresoSparisceSubito() {
        try (PartitaDiTest partita = sfidataDaLIdra(25)) {
            partita.gruppo().getLocazioneCorrente().collocaOggettoMissione(FabbricaOggetti.crea(TipoOggetto.MONETA));
            assertEquals(TipoOggetto.MONETA, partita.gruppo().getTipoOggettoInLocazione());
            int monete = partita.gruppo().getMonete();

            // L'amicizia riesce e l'avversario sfida: la sfida è ancora in corso, e l'oggetto è già sparito
            Dado.trucca(1, 1);
            partita.comando(Comando.AMICIZIA);
            assertEquals(LE_TRE_MOSSE, new HashSet<>(partita.comandiDisponibili()), "la sfida è in corso");
            assertNull(partita.gruppo().getTipoOggettoInLocazione());
            assertEquals(monete, partita.gruppo().getMonete());
        }
    }

    @Test
    void ancheDopoUnaCorruzioneRiuscitaLOggettoNonPresoSparisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(25)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
            partita.assertStato(Stato.IN_LOCAZIONE);
            partita.gruppo().getLocazioneCorrente().collocaOggettoMissione(FabbricaOggetti.crea(TipoOggetto.MONETA));
            assertEquals(TipoOggetto.MONETA, partita.gruppo().getTipoOggettoInLocazione());
            int monete = partita.gruppo().getMonete();

            Dado.trucca(10);
            partita.comando(Comando.CORRUZIONE);

            assertNull(partita.gruppo().getTipoOggettoInLocazione(), "con la corruzione l'oggetto non si prende e non resta lì");
            assertTrue(partita.gruppo().getMonete() <= monete, "e non è stato preso");
        }
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
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("L'Idra dice: \"") && !t.contains("Poffarre")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void allaTerzaSfidaVintaPartonoICampioni() {
        try (PartitaDiTest partita = sfidataDaLIdra(25)) {
            partita.gruppo().addSfidaVinta();
            partita.gruppo().addSfidaVinta();
            assertFalse(LaSfidaDeiCampioni.isPartita());
            Dado.trucca(1, 1);
            partita.comando(Comando.AMICIZIA);
            Dado.trucca(3, 3, 3);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);
            partita.comando(Comando.CARTA);

            assertEquals(3, partita.gruppo().getSfideVinte());
            assertTrue(LaSfidaDeiCampioni.isPartita());
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("L'Idra dice: \"") && t.contains("torneo")),
                    String.valueOf(partita.testi()));
        }
    }

    @Test
    void conLaMissioneGiaPartitaChiStringeAmiciziaNonVieneSfidatoPiu() {
        try (PartitaDiTest partita = sfidataDaLIdra(25)) {
            LaSfidaDeiCampioni.avvia();
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
