package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.SupertipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Chi è SPAVENTATO si difende peggio: SAGGEZZA e CORAGGIO mitigano la penalità ma non la annullano.
 * Attacco e difesa fisici sono fissati (40 contro 40, probabilità 75) per restare lontano dai limiti 5-95.
 */
class CalcolatoreCombattimentoSpaventatoTest {

    private Guerriero attaccante;
    private Guerriero difensore;

    @BeforeEach
    void preparaCombattenti() {
        ModelloDati.setIstanza(new ModelloDati());
        attaccante = new Guerriero("Attaccante", 5);
        fissa(attaccante, TipoAttributo.PRECISIONE, 20);
        fissa(attaccante, TipoAttributo.DESTREZZA, 20);
        fissa(attaccante, TipoAttributo.STANCHEZZA, 0);
        difensore = new Guerriero("Difensore", 5);
        fissa(difensore, TipoAttributo.VELOCITA, 20);
        fissa(difensore, TipoAttributo.DESTREZZA, 20);
        fissa(difensore, TipoAttributo.PARATA, 0);
        fissa(difensore, TipoAttributo.STANCHEZZA, 0);
    }

    @Test
    void unoSpaventatoSiDifendePeggio() {
        // Given
        int senzaSpavento = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(attaccante, difensore, SupertipoDanno.FISICO);
        // When
        difensore.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 1, 0);
        int conSpavento = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(attaccante, difensore, SupertipoDanno.FISICO);
        // Then
        assertTrue(conSpavento > senzaSpavento, "spaventato " + conSpavento + ", non spaventato " + senzaSpavento);
    }

    @Test
    void saggezzaECoraggioAltissimiNonAnnullanoLaPenalita() {
        // Given
        fissa(difensore, TipoAttributo.SAGGEZZA, 1000);
        fissa(difensore, TipoAttributo.CORAGGIO, 1000);
        int senzaSpavento = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(attaccante, difensore, SupertipoDanno.FISICO);
        // When
        difensore.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 1, 0);
        int conSpavento = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(attaccante, difensore, SupertipoDanno.FISICO);
        // Then
        assertTrue(conSpavento > senzaSpavento, "spaventato " + conSpavento + ", non spaventato " + senzaSpavento);
    }

    private static void fissa(Guerriero personaggio, TipoAttributo attributo, int valore) {
        personaggio.addModificatore(new ModificatoreAttributo(attributo, TipoModificatore.QUANTITA_ASSOLUTA, valore));
    }
}
