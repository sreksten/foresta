package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.gruppi.DatiDiProva;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoInizioRound;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
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
        DatiDiProva.nuovaPartita();
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

    private static final StatoDelCombattimento.Lato G = StatoDelCombattimento.Lato.GIOCATORE;
    private static final StatoDelCombattimento.Lato A = StatoDelCombattimento.Lato.AVVERSARI;

    private void ricominciaConGruppiNuovi() {
        StatoDelCombattimento.azzeraIstanza();
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        giocatore = GruppoGiocatore.getIstanza();
        avversari = GruppoAvversario.getIstanza();
        stato = StatoDelCombattimento.getIstanza();
    }

    private List<StatoDelCombattimento.Lato> piano(int miei, int loro) {
        ricominciaConGruppiNuovi();
        for (int i = 0; i < miei; i++) {
            nelGiocatore(TipoPersonaggio.GUERRIERO);
        }
        for (int i = 0; i < loro; i++) {
            negliAvversari();
        }
        return stato.getPiano();
    }

    @Test
    void laListaSaltaChiECaduto() {
        Personaggio a = negliAvversari();
        Personaggio b = negliAvversari();
        Personaggio c = negliAvversari();
        assertSame(a, stato.getProssimoAttaccante(avversari));
        stato.registraAzione(a);
        assertSame(b, stato.getProssimoAttaccante(avversari));
        b.muore("di prova");
        assertSame(c, stato.getProssimoAttaccante(avversari), "chi è caduto non fa niente: si passa al successivo");
        assertSame(c, stato.getProssimoAttaccante(avversari), "il posto si consuma solo quando si registra l'azione");
    }

    @Test
    void ognunoAgisceQuandoTocca() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio g2 = nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        assertSame(g1, stato.getProssimoAttaccante(giocatore));
        assertNull(stato.getProssimoAttaccante(avversari), "non tocca a un avversario");
        stato.registraAzione(g1);
        assertSame(a1, stato.getProssimoAttaccante(avversari));
        stato.registraAzione(a1);
        assertSame(g2, stato.getProssimoAttaccante(giocatore));
        stato.registraAzione(g2);
        assertSame(a2, stato.getProssimoAttaccante(avversari));
    }

    @Test
    void chiEsceDalGruppoSiTogliDallaLista() {
        Personaggio a = negliAvversari();
        Personaggio b = negliAvversari();
        Personaggio c = negliAvversari();
        stato.registraAzione(a);
        assertSame(b, stato.getProssimoAttaccante(avversari));
        avversari.rimuoviPersonaggio(b);
        // b non c'è più: dopo di lui viene c, e la lista non lo ricorda
        assertSame(c, stato.getProssimoAttaccante(avversari));
        assertEquals(java.util.Arrays.asList(a, c), stato.getOrdineDelRound());
    }

    @Test
    void chiArrivaACombattimentoInCorsoVaInFondo() {
        Personaggio g = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a = negliAvversari();
        assertEquals(java.util.Arrays.asList(g, a), stato.getOrdineDelRound());
        Personaggio scheletro = nelGiocatore(TipoPersonaggio.LADRO);
        assertEquals(java.util.Arrays.asList(g, a, scheletro), stato.getOrdineDelRound());
    }

    @Test
    void senzaNessunoInGradoDiAgireNonCeProssimo() {
        Personaggio a = negliAvversari();
        a.muore("di prova");
        assertNull(stato.getProssimoAttaccante(avversari));
        assertNull(stato.getProssimoLato());
    }

    @Test
    void laListaDistribuisceIGruppiInProporzione() {
        assertEquals(java.util.Arrays.asList(G, A), piano(1, 1));
        assertEquals(java.util.Arrays.asList(G, A, G, A, G, A, G, A), piano(4, 4), "a pari numero sono coppie");
        assertEquals(java.util.Arrays.asList(G, A, G, A, G), piano(3, 2));
        assertEquals(java.util.Arrays.asList(G, A, A, A), piano(1, 3));
        assertEquals(java.util.Arrays.asList(G, A, A, A, G, A, A), piano(2, 5));
        assertEquals(java.util.Arrays.asList(G, A, G, A, A, G, A, A), piano(3, 5));
        assertEquals(java.util.Arrays.asList(G, A, G, G, A, G, A, G), piano(5, 3));
        assertEquals(java.util.Arrays.asList(G, G, A, G, G), piano(4, 1), "l'avversario unico sta in mezzo ai miei");
    }

    @Test
    void laListaNonLasciaPiuDiDueDelloStessoGruppoDiSeguito() {
        // Per ogni proporzione fino a 8 contro 8, nel giro (anche da una fine all'altra) la differenza fra le serie è al massimo di uno
        for (int miei = 1; miei <= 8; miei++) {
            for (int loro = 1; loro <= 8; loro++) {
                List<StatoDelCombattimento.Lato> lista = piano(miei, loro);
                int maggiore = Math.max(miei, loro);
                int minore = Math.min(miei, loro);
                int serieMassima = (maggiore + minore - 1) / minore;
                int corrente = 1;
                int massimo = 1;
                for (int i = 1; i < lista.size() * 2; i++) {
                    corrente = lista.get(i % lista.size()) == lista.get((i - 1) % lista.size()) ? corrente + 1 : 1;
                    massimo = Math.max(massimo, corrente);
                }
                assertTrue(massimo <= serieMassima + 1, miei + " contro " + loro + ": " + lista);
            }
        }
    }

    @Test
    void chiNonPuoAgireHaIlSuoPostoMaVieneSaltato() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio panchina = nelGiocatore(TipoPersonaggio.MAGO);
        panchina.setInPanchina(true);
        Personaggio caduto = nelGiocatore(TipoPersonaggio.LADRO);
        caduto.muore("di prova");
        Personaggio a = negliAvversari();
        assertEquals(java.util.Arrays.asList(g1, panchina, a, caduto), stato.getOrdineDelRound(), "ci sono tutti");
        assertEquals(G, stato.getProssimoLato());
        assertFalse(stato.registraAzione(g1));
        assertEquals(A, stato.getProssimoLato());
        assertTrue(stato.registraAzione(a), "gli ultimi due posti non agiscono: il round finisce");
        assertEquals(2, stato.getRound());
    }

    @Test
    void ilProssimoLatoSegueLaLista() {
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
    void ilRoundFinisceQuandoLaListaESuperata() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        stato.reimposta();
        roundAnnunciati.clear();

        // Il giocatore fa agire sempre lo stesso personaggio: la seconda volta l'azione costa il posto di un altro, e il round finisce
        stato.registraAzione(g1);
        stato.registraAzione(a1);
        assertEquals(1, stato.getRound(), "la lista non è finita");
        assertTrue(stato.haAgitoNelRound(g1));
        stato.registraAzione(g1);
        stato.registraAzione(a2);
        assertEquals(2, stato.getRound());
        assertEquals(java.util.Collections.singletonList(2), roundAnnunciati);
        assertFalse(stato.haAgitoNelRound(g1), "nel round nuovo nessuno ha ancora agito");
        assertEquals(java.util.Arrays.asList(G, A, G, A), stato.getPiano(), "la lista è sempre la stessa");
    }

    @Test
    void chiCadeAMetaGiroSiSalta() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        stato.reimposta();
        assertEquals(java.util.Arrays.asList(G, A, A), stato.getPiano());
        stato.registraAzione(g1);
        stato.registraAzione(a1);
        // La lista non si rifà quando qualcuno cade: il suo turno si salta
        a2.muore("di prova");
        assertNull(stato.getProssimoLato());
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
    void leAzioniManualiFannoAgirePrimaENessunoPerdeIlTurno() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio g2 = nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio g3 = nelGiocatore(TipoPersonaggio.LADRO);
        Personaggio a1 = negliAvversari();
        stato.reimposta();
        assertEquals(java.util.Arrays.asList(g1, g2, a1, g3), stato.getOrdineDelRound());
        // Il giocatore fa agire il secondo: tocca ancora al primo
        stato.registraAzione(g2);
        assertSame(g1, stato.getProssimoAttaccante(giocatore));
        stato.registraAzione(g1);
        stato.registraAzione(a1);
        // Il secondo ha già agito: il suo posto si salta e tocca al terzo
        assertSame(g3, stato.getProssimoAttaccante(giocatore));
        assertTrue(stato.registraAzione(g3));
    }

    @Test
    void faAgirePerPrimoFaRipartireLaListaDaLui() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio g2 = nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio g3 = nelGiocatore(TipoPersonaggio.LADRO);
        Personaggio a1 = negliAvversari();
        Personaggio a2 = negliAvversari();
        assertEquals(java.util.Arrays.asList(g1, a1, g2, a2, g3), stato.getOrdineDelRound());
        stato.faAgirePerPrimo(giocatore, g3);
        // La lista riparte da lui: il primo avversario a muoversi è quello che lo segue nel giro
        assertEquals(java.util.Arrays.asList(g3, g1, a1, g2, a2), stato.getOrdineDelRound());
        assertSame(g3, stato.getProssimoAttaccante(giocatore));
        stato.registraAzione(g3);
        assertSame(g1, stato.getProssimoAttaccante(giocatore));
        stato.registraAzione(g1);
        assertSame(a1, stato.getProssimoAttaccante(avversari));
    }

    @Test
    void aGiroAvviatoLaSceltaScambiaConIlProssimoDeiMiei() {
        Personaggio g1 = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio g2 = nelGiocatore(TipoPersonaggio.MAGO);
        Personaggio g3 = nelGiocatore(TipoPersonaggio.LADRO);
        negliAvversari();
        // Lista g1 g2 a g3: dopo g1 toccherebbe a g2, ma il giocatore sceglie g3, che prende il suo posto
        stato.registraAzione(g1);
        stato.faAgirePerPrimo(giocatore, g3);
        assertSame(g3, stato.getProssimoAttaccante(giocatore), "al posto di g2");
        stato.registraAzione(g3);
        stato.registraAzione(stato.getProssimoAttaccante(avversari));
        assertSame(g2, stato.getProssimoAttaccante(giocatore), "g2 prende il posto di g3");
    }

    @Test
    void conUnaNuovaOndataLaListaSiRifaEIlRoundContinua() {
        Personaggio g = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a1 = negliAvversari();
        stato.reimposta();
        stato.registraAzione(g);
        stato.registraAzione(a1);
        assertEquals(2, stato.getRound());
        // I mostri sconfitti se ne vanno e ne arrivano di nuovi (qui il caduto resta nel gruppo): il round non riparte
        a1.muore("di prova");
        negliAvversari();
        negliAvversari();
        stato.nuovaOndata();
        assertEquals(2, stato.getRound());
        assertEquals(java.util.Arrays.asList(G, A, A, A), stato.getPiano());
    }

    @Test
    void reimpostaRiportaTuttoAUno() {
        Personaggio g = nelGiocatore(TipoPersonaggio.GUERRIERO);
        Personaggio a = negliAvversari();
        stato.reimposta();
        stato.registraAzione(g);
        stato.registraAzione(a);
        stato.reimposta();
        assertEquals(1, stato.getRound());
        assertFalse(stato.haAgitoNelRound(g));
        assertEquals(G, stato.getProssimoLato(), "la lista riparte dal primo");
    }
}
