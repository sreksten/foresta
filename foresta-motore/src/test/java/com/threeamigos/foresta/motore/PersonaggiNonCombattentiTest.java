package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.AlbaSacra;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.missioni.LaSfidaDeiCampioni;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Locandiere;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Locandiere, armaiolo, alchimista, venditore di pergamene, incantatore e moglie del bardo: personaggi non
 * combattenti, con le caratteristiche del bardo un poco più basse e nessuna capacità propria.
 */
class PersonaggiNonCombattentiTest {

    private static final List<TipoPersonaggio> NON_COMBATTENTI = Arrays.asList(
            TipoPersonaggio.LOCANDIERE, TipoPersonaggio.ARMAIOLO, TipoPersonaggio.ALCHIMISTA,
            TipoPersonaggio.VENDITORE_DI_PERGAMENE, TipoPersonaggio.INCANTATORE, TipoPersonaggio.MOGLIE_DEL_BARDO);

    private static final TipoAttributo[] PRIMARI = {TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE,
            TipoAttributo.INTELLIGENZA, TipoAttributo.SAGGEZZA, TipoAttributo.CARISMA, TipoAttributo.FORTUNA};

    @BeforeEach
    void preparaModello() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    @Test
    void siCreanoDallaFabbricaConIlLoroTipo() {
        for (TipoPersonaggio tipo : NON_COMBATTENTI) {
            Personaggio personaggio = FabbricaPersonaggi.crea(tipo, 1);
            assertEquals(tipo, personaggio.getClasse());
            assertFalse(personaggio.getNomeSingolare().isEmpty());
            assertFalse(personaggio.getNomePlurale().isEmpty());
        }
        assertEquals(Personaggio.Sesso.FEMMINA, FabbricaPersonaggi.crea(TipoPersonaggio.MOGLIE_DEL_BARDO, 1).getSesso());
        assertEquals("Moglie del Bardo", FabbricaPersonaggi.crea(TipoPersonaggio.MOGLIE_DEL_BARDO, 1).getNomeSingolare());
        assertEquals("Venditori di pergamene", FabbricaPersonaggi.crea(TipoPersonaggio.VENDITORE_DI_PERGAMENE, 1).getNomePlurale());
        assertEquals("Oste", new Locandiere("Oste", 3).getNomeProprio().orElse("?"));
    }

    @Test
    void sonoUnPocoPiuDeboliDelBardo() {
        Personaggio bardo = FabbricaPersonaggi.modello(TipoPersonaggio.BARDO);
        for (TipoPersonaggio tipo : NON_COMBATTENTI) {
            Personaggio personaggio = FabbricaPersonaggi.modello(tipo);
            for (TipoAttributo attributo : PRIMARI) {
                double massimo = personaggio.getMaxStatistica(attributo);
                double massimoDelBardo = bardo.getMaxStatistica(attributo);
                assertTrue(massimo < massimoDelBardo, tipo + " " + attributo + ": " + massimo + " non è sotto " + massimoDelBardo);
                assertTrue(massimo > massimoDelBardo * 0.7, tipo + " " + attributo + ": troppo più debole del bardo");
            }
            assertTrue(personaggio.getSaluteBase() < bardo.getSaluteBase());
            assertTrue(personaggio.getSaluteBase() > bardo.getSaluteBase() * 0.7);
        }
    }

    @Test
    void nonHannoCapacitaPropriePerNaturaENonSiIncontranoComeAmici() {
        for (TipoPersonaggio tipo : NON_COMBATTENTI) {
            assertFalse(DardoArcano.conosciutoDa(tipo));
            assertFalse(AlbaSacra.conosciutaDa(tipo));
            Personaggio modello = FabbricaPersonaggi.modello(tipo);
            assertFalse(modello.isAmichevole(), tipo + " non è un avversario con cui fare amicizia");
            assertFalse(LaSfidaDeiCampioni.classiAmichevoli().contains(tipo), tipo + " non è un campione");
        }
    }

    @Test
    void crescendoDiLivelloAumentanoMaRestanoSottoIlBardo() {
        Personaggio bardo = FabbricaPersonaggi.crea(TipoPersonaggio.BARDO, 5);
        for (TipoPersonaggio tipo : NON_COMBATTENTI) {
            Personaggio basso = FabbricaPersonaggi.crea(tipo, 1);
            Personaggio alto = FabbricaPersonaggi.crea(tipo, 5);
            assertEquals(5, alto.getLivello());
            assertTrue(alto.getSaluteMassima() > basso.getSaluteMassima(), tipo + ": la salute non cresce col livello");
            assertTrue(alto.getSaluteMassima() < bardo.getSaluteMassima() * 1.05, tipo + " a livello 5 supera il bardo");
        }
    }
}
