package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.combattimento.ApplicazioneDelRisultato;
import com.threeamigos.foresta.combattimento.DannoRisultante;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli ospiti vulnerabili ricevono gli effetti di stato come chiunque: li subiscono a ogni turno e li perdono alla scadenza, e una reazione
 * di cura ad area che li investe cura il gruppo del giocatore, non gli avversari.
 */
class ScenarioStatiDegliOspitiTest {

    @Test
    void gliStatiDiUnOspiteVulnerabileScadono() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(298)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            CoordinateMD bosco = unaCasellaDi(TipoLocazione.BOSCO);
            partita.gruppo().setCoordinate(bosco);
            Locazione locazione = CostruttoreDellaForesta.costruisciIstanza(bosco);
            partita.gruppo().setLocazioneCorrente(locazione);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            avversari.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3));
            // Il primo passo è l'ingresso nella locazione
            locazione.impostaAzioni(partita.gruppo(), avversari, null);

            Personaggio ospite = new Viandante("Ottone", 1);
            partita.gruppo().aggiungiOspite(ospite, true);
            ospite.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 2, 1);
            int salute = ospite.getSalute();
            locazione.impostaAzioni(partita.gruppo(), avversari, null);
            assertTrue(ospite.hasEffettoDiStato(TipoEffettoDiStato.AVVELENATO));
            assertTrue(ospite.getSalute() < salute, "il veleno colpisce anche l'ospite");

            locazione.impostaAzioni(partita.gruppo(), avversari, null);
            locazione.impostaAzioni(partita.gruppo(), avversari, null);
            assertFalse(ospite.hasEffettoDiStato(TipoEffettoDiStato.AVVELENATO), "dopo la durata lo stato è scaduto");
        }
    }

    @Test
    void laCuraAdAreaSuUnOspiteCuraIlGruppoENonGliAvversari() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(299)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio ospite = new Viandante("Ottone", 1);
            partita.gruppo().aggiungiOspite(ospite, true);
            Personaggio capo = partita.gruppo().getCapo();
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
            GruppoAvversario.getIstanza().reimposta();
            GruppoAvversario.getIstanza().aggiungiPersonaggio(goblin);
            capo.subSalute(30, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            goblin.subSalute(30, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            int saluteCapo = capo.getSalute();
            int saluteGoblin = goblin.getSalute();

            DannoRisultante colpo = new DannoRisultante(goblin, ospite);
            colpo.setDanno(0);
            colpo.setCuraAdArea(20);
            ApplicazioneDelRisultato.applica(ospite, colpo);

            assertTrue(capo.getSalute() > saluteCapo, "il gruppo del giocatore è curato");
            assertEquals(saluteGoblin, goblin.getSalute(), "gli avversari no");
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
