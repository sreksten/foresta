package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.tattiche.*;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tattiche.valutazione.SceltaIncantesimo;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il valutatore: scorre la tattica di un personaggio e decide che cosa fa (tattiche_di_combattimento_automatiche.md §11).
 */
class ValutatoreTest {

    private Personaggio mago;
    private Personaggio guerriero;
    private Personaggio bardo;
    private Personaggio goblin1;
    private Personaggio goblin2;
    private Personaggio strega;
    private Personaggio lich;
    private Gruppo scorteDeiMiei;
    private Gruppo scorteDeiMostri;

    private static Gruppo gruppoVuoto() {
        return new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return false;
            }
        };
    }

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        StatoDelCombattimento.getIstanza().reimposta();
        mago = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1);
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        bardo = FabbricaPersonaggi.crea(TipoPersonaggio.BARDO, 1);
        goblin1 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        goblin2 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        lich = FabbricaPersonaggi.crea(TipoPersonaggio.LICH, 1);
        mago.addMagia(mago.getMagiaMassima());
        bardo.addMagia(bardo.getMagiaMassima());
        scorteDeiMiei = gruppoVuoto();
        scorteDeiMostri = gruppoVuoto();
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private ContestoTattico perIMiei(Personaggio attore, Personaggio... avversari) {
        return new ContestoTattico(attore, true, Arrays.asList(mago, guerriero, bardo), Arrays.asList(avversari), Collections.emptyList())
                .conGruppoDegliAlleati(scorteDeiMiei);
    }

    private ContestoTattico perIMostri(Personaggio attore, List<Personaggio> mostri, List<Personaggio> ospiti) {
        return new ContestoTattico(attore, false, mostri, Arrays.asList(mago, guerriero), ospiti).conGruppoDegliAlleati(scorteDeiMostri);
    }

    private static SelettoreMD su(TipoRosa rosa, TipoOrdinamento ordinamento, FiltroMD... filtri) {
        return new SelettoreMD(rosa, Arrays.asList(filtri), OrdinamentoMD.di(ordinamento));
    }

    private static FiltroMD classe(TipoPersonaggio classe) {
        return FiltroMD.conClasse(TipoFiltro.CLASSE, classe);
    }

    private static VoceMD voce(SelettoreMD selettore, TipoAzione... azioni) {
        List<AzioneMD> lista = new ArrayList<>();
        for (TipoAzione azione : azioni) {
            lista.add(AzioneMD.di(azione));
        }
        return new VoceMD(selettore, Collections.emptyList(), lista);
    }

    private static boolean spiegazioneContiene(Decisione decisione, String testo) {
        return decisione.getSpiegazione().stream().anyMatch(riga -> riga.contains(testo));
    }

    // ---- la sequenza: "silenzia, altrimenti fulmine, altrimenti attacca" ----

    @Test
    void laSequenzaPassaAllAzioneSuccessivaQuandoLaPrimaNonSiPuoFare() {
        TatticaMD tattica = TatticaMD.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, classe(TipoPersonaggio.STREGA)),
                TipoAzione.SILENZIO, TipoAzione.FULMINE, TipoAzione.ATTACCO_FISICO));
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.SILENZIO, 1);
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.FULMINE, 1);
        ContestoTattico contesto = perIMiei(mago, goblin1, strega);

        Decisione silenzio = Valutatore.decidi(tattica, contesto);
        assertEquals(TipoAzione.SILENZIO, silenzio.getTipoEseguito());
        assertSame(strega, silenzio.getBersaglio());
        assertEquals("0", silenzio.getIdentificativoVoce());

        // Già silenziata: il fulmine
        strega.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        Decisione fulmine = Valutatore.decidi(tattica, contesto);
        assertEquals(TipoAzione.FULMINE, fulmine.getTipoEseguito());
        assertTrue(spiegazioneContiene(fulmine, "SILENZIO scartata"));

        // Senza pergamene di fulmine: l'attacco fisico
        scorteDeiMiei.subIncantesimi(TipoIncantesimo.FULMINE, 1);
        assertEquals(TipoAzione.ATTACCO_FISICO, Valutatore.decidi(tattica, contesto).getTipoEseguito());
    }

    @Test
    void ilLichEImmuneAlSilenzioEQuindiNonSiProvaNemmeno() {
        TatticaMD tattica = TatticaMD.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, classe(TipoPersonaggio.LICH)),
                TipoAzione.SILENZIO, TipoAzione.ATTACCO_FISICO));
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.SILENZIO, 1);
        Decisione decisione = Valutatore.decidi(tattica, perIMiei(mago, lich));
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito());
        assertTrue(spiegazioneContiene(decisione, "immune al silenzio"), decisione.getSpiegazione().toString());
    }

    @Test
    void dopoDueFallimentiPerCasoSiRinunciaEUnaRiuscitaAzzeraIlConto() {
        TatticaMD tattica = TatticaMD.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, classe(TipoPersonaggio.STREGA)),
                TipoAzione.SILENZIO, TipoAzione.ATTACCO_FISICO));
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.SILENZIO, 3);
        ContestoTattico contesto = perIMiei(mago, strega);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.registraEsitoAzione(mago, strega, TipoAzione.SILENZIO, EsitoAzione.MANCATA);
        assertEquals(TipoAzione.SILENZIO, Valutatore.decidi(tattica, contesto).getTipoEseguito(), "un solo fallimento: si riprova");
        stato.registraEsitoAzione(mago, strega, TipoAzione.SILENZIO, EsitoAzione.MANCATA);
        Decisione rinuncia = Valutatore.decidi(tattica, contesto);
        assertEquals(TipoAzione.ATTACCO_FISICO, rinuncia.getTipoEseguito());
        assertTrue(spiegazioneContiene(rinuncia, "già fallita"));
        // Un altro personaggio ha il suo conto
        assertEquals(0, stato.getTentativiFalliti(guerriero, strega, TipoAzione.SILENZIO));
        stato.registraEsitoAzione(mago, strega, TipoAzione.SILENZIO, EsitoAzione.RIUSCITA);
        assertEquals(TipoAzione.SILENZIO, Valutatore.decidi(tattica, contesto).getTipoEseguito());
    }

    @Test
    void UnImmunitaScopertaValePerTuttiEDaSubito() {
        TatticaMD tattica = TatticaMD.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), TipoAzione.FULMINE, TipoAzione.ATTACCO_FISICO));
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.FULMINE, 2);
        ContestoTattico delMago = perIMiei(mago, goblin1);
        ContestoTattico delBardo = perIMiei(bardo, goblin1);
        bardo.addMagia(bardo.getMagiaMassima());
        assertEquals(TipoAzione.FULMINE, Valutatore.decidi(tattica, delMago).getTipoEseguito());
        // Il goblin porta un artefatto che lo rende immune al fulmine: lo scopre il Bardo con un colpo a vuoto, e lo sa anche il Mago
        StatoDelCombattimento.getIstanza().registraEsitoAzione(bardo, goblin1, TipoAzione.FULMINE, EsitoAzione.IMMUNE);
        assertEquals(TipoAzione.ATTACCO_FISICO, Valutatore.decidi(tattica, delMago).getTipoEseguito());
        assertEquals(TipoAzione.ATTACCO_FISICO, Valutatore.decidi(tattica, delBardo).getTipoEseguito());
    }

    // ---- il ripiego, le condizioni, le voci disattivate ----

    @Test
    void senzaVociEseguibiliSiAttaccaIlPrimoAvversario() {
        Decisione decisione = Valutatore.decidi(new TatticaMD(Collections.emptyList()), perIMiei(guerriero, goblin1, goblin2));
        assertTrue(decisione.isRipiego());
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito());
        assertSame(goblin1, decisione.getBersaglio());
        assertNull(decisione.getIdentificativoVoce());
        // Nessun avversario in campo: niente da fare
        goblin1.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        goblin2.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertNull(Valutatore.decidi(new TatticaMD(Collections.emptyList()), perIMiei(guerriero, goblin1, goblin2)));
    }

    @Test
    void leVociDisattivateEQuelleConCondizioniFalseSiSaltano() {
        VoceMD secondoRound = new VoceMD(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), Collections.singletonList(
                PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.UGUALE, 2)), Collections.singletonList(AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
        VoceMD disattivata = voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), TipoAzione.ATTACCO_FISICO).disattivata();
        VoceMD buona = voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), TipoAzione.ATTACCO_FISICO);
        goblin2.subSalute(goblin2.getSalute() - Math.max(1, goblin1.getSalute() / 2), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertTrue(goblin2.getSalute() < goblin1.getSalute());
        Decisione decisione = Valutatore.decidi(TatticaMD.di(secondoRound, disattivata, buona), perIMiei(guerriero, goblin1, goblin2));
        assertEquals(2, decisione.getIndiceVoce());
        assertSame(goblin2, decisione.getBersaglio(), "il più ferito");
        assertTrue(spiegazioneContiene(decisione, "voce 2: disattivata"));
        assertTrue(spiegazioneContiene(decisione, "voce 1: le condizioni non sono tutte vere"));
    }

    // ---- azioni su un gruppo e su un alleato ----

    @Test
    void ilCantoDiValoreSiCantaFinoAChiNonELIspiratoESiSaltaQuandoTuttiLoSono() {
        TatticaMD tattica = TatticaMD.di(
                new VoceMD(su(TipoRosa.ALLEATI, TipoOrdinamento.PRIMO, FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.ISPIRATO).negato()),
                        Collections.singletonList(PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.UGUALE, 1)),
                        Collections.singletonList(AzioneMD.di(TipoAzione.CANTO_DI_VALORE))));
        ContestoTattico contesto = perIMiei(bardo, goblin1);
        Decisione canto = Valutatore.decidi(tattica, contesto);
        assertEquals(TipoAzione.CANTO_DI_VALORE, canto.getTipoEseguito());
        assertNull(canto.getBersaglio(), "agisce su tutto il gruppo");
        for (Personaggio p : Arrays.asList(mago, guerriero, bardo)) {
            p.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
        }
        assertTrue(Valutatore.decidi(tattica, contesto).isRipiego());
    }

    @Test
    void laPozioneDiChiStaMale() {
        TatticaMD tattica = TatticaMD.di(voce(su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO,
                FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, TipoConfronto.MINORE, 50)), TipoAzione.POZIONE_SALUTE));
        ContestoTattico contesto = perIMiei(guerriero, goblin1);
        assertTrue(Valutatore.decidi(tattica, contesto).isRipiego(), "sta bene");
        guerriero.subSalute(guerriero.getSaluteMassima() * 6 / 10, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertTrue(Valutatore.decidi(tattica, contesto).isRipiego(), "sta male ma non ci sono pozioni");
        scorteDeiMiei.addPozioniSalute(1);
        Decisione bevi = Valutatore.decidi(tattica, contesto);
        assertEquals(TipoAzione.POZIONE_SALUTE, bevi.getTipoEseguito());
        assertSame(guerriero, bevi.getBersaglio(), "beve chi la prende");
    }

    @Test
    void laResurrezioneVuoleUnCadutoELaPergamena() {
        TatticaMD tattica = TatticaMD.di(voce(su(TipoRosa.ALLEATI_CADUTI, TipoOrdinamento.PRIMO), TipoAzione.RESURREZIONE));
        ContestoTattico contesto = perIMiei(mago, goblin1);
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.RESURREZIONE, 1);
        assertTrue(Valutatore.decidi(tattica, contesto).isRipiego(), "nessun caduto");
        guerriero.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        Decisione decisione = Valutatore.decidi(tattica, contesto);
        assertEquals(TipoAzione.RESURREZIONE, decisione.getTipoEseguito());
        assertSame(guerriero, decisione.getBersaglio());
    }

    @Test
    void deboleAllAzioneSceglieChiSubisceDiPiuLAzioneDellaVoce() {
        goblin2.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.FULMINE, 1);
        TatticaMD tattica = TatticaMD.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, FiltroMD.senzaParametro(TipoFiltro.DEBOLE_ALL_AZIONE)),
                TipoAzione.FULMINE));
        Decisione decisione = Valutatore.decidi(tattica, perIMiei(mago, goblin1, goblin2));
        assertEquals(TipoAzione.FULMINE, decisione.getTipoEseguito());
        assertSame(goblin2, decisione.getBersaglio(), "il bagnato, anche se è il secondo");
    }

    @Test
    void unMostroPescaFraIMieiEGliOspitiEUnaCuraNonVaAgliOspitiDeiMostri() {
        Personaggio ostaggio = new Viandante("Ottone", 1);
        ContestoTattico contesto = perIMostri(goblin1, Arrays.asList(goblin1, goblin2), Collections.singletonList(ostaggio));
        Decisione sull_ostaggio = Valutatore.decidi(TatticaMD.di(voce(su(TipoRosa.OSPITI, TipoOrdinamento.PRIMO), TipoAzione.ATTACCO_FISICO)), contesto);
        assertSame(ostaggio, sull_ostaggio.getBersaglio());
        VoceMD curaPerOspiti = voce(su(TipoRosa.OSPITI, TipoOrdinamento.PRIMO), TipoAzione.POZIONE_SALUTE);
        assertFalse(curaPerOspiti.problemi(false).isEmpty(), "un mostro non cura gli ospiti");
        assertTrue(curaPerOspiti.problemi(true).isEmpty());
    }

    // ---- l'incantesimo di danno e i suoi criteri ----

    private Decisione conCriterio(CriterioIncantesimo criterio, Personaggio attore, ContestoTattico contesto) {
        return Valutatore.decidi(TatticaMD.di(new VoceMD(null, Collections.emptyList(), Collections.singletonList(AzioneMD.incantesimoDiDanno(criterio, false)))),
                contesto);
    }

    private static final List<TipoIncantesimo> TRE = Arrays.asList(TipoIncantesimo.FUOCO, TipoIncantesimo.FULMINE, TipoIncantesimo.GELO);

    private void pergameneDelleTre() {
        for (TipoIncantesimo classe : TRE) {
            scorteDeiMiei.addIncantesimi(classe, 1);
        }
    }

    @Test
    void ilPiuAdattoGuardaIlBersaglioIlPiuPotenteNo() {
        pergameneDelleTre();
        goblin1.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        ContestoTattico contesto = perIMiei(mago, goblin1);
        // Il fulmine su un bagnato pesa 1,5 e il fuoco 0,5
        assertEquals(TipoAzione.FULMINE, conCriterio(CriterioIncantesimo.PIU_ADATTO, mago, contesto).getTipoEseguito());
        assertEquals(TipoAzione.FULMINE, conCriterio(CriterioIncantesimo.PIU_ADATTO_ESPERTO, mago, contesto).getTipoEseguito());
        TipoIncantesimo piuPotente = TRE.get(0);
        for (TipoIncantesimo classe : TRE) {
            if (((IncantesimoMalefico) FabbricaIncantesimi.crea(classe, 1)).getDanni()
                    > ((IncantesimoMalefico) FabbricaIncantesimi.crea(piuPotente, 1)).getDanni()) {
                piuPotente = classe;
            }
        }
        assertEquals(TipoAzione.di(piuPotente), conCriterio(CriterioIncantesimo.PIU_POTENTE, mago, contesto).getTipoEseguito());
    }

    @Test
    void aCasoSceglieFraQuelliLanciabiliEIncantesimoDiDannoNonComprendeMorteESilenzio() {
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.MORTE, 1);
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.SILENZIO, 1);
        ContestoTattico contesto = perIMiei(mago, goblin1);
        assertTrue(SceltaIncantesimo.lanciabiliContro(mago, goblin1, contesto).isEmpty());
        assertTrue(conCriterio(CriterioIncantesimo.A_CASO, mago, contesto).isRipiego());
        pergameneDelleTre();
        for (int i = 0; i < 20; i++) {
            Decisione decisione = conCriterio(CriterioIncantesimo.A_CASO, mago, contesto);
            assertTrue(TRE.contains(decisione.getIncantesimo()), decisione.toString());
        }
    }

    @Test
    void ilPiuEconomicoCheBastaEIlMenoCaroFraQuelliCheUccidono() {
        pergameneDelleTre();
        ContestoTattico contesto = perIMiei(mago, goblin1);
        // Con un goblin quasi morto bastano tutti: il meno caro
        goblin1.subSalute(goblin1.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        TipoIncantesimo menoCaro = TRE.get(0);
        for (TipoIncantesimo classe : TRE) {
            if (FabbricaIncantesimi.costoAcquisto(classe) < FabbricaIncantesimi.costoAcquisto(menoCaro)) {
                menoCaro = classe;
            }
        }
        // Può darsi che un incantesimo non basti nemmeno su un goblin con 1 punto (resistenze): si controlla solo che sia fra quelli adatti
        Decisione economico = conCriterio(CriterioIncantesimo.PIU_ECONOMICO_CHE_BASTA, mago, contesto);
        assertTrue(TRE.contains(economico.getIncantesimo()));
        if (TRE.stream().allMatch(c -> CalcolatoreCombattimento.dannoAtteso(mago, goblin1, (com.threeamigos.foresta.artefatti.Arma)
                FabbricaIncantesimi.crea(c, 1)) >= goblin1.getSalute())) {
            assertEquals(TipoAzione.di(menoCaro), economico.getTipoEseguito());
        }
        // Con un nemico che nessuno uccide, il più adatto esperto
        Personaggio gigante = FabbricaPersonaggi.crea(TipoPersonaggio.IDRA, 9);
        ContestoTattico controIdra = perIMiei(mago, gigante);
        assertEquals(conCriterio(CriterioIncantesimo.PIU_ADATTO_ESPERTO, mago, controIdra).getTipoEseguito(),
                conCriterio(CriterioIncantesimo.PIU_ECONOMICO_CHE_BASTA, mago, controIdra).getTipoEseguito());
    }

    @Test
    void unMostroLancia_DalSuoRepertorioSenzaPergamene() {
        ContestoTattico contesto = perIMostri(strega, Collections.singletonList(strega), Collections.emptyList());
        Decisione decisione = conCriterio(CriterioIncantesimo.PIU_POTENTE, strega, contesto);
        assertFalse(decisione.isRipiego());
        assertTrue(strega.getRepertorioIncantesimi().contains(decisione.getIncantesimo()));
        assertTrue(SceltaIncantesimo.ELEMENTALI.contains(decisione.getIncantesimo()));
        // Silenziata, non può
        strega.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertTrue(conCriterio(CriterioIncantesimo.PIU_POTENTE, strega, contesto).isRipiego());
    }

    @Test
    void soloSePiuForteDellAttaccoLoLasciaPerLeArmiQuandoRendonoDiPiu() {
        scorteDeiMiei.addIncantesimi(TipoIncantesimo.FULMINE, 1);
        ContestoTattico contesto = perIMiei(guerriero, goblin1);
        double incantesimo = CalcolatoreCombattimento.dannoAtteso(guerriero, goblin1, (com.threeamigos.foresta.artefatti.Arma) FabbricaIncantesimi.crea(TipoIncantesimo.FULMINE, 1));
        double mischia = CalcolatoreCombattimento.dannoAttesoDellaMischia(guerriero, goblin1);
        TatticaMD tattica = TatticaMD.di(new VoceMD(null, Collections.emptyList(), Arrays.asList(
                AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_ADATTO, true), AzioneMD.di(TipoAzione.ATTACCO_FISICO))));
        guerriero.addMagia(guerriero.getMagiaMassima());
        Decisione decisione = Valutatore.decidi(tattica, contesto);
        assertEquals(incantesimo > mischia ? TipoAzione.FULMINE : TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito(),
                "incantesimo " + incantesimo + " mischia " + mischia);
    }

    // ---- le voci non valide ----

    @Test
    void laLetturaRifiutaLeVociIncoerenti() {
        assertThrows(IllegalArgumentException.class, () -> AzioneMD.di(TipoAzione.INCANTESIMO_DI_DANNO));
        assertThrows(IllegalArgumentException.class, () -> AzioneMD.di(TipoAzione.POZIONE_SALUTE).soloSePiuForteDellAttacco());
        assertThrows(IllegalArgumentException.class, () -> new VoceMD(null, Collections.emptyList(), Collections.emptyList()));
        VoceMD mista = voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), TipoAzione.FULMINE, TipoAzione.POZIONE_SALUTE);
        assertFalse(mista.problemi(true).isEmpty(), "portate diverse");
        VoceMD attaccoAgliAlleati = voce(su(TipoRosa.ALLEATI, TipoOrdinamento.PRIMO), TipoAzione.ATTACCO_FISICO);
        assertFalse(attaccoAgliAlleati.problemi(true).isEmpty());
        TatticaMD tattica = TatticaMD.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), TipoAzione.ATTACCO_FISICO), attaccoAgliAlleati);
        assertEquals(1, tattica.problemi(true).size());
        assertTrue(tattica.problemi(true).get(0).startsWith("voce 2"));
        assertEquals(Collections.emptyList(), TatticaMD.di(voce(null, TipoAzione.ATTACCO_FISICO)).problemi(true), "il selettore di base");
    }

    @Test
    void laSpiegazioneDiUnIncantesimoDiDannoRiportaIDanniAttesiConfrontati() {
        pergameneDelleTre();
        goblin1.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        ContestoTattico contesto = perIMiei(mago, goblin1);
        Decisione decisione = conCriterio(CriterioIncantesimo.PIU_ADATTO, mago, contesto);
        assertEquals(TipoAzione.FULMINE, decisione.getTipoEseguito());
        String spiegazione = String.join("\n", decisione.getSpiegazione());
        assertTrue(spiegazione.contains("PIU_ADATTO: FUOCO "), spiegazione);
        assertTrue(spiegazione.contains(", FULMINE "), spiegazione);
        assertTrue(spiegazione.contains("sceglie FULMINE"), spiegazione);
        // Con il confronto con l'attacco, anche il danno delle armi
        Decisione conConfronto = Valutatore.decidi(TatticaMD.di(new VoceMD(null, Collections.emptyList(), Collections.singletonList(
                AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_ADATTO, true)))), contesto);
        if (!conConfronto.isRipiego()) {
            assertTrue(String.join("\n", conConfronto.getSpiegazione()).contains("attacco con le armi"));
        }
    }

    @Test
    void ilSelettoreRiportaLeMisureDellOrdinamento() {
        ferisciLievemente(goblin2);
        Decisione decisione = Valutatore.decidi(TatticaMD.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), TipoAzione.ATTACCO_FISICO)),
                perIMiei(guerriero, goblin1, goblin2));
        String spiegazione = String.join("\n", decisione.getSpiegazione());
        assertTrue(spiegazione.contains("MENO_SALUTE (" + goblin1.getNome() + " " + goblin1.getSalute() + ", " + goblin2.getNome() + " " + goblin2.getSalute() + ")"),
                spiegazione);
    }

    private static void ferisciLievemente(Personaggio p) {
        p.subSalute(1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }
}
