package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.oggetti.Incantamento;
import com.threeamigos.foresta.personaggi.Guerriero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il BERSERK aumenta il danno fisico di un guerriero ferito, e solo quello (vedi
 * TipoEffettoDiStato.BERSERK). Il critico è neutralizzato (CRITICO dell'attaccante a 0,
 * FORTUNA del difensore altissima) e le armi non hanno incantamenti, così il danno è
 * deterministico: gli effetti di stato tirati a caso non lo modificano.
 */
class CalcolatoreCombattimentoBerserkTest {

    private Guerriero attaccante;
    private Guerriero difensore;

    @BeforeEach
    void preparaCombattenti() {
        ModelloDati.setIstanza(new ModelloDati());
        attaccante = new Guerriero("Berserker", 5);
        attaccante.addModificatore(new ModificatoreAttributo(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        difensore = new Guerriero("Bersaglio", 5);
        difensore.addModificatore(new ModificatoreAttributo(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
        // Ferito: gli resta un quarto della salute massima
        attaccante.getModelloDati().setSalute(attaccante.getSaluteMassima() / 4.0d);
    }

    @Test
    void ilBerserkAumentaIlDannoFisicoDiUnGuerrieroFerito() {
        // Given
        Arma spada = arma(TipoDanno.TAGLIENTE);
        int dannoSenzaBerserk = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, spada).getDanno();
        // When
        attaccante.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 1, 0);
        int dannoConBerserk = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, spada).getDanno();
        // Then
        assertTrue(dannoConBerserk > dannoSenzaBerserk,
                "con BERSERK " + dannoConBerserk + ", senza " + dannoSenzaBerserk);
    }

    @Test
    void ilBerserkNonAumentaIlDannoElementale() {
        // Given
        Arma armaDiFuoco = arma(TipoDanno.FUOCO);
        int dannoSenzaBerserk = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, armaDiFuoco).getDanno();
        // When
        attaccante.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 1, 0);
        int dannoConBerserk = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, armaDiFuoco).getDanno();
        // Then
        assertEquals(dannoSenzaBerserk, dannoConBerserk);
    }

    private static Arma arma(TipoDanno tipoDanno) {
        return new Arma() {
            @Override
            public int getDanni() {
                return 10;
            }

            @Override
            public int getLivello() {
                return 5;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return tipoDanno;
            }

            @Override
            public boolean isIncantata() {
                return false;
            }

            @Override
            public Collection<Incantamento> getIncantamenti() {
                return Collections.emptyList();
            }
        };
    }
}
