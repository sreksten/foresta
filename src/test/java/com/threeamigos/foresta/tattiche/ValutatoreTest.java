package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.EsitoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
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

    private static Selettore su(TipoRosa rosa, TipoOrdinamento ordinamento, Filtro... filtri) {
        return new Selettore(rosa, Arrays.asList(filtri), Ordinamento.di(ordinamento));
    }

    private static Filtro classe(TipoPersonaggio classe) {
        return Filtro.conClasse(TipoFiltro.CLASSE, classe);
    }

    private static Voce voce(Selettore selettore, TipoAzione... azioni) {
        List<Azione> lista = new ArrayList<>();
        for (TipoAzione azione : azioni) {
            lista.add(Azione.di(azione));
        }
        return new Voce(selettore, Collections.emptyList(), lista);
    }

    private static boolean spiegazioneContiene(Decisione decisione, String testo) {
        return decisione.getSpiegazione().stream().anyMatch(riga -> riga.contains(testo));
    }

    // ---- la sequenza: "silenzia, altrimenti fulmine, altrimenti attacca" ----

    @Test
    void laSequenzaPassaAllAzioneSuccessivaQuandoLaPrimaNonSiPuoFare() {
        Tattica tattica = Tattica.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, classe(TipoPersonaggio.STREGA)),
                TipoAzione.SILENZIO, TipoAzione.FULMINE, TipoAzione.ATTACCO_FISICO));
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.SILENZIO, 1);
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.FULMINE, 1);
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
        scorteDeiMiei.subIncantesimi(ClasseIncantesimo.FULMINE, 1);
        assertEquals(TipoAzione.ATTACCO_FISICO, Valutatore.decidi(tattica, contesto).getTipoEseguito());
    }

    @Test
    void ilLichEImmuneAlSilenzioEQuindiNonSiProvaNemmeno() {
        Tattica tattica = Tattica.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, classe(TipoPersonaggio.LICH)),
                TipoAzione.SILENZIO, TipoAzione.ATTACCO_FISICO));
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.SILENZIO, 1);
        Decisione decisione = Valutatore.decidi(tattica, perIMiei(mago, lich));
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito());
        assertTrue(spiegazioneContiene(decisione, "immune al silenzio"), decisione.getSpiegazione().toString());
    }

    @Test
    void dopoDueFallimentiPerCasoSiRinunciaEUnaRiuscitaAzzeraIlConto() {
        Tattica tattica = Tattica.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, classe(TipoPersonaggio.STREGA)),
                TipoAzione.SILENZIO, TipoAzione.ATTACCO_FISICO));
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.SILENZIO, 3);
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
        Tattica tattica = Tattica.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), TipoAzione.FULMINE, TipoAzione.ATTACCO_FISICO));
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.FULMINE, 2);
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
        Decisione decisione = Valutatore.decidi(new Tattica(Collections.emptyList()), perIMiei(guerriero, goblin1, goblin2));
        assertTrue(decisione.isRipiego());
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito());
        assertSame(goblin1, decisione.getBersaglio());
        assertNull(decisione.getIdentificativoVoce());
        // Nessun avversario in campo: niente da fare
        goblin1.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        goblin2.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertNull(Valutatore.decidi(new Tattica(Collections.emptyList()), perIMiei(guerriero, goblin1, goblin2)));
    }

    @Test
    void leVociDisattivateEQuelleConCondizioniFalseSiSaltano() {
        Voce secondoRound = new Voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), Collections.singletonList(
                Predicato.conConfrontoENumero(TipoPredicato.ROUND, Confronto.UGUALE, 2)), Collections.singletonList(Azione.di(TipoAzione.ATTACCO_FISICO)));
        Voce disattivata = voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), TipoAzione.ATTACCO_FISICO).disattivata();
        Voce buona = voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.MENO_SALUTE), TipoAzione.ATTACCO_FISICO);
        goblin2.subSalute(goblin2.getSalute() - Math.max(1, goblin1.getSalute() / 2), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertTrue(goblin2.getSalute() < goblin1.getSalute());
        Decisione decisione = Valutatore.decidi(Tattica.di(secondoRound, disattivata, buona), perIMiei(guerriero, goblin1, goblin2));
        assertEquals(2, decisione.getIndiceVoce());
        assertSame(goblin2, decisione.getBersaglio(), "il più ferito");
        assertTrue(spiegazioneContiene(decisione, "voce 2: disattivata"));
        assertTrue(spiegazioneContiene(decisione, "voce 1: le condizioni non sono tutte vere"));
    }

    // ---- azioni su un gruppo e su un alleato ----

    @Test
    void ilCantoDiValoreSiCantaFinoAChiNonELIspiratoESiSaltaQuandoTuttiLoSono() {
        Tattica tattica = Tattica.di(
                new Voce(su(TipoRosa.ALLEATI, TipoOrdinamento.PRIMO, Filtro.conStato(TipoFiltro.STATO, TipoEffettoDiStato.ISPIRATO).negato()),
                        Collections.singletonList(Predicato.conConfrontoENumero(TipoPredicato.ROUND, Confronto.UGUALE, 1)),
                        Collections.singletonList(Azione.di(TipoAzione.CANTO_DI_VALORE))));
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
        Tattica tattica = Tattica.di(voce(su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO,
                Filtro.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, Confronto.MINORE, 50)), TipoAzione.POZIONE_SALUTE));
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
        Tattica tattica = Tattica.di(voce(su(TipoRosa.ALLEATI_CADUTI, TipoOrdinamento.PRIMO), TipoAzione.RESURREZIONE));
        ContestoTattico contesto = perIMiei(mago, goblin1);
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.RESURREZIONE, 1);
        assertTrue(Valutatore.decidi(tattica, contesto).isRipiego(), "nessun caduto");
        guerriero.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        Decisione decisione = Valutatore.decidi(tattica, contesto);
        assertEquals(TipoAzione.RESURREZIONE, decisione.getTipoEseguito());
        assertSame(guerriero, decisione.getBersaglio());
    }

    @Test
    void deboleAllAzioneSceglieChiSubisceDiPiuLAzioneDellaVoce() {
        goblin2.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.FULMINE, 1);
        Tattica tattica = Tattica.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO, Filtro.senzaParametro(TipoFiltro.DEBOLE_ALL_AZIONE)),
                TipoAzione.FULMINE));
        Decisione decisione = Valutatore.decidi(tattica, perIMiei(mago, goblin1, goblin2));
        assertEquals(TipoAzione.FULMINE, decisione.getTipoEseguito());
        assertSame(goblin2, decisione.getBersaglio(), "il bagnato, anche se è il secondo");
    }

    @Test
    void unMostroPescaFraIMieiEGliOspitiEUnaCuraNonVaAgliOspitiDeiMostri() {
        Personaggio ostaggio = new Viandante("Ottone", 1);
        ContestoTattico contesto = perIMostri(goblin1, Arrays.asList(goblin1, goblin2), Collections.singletonList(ostaggio));
        Decisione sull_ostaggio = Valutatore.decidi(Tattica.di(voce(su(TipoRosa.OSPITI, TipoOrdinamento.PRIMO), TipoAzione.ATTACCO_FISICO)), contesto);
        assertSame(ostaggio, sull_ostaggio.getBersaglio());
        Voce curaPerOspiti = voce(su(TipoRosa.OSPITI, TipoOrdinamento.PRIMO), TipoAzione.POZIONE_SALUTE);
        assertFalse(curaPerOspiti.problemi(false).isEmpty(), "un mostro non cura gli ospiti");
        assertTrue(curaPerOspiti.problemi(true).isEmpty());
    }

    // ---- l'incantesimo di danno e i suoi criteri ----

    private Decisione conCriterio(CriterioIncantesimo criterio, Personaggio attore, ContestoTattico contesto) {
        return Valutatore.decidi(Tattica.di(new Voce(null, Collections.emptyList(), Collections.singletonList(Azione.incantesimoDiDanno(criterio, false)))),
                contesto);
    }

    private static final List<ClasseIncantesimo> TRE = Arrays.asList(ClasseIncantesimo.FUOCO, ClasseIncantesimo.FULMINE, ClasseIncantesimo.GELO);

    private void pergameneDelleTre() {
        for (ClasseIncantesimo classe : TRE) {
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
        ClasseIncantesimo piuPotente = TRE.get(0);
        for (ClasseIncantesimo classe : TRE) {
            if (((IncantesimoMalefico) FabbricaIncantesimi.crea(classe, 1)).getDanni()
                    > ((IncantesimoMalefico) FabbricaIncantesimi.crea(piuPotente, 1)).getDanni()) {
                piuPotente = classe;
            }
        }
        assertEquals(TipoAzione.di(piuPotente), conCriterio(CriterioIncantesimo.PIU_POTENTE, mago, contesto).getTipoEseguito());
    }

    @Test
    void aCasoSceglieFraQuelliLanciabiliEIncantesimoDiDannoNonComprendeMorteESilenzio() {
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.MORTE, 1);
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.SILENZIO, 1);
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
        ClasseIncantesimo menoCaro = TRE.get(0);
        for (ClasseIncantesimo classe : TRE) {
            if (FabbricaIncantesimi.costoAcquisto(classe) < FabbricaIncantesimi.costoAcquisto(menoCaro)) {
                menoCaro = classe;
            }
        }
        // Può darsi che un incantesimo non basti nemmeno su un goblin con 1 punto (resistenze): si controlla solo che sia fra quelli adatti
        Decisione economico = conCriterio(CriterioIncantesimo.PIU_ECONOMICO_CHE_BASTA, mago, contesto);
        assertTrue(TRE.contains(economico.getIncantesimo()));
        if (TRE.stream().allMatch(c -> CalcolatoreCombattimento.dannoAtteso(mago, goblin1, (com.threeamigos.foresta.interfacce.Arma)
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
        scorteDeiMiei.addIncantesimi(ClasseIncantesimo.FULMINE, 1);
        ContestoTattico contesto = perIMiei(guerriero, goblin1);
        double incantesimo = CalcolatoreCombattimento.dannoAtteso(guerriero, goblin1, (com.threeamigos.foresta.interfacce.Arma) FabbricaIncantesimi.crea(ClasseIncantesimo.FULMINE, 1));
        double mischia = CalcolatoreCombattimento.dannoAttesoDellaMischia(guerriero, goblin1);
        Tattica tattica = Tattica.di(new Voce(null, Collections.emptyList(), Arrays.asList(
                Azione.incantesimoDiDanno(CriterioIncantesimo.PIU_ADATTO, true), Azione.di(TipoAzione.ATTACCO_FISICO))));
        guerriero.addMagia(guerriero.getMagiaMassima());
        Decisione decisione = Valutatore.decidi(tattica, contesto);
        assertEquals(incantesimo > mischia ? TipoAzione.FULMINE : TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito(),
                "incantesimo " + incantesimo + " mischia " + mischia);
    }

    // ---- le voci non valide ----

    @Test
    void laLetturaRifiutaLeVociIncoerenti() {
        assertThrows(IllegalArgumentException.class, () -> Azione.di(TipoAzione.INCANTESIMO_DI_DANNO));
        assertThrows(IllegalArgumentException.class, () -> Azione.di(TipoAzione.POZIONE_SALUTE).soloSePiuForteDellAttacco());
        assertThrows(IllegalArgumentException.class, () -> new Voce(null, Collections.emptyList(), Collections.emptyList()));
        Voce mista = voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), TipoAzione.FULMINE, TipoAzione.POZIONE_SALUTE);
        assertFalse(mista.problemi(true).isEmpty(), "portate diverse");
        Voce attaccoAgliAlleati = voce(su(TipoRosa.ALLEATI, TipoOrdinamento.PRIMO), TipoAzione.ATTACCO_FISICO);
        assertFalse(attaccoAgliAlleati.problemi(true).isEmpty());
        Tattica tattica = Tattica.di(voce(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), TipoAzione.ATTACCO_FISICO), attaccoAgliAlleati);
        assertEquals(1, tattica.problemi(true).size());
        assertTrue(tattica.problemi(true).get(0).startsWith("voce 2"));
        assertEquals(Collections.emptyList(), Tattica.di(voce(null, TipoAzione.ATTACCO_FISICO)).problemi(true), "il selettore di base");
    }
}
