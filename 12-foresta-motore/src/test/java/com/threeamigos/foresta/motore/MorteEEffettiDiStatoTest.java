package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Con la morte finiscono gli effetti di stato: chi risorge non li ritrova.
 */
class MorteEEffettiDiStatoTest {

    @Test
    void chiMuoreNonHaPiuEffettiDiStatoENeppureQuandoRisorge() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(295)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio p = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 3);
            p.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 5, 3);
            p.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 5, 2);
            assertTrue(p.hasEffettoDiStato(TipoEffettoDiStato.AVVELENATO));

            partita.eventi().ascolta(NotificaVariazioneEffettoDiStatoPersonaggio.class);
            p.subSalute(p.getSalute() + 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            assertFalse(p.isVivo());
            assertTrue(partita.eventi().tutti(NotificaVariazioneEffettoDiStatoPersonaggio.class).isEmpty(), "un morto non notifica la rimozione degli effetti");
            assertTrue(p.getEffettiDiStato().isEmpty());

            p.resuscita();
            assertTrue(p.isVivo());
            assertTrue(p.getEffettiDiStato().isEmpty());
            assertEquals(0, p.getQuantitaEffettoDiStato(TipoEffettoDiStato.AVVELENATO));
        }
    }

    @Test
    void chiMuoreNonHaPiuModificatoriATempoMaTieneQuelliPermanenti() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(296)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio p = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 3);
            p.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.VARIAZIONE_FISSA, -3, "Maledizione",
                    UnitaDurataModificatore.TURNI, 5, BersaglioModificatore.PORTATORE));
            ModificatoreAttributoMD benedizione = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2, "Benedizione");
            p.addModificatore(benedizione);
            assertEquals(1, p.getModificatoriTemporanei().size());

            p.subSalute(p.getSalute() + 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            assertFalse(p.isVivo());
            assertTrue(p.getModificatoriTemporanei().isEmpty(), "i modificatori a tempo finiscono con la morte");
            assertTrue(p.getModelloDati().getModificatori().contains(benedizione), "quelli permanenti restano");

            p.resuscita();
            assertTrue(p.getModificatoriTemporanei().isEmpty(), "e non tornano con la resurrezione");
        }
    }
}
