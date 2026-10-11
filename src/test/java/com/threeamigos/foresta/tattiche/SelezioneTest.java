package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.DannoRisultante;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
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
        ModelloDati.setIstanza(new ModelloDati());
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

    private static boolean passa(Filtro filtro, Personaggio candidato, ContestoTattico contesto) {
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
        assertThrows(IllegalArgumentException.class, () -> Filtro.senzaParametro(TipoFiltro.SALUTE));
        assertThrows(IllegalArgumentException.class, () -> Filtro.conStato(TipoFiltro.CLASSE, TipoEffettoDiStato.BAGNATO));
        assertThrows(IllegalArgumentException.class, () -> Filtro.conConfronto(TipoFiltro.MAGICO, Confronto.MINORE));
        assertDoesNotThrow(() -> Filtro.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MINORE, 50));
    }

    @Test
    void laSaluteInPuntiEInPercentuale() {
        ContestoTattico contesto = perIMiei(guerriero);
        int salute = goblin1.getSalute();
        assertTrue(passa(Filtro.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.UGUALE, salute), goblin1, contesto));
        assertTrue(passa(Filtro.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MAGGIORE, salute - 1), goblin1, contesto));
        assertFalse(passa(Filtro.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MINORE, salute), goblin1, contesto));
        // "Almeno" è NON MINORE
        assertTrue(passa(Filtro.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MINORE, salute).negato(), goblin1, contesto));

        ferisci(goblin2, goblin2.getSaluteMassima() / 2 + 1);
        Filtro menoDellaMeta = Filtro.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, Confronto.MINORE, 50);
        assertTrue(passa(menoDellaMeta, goblin2, contesto));
        assertFalse(passa(menoDellaMeta, goblin1, contesto));
    }

    @Test
    void ilLivelloAssolutoERispettoAlMio() {
        Personaggio goblinForte = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
        ContestoTattico contesto = perIMiei(guerriero);
        assertTrue(passa(Filtro.conConfrontoENumero(TipoFiltro.LIVELLO, Confronto.UGUALE, 3), goblinForte, contesto));
        assertTrue(passa(Filtro.conConfronto(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, Confronto.MAGGIORE), goblinForte, contesto));
        assertFalse(passa(Filtro.conConfronto(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, Confronto.MAGGIORE), goblin1, contesto));
        assertTrue(passa(Filtro.conConfronto(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, Confronto.UGUALE), goblin1, contesto));
    }

    @Test
    void laClasseEIlRuoloEMagoEMagaSonoLaStessaCosa() {
        ContestoTattico contesto = perIMiei(guerriero);
        Filtro mago = Filtro.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.MAGO);
        assertTrue(passa(mago, maga, contesto));
        assertFalse(passa(mago, ladro, contesto));
        assertTrue(passa(Filtro.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.MAGA), FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1), contesto));
        assertTrue(TipoPersonaggio.BARDO.stessoRuoloDi(TipoPersonaggio.CANTASTORIE));
        assertFalse(TipoPersonaggio.BARDO.stessoRuoloDi(TipoPersonaggio.MAGO));
    }

    @Test
    void laCategoriaElOStato() {
        ContestoTattico contesto = perIMiei(guerriero);
        Filtro nonMorto = Filtro.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.NON_MORTO);
        assertTrue(passa(nonMorto, fantasma, contesto));
        assertFalse(passa(nonMorto, goblin1, contesto));
        Filtro bagnato = Filtro.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BAGNATO);
        assertFalse(passa(bagnato, goblin1, contesto));
        goblin1.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        assertTrue(passa(bagnato, goblin1, contesto));
        assertFalse(passa(bagnato.negato(), goblin1, contesto));
    }

    @Test
    void ilFiltroMagicoSegueLeTreRegole() {
        ContestoTattico contesto = perIMiei(guerriero);
        Filtro magico = Filtro.senzaParametro(TipoFiltro.MAGICO);
        // Una classe con un'abilità innata (il dardo arcano): magica se ha la magia e non è silenziata
        assertTrue(maga.getMagia() > 0);
        assertTrue(passa(magico, maga, contesto));
        maga.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertFalse(passa(magico, maga, contesto));
        // Un mostro di una classe magica lo è subito; uno che non lancia incantesimi no
        Personaggio strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        ContestoTattico delMostro = new ContestoTattico(goblin1, false, Arrays.asList(goblin1, strega), Arrays.asList(guerriero),
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
        Filtro debole = Filtro.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.SACRO);
        assertTrue(passa(debole, fantasma, contesto));
        assertFalse(passa(debole, goblin1, contesto));
        // Un immune non è debole, e "debole" non guarda la difesa
        Personaggio drago = FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 1);
        assertTrue(passa(Filtro.conTipoDanno(TipoFiltro.IMMUNE_A, TipoDanno.FUOCO), drago, contesto));
        assertFalse(passa(Filtro.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.FUOCO), drago, contesto));
        assertFalse(passa(Filtro.conTipoDanno(TipoFiltro.IMMUNE_A, TipoDanno.FUOCO), goblin1, contesto));
    }

    @Test
    void deboleAllAzioneGuardaIlTipoDiDannoDellAzioneInValutazione() {
        goblin1.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        Filtro debole = Filtro.senzaParametro(TipoFiltro.DEBOLE_ALL_AZIONE);
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
        assertTrue(passa(Filtro.senzaParametro(TipoFiltro.IL_CAPO), guerriero, contesto), "il primo dei miei");
        assertTrue(passa(Filtro.senzaParametro(TipoFiltro.IL_CAPO), goblin1, contesto), "il primo dei mostri");
        assertFalse(passa(Filtro.senzaParametro(TipoFiltro.IL_CAPO), ladro, contesto));
        assertTrue(passa(Filtro.senzaParametro(TipoFiltro.ME_STESSO), maga, contesto));
        assertFalse(passa(Filtro.senzaParametro(TipoFiltro.ME_STESSO), guerriero, contesto));
        assertTrue(passa(Filtro.senzaParametro(TipoFiltro.ME_STESSO).negato(), guerriero, contesto), "un altro che me");
    }

    @Test
    void ilBersaglioDellUltimaVoltaEChiMiHaAttaccato() {
        ContestoTattico contesto = perIMiei(guerriero);
        Filtro scelto = Filtro.senzaParametro(TipoFiltro.SCELTO_L_ULTIMA_VOLTA);
        Filtro attaccante = Filtro.senzaParametro(TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO);
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
    void lArmaCheDaUnTipoDiDanno() {
        ContestoTattico contesto = perIMiei(guerriero);
        TipoDanno daLei = goblin1.getArmaEquipaggiata().getTipoDanno();
        assertTrue(passa(Filtro.conTipoDanno(TipoFiltro.ARMA_CHE_DA, daLei), goblin1, contesto));
        TipoDanno altro = daLei == TipoDanno.SACRO ? TipoDanno.FUOCO : TipoDanno.SACRO;
        assertFalse(passa(Filtro.conTipoDanno(TipoFiltro.ARMA_CHE_DA, altro), goblin1, contesto));
    }

    @Test
    void gliOspitiNonPassanoMaiIFiltriDiClasseECategoria() {
        ContestoTattico contesto = perIMostri(goblin1);
        assertFalse(passa(Filtro.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.UMANOIDE), ostaggio, contesto));
        assertFalse(passa(Filtro.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.VIANDANTE), ostaggio, contesto));
        assertFalse(passa(Filtro.senzaParametro(TipoFiltro.MAGICO), ostaggio, contesto));
        // I filtri su salute e stati li comprendono
        assertTrue(passa(Filtro.conConfrontoENumero(TipoFiltro.SALUTE, Confronto.MAGGIORE, 0), ostaggio, contesto));
    }

    // ---- gli ordinamenti ----

    private static Personaggio scegli(Ordinamento ordinamento, Personaggio... candidati) {
        return Ordinamenti.scegli(Arrays.asList(candidati), ordinamento);
    }

    @Test
    void ogniTipoDiOrdinamentoSceglieUnCandidato() {
        for (TipoOrdinamento tipo : TipoOrdinamento.values()) {
            Ordinamento ordinamento = tipo.getParametro() == com.threeamigos.foresta.tipi.tattiche.TipoParametro.NESSUNO
                    ? Ordinamento.di(tipo) : Ordinamento.di(tipo, TipoDanno.SACRO);
            assertNotNull(scegli(ordinamento, goblin1, goblin2), tipo.name());
            assertNull(Ordinamenti.scegli(Collections.emptyList(), ordinamento), tipo.name());
        }
        assertThrows(IllegalArgumentException.class, () -> Ordinamento.di(TipoOrdinamento.PIU_DEBOLE_A));
        assertThrows(IllegalArgumentException.class, () -> Ordinamento.di(TipoOrdinamento.PRIMO, TipoDanno.SACRO));
    }

    @Test
    void ilPrimoEAParitaVinceIlPrimo() {
        assertSame(goblin1, scegli(Ordinamento.di(TipoOrdinamento.PRIMO), goblin1, goblin2));
        assertSame(goblin1, scegli(Ordinamento.di(TipoOrdinamento.MENO_SALUTE), goblin1, goblin2), "a parità il primo");
        assertSame(goblin1, scegli(Ordinamento.di(TipoOrdinamento.PIU_SALUTE), goblin1, goblin2), "a parità il primo");
    }

    @Test
    void laSaluteEIlLivello() {
        ferisci(goblin1, 3);
        assertSame(goblin1, scegli(Ordinamento.di(TipoOrdinamento.MENO_SALUTE), goblin2, goblin1));
        assertSame(goblin2, scegli(Ordinamento.di(TipoOrdinamento.PIU_SALUTE), goblin1, goblin2));
        Personaggio goblinForte = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
        assertSame(goblinForte, scegli(Ordinamento.di(TipoOrdinamento.LIVELLO_PIU_ALTO), goblin1, goblinForte));
        assertSame(goblin1, scegli(Ordinamento.di(TipoOrdinamento.LIVELLO_PIU_BASSO), goblinForte, goblin1));
    }

    @Test
    void ilPiuDeboleEIlPiuResistente() {
        Personaggio drago = FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 1);
        // Il fantasma è debole al sacro, il Drago non lo è: il goblin sta in mezzo
        assertSame(fantasma, scegli(Ordinamento.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.SACRO), goblin1, drago, fantasma));
        // Il Drago è immune al fuoco: è il più resistente
        assertSame(drago, scegli(Ordinamento.di(TipoOrdinamento.PIU_RESISTENTE_A, TipoDanno.FUOCO), goblin1, drago, fantasma));
    }

    @Test
    void laMagia() {
        goblin1.subMagia(goblin1.getMagia());
        assertSame(maga, scegli(Ordinamento.di(TipoOrdinamento.PIU_MAGIA), guerriero, maga, goblin1));
        assertSame(goblin1, scegli(Ordinamento.di(TipoOrdinamento.MENO_MAGIA), guerriero, maga, goblin1));
    }

    @Test
    void aCasoSceglieFraTuttiEConIlSemeERipetibile() {
        List<Personaggio> candidati = Arrays.asList(goblin1, goblin2, fantasma);
        Dado.impostaSeme(7);
        Personaggio primo = Ordinamenti.scegli(candidati, Ordinamento.di(TipoOrdinamento.A_CASO));
        Dado.impostaSeme(7);
        assertSame(primo, Ordinamenti.scegli(candidati, Ordinamento.di(TipoOrdinamento.A_CASO)));
        assertTrue(candidati.contains(primo));
    }

    // ---- il selettore ----

    @Test
    void ilSelettoreApplicaLaRosaIFiltriEL_Ordinamento() {
        ferisci(goblin2, 2);
        Selettore selettore = new Selettore(TipoRosa.AVVERSARI,
                Collections.singletonList(Filtro.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.UMANOIDE)),
                Ordinamento.di(TipoOrdinamento.MENO_SALUTE));
        RisultatoSelezione risultato = selettore.seleziona(perIMiei(maga));
        assertSame(goblin2, risultato.getScelto());
        assertEquals(Arrays.asList(goblin1, goblin2), risultato.getRimasti());
        assertEquals(3, risultato.getPassi().size());
        assertEquals("AVVERSARI: 3", risultato.getPassi().get(0));
        assertTrue(risultato.getPassi().get(1).endsWith(": 2"), risultato.getPassi().get(1));
        assertFalse(risultato.isVuoto());
    }

    @Test
    void senzaCandidatiLaVoceNonEEseguibile() {
        Selettore selettore = new Selettore(TipoRosa.AVVERSARI,
                Collections.singletonList(Filtro.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO)),
                Ordinamento.di(TipoOrdinamento.PRIMO));
        RisultatoSelezione risultato = selettore.seleziona(perIMiei(maga));
        assertNull(risultato.getScelto());
        assertTrue(risultato.isVuoto());
        // Con lo stato sui nemici e la negazione ("non ancora silenziato") rimangono tutti
        Selettore nonSilenziati = new Selettore(TipoRosa.AVVERSARI,
                Collections.singletonList(Filtro.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO).negato()),
                Ordinamento.di(TipoOrdinamento.PRIMO));
        assertEquals(3, nonSilenziati.seleziona(perIMiei(maga)).getRimasti().size());
    }

    @Test
    void ilCantoDiValoreSiSaltaQuandoTuttiSonoIspirati() {
        Selettore senzaIspirazione = new Selettore(TipoRosa.ALLEATI,
                Collections.singletonList(Filtro.conStato(TipoFiltro.STATO, TipoEffettoDiStato.ISPIRATO).negato()),
                Ordinamento.di(TipoOrdinamento.PRIMO));
        ContestoTattico contesto = perIMiei(maga);
        assertFalse(senzaIspirazione.seleziona(contesto).isVuoto());
        for (Personaggio p : Arrays.asList(guerriero, maga, ladro)) {
            p.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
        }
        assertTrue(senzaIspirazione.seleziona(contesto).isVuoto());
    }

    @Test
    void ilMostroSceglieFraIMieiEGliOspitiConLaStessaRosaDiOggi() {
        Selettore selettore = new Selettore(TipoRosa.AVVERSARI_E_OSPITI, Collections.emptyList(), Ordinamento.di(TipoOrdinamento.MENO_SALUTE));
        RisultatoSelezione risultato = selettore.seleziona(perIMostri(goblin1));
        assertEquals(4, risultato.getRimasti().size());
        assertTrue(risultato.getRimasti().contains(ostaggio));
        assertThrows(IllegalStateException.class,
                () -> new Selettore(TipoRosa.AVVERSARI_E_OSPITI, Collections.emptyList(), Ordinamento.di(TipoOrdinamento.PRIMO)).seleziona(perIMiei(maga)));
    }

    // ---- i tipi di azione ----

    @Test
    void ogniIncantesimoHaLaSuaAzioneEOgniAzioneHaRoseValide() {
        for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
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
