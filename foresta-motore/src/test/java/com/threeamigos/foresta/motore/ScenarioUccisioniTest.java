package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.bestiario.RegistroBestiario;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePuntiEsperienzaPersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chiunque sia caduto fra gli avversari, comunque sia successo, conta come sconfitto una volta sola: lo sa l'esperienza, il bestiario e i
 * trofei (tramite InternoAvversarioSconfitto). Qui gli avversari muoiono senza passare da nessun attacco o incantesimo del gioco, e
 * il conteggio avviene lo stesso alla prima azione dopo.
 */
class ScenarioUccisioniTest {

    private static List<Personaggio> avversariAllIngresso(PartitaDiTest partita) {
        partita.comando(Comando.ESCI_DA_CITTA);
        for (int passo = 0; passo < 12 && GruppoAvversario.getIstanza().getPersonaggi().isEmpty(); passo++) {
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            partita.comando(passo % 2 == 0 ? Comando.NORD : Comando.EST).comando(Comando.NUMERO_1);
        }
        return new ArrayList<>(GruppoAvversario.getIstanza().getPersonaggi());
    }

    @Test
    void chiCadeInQualunqueModoVieneContatoUnaVoltaSolaPerEsperienzaBestiarioETrofei() {
        for (int seme = 600; seme < 606; seme++) {
            try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
                // Il gruppo regge: lo scontro non si decide sui dadi
                partita.gruppo().getCapo().getModelloDati().set(TipoAttributo.SALUTE, 1_000_000);
                List<Personaggio> avversari = avversariAllIngresso(partita);
                if (avversari.isEmpty()) {
                    continue;
                }
                partita.eventi().ascolta(InternoAvversarioSconfitto.class, NotificaVariazionePuntiEsperienzaPersonaggio.class);
                                avversari.forEach(p -> p.muore("è morto da solo"));
                // Il primo cade e se ne va dal gruppo, come il minion della Mietitura (sostituito da uno scheletro del giocatore)
                GruppoAvversario.getIstanza().rimuoviPersonaggio(avversari.get(0));

                partita.comando(Comando.COMBATTIMENTO);
                for (int giri = 0; partita.stato() == Stato.IN_COMBATTIMENTO && giri < 100; giri++) {
                    partita.scatta();
                }
                List<TipoPersonaggio> sconfitti = partita.eventi().tutti(InternoAvversarioSconfitto.class).stream()
                        .map(InternoAvversarioSconfitto::getClasse).collect(Collectors.toList());
                assertEquals(avversari.size(), sconfitti.size(), "ognuno una volta sola: " + sconfitti);
                for (Personaggio avversario : avversari) {
                    assertTrue(RegistroBestiario.isSbloccata(avversario.getClasse()), avversario.getClasse().name());
                }
                assertFalse(partita.eventi().tutti(NotificaVariazionePuntiEsperienzaPersonaggio.class).isEmpty(), "e dà esperienza");
                return;
            }
        }
        fail("nessun incontro nei semi provati");
    }
}
