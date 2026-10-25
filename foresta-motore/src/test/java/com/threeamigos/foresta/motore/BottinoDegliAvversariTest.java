package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le scorte del gruppo avversario: un 20% per avversario di una pergamena (qualsiasi se è un umanoide, del suo repertorio se è
 * magico) e per gli umanoidi di una pozione della Salute; a scontro vinto il residuo passa al giocatore.
 */
class BottinoDegliAvversariTest {

    private final List<String> messaggi = new ArrayList<>();
    private final Consumer<NotificaTestoParagrafo> ascoltatore = e -> messaggi.add(e.getMessaggio());

    private GruppoGiocatore giocatore;
    private GruppoAvversario avversari;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        BusEventi.impostaConsegna(Runnable::run);
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        giocatore = GruppoGiocatore.getIstanza();
        avversari = GruppoAvversario.getIstanza();
        BusEventi.iscriviti(NotificaTestoParagrafo.class, ascoltatore);
    }

    @AfterEach
    void ripristina() {
        BusEventi.cancellati(NotificaTestoParagrafo.class, ascoltatore);
        BusEventi.impostaConsegna(null);
        assertEquals(0, Dado.trucchiRimasti(), "il test ha previsto più lanci di quelli fatti");
        Dado.ripristina();
    }

    private static int pergamene(GruppoAvversario gruppo) {
        int totale = 0;
        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            totale += gruppo.getIncantesimi(classe);
        }
        return totale;
    }

    @Test
    void unUmanoidePuoPortareUnIncantesimoQualsiasi() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        // Il 20% riesce con un tiro di 20; la terza classe di incantesimo è la Terra; nessuna pozione (tiro 99)
        Dado.trucca(20, 3, 99);
        BottinoDegliAvversari.fornisciLeScorte(avversari, Collections.singletonList(goblin));
        assertEquals(1, pergamene(avversari));
        assertEquals(1, avversari.getIncantesimi(TipoIncantesimo.values()[2]));
        assertEquals(0, avversari.getPozioniSalute());
    }

    @Test
    void ancheUnUmanoideMagicoPortaUnIncantesimoQualsiasiEnonSoloDelSuoRepertorio() {
        Personaggio strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        assertTrue(strega.isMagico());
        // La nona classe è la Resurrezione, che non è fra i malefici del suo repertorio
        Dado.trucca(1, 9, 99);
        BottinoDegliAvversari.fornisciLeScorte(avversari, Collections.singletonList(strega));
        assertEquals(1, avversari.getIncantesimi(TipoIncantesimo.RESURREZIONE));
    }

    @Test
    void unNonUmanoideMagicoPortaUnaPergamenaDeiSuoiIncantesimi() {
        Personaggio fantasma = FabbricaPersonaggi.crea(TipoPersonaggio.FANTASMA, 1);
        Dado.trucca(1);
        BottinoDegliAvversari.fornisciLeScorte(avversari, Collections.singletonList(fantasma));
        // Il suo repertorio è il Gelo e basta: un solo incantesimo possibile, senza altri tiri; e non è umanoide, quindi niente pozioni
        assertEquals(1, avversari.getIncantesimi(TipoIncantesimo.GELO));
        assertEquals(1, pergamene(avversari));
        assertEquals(0, avversari.getPozioniSalute());
    }

    @Test
    void oltreIlVentiPerCentoNonSiPortaNiente() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Dado.trucca(21, 21);
        BottinoDegliAvversari.fornisciLeScorte(avversari, Collections.singletonList(goblin));
        assertEquals(0, pergamene(avversari));
        assertEquals(0, avversari.getPozioniSalute());
    }

    @Test
    void chiNonEMagicoNeUmanoideNonPortaNiente() {
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        Dado.trucca(1);
        BottinoDegliAvversari.fornisciLeScorte(avversari, Collections.singletonList(scheletro));
        assertEquals(0, pergamene(avversari));
        assertEquals(0, avversari.getPozioniSalute());
    }

    @Test
    void gliUmanoidiPortanoAncheUnaPozioneDellaSalute() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Dado.trucca(99, 20);
        BottinoDegliAvversari.fornisciLeScorte(avversari, Collections.singletonList(goblin));
        assertEquals(1, avversari.getPozioniSalute());
        assertEquals(0, pergamene(avversari));
    }

    @Test
    void ilTiroSiFaPerOgniAvversario() {
        Personaggio g1 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Personaggio g2 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        // Il primo: una pergamena (la prima classe) e una pozione; il secondo: niente
        Dado.trucca(5, 1, 7, 99, 99);
        BottinoDegliAvversari.fornisciLeScorte(avversari, Arrays.asList(g1, g2));
        assertEquals(1, pergamene(avversari));
        assertEquals(1, avversari.getPozioniSalute());
    }

    @Test
    void aScontroVintoIlResiduoPassaAlGiocatoreEloDice() {
        avversari.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        avversari.addIncantesimi(TipoIncantesimo.ARIA, 2);
        avversari.addPozioniSalute(1);
        int fuoco = giocatore.getIncantesimi(TipoIncantesimo.FUOCO);
        BottinoDegliAvversari.raccogli(giocatore, avversari);
        assertEquals(fuoco + 1, giocatore.getIncantesimi(TipoIncantesimo.FUOCO));
        assertEquals(0, pergamene(avversari));
        assertEquals(0, avversari.getPozioniSalute());
        assertEquals(1, messaggi.size(), String.valueOf(messaggi));
        assertTrue(messaggi.get(0).contains("1 incantesimo del Fuoco") && messaggi.get(0).contains("2 incantesimi dell'Aria")
                && messaggi.get(0).contains("1 pozione della Salute"), messaggi.get(0));
    }

    @Test
    void senzaResiduoNonCeMessaggio() {
        BottinoDegliAvversari.raccogli(giocatore, avversari);
        assertTrue(messaggi.isEmpty());
    }
}
