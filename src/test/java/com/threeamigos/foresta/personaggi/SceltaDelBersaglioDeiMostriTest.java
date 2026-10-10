package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chi attacca un mostro: il Mago o la Maga più potenti, poi il Sacerdote o la Sacerdotessa più potenti, poi uno a
 * caso fra i personaggi vivi e gli ospiti vulnerabili.
 */
class SceltaDelBersaglioDeiMostriTest {

    private GruppoGiocatore gruppo;

    @BeforeEach
    void preparaGruppo() {
        ModelloDati.setIstanza(new ModelloDati());
        gruppo = new GruppoGiocatore();
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
            assertSame(maga, PersonaggioBase.scegliBersaglio(gruppo));
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
            assertSame(sacerdotessa, PersonaggioBase.scegliBersaglio(gruppo));
        }
    }

    @Test
    void unMagoMortoNonEUnBersaglio() {
        Personaggio guerriero = new Guerriero("G", 9);
        Personaggio mago = new Mago("M", 3);
        gruppo.aggiungiPersonaggio(guerriero);
        gruppo.aggiungiPersonaggio(mago);
        mago.muore("di prova");
        assertSame(guerriero, PersonaggioBase.scegliBersaglio(gruppo));
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
            Personaggio bersaglio = PersonaggioBase.scegliBersaglio(gruppo);
            assertNotSame(pellegrino, bersaglio);
            scelti[bersaglio == guerriero ? 0 : bersaglio == ladro ? 1 : 2]++;
        }
        for (int quanti : scelti) {
            assertTrue(quanti > 800 && quanti < 1200, "scelti: " + java.util.Arrays.toString(scelti));
        }
    }

    @Test
    void conTuttiInPanchinaNonCeNessunoDaAttaccare() {
        assertNull(PersonaggioBase.scegliBersaglio(gruppo));
    }
}
