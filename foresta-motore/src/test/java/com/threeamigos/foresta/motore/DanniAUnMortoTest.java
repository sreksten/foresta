package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Un personaggio già morto non subisce altri danni: la morte non si annuncia due volte e non si ripetono gli effetti.
 */
class DanniAUnMortoTest {

    @Test
    void ilSecondoDannoAChiEGiaMortoNonAnnunciaUnaSecondaMorte() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(415)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 5);
            partita.testi().clear();

            goblin.subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
            assertFalse(goblin.isVivo());
            long annunci = partita.testi().stream().filter(t -> t.contains("è mort")).count();
            assertEquals(1, annunci, String.valueOf(partita.testi()));

            goblin.subSalute(10000, null, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
            assertEquals(annunci, partita.testi().stream().filter(t -> t.contains("è mort")).count(), String.valueOf(partita.testi()));
            assertEquals(1, partita.testi().stream().filter(t -> t.contains("è mort")).count());
        }
    }
}
