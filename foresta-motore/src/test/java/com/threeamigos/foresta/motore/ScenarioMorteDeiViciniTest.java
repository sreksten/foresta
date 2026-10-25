package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Un avversario che muore senza un colpo diretto del gruppo (la reazione ad area su un vicino: elettrocuzione, esplosione di gas, rigetto)
 * conta comunque come sconfitto, una volta sola.
 */
class ScenarioMorteDeiViciniTest {

    @Test
    void ilVicinoCadutoPerUnaReazioneAdAreaContaComeSconfitto() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(293)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            CoordinateMD bosco = unaCasellaDi(TipoLocazione.BOSCO);
            partita.gruppo().setCoordinate(bosco);
            Locazione locazione = CostruttoreDellaForesta.costruisciIstanza(bosco);
            partita.gruppo().setLocazioneCorrente(locazione);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            Personaggio colpito = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
            Personaggio vicino = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
            Personaggio altro = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
            avversari.aggiungiPersonaggio(colpito);
            avversari.aggiungiPersonaggio(vicino);
            avversari.aggiungiPersonaggio(altro);
            partita.eventi().ascolta(InternoAvversarioSconfitto.class);
            locazione.impostaAzioni(partita.gruppo(), avversari, null);

            vicino.subSalute(vicino.getSalute() + 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            locazione.impostaAzioni(partita.gruppo(), avversari, null);
            locazione.impostaAzioni(partita.gruppo(), avversari, null);

            assertEquals(1, partita.eventi().tutti(InternoAvversarioSconfitto.class).size());
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
