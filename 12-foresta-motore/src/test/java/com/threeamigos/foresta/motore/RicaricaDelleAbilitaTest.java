package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.abilita.AbilitaInnate;
import com.threeamigos.foresta.canti.CantoDelBardo;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Druido;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.CostantiCanti;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La ricarica delle abilità innate e dei canti: dopo l'uso non si possono riusare finché non è passato qualche turno, e i turni passano a ogni
 * round, a ogni passo fra una locazione e l'altra e a ogni ora di sonno, insieme agli effetti di stato.
 */
class RicaricaDelleAbilitaTest {

    private static Druido druido() {
        Druido druido = new Druido("Quercia", 5);
        druido.addMagia(1000);
        return druido;
    }

    @Test
    void ogniAbilitaEOgniCantoHaUnaRicarica() {
        for (TipoAbilita tipo : TipoAbilita.values()) {
            assertTrue(tipo.getRicarica() > 0, tipo.name());
        }
        for (TipoCanto tipo : TipoCanto.values()) {
            assertTrue(tipo.getRicarica() > 0, tipo.name());
        }
    }

    @Test
    void chiDaUnaDurataSiRicaricaInUnTurnoDiPiu() {
        assertEquals(CostantiAbilitaInnate.ABILITA_PELLE_DI_CORTECCIA_DURATA + 1, TipoAbilita.PELLE_DI_CORTECCIA.getRicarica());
        assertEquals(CostantiAbilitaInnate.ABILITA_RIGENERAZIONE_DURATA + 1, TipoAbilita.RIGENERAZIONE.getRicarica());
        assertEquals(CostantiCanti.CANTO_DI_VALORE_DURATA + 1, TipoCanto.DI_VALORE.getRicarica());
    }

    @Test
    void ilCantoCheCuraDiPiuNonCostaDiMenoDiChiCuraDiMeno() {
        assertTrue(CostantiCanti.CANTO_CURATIVO_BASE > CostantiAbilitaInnate.ABILITA_IMPOSIZIONE_DELLE_MANI_BASE);
        assertTrue(TipoCanto.CURATIVO.getCostoLancio() > TipoAbilita.IMPOSIZIONE_DELLE_MANI.getCostoLancio());
    }

    @Test
    void unAbilitaInRicaricaNonSiPuoUsareFinoAllaFineDeiTurni() {
        Druido druido = druido();
        assertTrue(AbilitaInnate.puoUsare(druido, TipoAbilita.PELLE_DI_CORTECCIA));

        AbilitaInnate.avviaRicarica(druido, TipoAbilita.PELLE_DI_CORTECCIA);

        assertTrue(AbilitaInnate.isInRicarica(druido, TipoAbilita.PELLE_DI_CORTECCIA));
        assertFalse(AbilitaInnate.puoUsare(druido, TipoAbilita.PELLE_DI_CORTECCIA));
        assertTrue(AbilitaInnate.puoUsare(druido, TipoAbilita.RADICI), "le altre abilità non c'entrano");
        for (int turno = 1; turno < TipoAbilita.PELLE_DI_CORTECCIA.getRicarica(); turno++) {
            druido.trascorriUnTurno();
            assertFalse(AbilitaInnate.puoUsare(druido, TipoAbilita.PELLE_DI_CORTECCIA), "dopo " + turno + " turni");
        }
        druido.trascorriUnTurno();
        assertTrue(AbilitaInnate.puoUsare(druido, TipoAbilita.PELLE_DI_CORTECCIA));
        assertTrue(druido.getRicariche().isEmpty());
    }

    @Test
    void unCantoInRicaricaNonSiPuoCantare() {
        Bardo bardo = new Bardo("Orfeo", 5);
        bardo.addMagia(1000);
        assertTrue(CantoDelBardo.puoCantare(bardo, TipoCanto.CURATIVO));

        CantoDelBardo.avviaRicarica(bardo, TipoCanto.CURATIVO);

        assertFalse(CantoDelBardo.puoCantare(bardo, TipoCanto.CURATIVO));
        assertTrue(CantoDelBardo.puoCantare(bardo, TipoCanto.IPNOTICO));
        for (int turno = 0; turno < TipoCanto.CURATIVO.getRicarica(); turno++) {
            bardo.trascorriUnTurno();
        }
        assertTrue(CantoDelBardo.puoCantare(bardo, TipoCanto.CURATIVO));
    }

    @Test
    void ilTurnoFaScalareInsiemeLeRicaricheEGliEffettiDiStato() {
        Guerriero guerriero = new Guerriero("Reginald", 3);
        int salute = guerriero.getSalute();
        guerriero.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 2, 5);
        guerriero.setRicarica("RADICI", 3);

        guerriero.trascorriUnTurno();

        assertEquals(salute - 5, guerriero.getSalute(), "il sanguinamento fa il suo danno");
        assertTrue(guerriero.hasEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO));
        assertEquals(2, guerriero.getRicarica("RADICI"));

        guerriero.trascorriUnTurno();

        assertEquals(salute - 10, guerriero.getSalute());
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO), "scaduto");
        assertEquals(1, guerriero.getRicarica("RADICI"));
    }

    @Test
    void ancheChiDaFineLocazioneSanguinaECalaLaRicarica() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio capo = partita.gruppo().getCapo();
            int saluteAttesa = Math.min(capo.getSaluteMassima(), capo.getSalute() + capo.getRigenerazioneSalute()) - 4;
            capo.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 4);
            capo.setRicarica("SCIAME", 3);

            partita.comando(Comando.ESCI_DA_CITTA);

            assertEquals(2, capo.getRicarica("SCIAME"), "la ricarica cala per ogni passo fra una locazione e l'altra");
            assertTrue(capo.hasEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO));
            assertEquals(2, capo.getModelloDati().getEffettiDiStato().iterator().next().getDurata());
            // Prima il recupero di fine locazione, poi il danno del sanguinamento
            assertEquals(saluteAttesa, capo.getSalute());
        }
    }
}
