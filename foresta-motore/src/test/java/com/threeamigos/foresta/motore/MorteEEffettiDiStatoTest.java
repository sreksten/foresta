package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
