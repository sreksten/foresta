package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoInizioRound;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il round e il round-robin dello StatoDelCombattimento.
 */
class StatoDelCombattimentoTest {

    private final List<Integer> roundAnnunciati = new ArrayList<>();
    private final Consumer<InternoInizioRound> ascoltatore = e -> roundAnnunciati.add(e.getNumeroRound());

    private GruppoGiocatore giocatore;
    private GruppoAvversario avversari;
    private StatoDelCombattimento stato;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        BusEventi.impostaConsegna(Runnable::run);
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        StatoDelCombattimento.azzeraIstanza();
        giocatore = GruppoGiocatore.getIstanza();
        avversari = GruppoAvversario.getIstanza();
        stato = StatoDelCombattimento.getIstanza();
        BusEventi.iscriviti(InternoInizioRound.class, ascoltatore);
    }

    @AfterEach
    void ripristina() {
        BusEventi.cancellati(InternoInizioRound.class, ascoltatore);
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
    }

    private static Personaggio goblin(int livello) {
        return FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, livello);
    }

    private Personaggio nelGiocatore(TipoPersonaggio tipo) {
        Personaggio p = FabbricaPersonaggi.crea(tipo, 1);
        giocatore.aggiungiPersonaggio(p);
        return p;
    }

    private Personaggio negliAvversari() {
        Personaggio p = goblin(1);
        avversari.aggiungiPersonaggio(p);
        return p;
    }

    @Test
    void iniziaDalRoundUnoEloAnnuncia() {
        stato.reimposta();
        assertEquals(1, stato.getRound());
        assertEquals(java.util.Collections.singletonList(1), roundAnnunciati);
    }

    @Test
    void ilRoundRobinGiraSulGruppoESaltaChiECaduto() {
        Personaggio a = negliAvversari();
        Personaggio b = negliAvversari();
        Personaggio c = negliAvversari();
        assertSame(a, stato.getProssimoAttaccante(avversari));
        assertSame(b, stato.getProssimoAttaccante(avversari));
        b.muore("di prova");
        assertSame(c, stato.getProssimoAttaccante(avversari));
        // Il giro riparte dal primo, saltando il caduto
        assertSame(a, stato.getProssimoAttaccante(avversari));
        assertSame(c, stato.getProssimoAttaccante(avversari));
    }

    @Test
    void ognunGruppoHaIlSuoPuntatore() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio g2 = nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        assertSame(g1, stato.getProssimoAttaccante(giocatore));
        assertSame(a1, stato.getProssimoAttaccante(avversari));
        assertSame(g2, stato.getProssimoAttaccante(giocatore));
        assertSame(a2, stato.getProssimoAttaccante(avversari));
    }

    @Test
    void seIlPuntatoreUsciDalGruppoSiRipartePerIlSuccessore() {
        Personaggio a = negliAvversari();
        Personaggio b = negliAvversari();
        Personaggio c = negliAvversari();
        stato.getProssimoAttaccante(avversari);
        assertSame(b, stato.getProssimoAttaccante(avversari));
        avversari.rimuoviPersonaggio(b);
        // b non c'è più: dopo di lui veniva c
        assertSame(c, stato.getProssimoAttaccante(avversari));
        assertNotNull(a);
    }

    @Test
    void senzaNessunoInGradoDiAgireNonCeProssimo() {
        Personaggio a = negliAvversari();
        a.muore("di prova");
        assertNull(stato.getProssimoAttaccante(avversari));
    }

    @Test
    void ilRoundFinisceQuandoTuttiGliAvversariHannoAgito() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        stato.reimposta();
        roundAnnunciati.clear();

        // Il giocatore fa agire sempre lo stesso personaggio: il round arriva comunque a una fine
        stato.registraAzione(g1);
        stato.registraAzione(a1);
        assertEquals(1, stato.getRound(), "a2 non ha ancora agito");
        assertTrue(stato.haAgitoNelRound(g1));
        stato.registraAzione(g1);
        stato.registraAzione(a2);
        assertEquals(2, stato.getRound());
        assertEquals(java.util.Collections.singletonList(2), roundAnnunciati);
        assertFalse(stato.haAgitoNelRound(g1), "nel round nuovo nessuno ha ancora agito");
    }

    @Test
    void chiCadeNonVieneAspettatoPerFinireIlRound() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        stato.reimposta();
        stato.registraAzione(g1);
        stato.registraAzione(a1);
        a2.muore("di prova");
        stato.registraAzione(g1);
        assertEquals(2, stato.getRound());
    }

    @Test
    void senzaAvversariVivilIRoundNonAvanza() {
        Personaggio g = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a = negliAvversari();
        stato.reimposta();
        a.muore("di prova");
        stato.registraAzione(g);
        stato.registraAzione(g);
        assertEquals(1, stato.getRound());
    }

    @Test
    void conUnaNuovaOndataIlRoundContinua() {
        Personaggio g = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a1 = negliAvversari();
        stato.reimposta();
        stato.registraAzione(g);
        stato.registraAzione(a1);
        assertEquals(2, stato.getRound());
        // I mostri sconfitti se ne vanno e ne arrivano di nuovi: il round non riparte
        a1.muore("di prova");
        Personaggio a2 = negliAvversari();
        assertEquals(2, stato.getRound());
        stato.registraAzione(g);
        assertEquals(2, stato.getRound(), "il nuovo mostro non ha ancora agito");
        stato.registraAzione(a2);
        assertEquals(3, stato.getRound());
    }

    @Test
    void reimpostaRiportaTuttoAUno() {
        Personaggio g = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a = negliAvversari();
        stato.reimposta();
        stato.registraAzione(g);
        stato.registraAzione(a);
        stato.getProssimoAttaccante(avversari);
        stato.reimposta();
        assertEquals(1, stato.getRound());
        assertFalse(stato.haAgitoNelRound(g));
        assertSame(a, stato.getProssimoAttaccante(avversari), "il giro riparte dal primo");
    }
}
