package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.notizie.Notizie;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Cantastorie;
import com.threeamigos.foresta.personaggi.Elfa;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.Guerriera;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladra;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Maga;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.strumenti.dado.Dado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le notizie delle locande per ogni classe che le può nominare, con il personaggio dell'altro genere o senza nessuno, e la scelta di una
 * notizia quando tutte quelle di una locanda sono tra le ultime già raccontate.
 */
class NotizieDelleLocandeCasiRimastiTest {

    @BeforeEach
    @AfterEach
    void gruppoPulito() {
        BusEventi.azzera();
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        GruppoGiocatore.azzeraIstanza();
        Dado.ripristina();
        Dado.impostaSeme(7);
    }

    private static void gruppoDi(com.threeamigos.foresta.personaggi.Personaggio... personaggi) {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        for (com.threeamigos.foresta.personaggi.Personaggio personaggio : personaggi) {
            gruppo.aggiungiPersonaggio(personaggio);
        }
    }

    @Test
    void ilBardoSiRiconoscePerCantastorieEViceversa() {
        gruppoDi(new Guerriero("Arsenio", 1), new Cantastorie("Dora", 1));
        assertEquals("Dora canta.", NotizieDelleLocande.validaNotizia("BARDO canta."));
        assertEquals("Dora canta.", NotizieDelleLocande.validaNotizia("CANTASTORIE canta."));
    }

    @Test
    void senzaBardoNeCantastorieLaNotiziaNonSiApplica() {
        gruppoDi(new Guerriero("Arsenio", 1));
        assertNull(NotizieDelleLocande.validaNotizia("BARDO canta."));
    }

    @Test
    void laLadraBastaPerUnaNotiziaSulLadro() {
        gruppoDi(new Guerriero("Arsenio", 1), new Ladra("Gualtiera", 1));
        assertEquals("Gualtiera ruba.", NotizieDelleLocande.validaNotizia("LADRO ruba."));
        assertEquals("Gualtiera ruba.", NotizieDelleLocande.validaNotizia("LADRA ruba."));
    }

    @Test
    void ilLadroBastaPerUnaNotiziaSullaLadra() {
        gruppoDi(new Guerriero("Arsenio", 1), new Ladro("Gualtiero", 1));
        assertEquals("Gualtiero ruba.", NotizieDelleLocande.validaNotizia("LADRA ruba."));
    }

    @Test
    void ilGuerrieroELaGuerrieraSiSostituisconoFraLoro() {
        gruppoDi(new Guerriera("Brunilde", 1));
        assertEquals("Brunilde combatte.", NotizieDelleLocande.validaNotizia("GUERRIERO combatte."));
        assertEquals("Brunilde combatte.", NotizieDelleLocande.validaNotizia("GUERRIERA combatte."));
        GruppoGiocatore.azzeraIstanza();
        gruppoDi(new Guerriero("Arsenio", 1));
        assertEquals("Arsenio combatte.", NotizieDelleLocande.validaNotizia("GUERRIERA combatte."));
    }

    @Test
    void senzaGuerrieriLaNotiziaNonSiApplica() {
        gruppoDi(new Ladro("Gualtiero", 1));
        assertNull(NotizieDelleLocande.validaNotizia("GUERRIERO combatte."));
    }

    @Test
    void ilMagoELaMagaSiSostituisconoFraLoro() {
        gruppoDi(new Guerriero("Arsenio", 1), new Maga("Merlina", 1));
        assertEquals("Merlina incanta.", NotizieDelleLocande.validaNotizia("MAGO incanta."));
        assertEquals("Merlina incanta.", NotizieDelleLocande.validaNotizia("MAGA incanta."));
        GruppoGiocatore.azzeraIstanza();
        gruppoDi(new Guerriero("Arsenio", 1), new Mago("Merlino", 1));
        assertEquals("Merlino incanta.", NotizieDelleLocande.validaNotizia("MAGA incanta."));
    }

    @Test
    void senzaMaghiLaNotiziaNonSiApplica() {
        gruppoDi(new Guerriero("Arsenio", 1));
        assertNull(NotizieDelleLocande.validaNotizia("MAGO incanta."));
    }

    @Test
    void l_elfoEL_elfaSiSostituisconoFraLoro() {
        gruppoDi(new Guerriero("Arsenio", 1), new Elfa("Elinor", 1));
        assertEquals("Elinor tira.", NotizieDelleLocande.validaNotizia("ELFO tira."));
        assertEquals("Elinor tira.", NotizieDelleLocande.validaNotizia("ELFA tira."));
        GruppoGiocatore.azzeraIstanza();
        gruppoDi(new Guerriero("Arsenio", 1), new Elfo("Elrond", 1));
        assertEquals("Elrond tira.", NotizieDelleLocande.validaNotizia("ELFA tira."));
    }

    @Test
    void senzaElfiLaNotiziaNonSiApplica() {
        gruppoDi(new Guerriero("Arsenio", 1));
        assertNull(NotizieDelleLocande.validaNotizia("ELFO tira."));
    }

    @Test
    void seTutteLeNotizieDiUnaLocandaSonoRecentiSiRipetonoComunque() {
        gruppoDi(new Guerriero("Arsenio", 1), new Bardo("Dante", 1), new Ladro("Gualtiero", 1), new Guerriera("Brunilde", 1),
                new Mago("Merlino", 1), new Elfo("Elrond", 1));
        // Si segna come recente ogni identificativo che la locanda può dare: la prima ricerca li scarta tutti
        List<NotiziaMD> recenti = new ArrayList<>();
        for (int i = 0; i < 3000; i++) {
            String notizia = ProduttoreDiLocandeCasuale.notiziaGrezza("CINGHIALE_VEGANO");
            String id = notizia.substring(0, notizia.indexOf('-'));
            if (recenti.stream().noneMatch(n -> n.getId().equals(id))) {
                recenti.add(new NotiziaMD(id, "già detta"));
            }
        }
        recenti.forEach(Notizie::aggiungiNotizia);

        NotiziaMD notizia = NotizieDelleLocande.getNotiziaLocanda("CINGHIALE_VEGANO", "il Cinghiale Vegano", "Gino");

        assertNotNull(notizia);
        assertTrue(recenti.stream().anyMatch(n -> n.getId().equals(notizia.getId())));
    }
}
