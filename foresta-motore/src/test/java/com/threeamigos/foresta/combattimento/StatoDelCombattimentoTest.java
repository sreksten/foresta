package com.threeamigos.foresta.combattimento;

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
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;

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

    private static StatoDelCombattimento.Lato G = StatoDelCombattimento.Lato.GIOCATORE;
    private static StatoDelCombattimento.Lato A = StatoDelCombattimento.Lato.AVVERSARI;

    @Test
    void ilPianoFaLeCoppieEPoiChiAvanzaAgisceDaSolo() {
        // 3 miei contro 2 avversari: due coppie, e il terzo dei miei senza risposta
        nelGiocatore(TipoPersonaggio.GUERRIERO);
        nelGiocatore(TipoPersonaggio.MAGO);
        nelGiocatore(TipoPersonaggio.LADRO);
        negliAvversari();
        negliAvversari();
        stato.reimposta();
        assertEquals(java.util.Arrays.asList(G, A, G, A, G), stato.getPiano());

        // 1 mio contro 3 avversari: il mio, e poi i tre avversari uno dietro l'altro
        StatoDelCombattimento.azzeraIstanza();
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        giocatore = GruppoGiocatore.getIstanza();
        avversari = GruppoAvversario.getIstanza();
        stato = StatoDelCombattimento.getIstanza();
        nelGiocatore(TipoPersonaggio.GUERRIERO);
        negliAvversari();
        negliAvversari();
        negliAvversari();
        assertEquals(java.util.Arrays.asList(G, A, A, A), stato.getPiano());
    }

    @Test
    void ilPianoNonContaChiNonPuoAgire() {
        nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio panchina = nelGiocatore(TipoPersonaggio.MAGO);
        panchina.setInPanchina(true);
        Personaggio caduto = nelGiocatore(TipoPersonaggio.LADRO);
        caduto.muore("di prova");
        negliAvversari();
        assertEquals(java.util.Arrays.asList(G, A), stato.getPiano());
    }

    @Test
    void ilProssimoLatoSegueIlPiano() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio g2 = nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        stato.reimposta();
        assertEquals(G, stato.getProssimoLato());
        assertFalse(stato.registraAzione(g1));
        assertEquals(A, stato.getProssimoLato());
        assertFalse(stato.registraAzione(a1));
        assertEquals(G, stato.getProssimoLato());
        assertFalse(stato.registraAzione(g2));
        assertEquals(A, stato.getProssimoLato());
        assertTrue(stato.registraAzione(a2), "con l'ultimo posto il round finisce");
    }

    @Test
    void ilRoundFinisceQuandoIlPianoESuperato() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        stato.reimposta();
        roundAnnunciati.clear();

        // Il giocatore fa agire sempre lo stesso personaggio: il round arriva comunque a una fine
        stato.registraAzione(g1);
        stato.registraAzione(a1);
        assertEquals(1, stato.getRound(), "il piano non è finito");
        assertTrue(stato.haAgitoNelRound(g1));
        stato.registraAzione(g1);
        stato.registraAzione(a2);
        assertEquals(2, stato.getRound());
        assertEquals(java.util.Collections.singletonList(2), roundAnnunciati);
        assertFalse(stato.haAgitoNelRound(g1), "nel round nuovo nessuno ha ancora agito");
        assertEquals(java.util.Arrays.asList(G, A, G, A), stato.getPiano(), "il piano si rifà a ogni round");
    }

    @Test
    void chiCadeLasciaUnPostoCheSiSalta() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        stato.reimposta();
        assertEquals(java.util.Arrays.asList(G, A, A), stato.getPiano());
        stato.registraAzione(g1);
        stato.registraAzione(a1);
        // Il piano non si rifà quando qualcuno cade: il posto di chi non c'è più si salta
        a2.muore("di prova");
        assertEquals(A, stato.getProssimoLato());
        assertTrue(stato.registraAzioneSaltata(A));
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
    void leAzioniManualiFannoAgirePoiIlSuccessivo() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio g2 = nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio g3 = nelGiocatore(TipoPersonaggio.LADRO);
        negliAvversari();
        stato.reimposta();
        // Il giocatore sceglie il secondo: il giro automatico riparte dal terzo
        stato.registraAzione(g2);
        assertSame(g3, stato.getProssimoAttaccante(giocatore));
        assertSame(g1, stato.getProssimoAttaccante(giocatore));
    }

    @Test
    void faAgirePerPrimoSceglieDaChiRicominciare() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio g3 = nelGiocatore(TipoPersonaggio.LADRO);
        stato.faAgirePerPrimo(giocatore, g3);
        assertSame(g3, stato.getProssimoAttaccante(giocatore));
        assertSame(g1, stato.getProssimoAttaccante(giocatore));
        stato.faAgirePerPrimo(giocatore, g1);
        assertSame(g1, stato.getProssimoAttaccante(giocatore));
    }

    @Test
    void conUnaNuovaOndataIlPianoSiRifaEIlRoundContinua() {
        Personaggio g = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a1 = negliAvversari();
        stato.reimposta();
        stato.registraAzione(g);
        stato.registraAzione(a1);
        assertEquals(2, stato.getRound());
        // I mostri sconfitti se ne vanno e ne arrivano di nuovi: il round non riparte
        a1.muore("di prova");
        negliAvversari();
        negliAvversari();
        stato.nuovaOndata();
        assertEquals(2, stato.getRound());
        assertEquals(java.util.Arrays.asList(G, A, A), stato.getPiano());
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
