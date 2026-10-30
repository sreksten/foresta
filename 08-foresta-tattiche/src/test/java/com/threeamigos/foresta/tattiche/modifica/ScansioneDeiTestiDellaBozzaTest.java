package com.threeamigos.foresta.tattiche.modifica;

import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.PredicatoMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoConfronto;
import com.threeamigos.foresta.tipi.TipoFiltro;
import com.threeamigos.foresta.tipi.TipoOrdinamento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoPredicato;
import com.threeamigos.foresta.tipi.TipoRosa;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le parole per ogni pezzo di una tattica, in tutte le forme: filtri con e senza rovescio, condizioni, azioni con criterio e arma, voci
 * disattivate e senza selettore. E le voci in coda che somigliano all'attacco di riserva ma non lo sono.
 */
class ScansioneDeiTestiDellaBozzaTest {

    private static void testoSensato(String testo) {
        assertNotNull(testo);
        assertFalse(testo.isEmpty());
        assertFalse(testo.contains("null"), testo);
    }

    @Test
    void ognFiltroSiLeggeConEConSenzaRovescio() {
        for (TipoFiltro tipo : TipoFiltro.values()) {
            FiltroMD filtro = BozzaTattica.filtroPredefinito(tipo);
            testoSensato(TestoTattica.filtro(filtro));
            testoSensato(TestoTattica.filtro(filtro.negato()));
        }
        testoSensato(TestoTattica.filtri(Arrays.asList(BozzaTattica.filtroPredefinito(TipoFiltro.SALUTE), BozzaTattica.filtroPredefinito(TipoFiltro.STATO))));
        assertEquals("", TestoTattica.filtri(Collections.emptyList()));
    }

    @Test
    void ognCondizioneEOgniAzioneSiLeggono() {
        for (TipoPredicato tipo : TipoPredicato.values()) {
            PredicatoMD predicato = BozzaTattica.predicatoPredefinito(tipo);
            testoSensato(TestoTattica.predicato(predicato));
            testoSensato(TestoTattica.predicato(predicato.negato()));
        }
        PredicatoMD conFiltri = PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.singletonList(BozzaTattica.filtroPredefinito(TipoFiltro.MAGICO)),
                TipoConfronto.MAGGIORE, 1);
        testoSensato(TestoTattica.predicato(conFiltri));
        for (TipoAzione tipo : TipoAzione.values()) {
            AzioneMD azione = BozzaTattica.azionePredefinita(tipo);
            testoSensato(TestoTattica.azione(azione));
            if (BozzaTattica.haSoloSePiuForte(azione)) {
                testoSensato(TestoTattica.azione(BozzaTattica.commutaSoloSePiuForte(azione)));
            }
        }
    }

    @Test
    void ognOrdinamentoESelettoreSiLeggono() {
        for (TipoOrdinamento tipo : TipoOrdinamento.values()) {
            testoSensato(TestoTattica.ordinamento(BozzaTattica.ordinamentoPredefinito(tipo)));
        }
        List<FiltroMD> filtri = Collections.singletonList(BozzaTattica.filtroPredefinito(TipoFiltro.SALUTE));
        for (TipoRosa rosa : TipoRosa.values()) {
            testoSensato(TestoTattica.selettore(new SelettoreMD(rosa, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO))));
            testoSensato(TestoTattica.selettore(new SelettoreMD(rosa, filtri, OrdinamentoMD.di(TipoOrdinamento.PRIMO))));
            testoSensato(TestoTattica.selettore(new SelettoreMD(rosa, filtri, null)));
        }
    }

    @Test
    void ognEnumeratoHaUnNomeInParole() {
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            testoSensato(TestoTattica.nome(classe));
        }
        assertEquals("minotauro gigante", TestoTattica.nome(TipoPersonaggio.MINOTAURO_GIGANTE));
        assertEquals("meno salute", TestoTattica.nome(TipoOrdinamento.MENO_SALUTE));
        for (TipoConfronto confronto : TipoConfronto.values()) {
            testoSensato(TestoTattica.confronto(confronto));
            testoSensato(TestoTattica.relazione(confronto, true));
            testoSensato(TestoTattica.spiega(confronto, true));
            testoSensato(TestoTattica.spiega(confronto, false));
        }
    }

    @Test
    void leVociSiLeggonoSenzaSelettoreEDisattivate() {
        VoceMD senzaSelettore = VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO));
        assertFalse(TestoTattica.riga(senzaSelettore).isEmpty());
        assertTrue(TestoTattica.spiegazione(senzaSelettore).size() >= 3);
        VoceMD disattivata = new VoceMD(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)),
                Collections.singletonList(PredicatoMD.probabilita(30)),
                Arrays.asList(AzioneMD.di(TipoAzione.ATTACCO_FISICO), AzioneMD.di(TipoAzione.FULMINE)), false, "Mia");
        assertTrue(TestoTattica.riga(disattivata).contains(" "));
        List<String> spiegazione = TestoTattica.spiegazione(disattivata);
        assertEquals(disattivata.isAttiva() ? 0 : 1, spiegazione.stream().filter(r -> r.equals(TestoTattica.testo("spiegazione.DISATTIVATA"))).count());
    }

    // ---- L'attacco di riserva in coda

    private static VoceMD voce(TipoRosa rosa, List<FiltroMD> filtri, OrdinamentoMD ordinamento, List<PredicatoMD> condizioni, boolean attiva,
                               AzioneMD... azioni) {
        return new VoceMD(new SelettoreMD(rosa, filtri, ordinamento), condizioni, Arrays.asList(azioni), attiva);
    }

    @Test
    void soloQuelloVeroEUnAttaccoDiRiservaInCoda() {
        OrdinamentoMD primo = OrdinamentoMD.di(TipoOrdinamento.PRIMO);
        AzioneMD attacco = AzioneMD.di(TipoAzione.ATTACCO_FISICO);
        VoceMD vera = voce(TipoRosa.AVVERSARI, Collections.emptyList(), primo, Collections.emptyList(), true, attacco);
        VoceMD[] quasi = {
                voce(TipoRosa.AVVERSARI, Collections.emptyList(), primo, Collections.emptyList(), false, attacco),
                voce(TipoRosa.AVVERSARI, Collections.emptyList(), primo, Collections.singletonList(PredicatoMD.probabilita(50)), true, attacco),
                voce(TipoRosa.AVVERSARI, Collections.emptyList(), primo, Collections.emptyList(), true, attacco, AzioneMD.di(TipoAzione.FULMINE)),
                voce(TipoRosa.AVVERSARI, Collections.emptyList(), primo, Collections.emptyList(), true, AzioneMD.di(TipoAzione.FULMINE)),
                voce(TipoRosa.ALLEATI, Collections.emptyList(), primo, Collections.emptyList(), true, attacco),
                voce(TipoRosa.AVVERSARI, Collections.singletonList(BozzaTattica.filtroPredefinito(TipoFiltro.MAGICO)), primo, Collections.emptyList(), true, attacco),
                voce(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE), Collections.emptyList(), true, attacco),
                voce(TipoRosa.AVVERSARI, Collections.emptyList(), null, Collections.emptyList(), true, attacco),
        };
        VoceMD alto = voce(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE), Collections.emptyList(), true,
                AzioneMD.di(TipoAzione.FULMINE));
        assertEquals(1, BozzaTattica.da(TatticaMD.di(alto, vera)).getVoci().size());
        for (VoceMD q : quasi) {
            assertEquals(2, BozzaTattica.da(TatticaMD.di(alto, q)).getVoci().size(), q.toString());
        }
    }

    @Test
    void iFiltriDelSelettoreEntranoNormalizzatiNellaBozza() {
        FiltroMD rovescio = BozzaTattica.filtro(TipoFiltro.SALUTE, true, TipoConfronto.MINORE, 50, null);
        VoceMD voce = voce(TipoRosa.AVVERSARI, Collections.singletonList(rovescio), OrdinamentoMD.di(TipoOrdinamento.PRIMO), Collections.emptyList(), true,
                AzioneMD.di(TipoAzione.FULMINE));
        BozzaTattica bozza = BozzaTattica.da(TatticaMD.di(voce));
        FiltroMD letto = bozza.getVoci().get(0).filtri.get(0);
        assertFalse(letto.isNegato());
        assertEquals(TipoConfronto.MAGGIORE, letto.getConfronto());
    }

    @Test
    void conAzioniDiPortataDiversaNessunaRosaVaBene() {
        BozzaTattica bozza = BozzaTattica.da(TatticaMD.VUOTA);
        BozzaTattica.Voce voce = bozza.aggiungiVoce();
        bozza.aggiungiAzione(voce, TipoAzione.ATTACCO_FISICO);
        TipoRosa prima = voce.rosa;
        // un'azione che agisce su un'altra portata, messa a mano nella lista
        for (TipoAzione altra : TipoAzione.values()) {
            if (altra != TipoAzione.INCANTESIMO_DEL_REPERTORIO
                    && com.threeamigos.foresta.tipi.PortataAzione.di(altra) != com.threeamigos.foresta.tipi.PortataAzione.di(TipoAzione.ATTACCO_FISICO)) {
                voce.azioni.add(BozzaTattica.azionePredefinita(altra));
                break;
            }
        }
        bozza.aggiungiAzione(voce, TipoAzione.ATTACCO_FISICO);
        assertNotNull(voce.rosa);
        assertNotNull(prima);
    }
}
