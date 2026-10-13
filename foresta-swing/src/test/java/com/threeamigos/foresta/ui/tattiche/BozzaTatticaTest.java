package com.threeamigos.foresta.ui.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.modellodati.tattiche.FiltroMD;
import com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.tattiche.PredicatoMD;
import com.threeamigos.foresta.modellodati.tattiche.SelettoreMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La bozza della tattica che la schermata modifica: si costruisce dalla tattica, si cambia, e solo se è a posto si trasforma di nuovo in tattica.
 */
class BozzaTatticaTest {

    private static VoceMD attaccoDiRiserva() {
        return VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)),
                AzioneMD.di(TipoAzione.ATTACCO_FISICO));
    }

    @Test
    void l_attaccoDiRiservaInCodaNonEntraNellaBozza() {
        TatticaMD tattica = TatticaMD.di(
                VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE)),
                        AzioneMD.di(TipoAzione.FULMINE)),
                attaccoDiRiserva());
        BozzaTattica bozza = BozzaTattica.da(tattica);
        assertEquals(1, bozza.getVoci().size());
        assertTrue(bozza.problemi().isEmpty());
        assertTrue(bozza.descrive(tattica));
        assertEquals(1, bozza.toTattica().getVoci().size());
        assertTrue(BozzaTattica.da(TatticaMD.VUOTA).getVoci().isEmpty());
    }

    @Test
    void unaVoceNuovaEGiaAPostoESiSposta() {
        BozzaTattica bozza = BozzaTattica.da(TatticaMD.VUOTA);
        BozzaTattica.Voce prima = bozza.aggiungiVoce();
        assertNull(prima.rosa, "una voce nuova è completamente vuota");
        assertTrue(prima.azioni.isEmpty());
        assertFalse(bozza.problemi().isEmpty(), "senza azioni non si conferma");
        bozza.aggiungiAzione(prima, TipoAzione.ATTACCO_FISICO);
        assertEquals(TipoRosa.AVVERSARI, prima.rosa, "la rosa arriva con la prima azione");
        BozzaTattica.Voce seconda = bozza.aggiungiVoce();
        bozza.aggiungiAzione(seconda, TipoAzione.FULMINE);
        assertTrue(bozza.problemi().isEmpty(), bozza.problemi().toString());
        assertFalse(bozza.spostaVoce(0, -1));
        assertTrue(bozza.spostaVoce(1, -1));
        assertSame(seconda, bozza.getVoci().get(0));
        assertFalse(bozza.spostaVoce(1, 1));
        bozza.rimuoviVoce(0);
        assertEquals(1, bozza.getVoci().size());
    }

    @Test
    void ilNomeSegueLaVoceQuandoSiSposta() {
        TatticaMD tattica = TatticaMD.di(VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO)),
                new VoceMD(null, Collections.emptyList(), Collections.singletonList(AzioneMD.di(TipoAzione.FULMINE)), true, "Mia"));
        BozzaTattica bozza = BozzaTattica.da(tattica);
        assertTrue(bozza.spostaVoce(0, 1));
        // La voce che non aveva un nome ha ricevuto "Tattica 1" e se lo porta dietro
        assertEquals("Mia", bozza.getVoci().get(0).nome);
        assertEquals("Tattica 1", bozza.getVoci().get(1).nome);
        assertFalse(bozza.descrive(tattica));
        // Rimessa al suo posto, la tattica torna uguale
        assertTrue(bozza.spostaVoce(1, -1));
        assertTrue(bozza.descrive(tattica));
        assertEquals("Tattica 1", bozza.toTattica().getVoci().get(0).getNome());
    }

    @Test
    void unaVoceSenzaAzioniNonSiPuoConfermare() {
        BozzaTattica bozza = BozzaTattica.da(TatticaMD.VUOTA);
        bozza.aggiungiVoce();
        assertFalse(bozza.problemi().isEmpty());
        assertTrue(bozza.toTattica().getVoci().isEmpty());
    }

    @Test
    void unaAzioneCheNonAccettaLaRosaLaCambia() {
        BozzaTattica bozza = BozzaTattica.da(TatticaMD.VUOTA);
        BozzaTattica.Voce voce = bozza.aggiungiVoce();
        bozza.aggiungiAzione(voce, TipoAzione.ATTACCO_FISICO);
        bozza.sostituisciAzione(voce, 0, TipoAzione.RESURREZIONE);
        assertEquals(TipoRosa.ALLEATI_CADUTI, voce.rosa);
        assertTrue(bozza.problemi().isEmpty(), bozza.problemi().toString());
        // Un'azione di un'altra portata non si può affiancare
        assertFalse(BozzaTattica.azioniAmmesse(voce).contains(TipoAzione.FULMINE));
        assertTrue(BozzaTattica.azioniAmmesse(voce).contains(TipoAzione.RESURREZIONE));
        assertEquals(Collections.singletonList(TipoRosa.ALLEATI_CADUTI), BozzaTattica.roseAmmesse(voce));
        // Una voce vuota con la rosa già scelta propone solo le azioni che la accettano
        BozzaTattica.Voce vuota = bozza.aggiungiVoce();
        vuota.rosa = TipoRosa.ALLEATI_CADUTI;
        assertEquals(Collections.singletonList(TipoAzione.RESURREZIONE), BozzaTattica.azioniAmmesse(vuota));
    }

    @Test
    void ogniTipoHaUnValorePredefinitoCheSiCostruisce() {
        for (TipoFiltro tipo : TipoFiltro.values()) {
            FiltroMD filtro = BozzaTattica.filtroPredefinito(tipo);
            assertEquals(tipo, filtro.getTipo());
            assertFalse(TestoTattica.filtro(filtro).contains("null"));
        }
        for (TipoPredicato tipo : TipoPredicato.values()) {
            assertEquals(tipo, BozzaTattica.predicatoPredefinito(tipo).getTipo());
        }
        for (TipoAzione tipo : TipoAzione.values()) {
            assertEquals(tipo, BozzaTattica.azionePredefinita(tipo).getTipo());
        }
        for (TipoOrdinamento tipo : TipoOrdinamento.values()) {
            assertEquals(tipo, BozzaTattica.ordinamentoPredefinito(tipo).getTipo());
        }
    }

    @Test
    void suSeStessiNonSiChiedeDiClasseCategoriaOChiHaAttaccato() {
        for (TipoFiltro tipo : Arrays.asList(TipoFiltro.CLASSE, TipoFiltro.CATEGORIA, TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoFiltro.IL_CAPO,
                TipoFiltro.ME_STESSO, TipoFiltro.SCELTO_L_ULTIMA_VOLTA, TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO, TipoFiltro.DEBOLE_ALL_AZIONE)) {
            assertFalse(BozzaTattica.filtriAmmessi(TipoRosa.SE_STESSO).contains(tipo), tipo.name());
        }
        for (TipoFiltro tipo : Arrays.asList(TipoFiltro.CLASSE, TipoFiltro.CATEGORIA, TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoFiltro.DEBOLE_ALL_AZIONE)) {
            assertTrue(BozzaTattica.filtriAmmessi(TipoRosa.AVVERSARI).contains(tipo), tipo.name());
        }
        assertTrue(BozzaTattica.filtriAmmessi(TipoRosa.SE_STESSO).contains(TipoFiltro.SALUTE));
        // Cambiando la rosa di una condizione, i filtri che non hanno più senso cadono
        PredicatoMD conClasse = BozzaTattica.conFiltri(BozzaTattica.predicatoPredefinito(TipoPredicato.QUANTI),
                Arrays.asList(BozzaTattica.filtroPredefinito(TipoFiltro.CLASSE), BozzaTattica.filtroPredefinito(TipoFiltro.SALUTE)));
        assertEquals(1, BozzaTattica.conRosa(conClasse, TipoRosa.SE_STESSO).getFiltri().size());
        assertEquals(2, BozzaTattica.conRosa(conClasse, TipoRosa.ALLEATI).getFiltri().size());
    }

    @Test
    void soloIFiltriNonNumericiSiRovesciano() {
        for (TipoFiltro tipo : Arrays.asList(TipoFiltro.SALUTE, TipoFiltro.SALUTE_PERCENTUALE, TipoFiltro.MAGIA, TipoFiltro.LIVELLO,
                TipoFiltro.LIVELLO_RISPETTO_AL_MIO)) {
            assertFalse(BozzaTattica.isNegabile(tipo), tipo.name());
        }
        for (TipoFiltro tipo : Arrays.asList(TipoFiltro.STATO, TipoFiltro.MAGICO, TipoFiltro.CLASSE, TipoFiltro.DEBOLE_A)) {
            assertTrue(BozzaTattica.isNegabile(tipo), tipo.name());
        }
        // "Al massimo" e "almeno" sono i confronti rovesciati
        FiltroMD salute = BozzaTattica.filtroPredefinito(TipoFiltro.SALUTE);
        FiltroMD alMassimo = BozzaTattica.conRelazione(salute, Confronto.MAGGIORE, true);
        assertTrue(alMassimo.isNegato());
        assertEquals("salute non maggiore di 50", TestoTattica.filtro(alMassimo));
        assertEquals("salute non minore di 50", TestoTattica.filtro(BozzaTattica.conRelazione(salute, Confronto.MINORE, true)));
        assertEquals("salute minore di 50", TestoTattica.filtro(BozzaTattica.conRelazione(alMassimo, Confronto.MINORE, false)));
        PredicatoMD round = BozzaTattica.predicatoPredefinito(TipoPredicato.ROUND);
        assertTrue(BozzaTattica.conRelazione(round, Confronto.MINORE, true).isNegato());
    }

    @Test
    void unConfrontoRovesciatoSuUnNumeroSiNormalizza() {
        FiltroMD almeno = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MINORE, 50).negato();
        FiltroMD normale = BozzaTattica.normalizza(almeno);
        assertFalse(normale.isNegato());
        assertEquals(Confronto.MAGGIORE, normale.getConfronto());
        assertEquals(49, normale.getNumero());
        FiltroMD alMassimo = BozzaTattica.normalizza(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MAGGIORE, 50).negato());
        assertEquals(Confronto.MINORE, alMassimo.getConfronto());
        assertEquals(51, alMassimo.getNumero());
        // Ai limiti, e per "diverso da", resta com'è
        FiltroMD zero = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MINORE, 0).negato();
        assertEquals(zero, BozzaTattica.normalizza(zero));
        FiltroMD diverso = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.UGUALE, 5).negato();
        assertEquals(diverso, BozzaTattica.normalizza(diverso));
        // Anche nelle condizioni, e nei filtri di un Quanti
        PredicatoMD round = BozzaTattica.normalizza(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, Confronto.MINORE, 3).negato());
        assertFalse(round.isNegato());
        assertEquals(2, round.getNumero());
        PredicatoMD quanti = BozzaTattica.normalizza(PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.singletonList(almeno), Confronto.MAGGIORE, 0));
        assertFalse(quanti.getFiltri().get(0).isNegato());
    }

    @Test
    void ILivelliDiAstuziaEIntelligenzaSonoDeiMostriNonDelGiocatore() {
        assertFalse(BozzaTattica.predicatiAmmessi().contains(TipoPredicato.ASTUZIA));
        assertFalse(BozzaTattica.predicatiAmmessi().contains(TipoPredicato.INTELLIGENZA));
        assertTrue(BozzaTattica.predicatiAmmessi().contains(TipoPredicato.QUANTI));
        assertTrue(BozzaTattica.predicatiAmmessi().contains(TipoPredicato.ROUND));
        assertEquals(TipoPredicato.values().length - 2, BozzaTattica.predicatiAmmessi().size());
    }

    @Test
    void ILegamiConLaStoriaDiChiDecideSonoOrdinamentiENonFiltri() {
        for (TipoFiltro tipo : Arrays.asList(TipoFiltro.IL_CAPO, TipoFiltro.ME_STESSO, TipoFiltro.SCELTO_L_ULTIMA_VOLTA, TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO)) {
            for (TipoRosa rosa : TipoRosa.values()) {
                assertFalse(BozzaTattica.filtriAmmessi(rosa).contains(tipo), rosa + " " + tipo);
            }
        }
        assertTrue(BozzaTattica.filtriAmmessi(TipoRosa.AVVERSARI).contains(TipoFiltro.MAGICO));
        // Chi mi ha attaccato per ultimo e l'ultimo scelto si offrono fra gli avversari, non fra gli alleati
        for (TipoOrdinamento tipo : Arrays.asList(TipoOrdinamento.ULTIMO_ATTACCANTE, TipoOrdinamento.ULTIMO_SCELTO)) {
            assertTrue(BozzaTattica.ordinamentiAmmessi(TipoRosa.AVVERSARI).contains(tipo), tipo.name());
            assertTrue(BozzaTattica.ordinamentiAmmessi(null).contains(tipo), "una voce ancora senza rosa");
            assertFalse(BozzaTattica.ordinamentiAmmessi(TipoRosa.ALLEATI).contains(tipo), tipo.name());
            assertFalse(BozzaTattica.ordinamentiAmmessi(TipoRosa.SE_STESSO).contains(tipo), tipo.name());
        }
        assertTrue(BozzaTattica.ordinamentiAmmessi(TipoRosa.ALLEATI).contains(TipoOrdinamento.MENO_SALUTE));
    }

    @Test
    void leModificheRifannoIDatiImmutabili() {
        FiltroMD salute = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MINORE, 50);
        assertEquals(55, BozzaTattica.conNumero(salute, 55).getNumero());
        assertEquals(0, BozzaTattica.conNumero(salute, -5).getNumero());
        assertEquals(100, BozzaTattica.conNumero(BozzaTattica.filtroPredefinito(TipoFiltro.SALUTE_PERCENTUALE), 500).getNumero());
        assertEquals(Confronto.MAGGIORE, BozzaTattica.conConfronto(salute, Confronto.MAGGIORE).getConfronto());
        assertTrue(salute.negato().isNegato());
        assertFalse(salute.negato().negato().isNegato());
        PredicatoMD quanti = BozzaTattica.predicatoPredefinito(TipoPredicato.QUANTI);
        PredicatoMD conFiltro = BozzaTattica.conFiltri(quanti, Collections.singletonList(salute));
        assertEquals(1, conFiltro.getFiltri().size());
        assertEquals(TipoRosa.OSPITI, BozzaTattica.conRosa(conFiltro, TipoRosa.OSPITI).getRosa());
        assertEquals(30, BozzaTattica.conNumero(PredicatoMD.probabilita(50), 30).getNumero());
    }
}
