package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tattiche.rose.Rose;
import com.threeamigos.foresta.tattiche.valutazione.Filtri;
import com.threeamigos.foresta.tattiche.valutazione.Ordinamenti;
import com.threeamigos.foresta.tattiche.valutazione.SceltaIncantesimo;
import com.threeamigos.foresta.tipi.EsitoAzione;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoConfronto;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoFiltro;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoOrdinamento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRosa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi che gli scontri interi non producono: le tattiche base che i numeri di oggi non generano, i contesti senza gruppo, i non morti nel
 * gruppo del giocatore, un filtro su un personaggio senza salute massima.
 */
class UltimiRamiDelleTatticheTest {

    private static Gruppo gruppo(boolean delGiocatore, Personaggio... membri) {
        return new Gruppo() {
            {
                personaggi.addAll(Arrays.asList(membri));
                capo = membri[0];
            }

            @Override
            public boolean isGruppoGiocatore() {
                return delGiocatore;
            }
        };
    }

    @BeforeEach
    void prepara() {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
        DatiDiProvaDelleTattiche.nuovaPartita();
        StatoDelCombattimento.getIstanza().reimposta();
    }

    @AfterEach
    void ripulisce() {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
    }

    // ---- Le tattiche base che oggi nessuna classe genera

    @Test
    void leFasceDeiMostriPerCombinazioniCheOggiNonEsistono() throws Exception {
        Method esperto = TatticheBase.class.getDeclaredMethod("fasciaEsperto", TipoPersonaggio.class, boolean.class, boolean.class, int.class);
        esperto.setAccessible(true);
        Method stratega = TatticheBase.class.getDeclaredMethod("fasciaStratega", TipoPersonaggio.class, boolean.class, int.class);
        stratega.setAccessible(true);
        for (boolean cacciatore : new boolean[] {false, true}) {
            for (boolean magico : new boolean[] {false, true}) {
                List<?> voci = (List<?>) esperto.invoke(null, TipoPersonaggio.GIGANTE, magico, cacciatore, 60);
                assertFalse(voci.isEmpty());
            }
        }
        for (boolean magico : new boolean[] {false, true}) {
            assertFalse(((List<?>) stratega.invoke(null, TipoPersonaggio.GIGANTE, magico, 90)).isEmpty());
        }
    }

    // ---- Il contesto senza gruppo

    @Test
    void unContestoSenzaGruppoNonHaScorteNeCapo() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        ContestoTattico vuoto = new ContestoTattico(goblin, false, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertEquals(0, vuoto.getPergamene(TipoIncantesimo.FUOCO));
        assertEquals(0, vuoto.getPozioniSalute());
        assertEquals(0, vuoto.getPozioniSaluteGrandi());
        assertEquals(0, vuoto.getPozioniMagia());
        assertEquals(0, vuoto.getPozioniMagiaGrandi());
        assertFalse(vuoto.isCapo(goblin));
    }

    // ---- I filtri

    @Test
    void ilFiltroDellaSalutePercentualeSenzaSaluteMassima() {
        Personaggio senzaSalute = new Goblin(1) {
            @Override
            public int getSaluteMassima() {
                return 0;
            }
        };
        Personaggio mago = new Mago("Merlino", 3);
        Gruppo miei = gruppo(true, mago);
        Gruppo mostri = gruppo(false, senzaSalute);
        ContestoTattico contesto = ContestoTattico.per(mago, miei, mostri);
        FiltroMD zero = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, TipoConfronto.UGUALE, 0);
        assertTrue(Filtri.ammette(zero, senzaSalute, contesto));
    }

    @Test
    void ilFiltroDelleArmeConOgniTipoDiDanno() {
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        Gruppo miei = gruppo(true, guerriero);
        ContestoTattico contesto = ContestoTattico.per(guerriero, miei, gruppo(false, FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1)));
        int passano = 0;
        for (TipoDanno danno : TipoDanno.values()) {
            if (Filtri.ammette(FiltroMD.conTipoDanno(TipoFiltro.ARMA_CHE_DA, danno), guerriero, contesto)) {
                passano++;
            }
        }
        assertTrue(passano >= 1, "l'arma del guerriero dà almeno un tipo di danno");
        // con una seconda arma in mano, basta che ne dia uno la seconda
        Personaggio conDueArmi = new Guerriero("Zeno", 5) {
            @Override
            public java.util.Optional<com.threeamigos.foresta.artefatti.Arma> getArmaSecondaria() {
                return java.util.Optional.of((com.threeamigos.foresta.artefatti.Arma) new com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle(new java.util.Random(4))
                        .generaArtefatto(TipoArtefatto.MAZZA, 5));
            }
        };
        int conLaSeconda = 0;
        for (TipoDanno danno : TipoDanno.values()) {
            if (Filtri.ammette(FiltroMD.conTipoDanno(TipoFiltro.ARMA_CHE_DA, danno), conDueArmi, contesto)) {
                conLaSeconda++;
            }
        }
        assertTrue(conLaSeconda >= 1);
        // un'arma che dà un danno che la principale non dà: passa solo per la seconda
        boolean soloLaSeconda = false;
        for (TipoDanno danno : TipoDanno.values()) {
            FiltroMD filtro = FiltroMD.conTipoDanno(TipoFiltro.ARMA_CHE_DA, danno);
            soloLaSeconda |= Filtri.ammette(filtro, conDueArmi, contesto) && !Filtri.ammette(filtro, guerriero, contesto);
        }
        assertTrue(soloLaSeconda);
    }

    @Test
    void ilFiltroMagicoPerChiHaMagiaEHaGiaLanciato() {
        Personaggio conMagia = new Guerriero("Arsenio", 5) {
            @Override
            public int getMagia() {
                return 5;
            }
        };
        Gruppo miei = gruppo(true, conMagia);
        ContestoTattico contesto = ContestoTattico.per(conMagia, miei, gruppo(false, FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1)));
        FiltroMD magico = FiltroMD.senzaParametro(TipoFiltro.MAGICO);
        assertFalse(Filtri.ammette(magico, conMagia, contesto));
        Personaggio senzaMagia = new Guerriero("Zeno", 5) {
            @Override
            public int getMagia() {
                return 0;
            }
        };
        assertFalse(Filtri.ammette(magico, senzaMagia, ContestoTattico.per(senzaMagia, gruppo(true, senzaMagia), gruppo(false, FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1)))));
        StatoDelCombattimento.getIstanza().registraIncantesimoLanciato(conMagia);
        assertTrue(Filtri.ammette(magico, conMagia, contesto));
    }

    // ---- Gli ordinamenti

    @Test
    void leMisureDegliOrdinamentiInParole() {
        Personaggio a = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Personaggio b = FabbricaPersonaggi.crea(TipoPersonaggio.TROLL, 7);
        b.subSalute(b.getSalute() / 3, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        List<Personaggio> candidati = Arrays.asList(a, b);
        boolean conFrazioni = false;
        for (TipoOrdinamento tipo : TipoOrdinamento.values()) {
            String misure = Ordinamenti.descriviLeMisure(candidati, com.threeamigos.foresta.tattiche.modifica.BozzaTattica.ordinamentoPredefinito(tipo));
            assertNotNull(misure);
            if (tipo == TipoOrdinamento.ULTIMO_SCELTO || tipo == TipoOrdinamento.ULTIMO_ATTACCANTE || tipo == TipoOrdinamento.PRIMO
                    || tipo == TipoOrdinamento.A_CASO) {
                assertEquals("", misure, tipo.name());
            } else {
                assertFalse(misure.isEmpty(), tipo.name());
                conFrazioni |= misure.matches(".*\\d\\.\\d\\d.*");
            }
        }
        assertTrue(conFrazioni, "almeno una misura ha i decimali");
    }

    // ---- Le rose

    @Test
    void unNonMortoNelGruppoDelGiocatoreNonSiCuraNeSiRisuscita() {
        GruppoGiocatore giocatore = GruppoGiocatore.getIstanza();
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        guerriero.getModelloDati().setPng(false);
        Personaggio scheletroVivo = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        Personaggio scheletroCaduto = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        Personaggio caduto = new Guerriero("Caino", 3);
        caduto.getModelloDati().setPng(false);
        Personaggio inPanchina = new Guerriero("Abele", 3);
        inPanchina.getModelloDati().setPng(false);
        for (Personaggio p : Arrays.asList(guerriero, scheletroVivo, scheletroCaduto, caduto, inPanchina)) {
            giocatore.aggiungiPersonaggio(p);
        }
        for (Personaggio p : Arrays.asList(scheletroCaduto, caduto, inPanchina)) {
            p.subSalute(p.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        }
        inPanchina.setInPanchina(true);
        ContestoTattico contesto = ContestoTattico.per(guerriero, giocatore, gruppo(false, FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1)));
        List<Personaggio> caduti = Rose.di(TipoRosa.ALLEATI_CADUTI).candidati(contesto);
        assertEquals(Collections.singletonList(caduto), caduti);
        List<Personaggio> inCampo = Rose.di(TipoRosa.ALLEATI_E_OSPITI).candidati(contesto);
        assertTrue(inCampo.contains(guerriero));
        assertFalse(inCampo.contains(scheletroVivo));
    }

    // ---- La scelta dell'incantesimo e il canto a vuoto

    @Test
    void unaImmunitaScopertaFermaLaScelta() {
        Personaggio mago = new Mago("Merlino", 5);
        mago.getModelloDati().setPng(false);
        mago.addMagia(mago.getMagiaMassima());
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        GruppoGiocatore giocatore = GruppoGiocatore.getIstanza();
        giocatore.aggiungiPersonaggio(mago);
        giocatore.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        Gruppo mostri = gruppo(false, goblin);
        AzioneMD danno = AzioneMD.incantesimoDiDanno(com.threeamigos.foresta.tipi.CriterioIncantesimo.PIU_ADATTO, false);
        ContestoTattico contesto = ContestoTattico.per(mago, giocatore, mostri);
        assertNotNull(SceltaIncantesimo.scegli(danno, mago, goblin, contesto));
        StatoDelCombattimento.getIstanza().registraEsitoAzione(mago, goblin, TipoAzione.FUOCO, EsitoAzione.IMMUNE);
        assertNull(SceltaIncantesimo.scegli(danno, mago, goblin, contesto));
    }

    @Test
    void ilCantoCheNonHaNessunoSuCuiCantareNonCostaIlTurnoDiRicarica() {
        Personaggio bardo = new Bardo("Orfeo", 5);
        bardo.getModelloDati().setPng(false);
        bardo.addMagia(bardo.getMagiaMassima());
        bardo.addSalute(bardo.getSaluteMassima());
        com.threeamigos.foresta.modellodati.CantiImparatiMD canti = new com.threeamigos.foresta.modellodati.CantiImparatiMD();
        com.threeamigos.foresta.canti.CantiImparati.setFornitoreDiDati(() -> canti);
        com.threeamigos.foresta.canti.CantiImparati.impara(TipoCanto.CURATIVO);
        Gruppo miei = gruppo(true, bardo);
        Gruppo mostri = gruppo(false, FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1));
        EsecutoreTattiche esecutore = new EsecutoreTattiche(miei, mostri, StatoDelCombattimento.getIstanza(), (u, v) -> { });
        // il bardo è in piena salute: il canto curativo su di lui non ha luogo
        Decisione decisione = new Decisione(0, AzioneMD.di(TipoAzione.CANTO_CURATIVO), TipoAzione.CANTO_CURATIVO, null, bardo, new ArrayList<>());
        esecutore.esegui(bardo, decisione);
        assertFalse(StatoDelCombattimento.getIstanza().haLanciatoUnIncantesimo(bardo));
    }

    // ---- Le decisioni a parole

    @Test
    void unaDecisioneSenzaBersaglioEUnRipiegoSiScrivono() {
        Decisione senza = new Decisione(2, AzioneMD.di(TipoAzione.ALBA_SACRA), TipoAzione.ALBA_SACRA, null, null, new ArrayList<>());
        assertTrue(senza.toString().contains("voce 3"));
        assertFalse(senza.toString().contains(" su "));
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        Decisione ripiego = new Decisione(-1, AzioneMD.di(TipoAzione.ATTACCO_FISICO), TipoAzione.ATTACCO_FISICO, null, goblin, new ArrayList<>());
        assertTrue(ripiego.isRipiego());
        assertTrue(ripiego.toString().contains("(ripiego)"));
        assertNull(ripiego.getIdentificativoVoce());
    }

    // ---- Il valutatore e il cambio d'arma

    @Test
    void ilCambioDArmaNonHaUnBersaglio() {
        GruppoGiocatore giocatore = GruppoGiocatore.getIstanza();
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        guerriero.getModelloDati().setPng(false);
        giocatore.aggiungiPersonaggio(guerriero);
        com.threeamigos.foresta.artefatti.GeneratoreArtefatti generatore = new com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle(new java.util.Random(3));
        for (int i = 0; i < 10; i++) {
            giocatore.addArtefatto(generatore.generaArtefatto(TipoArtefatto.SPADA, 5));
        }
        Gruppo mostri = gruppo(false, FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1));
        int decisi = 0;
        for (TipoDanno danno : TipoDanno.values()) {
            TatticaMD tattica = TatticaMD.di(VoceMD.su(new SelettoreMD(TipoRosa.SE_STESSO, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)),
                    AzioneMD.cambiaArma(danno)));
            Decisione decisione = Valutatore.decidi(tattica, ContestoTattico.per(guerriero, giocatore, mostri));
            if (decisione != null && decisione.getTipoEseguito() == TipoAzione.CAMBIA_ARMA) {
                assertNull(decisione.getBersaglio());
                decisi++;
            }
        }
        assertTrue(decisi >= 1);
    }

    // ---- Le uguaglianze dei dati

    @Test
    void dueDatiSiConfrontanoPerOgniCampo() {
        VoceMD voce = VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)),
                AzioneMD.di(TipoAzione.ATTACCO_FISICO));
        TatticaMD tattica = TatticaMD.di(voce);
        assertEquals(tattica, TatticaMD.di(voce));
        assertNotEquals(tattica, tattica.disabilitata());
        assertNotEquals(tattica, TatticaMD.VUOTA);
        assertNotEquals(tattica, "una tattica");
        OrdinamentoMD fuoco = OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.FUOCO);
        assertEquals(fuoco, OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.FUOCO));
        assertNotEquals(fuoco, OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.GELO));
        assertNotEquals(fuoco, OrdinamentoMD.di(TipoOrdinamento.PRIMO));
        assertNotEquals(fuoco, "un ordinamento");
    }
}
