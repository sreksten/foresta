package com.threeamigos.foresta.tattiche.modifica;

import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.PredicatoMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.tipi.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoConfronto;
import com.threeamigos.foresta.tipi.TipoFiltro;
import com.threeamigos.foresta.tipi.TipoOrdinamento;
import com.threeamigos.foresta.tipi.TipoParametro;
import com.threeamigos.foresta.tipi.TipoPredicato;
import com.threeamigos.foresta.tipi.TipoRosa;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le mani della bozza su ogni tipo di filtro, di condizione, di ordinamento e di azione: nessuno si rompe, e quello che si ricostruisce
 * somiglia a quello da cui si parte.
 */
class ScansioneDellaBozzaTest {

    @Test
    void ogniFiltroPredefinitoSiRicostruisceESiModifica() {
        for (TipoFiltro tipo : TipoFiltro.values()) {
            FiltroMD f = BozzaTattica.filtroPredefinito(tipo);
            assertEquals(tipo, f.getTipoFiltro(), tipo.name());
            assertEquals(f, BozzaTattica.conParametro(f, BozzaTattica.parametroDi(f)), tipo.name());
            assertEquals(f, BozzaTattica.conConfronto(f, f.getConfronto()), tipo.name());
            assertEquals(f, BozzaTattica.conNumero(f, f.getNumero()), tipo.name());
            BozzaTattica.conNumero(f, -50);
            BozzaTattica.conNumero(f, 5000);
            BozzaTattica.conRelazione(f, TipoConfronto.MINORE, false);
            BozzaTattica.normalizza(f);
            assertTrue(BozzaTattica.passo(tipo) >= 1);
            assertTrue(BozzaTattica.massimo(tipo) >= 100);
            if (BozzaTattica.isNegabile(tipo)) {
                FiltroMD negato = f.negato();
                assertEquals(negato, BozzaTattica.normalizza(negato));
                assertTrue(BozzaTattica.conRelazione(f, f.getConfronto(), true).isNegato());
            }
        }
        assertEquals(100, BozzaTattica.massimo(TipoFiltro.SALUTE_PERCENTUALE));
        assertEquals(5, BozzaTattica.passo(TipoFiltro.MAGIA));
    }

    @Test
    void ilRovescioDiUnConfrontoSuUnNumeroSiNormalizza() {
        FiltroMD salute = BozzaTattica.filtro(TipoFiltro.SALUTE, true, TipoConfronto.MINORE, 50, null);
        FiltroMD normalizzato = BozzaTattica.normalizza(salute);
        assertFalse(normalizzato.isNegato());
        assertEquals(TipoConfronto.MAGGIORE, normalizzato.getConfronto());
        assertEquals(49, normalizzato.getNumero());
        FiltroMD alMassimo = BozzaTattica.normalizza(BozzaTattica.filtro(TipoFiltro.SALUTE, true, TipoConfronto.MAGGIORE, 50, null));
        assertEquals(TipoConfronto.MINORE, alMassimo.getConfronto());
        assertEquals(51, alMassimo.getNumero());
        // ai limiti resta com'è
        FiltroMD zero = BozzaTattica.filtro(TipoFiltro.SALUTE, true, TipoConfronto.MINORE, 0, null);
        assertSame(zero, BozzaTattica.normalizza(zero));
        FiltroMD cima = BozzaTattica.filtro(TipoFiltro.SALUTE_PERCENTUALE, true, TipoConfronto.MAGGIORE, 100, null);
        assertSame(cima, BozzaTattica.normalizza(cima));
        FiltroMD diverso = BozzaTattica.filtro(TipoFiltro.SALUTE, true, TipoConfronto.UGUALE, 5, null);
        assertSame(diverso, BozzaTattica.normalizza(diverso));
    }

    @Test
    void ogniCondizionePredefinitaSiRicostruisceESiModifica() {
        for (TipoPredicato tipo : TipoPredicato.values()) {
            PredicatoMD p = BozzaTattica.predicatoPredefinito(tipo);
            assertEquals(tipo, p.getTipoPredicato(), tipo.name());
            BozzaTattica.conNumero(p, -3);
            BozzaTattica.conNumero(p, 5000);
            BozzaTattica.conNumero(p, 17);
            BozzaTattica.conConfronto(p, TipoConfronto.MINORE);
            BozzaTattica.conRelazione(p, TipoConfronto.MINORE, false);
            BozzaTattica.conFiltri(p, Collections.emptyList());
            BozzaTattica.normalizza(p);
            assertTrue(BozzaTattica.passo(tipo) >= 1);
            assertTrue(BozzaTattica.massimo(tipo) >= 100);
        }
        assertEquals(100, BozzaTattica.massimo(TipoPredicato.PROBABILITA));
        assertEquals(5, BozzaTattica.passo(TipoPredicato.PROBABILITA));
        assertFalse(BozzaTattica.predicatiAmmessi().contains(TipoPredicato.ASTUZIA));
        assertFalse(BozzaTattica.predicatiAmmessi().contains(TipoPredicato.INTELLIGENZA));
    }

    @Test
    void ilRovescioDiUnaCondizioneSiNormalizza() {
        for (TipoPredicato tipo : TipoPredicato.values()) {
            if (tipo == TipoPredicato.PROBABILITA) {
                continue;
            }
            PredicatoMD base = BozzaTattica.predicatoPredefinito(tipo);
            if (base.getTipoConfronto() == null) {
                continue;
            }
            for (TipoConfronto confronto : new TipoConfronto[] {TipoConfronto.MINORE, TipoConfronto.MAGGIORE, TipoConfronto.UGUALE}) {
                for (int numero : new int[] {0, 5, BozzaTattica.massimo(tipo)}) {
                    PredicatoMD rovescio = BozzaTattica.predicato(tipo, true, confronto, numero, TipoRosa.AVVERSARI,
                            tipo == TipoPredicato.QUANTI ? Collections.singletonList(
                                    BozzaTattica.filtro(TipoFiltro.SALUTE, true, TipoConfronto.MINORE, 10, null)) : Collections.emptyList());
                    PredicatoMD normalizzato = BozzaTattica.normalizza(rovescio);
                    assertNotNull(normalizzato);
                    if (confronto == TipoConfronto.MINORE && numero > 0) {
                        assertEquals(TipoConfronto.MAGGIORE, normalizzato.getTipoConfronto());
                        assertFalse(normalizzato.isNegato());
                    }
                }
            }
        }
        PredicatoMD probabilita = PredicatoMD.probabilita(30);
        assertEquals(probabilita, BozzaTattica.normalizza(probabilita));
    }

    @Test
    void unaCondizioneQuantiCambiaRosaESiTienePerFiltriChiHaSenso() {
        List<FiltroMD> filtri = Arrays.asList(BozzaTattica.filtroPredefinito(TipoFiltro.SALUTE), BozzaTattica.filtroPredefinito(TipoFiltro.CLASSE));
        PredicatoMD quanti = PredicatoMD.quanti(TipoRosa.AVVERSARI, filtri, TipoConfronto.MAGGIORE, 1);
        PredicatoMD suMe = BozzaTattica.conRosa(quanti, TipoRosa.SE_STESSO);
        assertEquals(TipoRosa.SE_STESSO, suMe.getTipoRosa());
        assertEquals(1, suMe.getFiltri().size(), "la classe non ha senso su se stessi");
        for (TipoRosa rosa : TipoRosa.values()) {
            assertEquals(rosa, BozzaTattica.conRosa(quanti, rosa).getTipoRosa());
        }
    }

    @Test
    void ogniOrdinamentoEOgniAzionePredefiniti() {
        for (TipoOrdinamento tipo : TipoOrdinamento.values()) {
            OrdinamentoMD ordinamento = BozzaTattica.ordinamentoPredefinito(tipo);
            assertEquals(tipo, ordinamento.getTipoOrdinamento());
        }
        for (TipoAzione tipo : TipoAzione.values()) {
            AzioneMD azione = BozzaTattica.azionePredefinita(tipo);
            assertEquals(tipo, azione.getTipo());
            if (BozzaTattica.haSoloSePiuForte(azione)) {
                AzioneMD commutata = BozzaTattica.commutaSoloSePiuForte(azione);
                assertTrue(commutata.isSoloSePiuForteDellAttacco(), tipo.name());
                assertFalse(BozzaTattica.commutaSoloSePiuForte(commutata).isSoloSePiuForteDellAttacco(), tipo.name());
            } else {
                assertThrows(IllegalArgumentException.class, () -> BozzaTattica.commutaSoloSePiuForte(azione));
            }
        }
        for (CriterioIncantesimo criterio : CriterioIncantesimo.values()) {
            assertEquals(criterio, BozzaTattica.conCriterio(BozzaTattica.azionePredefinita(TipoAzione.INCANTESIMO_DI_DANNO), criterio).getCriterio());
            assertEquals(criterio, BozzaTattica.conCriterio(BozzaTattica.azionePredefinita(TipoAzione.INCANTESIMO_DEL_REPERTORIO), criterio).getCriterio());
        }
    }

    @Test
    void ognParametroDiFiltroHaUnPredefinitoOUnPosto() {
        EnumSet<TipoParametro> visti = EnumSet.noneOf(TipoParametro.class);
        for (TipoFiltro tipo : TipoFiltro.values()) {
            visti.add(tipo.getParametro());
        }
        assertTrue(visti.contains(TipoParametro.STATO));
        assertTrue(visti.contains(TipoParametro.CLASSE));
        assertTrue(visti.contains(TipoParametro.CATEGORIA));
        assertTrue(visti.contains(TipoParametro.TIPO_DANNO));
        assertTrue(visti.contains(TipoParametro.CONFRONTO));
        assertTrue(visti.contains(TipoParametro.NESSUNO));
    }

    @Test
    void iFiltriEGliOrdinamentiAmmessiPerRosa() {
        for (TipoRosa rosa : TipoRosa.values()) {
            List<TipoFiltro> filtri = BozzaTattica.filtriAmmessi(rosa);
            assertFalse(filtri.contains(TipoFiltro.IL_CAPO));
            assertFalse(filtri.contains(TipoFiltro.ME_STESSO));
            assertEquals(rosa != TipoRosa.SE_STESSO, filtri.contains(TipoFiltro.CLASSE), rosa.name());
            boolean traGliAvversari = rosa == TipoRosa.AVVERSARI || rosa == TipoRosa.AVVERSARI_E_OSPITI;
            assertEquals(traGliAvversari, BozzaTattica.ordinamentiAmmessi(rosa).contains(TipoOrdinamento.ULTIMO_ATTACCANTE), rosa.name());
        }
        assertTrue(BozzaTattica.ordinamentiAmmessi(null).contains(TipoOrdinamento.ULTIMO_SCELTO));
    }

    @Test
    void leAzioniEDLeRoseAmmesseDallaVoce() {
        BozzaTattica bozza = BozzaTattica.da(TatticaMD.VUOTA);
        BozzaTattica.Voce voce = bozza.aggiungiVoce();
        assertFalse(BozzaTattica.roseAmmesse(voce).isEmpty());
        List<TipoAzione> tutte = BozzaTattica.azioniAmmesse(voce);
        assertFalse(tutte.contains(TipoAzione.INCANTESIMO_DEL_REPERTORIO));
        assertFalse(tutte.isEmpty());
        Set<TipoAzione> possibili = EnumSet.of(TipoAzione.ATTACCO_FISICO, TipoAzione.FULMINE);
        assertTrue(possibili.containsAll(BozzaTattica.azioniAmmesse(voce, possibili)));
        // con la rosa già scelta contano solo le azioni che la accettano
        voce.rosa = TipoRosa.SE_STESSO;
        assertTrue(BozzaTattica.azioniAmmesse(voce).size() <= tutte.size());
        // con un'azione, solo le azioni della sua portata
        for (TipoAzione primo : tutte) {
            BozzaTattica.Voce v = bozza.aggiungiVoce();
            bozza.aggiungiAzione(v, primo);
            assertNotNull(v.rosa, primo.name());
            assertTrue(BozzaTattica.azioniAmmesse(v).contains(primo), primo.name());
            for (TipoAzione secondo : BozzaTattica.azioniAmmesse(v)) {
                bozza.sostituisciAzione(v, 0, secondo);
                assertEquals(secondo, v.azioni.get(0).getTipo());
                assertTrue(BozzaTattica.roseAmmesse(v).contains(v.rosa), secondo.name());
            }
        }
        assertFalse(bozza.problemi().contains(null));
    }

    @Test
    void laBozzaDiOgniVoceSiConfermaOSiSpiega() {
        TatticaMD tattica = TatticaMD.di(
                VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE)),
                        AzioneMD.di(TipoAzione.FULMINE)),
                new VoceMD(null, Collections.singletonList(PredicatoMD.probabilita(40)), Collections.singletonList(AzioneMD.di(TipoAzione.ATTACCO_FISICO)),
                        false, null));
        BozzaTattica bozza = BozzaTattica.da(tattica);
        assertEquals(2, bozza.getVoci().size());
        assertEquals(BozzaTattica.nomePredefinito(1), bozza.getVoci().get(1).nome);
        assertFalse(bozza.getVoci().get(1).attiva);
        assertTrue(bozza.descrive(tattica));
        assertFalse(bozza.haVociSenzaAzioni());
        bozza.aggiungiVoce();
        assertTrue(bozza.haVociSenzaAzioni());
        assertFalse(bozza.descrive(tattica) && bozza.problemi().isEmpty());
        assertEquals(2, bozza.toTattica().getVoci().size());
        assertNotNull(bozza.toVoceMD(bozza.getVoci().get(0)));
        assertFalse(bozza.spostaVoce(-1, 1));
        assertFalse(bozza.spostaVoce(5, -1));
    }
}
