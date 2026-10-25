package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Come finisce una locazione: la locazione è completa, e l'oggetto si può prendere, anche se l'ultimo avversario muore
 * per un effetto di stato (un sanguinamento) fuori dalla mischia, per esempio dopo un incantesimo.
 */
class ScenarioFineLocazioneTest {

    @Test
    void sePerSanguinamentoMuoreLUltimoAvversarioLaLocazioneECompleta() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(301)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            CoordinateMD bosco = unaCasellaDi(TipoLocazione.BOSCO);
            partita.gruppo().setCoordinate(bosco);
            Locazione locazione = CostruttoreDellaForesta.costruisciIstanza(bosco);
            LocazioneCorrente.set(locazione);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
            avversari.aggiungiPersonaggio(goblin);
            // Un sanguinamento che lo uccide alla fine del turno
            goblin.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 100000);

            assertEquals(Stato.FINE_LOCAZIONE, locazione.impostaAzioni(partita.gruppo(), avversari, null));
            assertTrue(locazione.isCompleta(), "senza avversari vivi la locazione è completa e l'oggetto si può prendere");
        }
    }

    private static CoordinateMD unaCasellaDi(TipoLocazione tipo) {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == tipo) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella di " + tipo);
    }
}
