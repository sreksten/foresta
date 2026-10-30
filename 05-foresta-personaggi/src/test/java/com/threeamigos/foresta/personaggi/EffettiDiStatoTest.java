package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCaratteristicaOsservata;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli stati alterati di un personaggio e le ricariche delle sue abilità.
 */
class EffettiDiStatoTest {

    private final List<NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione> variazioni = new ArrayList<>();
    private final List<String> messaggi = new ArrayList<>();
    private final List<Object> osservazioni = new ArrayList<>();
    private final List<Runnable> iscrizioni = new ArrayList<>();

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        DatiDiProva.nuovaPartita();
        iscrizioni.add(BusEventi.iscriviti(NotificaVariazioneEffettoDiStatoPersonaggio.class, e -> variazioni.add(e.getTipo())));
        iscrizioni.add(BusEventi.iscriviti(InternoMessaggio.class, e -> messaggi.add(e.getMessaggioInterno())));
        iscrizioni.add(BusEventi.iscriviti(InternoCaratteristicaOsservata.class, osservazioni::add));
    }

    @AfterEach
    void ripulisce() {
        iscrizioni.forEach(Runnable::run);
        BusEventi.impostaConsegna(null);
    }

    private static Personaggio goblin() {
        return FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 5);
    }

    @Test
    void unEffettoNuovoSiAggiungeESiRaccontaUnaVoltaSola() {
        Personaggio p = goblin();
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 2);
        assertTrue(p.hasEffettoDiStato(TipoEffettoDiStato.AVVELENATO));
        assertEquals(3, p.getQuantitaEffettoDiStato(TipoEffettoDiStato.AVVELENATO));
        assertEquals(1, p.getEffettiDiStato().size());
        assertEquals(1, variazioni.size());
        assertEquals(NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.AGGIUNTA, variazioni.get(0));
        assertFalse(p.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO));
        assertEquals(0, p.getQuantitaEffettoDiStato(TipoEffettoDiStato.CONFUSO));
    }

    @Test
    void ripetutoRinforzaSoloSeDuraDiPiuFaPiuDanniOHaPiuIntensita() {
        Personaggio p = goblin();
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 2, 1.0d);
        variazioni.clear();
        // Uguale o più debole: niente
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 2, 1.0d);
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 1, 1, 0.5d);
        assertTrue(variazioni.isEmpty());
        // Più a lungo
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 5, 2, 1.0d);
        assertEquals(5, p.getQuantitaEffettoDiStato(TipoEffettoDiStato.AVVELENATO));
        // Più danni
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 5, 4, 1.0d);
        assertEquals(4, p.getEffettiDiStato().iterator().next().getDanniNelTempo());
        // Più intensità
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 5, 4, 3.0d);
        assertEquals(3.0d, p.getEffettiDiStato().iterator().next().getIntensita(), 1e-9);
        assertEquals(3, variazioni.size());
        assertEquals(1, p.getEffettiDiStato().size());
        assertEquals(NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.RINFORZO, variazioni.get(0));
    }

    @Test
    void unDurataNonValidaNonSiAccetta() {
        assertThrows(IllegalArgumentException.class, () -> goblin().addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 0, 1));
    }

    @Test
    void chiEMortoNonSubisceEffettiDiStato() {
        Personaggio p = goblin();
        p.muore("prova");
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 1);
        assertTrue(p.getEffettiDiStato().isEmpty());
    }

    @Test
    void chiEImmuneLoDiceEIlBestiarioLoRicorda() {
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        scheletro.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 1);
        assertTrue(scheletro.getEffettiDiStato().isEmpty());
        assertEquals(1, messaggi.size());
        assertTrue(messaggi.get(0).endsWith(" è immune a " + TipoEffettoDiStato.SANGUINAMENTO.getDescrizione() + "."), messaggi.get(0));
        assertEquals(1, osservazioni.size());
    }

    @Test
    void iDanniNelTempoColpisconoEPossonoUccidere() {
        Personaggio p = goblin();
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 4);
        p.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        int prima = p.getSalute();
        p.applicaDanniDaEffettiDiStato();
        assertEquals(prima - 4, p.getSalute());
        p.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 100000);
        p.applicaDanniDaEffettiDiStato();
        assertFalse(p.isVivo());
    }

    @Test
    void laRigenerazioneCuraSoloChiSiPuoCurare() {
        Personaggio p = goblin();
        p.subSalute(20, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int prima = p.getSalute();
        p.addEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE, 3, 0, 7.0d);
        p.applicaDanniDaEffettiDiStato();
        assertEquals(prima + 7, p.getSalute());
        // Un non morto del gruppo del giocatore non si cura
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(scheletro);
        scheletro.subSalute(1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int saluteScheletro = scheletro.getSalute();
        scheletro.addEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE, 3, 0, 7.0d);
        scheletro.applicaDanniDaEffettiDiStato();
        assertEquals(saluteScheletro, scheletro.getSalute());
    }

    @Test
    void chiMuoreDiVelenoNonSiRigeneraNelloStessoTurno() {
        Personaggio p = goblin();
        p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 100000);
        p.addEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE, 3, 0, 7.0d);
        p.applicaDanniDaEffettiDiStato();
        assertFalse(p.isVivo());
        assertEquals(0, p.getSalute());
    }

    @Test
    void ogniTurnoGliEffettiDurannoUnoInMenoEFinisconoQuandoScadono() {
        Personaggio p = goblin();
        p.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 2, 0);
        p.addEffettoDiStato(TipoEffettoDiStato.ACCECATO, 1, 0);
        variazioni.clear();
        p.riduciEffettiDiStato();
        assertEquals(1, p.getQuantitaEffettoDiStato(TipoEffettoDiStato.CONFUSO));
        assertFalse(p.hasEffettoDiStato(TipoEffettoDiStato.ACCECATO));
        assertTrue(variazioni.contains(NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.DECADIMENTO));
        assertTrue(variazioni.contains(NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.RIMOZIONE));
        p.riduciEffettiDiStato();
        assertTrue(p.getEffettiDiStato().isEmpty());
    }

    @Test
    void siTolgonoUnoPerTipoSoloINegativiOTutti() {
        Personaggio p = goblin();
        p.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        p.addEffettoDiStato(TipoEffettoDiStato.ACCECATO, 3, 0);
        p.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
        p.rimuoviEffettoDiStato(TipoEffettoDiStato.CONFUSO);
        assertFalse(p.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO));
        assertEquals(2, p.getEffettiDiStato().size());
        p.rimuoviGliEffettiDiStatoNegativi();
        assertTrue(p.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO));
        assertFalse(p.hasEffettoDiStato(TipoEffettoDiStato.ACCECATO));
        p.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        p.rimuoviTuttiGliEffettiDiStato();
        assertTrue(p.getEffettiDiStato().isEmpty());
        // Togliere quel che non c'è non fa niente
        p.rimuoviEffettoDiStato(TipoEffettoDiStato.BERSERK);
    }

    @Test
    void leRicaricheScalganoDiUnoANonVannoSottoZeroSeVengonoRiportate() {
        Personaggio p = goblin();
        assertEquals(0, p.getRicarica("scacciare"));
        p.setRicarica("scacciare", 3);
        p.setRicarica("canto", 1);
        assertEquals(3, p.getRicarica("scacciare"));
        assertEquals(2, p.getRicariche().size());
        p.riduciRicariche();
        assertEquals(2, p.getRicarica("scacciare"));
        assertEquals(0, p.getRicarica("canto"));
    }
}
