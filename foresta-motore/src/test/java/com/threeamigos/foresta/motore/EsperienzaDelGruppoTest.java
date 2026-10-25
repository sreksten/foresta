package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.personaggi.GestoreProgressione;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPuntiEsperienzaAssegnati;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'esperienza del gruppo: il gruppo la dà ai suoi personaggi e lo dice con un evento, a cui le statistiche si iscrivono per far
 * salire il livello del mondo; quanta ne dà un fatto del gioco lo dice EsperienzaDelGruppo.
 */
class EsperienzaDelGruppoTest {

    private GruppoGiocatore gruppo;
    private final List<InternoPuntiEsperienzaAssegnati> assegnati = new ArrayList<>();

    @BeforeEach
    void nuovoGruppo() {
        BusEventi.azzera();
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore.azzeraIstanza();
        gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggioSenzaNotificare(new Guerriero("Pippo", 1));
        BusEventi.iscriviti(InternoPuntiEsperienzaAssegnati.class, assegnati::add);
    }

    @AfterEach
    void pulisci() {
        BusEventi.azzera();
        GruppoGiocatore.azzeraIstanza();
    }

    @Test
    void ilGruppoDaEsperienzaAiPersonaggiELoDiceConUnEvento() {
        int prima = gruppo.getCapo().getEsperienza();

        gruppo.addPuntiEsperienza(50);

        assertEquals(1, assegnati.size());
        assertEquals(50, assegnati.get(0).getPunti());
        assertEquals(prima + 50, gruppo.getCapo().getEsperienza());
    }

    @Test
    void leStatisticheAscoltanoEdAggiornanoIlLivelloDelMondo() {
        Statistiche.registrati();
        int prima = Statistiche.getPuntiEsperienza();

        gruppo.addPuntiEsperienza(70);

        assertEquals(prima + 70, Statistiche.getPuntiEsperienza());
    }

    @Test
    void senzaStatisticheIscritteIlMondoNonSente() {
        int prima = Statistiche.getPuntiEsperienza();

        gruppo.addPuntiEsperienza(70);

        assertEquals(prima, Statistiche.getPuntiEsperienza());
    }

    @Test
    void unFattoDelGiocoDaUnaPercentualeDiQuelloCheServePerIlLivelloSuccessivo() {
        int richiesti = GestoreProgressione.getXpRichiestiPerProssimoLivello(Statistiche.getLivello());

        EsperienzaDelGruppo.completaMissioneSecondaria();
        EsperienzaDelGruppo.completaMissionePrincipale();
        EsperienzaDelGruppo.acquisisciArtefattoMinore();

        assertEquals(3, assegnati.size());
        assertEquals(richiesti * 20 / 100, assegnati.get(0).getPunti());
        assertEquals(richiesti * 50 / 100, assegnati.get(1).getPunti());
        assertEquals(richiesti * 5 / 100, assegnati.get(2).getPunti());
    }
}
