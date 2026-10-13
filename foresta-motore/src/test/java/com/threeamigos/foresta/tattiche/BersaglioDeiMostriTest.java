package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Cantastorie;
import com.threeamigos.foresta.personaggi.Elfa;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Maga;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.personaggi.Sacerdotessa;
import com.threeamigos.foresta.personaggi.Troll;
import com.threeamigos.foresta.personaggi.Viandante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chi attacca un mostro con la sua tattica base (RegistroTattiche): il Mago o la Maga più potenti, poi il Sacerdote o la Sacerdotessa più potenti, poi l'Elfo o l'Elfa più potenti, poi il Bardo
 * o la Cantastorie più potenti, poi uno a caso fra i personaggi vivi e gli ospiti vulnerabili. Con i gruppi veri della partita (con gli ospiti).
 */
class BersaglioDeiMostriTest {

    private GruppoGiocatore gruppo;
    private Personaggio mostro;
    private Gruppo gruppoDeiMostri;

    @BeforeEach
    void preparaGruppo() {
        ModelloDati.setIstanza(new ModelloDati());
        StatoDelCombattimento.getIstanza().reimposta();
        gruppo = new GruppoGiocatore();
        mostro = new Troll(1);
        gruppoDeiMostri = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return false;
            }
        };
        gruppoDeiMostri.rimuoviPersonaggi();
        gruppoDeiMostri.aggiungiPersonaggio(mostro);
    }

    /**
     * Chi sceglie un mostro come bersaglio, o null se non c'è nessuno da attaccare
     */
    private Personaggio scegliBersaglio(GruppoGiocatore miei) {
        Decisione decisione = Valutatore.decidi(RegistroTattiche.tatticaDiOggi(), ContestoTattico.per(mostro, miei, gruppoDeiMostri));
        return decisione == null ? null : decisione.getBersaglio();
    }

    @Test
    void ilMagoPiuPotenteEIlPrimoBersaglioAncheConUnOspiteVulnerabile() {
        Personaggio guerriero = new Guerriero("G", 9);
        Personaggio mago = new Mago("M", 3);
        Personaggio maga = new Maga("N", 5);
        Personaggio sacerdote = new Sacerdote("S", 9);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(mago);
        gruppo.aggiungiPersonaggio(maga);
        gruppo.aggiungiPersonaggio(sacerdote);
        gruppo.aggiungiOspite(new Viandante("Ottone", 1), true);
        for (int i = 0; i < 100; i++) {
            assertSame(maga, scegliBersaglio(gruppo));
        }
    }

    @Test
    void senzaMaghiIlSacerdoteOLaSacerdotessaPiuPotenti() {
        Personaggio guerriero = new Guerriero("G", 9);
        Personaggio sacerdote = new Sacerdote("S", 2);
        Personaggio sacerdotessa = new Sacerdotessa("T", 4);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(sacerdote);
        gruppo.aggiungiPersonaggio(sacerdotessa);
        gruppo.aggiungiOspite(new Viandante("Ottone", 1), true);
        for (int i = 0; i < 100; i++) {
            assertSame(sacerdotessa, scegliBersaglio(gruppo));
        }
    }

    @Test
    void senzaMaghiNeSacerdotiGliElfiPiuPotenti() {
        Personaggio guerriero = new Guerriero("G", 9);
        Personaggio elfo = new Elfo("E", 2);
        Personaggio elfa = new Elfa("F", 4);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(elfo);
        gruppo.aggiungiPersonaggio(elfa);
        gruppo.aggiungiOspite(new Viandante("Ottone", 1), true);
        for (int i = 0; i < 100; i++) {
            assertSame(elfa, scegliBersaglio(gruppo));
        }
    }

    @Test
    void senzaMaghiSacerdotiNeElfiIlBardoOLaCantastorieDiLivelloPiuAlto() {
        Personaggio guerriero = new Guerriero("G", 9);
        Personaggio bardo = new Bardo("B", 2);
        Personaggio cantastorie = new Cantastorie("C", 4);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(bardo);
        gruppo.aggiungiPersonaggio(cantastorie);
        gruppo.aggiungiOspite(new Viandante("Ottone", 1), true);
        for (int i = 0; i < 100; i++) {
            assertSame(cantastorie, scegliBersaglio(gruppo));
        }
    }

    @Test
    void ilBardoVieneDopoGliElfi() {
        Personaggio bardo = new Bardo("B", 9);
        Personaggio elfa = new Elfa("F", 1);
        gruppo.aggiungiPersonaggio(bardo);
        gruppo.aggiungiPersonaggio(elfa);
        assertSame(elfa, scegliBersaglio(gruppo));
    }

    @Test
    void unMagoMortoNonEUnBersaglio() {
        Personaggio guerriero = new Guerriero("G", 9);
        Personaggio mago = new Mago("M", 3);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(mago);
        mago.muore("di prova");
        assertSame(guerriero, scegliBersaglio(gruppo));
    }

    @Test
    void senzaMaghiNeSacerdotiSiSceglieACasoComprendendoLOspiteVulnerabile() {
        Personaggio guerriero = new Guerriero("G", 9);
        Personaggio ladro = new Ladro("L", 3);
        Personaggio ostaggio = new Viandante("Ottone", 1);
        Personaggio pellegrino = new Viandante("Anselmo", 1);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(ladro);
        gruppo.aggiungiOspite(ostaggio, true);
        gruppo.aggiungiOspite(pellegrino);
        int[] scelti = new int[3];
        for (int i = 0; i < 3000; i++) {
            Personaggio bersaglio = scegliBersaglio(gruppo);
            assertNotSame(pellegrino, bersaglio);
            scelti[bersaglio == guerriero ? 0 : bersaglio == ladro ? 1 : 2]++;
        }
        for (int quanti : scelti) {
            assertTrue(quanti > 800 && quanti < 1200, "scelti: " + java.util.Arrays.toString(scelti));
        }
    }

    @Test
    void conTuttiInPanchinaNonCeNessunoDaAttaccare() {
        assertNull(scegliBersaglio(gruppo));
    }
}
