package com.threeamigos.foresta.ui.tattiche;

import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.tattiche.FiltroMD;
import com.threeamigos.foresta.modellodati.tattiche.PredicatoMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.tipi.tattiche.TipoConfronto;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * I testi della schermata delle tattiche: ogni voce della tattica di oggi dei mostri ha una riga e una spiegazione leggibili.
 */
class TestoTatticaTest {

    @Test
    void ogniVoceHaUnaRigaEUnaSpiegazione() {
        for (VoceMD voce : RegistroTattiche.tatticaDiOggi().getVoci()) {
            String riga = TestoTattica.riga(voce);
            assertFalse(riga.isEmpty());
            assertFalse(riga.contains("null"), riga);
            for (String testo : TestoTattica.spiegazione(voce)) {
                assertFalse(testo.contains("null"), testo);
            }
        }
    }

    @Test
    void ogniElementoHaUnaSpiegazione() {
        for (com.threeamigos.foresta.tipi.tattiche.TipoRosa t : com.threeamigos.foresta.tipi.tattiche.TipoRosa.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (TipoFiltro t : TipoFiltro.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (com.threeamigos.foresta.tipi.tattiche.TipoPredicato t : com.threeamigos.foresta.tipi.tattiche.TipoPredicato.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento t : com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (com.threeamigos.foresta.tipi.tattiche.CriterioIncantesimo t : com.threeamigos.foresta.tipi.tattiche.CriterioIncantesimo.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (com.threeamigos.foresta.tipi.tattiche.TipoAzione t : com.threeamigos.foresta.tipi.tattiche.TipoAzione.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (TipoConfronto c : TipoConfronto.values()) {
            assertFalse(TestoTattica.spiega(c, false).isEmpty());
            assertFalse(TestoTattica.spiega(c, true).isEmpty());
        }
        assertEquals("Magie abilitate", TipoFiltro.MAGICO.getDescrizione());
    }

    @Test
    void ilConfrontoNegatoSiLeggeComeIlSuoContrario() {
        assertEquals("minore di", TestoTattica.confronto(TipoConfronto.MINORE));
        assertEquals("non maggiore di", TestoTattica.relazione(TipoConfronto.MAGGIORE, true));
        assertEquals("salute minore di 50", TestoTattica.filtro(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 50)));
        assertEquals("salute non minore di 50", TestoTattica.filtro(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 50).negato()));
        assertEquals("senza stato silenziato", TestoTattica.filtro(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO).negato()));
        assertEquals("nel 67% dei casi", TestoTattica.predicato(PredicatoMD.probabilita(67)));
    }
}
