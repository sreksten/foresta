package com.threeamigos.foresta.tattiche.modifica;

import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.modellodati.PredicatoMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.tipi.TipoConfronto;
import com.threeamigos.foresta.tipi.TipoFiltro;
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
    void laSpiegazioneDiUnaVoceSiLeggeComeUnDiscorso() {
        VoceMD voce = com.threeamigos.foresta.tattiche.TatticheBase.deiGiocabili(com.threeamigos.foresta.tipi.TipoPersonaggio.GUERRIERO).getVoci().get(2);
        assertEquals(java.util.Arrays.asList(
                "Vale sempre, appena si può fare.",
                "Guarda tra i nemici.",
                "Tiene solo chi rispetta tutti questi requisiti: salute in percentuale minore di 50.",
                "Se ne restano più di uno, sceglie quello con meno salute.",
                "Poi prova a fare: Attacco fisico.",
                "Se nessuna azione riesce, salta questa voce e passa a quella dopo."), TestoTattica.spiegazione(voce));
    }

    @Test
    void ogniElementoHaUnaSpiegazione() {
        for (com.threeamigos.foresta.tipi.TipoRosa t : com.threeamigos.foresta.tipi.TipoRosa.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (TipoFiltro t : TipoFiltro.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (com.threeamigos.foresta.tipi.TipoPredicato t : com.threeamigos.foresta.tipi.TipoPredicato.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (com.threeamigos.foresta.tipi.TipoOrdinamento t : com.threeamigos.foresta.tipi.TipoOrdinamento.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (com.threeamigos.foresta.tipi.CriterioIncantesimo t : com.threeamigos.foresta.tipi.CriterioIncantesimo.values()) {
            assertFalse(TestoTattica.spiega(t).isEmpty(), t.name());
        }
        for (com.threeamigos.foresta.tipi.TipoAzione t : com.threeamigos.foresta.tipi.TipoAzione.values()) {
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
