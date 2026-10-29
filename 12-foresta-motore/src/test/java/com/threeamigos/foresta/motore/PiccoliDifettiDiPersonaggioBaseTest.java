package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatoVitalePersonaggio;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Piccoli difetti di PersonaggioBase: stanchezza, carico, ordine delle notifiche di morte, resurrezione, massimi.
 */
class PiccoliDifettiDiPersonaggioBaseTest {

    private static PersonaggioBase guerriero() {
        return (PersonaggioBase) FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 3);
    }

    @Test
    void laStanchezzaNonScendeSottoZeroNemmenoNellaNotifica() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(401)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            PersonaggioBase p = guerriero();
            p.addStanchezza(2);
            partita.eventi().ascolta(NotificaVariazioneStatistichePersonaggio.class);

            p.subStanchezza(9);

            assertEquals(0, p.getStanchezza());
            List<NotificaVariazioneStatistichePersonaggio> notifiche = partita.eventi().tutti(NotificaVariazioneStatistichePersonaggio.class);
            assertEquals(1, notifiche.size());
            assertEquals(0.0, notifiche.get(0).getNuovoValore());
        }
    }

    @Test
    void laVariazioneTemporaneaDelCaricoContaIModificatoriUnaVoltaSola() {
        PersonaggioBase p = guerriero();
        int prima = p.getCaricoMassimo();
        p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CARICO_MASSIMO, TipoModificatore.VARIAZIONE_PERCENTUALE, 50, "",
                UnitaDurataModificatore.TURNI, 3, BersaglioModificatore.PORTATORE));
        int dopo = p.getCaricoMassimo();
        assertTrue(dopo > prima);
        assertEquals(dopo - prima, p.getVariazioneDaTemporanei(TipoAttributo.CARICO_MASSIMO), 1.0);
    }

    @Test
    void allaNotificaDiMorteLaSaluteEGiaZeroEAllaResurrezioneSiNotificanoSaluteEStanchezza() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(402)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            PersonaggioBase p = guerriero();
            AtomicInteger saluteAllaMorte = new AtomicInteger(-1);
            BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, e -> {
                if (!e.isVivo()) {
                    saluteAllaMorte.set(p.getSalute());
                }
            });

            p.subSalute(p.getSalute() + 5, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            assertFalse(p.isVivo());
            assertEquals(0, saluteAllaMorte.get(), "chi ascolta la morte trova la salute a 0");

            partita.eventi().ascolta(NotificaVariazioneStatistichePersonaggio.class);
            p.resuscita();
            List<NotificaVariazioneStatistichePersonaggio> notifiche = partita.eventi().tutti(NotificaVariazioneStatistichePersonaggio.class);
            assertTrue(notifiche.stream().anyMatch(n -> n.getTipoAttributo() == TipoAttributo.SALUTE && n.getValorePrecedente() == 0.0
                    && n.getNuovoValore() == p.getSalute()), "la salute della resurrezione si notifica");
            assertTrue(notifiche.stream().anyMatch(n -> n.getTipoAttributo() == TipoAttributo.STANCHEZZA && n.getNuovoValore() == 9.0));
        }
    }

    @Test
    void sopraIlMassimoUnaCuraNonAbbassaLaSalute() {
        PersonaggioBase p = guerriero();
        int sopra = p.getSaluteMassima() + 20;
        p.getModelloDati().setSalute(sopra);
        p.addSalute(5);
        assertEquals(sopra, p.getModelloDati().get(TipoAttributo.SALUTE), 0.0);
    }
}
