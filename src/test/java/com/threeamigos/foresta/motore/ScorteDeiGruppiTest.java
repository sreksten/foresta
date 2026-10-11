package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaIncantesimi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniSalute;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le scorte di incantesimi e di pozioni sono di ogni gruppo, non solo di quello del giocatore: le variazioni di quello del
 * giocatore si annunciano alla UI, quelle del gruppo avversario no.
 */
class ScorteDeiGruppiTest {

    private final List<Object> annunci = new ArrayList<>();
    private final Consumer<NotificaVariazioneDisponibilitaIncantesimi> ascoltaIncantesimi = annunci::add;
    private final Consumer<NotificaVariazioneDisponibilitaPozioniSalute> ascoltaPozioni = annunci::add;

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
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaIncantesimi.class, ascoltaIncantesimi);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSalute.class, ascoltaPozioni);
    }

    @AfterEach
    void ripristina() {
        BusEventi.cancellati(NotificaVariazioneDisponibilitaIncantesimi.class, ascoltaIncantesimi);
        BusEventi.cancellati(NotificaVariazioneDisponibilitaPozioniSalute.class, ascoltaPozioni);
        BusEventi.impostaConsegna(null);
    }

    @Test
    void ilGruppoAvversarioHaLeSueScorte() {
        avversari.addIncantesimi(ClasseIncantesimo.FUOCO, 2);
        avversari.addPozioniSalute(1);
        avversari.addPozioniMagia(3);
        assertEquals(2, avversari.getIncantesimi(ClasseIncantesimo.FUOCO));
        assertEquals(1, avversari.getPozioniSalute());
        assertEquals(3, avversari.getPozioniMagia());
        avversari.subIncantesimi(ClasseIncantesimo.FUOCO, 1);
        assertEquals(1, avversari.getIncantesimi(ClasseIncantesimo.FUOCO));
        // Non sono quelle del giocatore
        assertEquals(0, giocatore.getIncantesimi(ClasseIncantesimo.FUOCO));
    }

    @Test
    void leVariazioniDelGruppoAvversarioNonSiAnnunciano() {
        avversari.addIncantesimi(ClasseIncantesimo.GELO, 1);
        avversari.addPozioniSalute(2);
        assertTrue(annunci.isEmpty(), String.valueOf(annunci));
        giocatore.addIncantesimi(ClasseIncantesimo.GELO, 1);
        giocatore.addPozioniSalute(2);
        assertEquals(2, annunci.size());
    }

    @Test
    void unaPozioneDelGruppoAvversarioSiConsumaECuraChiLaBeve() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 5);
        avversari.aggiungiPersonaggio(goblin);
        goblin.subSalute(goblin.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int salute = goblin.getSalute();
        avversari.addPozioniSalute(1);
        avversari.consumaPozioneSalute(goblin);
        assertEquals(0, avversari.getPozioniSalute());
        assertTrue(goblin.getSalute() > salute);
    }

    @Test
    void leScorteDelGruppoAvversarioSiAzzeranoConLaLocazione() {
        avversari.addIncantesimi(ClasseIncantesimo.ARIA, 2);
        avversari.addPozioniMagia(1);
        avversari.reimposta();
        assertEquals(0, avversari.getIncantesimi(ClasseIncantesimo.ARIA));
        assertEquals(0, avversari.getPozioniMagia());
    }
}
