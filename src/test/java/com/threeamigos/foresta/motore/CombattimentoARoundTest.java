package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il combattimento a round (vedi tattiche_di_combattimento_automatiche.md §3): a ogni scatto del TIMER agisce una sola volta il prossimo
 * del piano del round, un mio personaggio e un avversario a turno, e i personaggi in più di un gruppo agiscono da soli; un'azione
 * del giocatore non ha "risposta", gli avversari agiscono quando il piano tocca a loro.
 */
class CombattimentoARoundTest {

    private static final StatoDelCombattimento.Lato G = StatoDelCombattimento.Lato.GIOCATORE;
    private static final StatoDelCombattimento.Lato A = StatoDelCombattimento.Lato.AVVERSARI;

    private static CoordinateMD unaCasellaDiBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella di bosco");
    }

    /** Una locazione con i miei {@code miei} personaggi e {@code avversari} goblin, tutti molto resistenti perché lo scontro non finisca */
    private static Locazione preparaLo(PartitaDiTest partita, int miei, int avversari) {
        CoordinateMD bosco = unaCasellaDiBosco();
        partita.gruppo().setCoordinate(bosco);
        Locazione locazione = Foresta.costruisciIstanza(bosco);
        partita.gruppo().setLocazioneCorrente(locazione);
        GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();
        gruppoAvversario.reimposta();
        for (int i = 1; i < miei; i++) {
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno" + i, 1));
        }
        for (int i = 0; i < avversari; i++) {
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
            resistente(goblin);
            gruppoAvversario.aggiungiPersonaggio(goblin);
        }
        for (Personaggio p : partita.gruppo().getPersonaggi()) {
            resistente(p);
        }
        StatoDelCombattimento.getIstanza().reimposta();
        return locazione;
    }

    private static void resistente(Personaggio p) {
        p.addModificatore(new com.threeamigos.foresta.modellodati.ModificatoreAttributoMD(com.threeamigos.foresta.tipi.TipoAttributo.COSTITUZIONE,
                com.threeamigos.foresta.tipi.TipoModificatore.QUANTITA_ASSOLUTA, 1000));
        p.addSalute(1_000_000);
    }

    /** Fa partire la mischia con il primo dei miei personaggi */
    private static void iniziaLaMischia(Locazione locazione, PartitaDiTest partita) {
        GruppoAvversario avversari = GruppoAvversario.getIstanza();
        locazione.impostaAzioni(partita.gruppo(), avversari, null);
        assertEquals(Stato.SCELTA_AUTOMATICA_PERSONAGGIO, locazione.impostaAzioni(partita.gruppo(), avversari, Comando.COMBATTIMENTO));
        assertEquals(Stato.IN_COMBATTIMENTO, locazione.impostaAzioni(partita.gruppo(), avversari, Comando.ofPersonaggio(0)));
    }

    private static List<StatoDelCombattimento.Lato> lati(PartitaDiTest partita) {
        List<StatoDelCombattimento.Lato> lati = new ArrayList<>();
        for (InternoAzioneInCombattimento evento : partita.eventi().tutti(InternoAzioneInCombattimento.class)) {
            lati.add(partita.gruppo().contiene(evento.getPersonaggio()) ? G : A);
        }
        return lati;
    }

    @Test
    void aOgniScattoAgisceUnoSoloAlternandoIDueGruppi() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(401)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 2, 3);
            partita.eventi().ascolta(InternoAzioneInCombattimento.class);
            iniziaLaMischia(locazione, partita);
            // L'inizio della mischia annuncia chi comincia (un evento): poi un'azione per ogni scatto
            int annunci = lati(partita).size();
            assertEquals(java.util.Arrays.asList(G, A, G, A, A), StatoDelCombattimento.getIstanza().getPiano());

            for (int i = 0; i < 5; i++) {
                assertEquals(Stato.IN_COMBATTIMENTO, locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.TIMER));
            }
            assertEquals(java.util.Arrays.asList(G, A, G, A, A), lati(partita).subList(annunci, annunci + 5));
            assertEquals(2, StatoDelCombattimento.getIstanza().getRound(), "con l'ultimo avversario il round è finito");
        }
    }

    @Test
    void ChiHaPiuCompagniDegliAvversariAgisceDaSolo() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(402)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 3, 1);
            partita.eventi().ascolta(InternoAzioneInCombattimento.class);
            iniziaLaMischia(locazione, partita);
            int annunci = lati(partita).size();
            for (int i = 0; i < 4; i++) {
                locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.TIMER);
            }
            // G A G G: dopo la coppia i due compagni in più agiscono da soli
            assertEquals(java.util.Arrays.asList(G, A, G, G), lati(partita).subList(annunci, annunci + 4));
            assertEquals(2, StatoDelCombattimento.getIstanza().getRound());
        }
    }

    @Test
    void ilGiocatoreCheAgisceNonHaUnaRispostaMaIlPianoDegliAvversari() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(403)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 1, 3);
            partita.eventi().ascolta(InternoAzioneInCombattimento.class);
            locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), null);
            assertEquals(Stato.SCELTA_AUTOMATICA_PERSONAGGIO,
                    locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.SINGOLO_ATTACCO));
            locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.ofPersonaggio(0));
            // Con tre avversari contro uno, un'azione mia fa agire tutti e tre, uno dopo l'altro
            assertEquals(java.util.Arrays.asList(G, A, A, A), lati(partita));
            assertEquals(2, StatoDelCombattimento.getIstanza().getRound());
        }
    }

    @Test
    void quandoGliAvversariSonoDiMenoLaMiaAzioneNonHaRisposta() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(404)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Locazione locazione = preparaLo(partita, 3, 1);
            partita.eventi().ascolta(InternoAzioneInCombattimento.class);
            locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), null);
            for (int i = 0; i < 2; i++) {
                locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.SINGOLO_ATTACCO);
                locazione.impostaAzioni(partita.gruppo(), GruppoAvversario.getIstanza(), Comando.ofPersonaggio(i));
            }
            // Piano G A G G: la mia prima azione ha la risposta, la seconda no
            assertEquals(java.util.Arrays.asList(G, A, G), lati(partita));
        }
    }

    @Test
    void gliAvversariDellaStessaSpecieSiDistinguonoConUnNumero() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(405)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            Personaggio g1 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
            avversari.aggiungiPersonaggio(g1);
            String senzaNumero = g1.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
            assertFalse(senzaNumero.matches(".*\\d$"), senzaNumero);
            Personaggio g2 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
            avversari.aggiungiPersonaggio(g2);
            assertEquals(senzaNumero + " 1", g1.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
            assertEquals(Character.toUpperCase(senzaNumero.charAt(0)) + senzaNumero.substring(1) + " 2",
                    g2.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
            // Un gruppo misto: il numero è la posizione, e solo chi ha un simile lo porta
            Personaggio troll = FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 1);
            avversari.aggiungiPersonaggio(troll);
            assertFalse(troll.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE).matches(".*\\d$"));
            assertEquals(3, troll.getOrdinale());
            assertEquals(senzaNumero + " 2", g2.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
        }
    }
}
