package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.BardoLocanda;
import com.threeamigos.foresta.personaggi.Gigante;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tutti i personaggi nascono PNG; smette di esserlo chi entra nel gruppo per restare (il giocatore, un compagno reclutato).
 * Gli aiuti a tempo e gli ospiti restano PNG.
 */
class ScenarioFlagPNGTest {

    @Test
    void sonoPNGFinoAllIngressoNelGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(242)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            assertFalse(partita.gruppo().getCapo().isPNG(), "il giocatore non è un PNG");

            Personaggio compagno = new Guerriero("Reginald", 1);
            assertTrue(compagno.isPNG());
            partita.gruppo().aggiungiPersonaggio(compagno);
            assertFalse(compagno.isPNG(), "chi entra per restare non è più un PNG");

            Personaggio aiuto = new Gigante(1);
            aiuto.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
            partita.gruppo().aggiungiPersonaggio(aiuto);
            assertTrue(aiuto.isPNG(), "un aiuto a tempo resta un PNG");

            Personaggio ospite = new BardoLocanda("Orfeo", 1);
            partita.gruppo().aggiungiOspite(ospite);
            assertTrue(ospite.isPNG(), "un ospite resta un PNG");
        }
    }
}
