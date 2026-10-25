package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.combattimento.DannoRisultante;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tattiche.valutazione.Filtri;
import com.threeamigos.foresta.tattiche.valutazione.Ordinamenti;
import com.threeamigos.foresta.tattiche.valutazione.Selettori;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il "SU" di una voce di tattica: i filtri, gli ordinamenti e il selettore che li mette insieme (tattiche_di_combattimento_automatiche.md §11.5, §11.6).
 */
class SelezioneTest {

    private Personaggio guerriero;
    private Personaggio maga;
    private Personaggio ladro;
    private Personaggio goblin1;
    private Personaggio goblin2;
    private Personaggio fantasma;
    private Personaggio ostaggio;

    @BeforeEach
    void prepara() {
        DatiDiProvaDelleTattiche.nuovaPartita();
        StatoDelCombattimento.getIstanza().reimposta();
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        maga = FabbricaPersonaggi.crea(TipoPersonaggio.MAGA, 1);
        ladro = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 1);
        goblin1 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        goblin2 = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        fantasma = FabbricaPersonaggi.crea(TipoPersonaggio.FANTASMA, 1);
        ostaggio = new Viandante("Ottone", 1);
        // I due goblin hanno la stessa salute, così le parità sono vere (le statistiche seguono il seme)
        int differenza = goblin1.getSalute() - goblin2.getSalute();
        if (differenza > 0) {
            ferisci(goblin1, differenza);
        } else if (differenza < 0) {
            ferisci(goblin2, -differenza);
        }
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private ContestoTattico perIMiei(Personaggio attore) {
        return new ContestoTattico(attore, true, Arrays.asList(guerriero, maga, ladro), Arrays.asList(goblin1, goblin2, fantasma),
                Collections.singletonList(ostaggio));
    }

    private ContestoTattico perIMostri(Personaggio attore) {
        return new ContestoTattico(attore, false, Arrays.asList(goblin1, goblin2, fantasma), Arrays.asList(guerriero, maga, ladro),
                Collections.singletonList(ostaggio));
    }

    private static boolean passa(FiltroMD filtro, Personaggio candidato, ContestoTattico contesto) {
        return Filtri.ammette(filtro, candidato, contesto);
    }

    private static void ferisci(Personaggio personaggio, int punti) {
        personaggio.subSalute(punti, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }

    // ---- i filtri ----

    @Test
    void ogniTipoDiFiltroHaLaSuaRisposta() {
        for (TipoFiltro tipo : TipoFiltro.values()) {
            assertTrue(Filtri.haLaSuaRisposta(tipo), tipo.name());
        }
    }

    @Test
    void ilParametroDeveEssereQuelloDelTipoDiFiltro() {
        assertThrows(IllegalArgumentException.class, () -> FiltroMD.senzaParametro(TipoFiltro.SALUTE));
        assertThrows(IllegalArgumentException.class, () -> FiltroMD.conStato(TipoFiltro.CLASSE, TipoEffettoDiStato.BAGNATO));
        assertThrows(IllegalArgumentException.class, () -> FiltroMD.conConfronto(TipoFiltro.MAGICO, TipoConfronto.MINORE));
        assertDoesNotThrow(() -> FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 50));
    }

    @Test
    void laSaluteInPuntiEInPercentuale() {
        ContestoTattico contesto = perIMiei(guerriero);
        int salute = goblin1.getSalute();
        assertTrue(passa(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.UGUALE, salute), goblin1, contesto));
        assertTrue(passa(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MAGGIORE, salute - 1), goblin1, contesto));
        assertFalse(passa(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, salute), goblin1, contesto));
        // "Almeno" è NON MINORE
        assertTrue(passa(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, salute).negato(), goblin1, contesto));

        ferisci(goblin2, goblin2.getSaluteMassima() / 2 + 1);
        FiltroMD menoDellaMeta = FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, TipoConfronto.MINORE, 50);
        assertTrue(passa(menoDellaMeta, goblin2, contesto));
        assertFalse(passa(menoDellaMeta, goblin1, contesto));
    }

    @Test
    void ilLivelloAssolutoERispettoAlMio() {
        Personaggio goblinForte = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
        ContestoTattico contesto = perIMiei(guerriero);
        assertTrue(passa(FiltroMD.conConfrontoENumero(TipoFiltro.LIVELLO, TipoConfronto.UGUALE, 3), goblinForte, contesto));
        assertTrue(passa(FiltroMD.conConfronto(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoConfronto.MAGGIORE), goblinForte, contesto));
        assertFalse(passa(FiltroMD.conConfronto(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoConfronto.MAGGIORE), goblin1, contesto));
        assertTrue(passa(FiltroMD.conConfronto(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoConfronto.UGUALE), goblin1, contesto));
    }

    @Test
    void laClasseEIlRuoloEMagoEMagaSonoLaStessaCosa() {
        ContestoTattico contesto = perIMiei(guerriero);
        FiltroMD mago = FiltroMD.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.MAGO);
        assertTrue(passa(mago, maga, contesto));
        assertFalse(passa(mago, ladro, contesto));
        assertTrue(passa(FiltroMD.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.MAGA), FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1), contesto));
        assertTrue(TipoPersonaggio.BARDO.stessoRuoloDi(TipoPersonaggio.CANTASTORIE));
        assertFalse(TipoPersonaggio.BARDO.stessoRuoloDi(TipoPersonaggio.MAGO));
    }

    @Test
    void laCategoriaElOStato() {
        ContestoTattico contesto = perIMiei(guerriero);
        FiltroMD nonMorto = FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.NON_MORTO);
        assertTrue(passa(nonMorto, fantasma, contesto));
        assertFalse(passa(nonMorto, goblin1, contesto));
        FiltroMD bagnato = FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BAGNATO);
        assertFalse(passa(bagnato, goblin1, contesto));
        goblin1.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        assertTrue(passa(bagnato, goblin1, contesto));
        assertFalse(passa(bagnato.negato(), goblin1, contesto));
    }

    @Test
    void ilFiltroMagicoSegueLeTreRegole() {
        ContestoTattico contesto = perIMiei(guerriero);
        FiltroMD magico = FiltroMD.senzaParametro(TipoFiltro.MAGICO);
        // Una classe con un'abilità innata (il dardo arcano): magica se ha la magia e non è silenziata
        assertTrue(maga.getMagia() > 0);
        assertTrue(passa(magico, maga, contesto));
        maga.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertFalse(passa(magico, maga, contesto));
        // Un mostro di una classe magica lo è subito; uno che non lancia incantesimi no
        Personaggio strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        ContestoTattico delMostro = new ContestoTattico(goblin1, false, Arrays.asList(goblin1, strega), Collections.singletonList(guerriero),
                Collections.emptyList());
        assertTrue(passa(magico, strega, delMostro));
        assertFalse(passa(magico, goblin1, delMostro));
        // Gli altri dei miei, solo se hanno già lanciato un incantesimo
        assertFalse(passa(magico, ladro, contesto));
        if (ladro.getMagia() > 0) {
            StatoDelCombattimento.getIstanza().registraIncantesimoLanciato(ladro);
            assertTrue(passa(magico, ladro, contesto));
        }
    }

    @Test
    void ilFiltroDeboleEImmune() {
        ContestoTattico contesto = perIMiei(guerriero);
        FiltroMD debole = FiltroMD.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.SACRO);
        assertTrue(passa(debole, fantasma, contesto));
        assertFalse(passa(debole, goblin1, contesto));
        // Un immune non è debole, e "debole" non guarda la difesa
        Personaggio drago = FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 1);
        assertTrue(passa(FiltroMD.conTipoDanno(TipoFiltro.IMMUNE_A, TipoDanno.FUOCO), drago, contesto));
        assertFalse(passa(FiltroMD.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.FUOCO), drago, contesto));
        assertFalse(passa(FiltroMD.conTipoDanno(TipoFiltro.IMMUNE_A, TipoDanno.FUOCO), goblin1, contesto));
    }

    @Test
    void deboleAllAzioneGuardaIlTipoDiDannoDellAzioneInValutazione() {
        goblin1.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        FiltroMD debole = FiltroMD.senzaParametro(TipoFiltro.DEBOLE_ALL_AZIONE);
        // Fuori da una voce non c'è azione; il fulmine su un bagnato pesa di più, il fuoco no
        assertFalse(passa(debole, goblin1, perIMiei(guerriero)));
        assertTrue(passa(debole, goblin1, perIMiei(guerriero).conAzione(TipoAzione.FULMINE)));
        assertFalse(passa(debole, goblin1, perIMiei(guerriero).conAzione(TipoAzione.FUOCO)));
        assertFalse(passa(debole, goblin2, perIMiei(guerriero).conAzione(TipoAzione.FULMINE)));
        assertFalse(passa(debole, goblin1, perIMiei(guerriero).conAzione(TipoAzione.ATTACCO_FISICO)), "l'attacco fisico non ha un tipo di danno fisso");
    }

    @Test
    void ilCapoEMeStesso() {
        ContestoTattico contesto = perIMiei(maga);
        assertTrue(passa(FiltroMD.senzaParametro(TipoFiltro.IL_CAPO), guerriero, contesto), "il primo dei miei");
        assertTrue(passa(FiltroMD.senzaParametro(TipoFiltro.IL_CAPO), goblin1, contesto), "il primo dei mostri");
        assertFalse(passa(FiltroMD.senzaParametro(TipoFiltro.IL_CAPO), ladro, contesto));
        assertTrue(passa(FiltroMD.senzaParametro(TipoFiltro.ME_STESSO), maga, contesto));
        assertFalse(passa(FiltroMD.senzaParametro(TipoFiltro.ME_STESSO), guerriero, contesto));
        assertTrue(passa(FiltroMD.senzaParametro(TipoFiltro.ME_STESSO).negato(), guerriero, contesto), "un altro che me");
    }

    @Test
    void ilBersaglioDellUltimaVoltaEChiMiHaAttaccato() {
        ContestoTattico contesto = perIMiei(guerriero);
        FiltroMD scelto = FiltroMD.senzaParametro(TipoFiltro.SCELTO_L_ULTIMA_VOLTA);
        FiltroMD attaccante = FiltroMD.senzaParametro(TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO);
        assertFalse(passa(scelto, goblin2, contesto));
        assertFalse(passa(attaccante, goblin1, contesto));
        StatoDelCombattimento.getIstanza().registraColpo(new DannoRisultante(guerriero, goblin2));
        StatoDelCombattimento.getIstanza().registraColpo(new DannoRisultante(goblin1, guerriero));
        assertTrue(passa(scelto, goblin2, contesto));
        assertFalse(passa(scelto, goblin1, contesto));
        assertTrue(passa(attaccante, goblin1, contesto));
        assertFalse(passa(attaccante, goblin2, contesto));
        StatoDelCombattimento.getIstanza().registraAlleato(guerriero, ladro);
        assertTrue(passa(scelto, ladro, contesto), "anche l'ultimo alleato");
    }

    @Test
    void chiMiHaAttaccatoELUltimoSceltoSonoOrdinamentiCheSceglieSoloLui() {
        ContestoTattico contesto = perIMiei(guerriero);
        OrdinamentoMD attaccante = OrdinamentoMD.di(TipoOrdinamento.ULTIMO_ATTACCANTE);
        OrdinamentoMD scelto = OrdinamentoMD.di(TipoOrdinamento.ULTIMO_SCELTO);
        List<Personaggio> candidati = Arrays.asList(goblin1, goblin2);
        assertNull(Ordinamenti.scegli(candidati, attaccante, contesto), "nessuno l'ha ancora attaccato");
        assertNull(Ordinamenti.scegli(candidati, scelto, contesto));
        StatoDelCombattimento.getIstanza().registraColpo(new DannoRisultante(guerriero, goblin2));
        StatoDelCombattimento.getIstanza().registraColpo(new DannoRisultante(goblin2, guerriero));
        // Non il primo dell'elenco: proprio lui
        assertSame(goblin2, Ordinamenti.scegli(candidati, attaccante, contesto));
        assertSame(goblin2, Ordinamenti.scegli(candidati, scelto, contesto));
        // Se non è fra i candidati, nessuno: la voce non si esegue e si passa alla successiva
        assertNull(Ordinamenti.scegli(Collections.singletonList(goblin1), attaccante, contesto));
        assertNull(Ordinamenti.scegli(Collections.singletonList(goblin1), scelto, contesto));
        // Dentro un selettore: la voce non ha bersaglio
        SelettoreMD selettore = new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), attaccante);
        assertSame(goblin2, Selettori.seleziona(selettore, contesto).getScelto());
        goblin2.getModelloDati().setSalute(0);
        goblin2.subSalute(1_000_000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertTrue(Selettori.seleziona(selettore, contesto).isVuoto(), "è caduto: non c'è più nessuno da vendicare");
    }

    @Test
    void lArmaCheDaUnTipoDiDanno() {
        ContestoTattico contesto = perIMiei(guerriero);
        TipoDanno daLei = goblin1.getArmaEquipaggiata().getTipoDanno();
        assertTrue(passa(FiltroMD.conTipoDanno(TipoFiltro.ARMA_CHE_DA, daLei), goblin1, contesto));
        TipoDanno altro = daLei == TipoDanno.SACRO ? TipoDanno.FUOCO : TipoDanno.SACRO;
        assertFalse(passa(FiltroMD.conTipoDanno(TipoFiltro.ARMA_CHE_DA, altro), goblin1, contesto));
    }

    @Test
    void gliOspitiNonPassanoMaiIFiltriDiClasseECategoria() {
        ContestoTattico contesto = perIMostri(goblin1);
        assertFalse(passa(FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.UMANOIDE), ostaggio, contesto));
        assertFalse(passa(FiltroMD.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.VIANDANTE), ostaggio, contesto));
        assertFalse(passa(FiltroMD.senzaParametro(TipoFiltro.MAGICO), ostaggio, contesto));
        // I filtri su salute e stati li comprendono
        assertTrue(passa(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MAGGIORE, 0), ostaggio, contesto));
    }

    // ---- gli ordinamenti ----

    private static Personaggio scegli(OrdinamentoMD ordinamento, Personaggio... candidati) {
        return Ordinamenti.scegli(Arrays.asList(candidati), ordinamento);
    }

    @Test
    void ogniTipoDiOrdinamentoSceglieUnCandidato() {
        for (TipoOrdinamento tipo : TipoOrdinamento.values()) {
            if (tipo == TipoOrdinamento.ULTIMO_ATTACCANTE || tipo == TipoOrdinamento.ULTIMO_SCELTO) {
                continue; // guardano la storia di chi decide: si provano a parte
            }
            OrdinamentoMD ordinamento = tipo.getParametro() == com.threeamigos.foresta.tipi.TipoParametro.NESSUNO
                    ? OrdinamentoMD.di(tipo) : OrdinamentoMD.di(tipo, TipoDanno.SACRO);
            assertNotNull(scegli(ordinamento, goblin1, goblin2), tipo.name());
            assertNull(Ordinamenti.scegli(Collections.emptyList(), ordinamento), tipo.name());
        }
        assertThrows(IllegalArgumentException.class, () -> OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A));
        assertThrows(IllegalArgumentException.class, () -> OrdinamentoMD.di(TipoOrdinamento.PRIMO, TipoDanno.SACRO));
    }

    @Test
    void ilPrimoEAParitaVinceIlPrimo() {
        assertSame(goblin1, scegli(OrdinamentoMD.di(TipoOrdinamento.PRIMO), goblin1, goblin2));
        assertSame(goblin1, scegli(OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE), goblin1, goblin2), "a parità il primo");
        assertSame(goblin1, scegli(OrdinamentoMD.di(TipoOrdinamento.PIU_SALUTE), goblin1, goblin2), "a parità il primo");
    }

    @Test
    void laSaluteEIlLivello() {
        ferisci(goblin1, 3);
        assertSame(goblin1, scegli(OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE), goblin2, goblin1));
        assertSame(goblin2, scegli(OrdinamentoMD.di(TipoOrdinamento.PIU_SALUTE), goblin1, goblin2));
        Personaggio goblinForte = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
        assertSame(goblinForte, scegli(OrdinamentoMD.di(TipoOrdinamento.LIVELLO_PIU_ALTO), goblin1, goblinForte));
        assertSame(goblin1, scegli(OrdinamentoMD.di(TipoOrdinamento.LIVELLO_PIU_BASSO), goblinForte, goblin1));
    }

    @Test
    void ilPiuDeboleEIlPiuResistente() {
        Personaggio drago = FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 1);
        // Il fantasma è debole al sacro, il Drago non lo è: il goblin sta in mezzo
        assertSame(fantasma, scegli(OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.SACRO), goblin1, drago, fantasma));
        // Il Drago è immune al fuoco: è il più resistente
        assertSame(drago, scegli(OrdinamentoMD.di(TipoOrdinamento.PIU_RESISTENTE_A, TipoDanno.FUOCO), goblin1, drago, fantasma));
    }

    @Test
    void laMagia() {
        goblin1.subMagia(goblin1.getMagia());
        assertSame(maga, scegli(OrdinamentoMD.di(TipoOrdinamento.PIU_MAGIA), guerriero, maga, goblin1));
        assertSame(goblin1, scegli(OrdinamentoMD.di(TipoOrdinamento.MENO_MAGIA), guerriero, maga, goblin1));
    }

    @Test
    void aCasoSceglieFraTuttiEConIlSemeERipetibile() {
        List<Personaggio> candidati = Arrays.asList(goblin1, goblin2, fantasma);
        Dado.impostaSeme(7);
        Personaggio primo = Ordinamenti.scegli(candidati, OrdinamentoMD.di(TipoOrdinamento.A_CASO));
        Dado.impostaSeme(7);
        assertSame(primo, Ordinamenti.scegli(candidati, OrdinamentoMD.di(TipoOrdinamento.A_CASO)));
        assertTrue(candidati.contains(primo));
    }

    // ---- il selettore ----

    @Test
    void ilSelettoreApplicaLaRosaIFiltriEL_Ordinamento() {
        ferisci(goblin2, 2);
        SelettoreMD selettore = new SelettoreMD(TipoRosa.AVVERSARI,
                Collections.singletonList(FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.UMANOIDE)),
                OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE));
        RisultatoSelezione risultato = Selettori.seleziona(selettore, perIMiei(maga));
        assertSame(goblin2, risultato.getScelto());
        assertEquals(Arrays.asList(goblin1, goblin2), risultato.getRimasti());
        assertEquals(3, risultato.getPassi().size());
        assertTrue(risultato.getPassi().get(0).startsWith("AVVERSARI: 3 ["), risultato.getPassi().get(0));
        assertTrue(risultato.getPassi().get(1).contains(": 2 ["), risultato.getPassi().get(1));
        assertFalse(risultato.isVuoto());
    }

    @Test
    void senzaCandidatiLaVoceNonEEseguibile() {
        SelettoreMD selettore = new SelettoreMD(TipoRosa.AVVERSARI,
                Collections.singletonList(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO)),
                OrdinamentoMD.di(TipoOrdinamento.PRIMO));
        RisultatoSelezione risultato = Selettori.seleziona(selettore, perIMiei(maga));
        assertNull(risultato.getScelto());
        assertTrue(risultato.isVuoto());
        // Con lo stato sui nemici e la negazione ("non ancora silenziato") rimangono tutti
        SelettoreMD nonSilenziati = new SelettoreMD(TipoRosa.AVVERSARI,
                Collections.singletonList(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO).negato()),
                OrdinamentoMD.di(TipoOrdinamento.PRIMO));
        assertEquals(3, Selettori.seleziona(nonSilenziati, perIMiei(maga)).getRimasti().size());
    }

    @Test
    void ilCantoDiValoreSiSaltaQuandoTuttiSonoIspirati() {
        SelettoreMD senzaIspirazione = new SelettoreMD(TipoRosa.ALLEATI,
                Collections.singletonList(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.ISPIRATO).negato()),
                OrdinamentoMD.di(TipoOrdinamento.PRIMO));
        ContestoTattico contesto = perIMiei(maga);
        assertFalse(Selettori.seleziona(senzaIspirazione, contesto).isVuoto());
        for (Personaggio p : Arrays.asList(guerriero, maga, ladro)) {
            p.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
        }
        assertTrue(Selettori.seleziona(senzaIspirazione, contesto).isVuoto());
    }

    @Test
    void ilMostroSceglieFraIMieiEGliOspitiConLaStessaRosaDiOggi() {
        SelettoreMD selettore = new SelettoreMD(TipoRosa.AVVERSARI_E_OSPITI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE));
        RisultatoSelezione risultato = Selettori.seleziona(selettore, perIMostri(goblin1));
        assertEquals(4, risultato.getRimasti().size());
        assertTrue(risultato.getRimasti().contains(ostaggio));
        assertThrows(IllegalStateException.class,
                () -> Selettori.seleziona(new SelettoreMD(TipoRosa.AVVERSARI_E_OSPITI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO)), perIMiei(maga)));
    }

    // ---- i tipi di azione ----

    @Test
    void ogniIncantesimoHaLaSuaAzioneEOgniAzioneHaRoseValide() {
        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            boolean trovata = false;
            for (TipoAzione azione : TipoAzione.values()) {
                trovata |= azione.getClasseIncantesimo() == classe;
            }
            assertTrue(trovata, "manca il TipoAzione di " + classe);
        }
        for (TipoAzione azione : TipoAzione.values()) {
            for (boolean delGiocatore : new boolean[]{true, false}) {
                assertFalse(azione.roseAmmesse(delGiocatore).isEmpty(), azione.name());
                for (TipoRosa rosa : azione.roseAmmesse(delGiocatore)) {
                    assertTrue(rosa.valePer(delGiocatore), azione + " ammette la rosa " + rosa + " che non vale per questo lato");
                }
            }
        }
        assertEquals(TipoDanno.FULMINE, TipoAzione.FULMINE.getTipoDanno());
        assertEquals(TipoDanno.ARCANO, TipoAzione.DARDO_ARCANO.getTipoDanno());
        assertNull(TipoAzione.ATTACCO_FISICO.getTipoDanno());
    }
}
