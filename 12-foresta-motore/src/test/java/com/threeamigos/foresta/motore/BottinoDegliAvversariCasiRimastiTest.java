package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Scheletro;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Altri casi delle scorte degli avversari (vedi {@link BottinoDegliAvversariTest}): un magico senza incantesimi propri non porta
 * pergamene, e a scontro vinto passano al giocatore anche le pozioni grandi e quelle della Magia.
 */
class BottinoDegliAvversariCasiRimastiTest {

    private final List<String> messaggi = new ArrayList<>();

    private GruppoGiocatore giocatore;
    private GruppoAvversario avversari;

    @BeforeEach
    void prepara() {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        giocatore = GruppoGiocatore.getIstanza();
        avversari = GruppoAvversario.getIstanza();
        BusEventi.iscriviti(NotificaTestoParagrafo.class, e -> messaggi.add(e.getMessaggio()));
    }

    @AfterEach
    void ripristina() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
        Dado.ripristina();
    }

    @Test
    void unMagicoSenzaRepertorioNonPortaPergamene() {
        Scheletro senzaIncantesimi = new Scheletro(1) {
            @Override
            public boolean isMagico() {
                return true;
            }

            @Override
            public Set<TipoIncantesimo> getRepertorioIncantesimi() {
                return Collections.emptySet();
            }
        };
        Dado.trucca(1);

        BottinoDegliAvversari.fornisciLeScorte(avversari, Collections.singletonList(senzaIncantesimi));

        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            assertEquals(0, avversari.getIncantesimi(classe));
        }
    }

    @Test
    void aScontroVintoPassanoAncheLePozioniGrandiEQuelleDellaMagia() {
        avversari.addPozioniSaluteGrande(2);
        avversari.addPozioniMagia(1);
        avversari.addPozioniMagiaGrande(3);

        BottinoDegliAvversari.raccogli(giocatore, avversari);

        assertEquals(2, giocatore.getPozioniSaluteGrande());
        assertEquals(1, giocatore.getPozioniMagia());
        assertEquals(3, giocatore.getPozioniMagiaGrande());
        assertEquals(0, avversari.getPozioniSaluteGrande());
        assertEquals(0, avversari.getPozioniMagia());
        assertEquals(0, avversari.getPozioniMagiaGrande());
        assertEquals(1, messaggi.size(), String.valueOf(messaggi));
        assertTrue(messaggi.get(0).contains("2 pozioni grandi della Salute") && messaggi.get(0).contains("1 pozione della Magia")
                && messaggi.get(0).contains("3 pozioni grandi della Magia"), messaggi.get(0));
    }
}
