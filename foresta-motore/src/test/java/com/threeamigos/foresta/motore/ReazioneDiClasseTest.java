package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaReazioneDiClassePersonaggio;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;

/**
 * Quando un personaggio, per la sua classe, è debole, resiste o è immune al tipo di danno che subisce, il danno si moltiplica e
 * parte una notifica che dice a quale tipo (la UI scrive "Debole a Sacro", "Immune a Aria"...).
 */
class ReazioneDiClasseTest {

    private final List<NotificaReazioneDiClassePersonaggio> notifiche = new ArrayList<>();
    private final java.util.function.Consumer<NotificaReazioneDiClassePersonaggio> ascoltatore = notifiche::add;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        BusEventi.impostaConsegna(Runnable::run);
        BusEventi.iscriviti(NotificaReazioneDiClassePersonaggio.class, ascoltatore);
    }

    @AfterEach
    void ripristina() {
        BusEventi.cancellati(NotificaReazioneDiClassePersonaggio.class, ascoltatore);
        BusEventi.impostaConsegna(null);
    }

    private static Arma arma(TipoDanno tipo, IncantamentoMD... incantamenti) {
        return new Arma() {
            @Override public int getDanni() { return 20; }
            @Override public int getLivello() { return 5; }
            @Override public TipoDanno getTipoDanno() { return tipo; }
            @Override public boolean isIncantata() { return incantamenti.length > 0; }
            @Override public Collection<IncantamentoMD> getIncantamenti() { return Arrays.asList(incantamenti); }
        };
    }

    private void colpisci(Personaggio difensore, Arma arma) {
        Personaggio attaccante = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 5);
        difensore.applicaRisultatoCombattimento(CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma));
    }

    @Test
    void unNonMortoColpitoDalSacroEDebole() {
        colpisci(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5), arma(TipoDanno.SACRO));
        assertEquals(1, notifiche.size());
        assertEquals(TipoDanno.SACRO, notifiche.get(0).getTipoDanno());
        assertEquals(2.0d, notifiche.get(0).getMoltiplicatore(), 1e-9);
    }

    @Test
    void unFantasmaColpitoDallAriaEImmune() {
        colpisci(FabbricaPersonaggi.crea(TipoPersonaggio.FANTASMA, 5), arma(TipoDanno.ARIA));
        assertEquals(1, notifiche.size());
        assertEquals(TipoDanno.ARIA, notifiche.get(0).getTipoDanno());
        assertEquals(0.0d, notifiche.get(0).getMoltiplicatore(), 1e-9);
    }

    @Test
    void ancheGliIncantamentiDellArmaContano() {
        colpisci(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5),
                arma(TipoDanno.FUOCO, new IncantamentoMD("Luce", TipoDanno.SACRO, 5, 0.1d)));
        assertEquals(1, notifiche.size());
        assertEquals(TipoDanno.SACRO, notifiche.get(0).getTipoDanno());
    }

    @Test
    void senzaDebolezzeNonParteNiente() {
        colpisci(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 5), arma(TipoDanno.SACRO));
        colpisci(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5), arma(TipoDanno.FUOCO));
        assertTrue(notifiche.isEmpty(), String.valueOf(notifiche));
    }
}
