package com.threeamigos.foresta.tattiche.valutazione;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tattiche.DatiDiProvaDelleTattiche;
import com.threeamigos.foresta.tattiche.Decisione;
import com.threeamigos.foresta.tattiche.Valutatore;
import com.threeamigos.foresta.tipi.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoOrdinamento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le scelte di incantesimi e azioni nei casi meno comuni: azioni su un gruppo intero, la scelta a caso che cambia da un controllo all'altro,
 * le armi che rendono più degli incantesimi.
 */
class ScelteResiduoTest {

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

    private Personaggio goblin;
    private Personaggio scheletro;
    private Gruppo mostri;

    @BeforeEach
    void prepara() {
        DatiDiProvaDelleTattiche.nuovaPartita();
        StatoDelCombattimento.getIstanza().reimposta();
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        mostri = gruppo(false, goblin, scheletro);
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private static Personaggio pronto(TipoPersonaggio classe, int livello) {
        Personaggio personaggio = FabbricaPersonaggi.crea(classe, livello);
        personaggio.addMagia(personaggio.getMagiaMassima());
        personaggio.addSalute(personaggio.getSaluteMassima());
        return personaggio;
    }

    // Un elfo con una spada da quel danno: più forte o più debole dei suoi incantesimi
    private GruppoGiocatore elfoArmato(int danni, Personaggio[] elfoRestituito) {
        GruppoGiocatore giocatore = new GruppoGiocatore();
        Personaggio elfo = pronto(TipoPersonaggio.ELFO, 5);
        giocatore.aggiungiPersonaggio(elfo);
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(TipoArtefatto.SPADA);
        md.setNome("di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(5);
        md.setPeso(0.1);
        md.setDanni(danni);
        Artefatto spada = Artefatto.di(md);
        giocatore.addArtefatto(spada);
        giocatore.equipaggia(elfo, spada);
        elfoRestituito[0] = elfo;
        return giocatore;
    }

    @Test
    void unGruppoSiColpisceConUnaVoceSuTuttoIlGruppo() {
        Personaggio druido = pronto(TipoPersonaggio.DRUIDO, 5);
        Gruppo miei = gruppo(true, druido);
        TatticaMD tattica = TatticaMD.di(VoceMD.su(null, AzioneMD.di(TipoAzione.RADICI)));

        Decisione decisione = Valutatore.decidi(tattica, ContestoTattico.per(druido, miei, mostri));

        assertEquals(TipoAzione.RADICI, decisione.getTipoEseguito());
        assertNull(decisione.getBersaglio(), "un'azione su un gruppo non ha un bersaglio solo");
        assertFalse(decisione.isRipiego());
    }

    @Test
    void seNessunaAzioneDiUnaVoceSuUnGruppoVaBenePassaAlRipiego() {
        Personaggio guerriero = pronto(TipoPersonaggio.GUERRIERO, 5);
        Gruppo miei = gruppo(true, guerriero);
        TatticaMD tattica = TatticaMD.di(VoceMD.su(null, AzioneMD.di(TipoAzione.RADICI)));

        Decisione decisione = Valutatore.decidi(tattica, ContestoTattico.per(guerriero, miei, mostri));

        assertTrue(decisione.isRipiego());
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito());
        assertTrue(decisione.getSpiegazione().stream().anyMatch(riga -> riga.contains("RADICI scartata")), decisione.getSpiegazione().toString());
    }

    @Test
    void conUnaSolaAzioneCheNonBastaPiuLaVoceVaAlRipiego() {
        Personaggio[] elfo = new Personaggio[1];
        GruppoGiocatore giocatore = elfoArmato(50, elfo);
        giocatore.addIncantesimi(TipoIncantesimo.ARIA, 1);
        giocatore.addIncantesimi(TipoIncantesimo.FULMINE, 1);
        Dado.trucca(2, 2, 2, 1);
        TatticaMD tattica = TatticaMD.di(VoceMD.su(null, AzioneMD.incantesimoDiDanno(CriterioIncantesimo.A_CASO, true)));

        Decisione decisione = Valutatore.decidi(tattica, ContestoTattico.per(elfo[0], giocatore, mostri));

        assertTrue(decisione.isRipiego());
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito());
    }

    @Test
    void unaSceltaACasoCheCambiaTraIlControlloELaSceltaVeraPassaAllAzioneSuccessiva() {
        Personaggio[] elfo = new Personaggio[1];
        GruppoGiocatore giocatore = elfoArmato(50, elfo);
        // Con quest'arma ARIA rende meno della mischia e FULMINE rende di più
        giocatore.addIncantesimi(TipoIncantesimo.ARIA, 1);
        giocatore.addIncantesimi(TipoIncantesimo.FULMINE, 1);
        // Ai controlli dei requisiti il dado dice FULMINE (la seconda faccia); alla scelta vera, l'ultimo lancio, dice ARIA
        Dado.trucca(2, 2, 2, 1);
        TatticaMD tattica = TatticaMD.di(VoceMD.su(null,
                AzioneMD.incantesimoDiDanno(CriterioIncantesimo.A_CASO, true), AzioneMD.di(TipoAzione.ATTACCO_FISICO)));

        Decisione decisione = Valutatore.decidi(tattica, ContestoTattico.per(elfo[0], giocatore, mostri));

        assertEquals(0, Dado.trucchiRimasti(), "i quattro lanci previsti ci sono stati tutti");
        assertEquals(TipoAzione.ATTACCO_FISICO, decisione.getTipoEseguito(), "l'incantesimo della scelta vera non basta: si passa all'attacco");
        assertFalse(decisione.isRipiego());
        assertTrue(decisione.getSpiegazione().stream().anyMatch(riga -> riga.contains("nessun incantesimo adatto alla scelta")),
                decisione.getSpiegazione().toString());
    }

    @Test
    void senzaUnBersaglioNonCeNienteDaLanciare() {
        Personaggio mago = pronto(TipoPersonaggio.MAGO, 5);
        Gruppo miei = gruppo(true, mago);
        miei.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        ContestoTattico contesto = ContestoTattico.per(mago, miei, mostri);

        assertTrue(SceltaIncantesimo.lanciabiliContro(mago, null, contesto).isEmpty());
        assertFalse(SceltaIncantesimo.lanciabiliContro(mago, goblin, contesto).isEmpty());
    }

    @Test
    void ilSilenzioNonSiLanciaSuChiEGiaSilenziato() {
        Personaggio mago = pronto(TipoPersonaggio.MAGO, 5);
        Gruppo miei = gruppo(true, mago);
        miei.addIncantesimi(TipoIncantesimo.SILENZIO, 1);
        ContestoTattico contesto = ContestoTattico.per(mago, miei, mostri);
        AzioneMD repertorio = AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.PIU_POTENTE, false);

        assertEquals(TipoIncantesimo.SILENZIO, SceltaIncantesimo.scegli(repertorio, mago, goblin, contesto));
        goblin.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertNull(SceltaIncantesimo.scegli(repertorio, mago, goblin, ContestoTattico.per(mago, miei, mostri)));
    }

    @Test
    void ilSilenzioNonSiLanciaSuChiEImmuneAlSilenzio() {
        Personaggio immune = null;
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            if (classe.isAvversario() && immune == null) {
                Personaggio candidato = FabbricaPersonaggi.crea(classe, 1);
                if (candidato.isImmuneAEffetto(TipoEffettoDiStato.SILENZIATO) && !candidato.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO)) {
                    immune = candidato;
                }
            }
        }
        assertNotNull(immune, "serve un avversario immune al silenzio");
        Personaggio mago = pronto(TipoPersonaggio.MAGO, 5);
        Gruppo miei = gruppo(true, mago);
        miei.addIncantesimi(TipoIncantesimo.SILENZIO, 1);
        ContestoTattico contesto = ContestoTattico.per(mago, miei, mostri);

        assertFalse(SceltaIncantesimo.lanciabiliContro(mago, immune, contesto).contains(TipoIncantesimo.SILENZIO));
        assertNull(SceltaIncantesimo.scegli(AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.PIU_POTENTE, false), mago, immune, contesto));
    }

    @Test
    void seLeArmieRendonoDiPiuLIncantesimoNonSiSceglie() {
        Personaggio[] elfo = new Personaggio[1];
        GruppoGiocatore giocatore = elfoArmato(500, elfo);
        giocatore.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        ContestoTattico contesto = ContestoTattico.per(elfo[0], giocatore, mostri);

        assertNull(SceltaIncantesimo.scegli(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, true), elfo[0], goblin, contesto));
        assertEquals(TipoIncantesimo.FUOCO,
                SceltaIncantesimo.scegli(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_POTENTE, false), elfo[0], goblin, contesto));
    }

    @Test
    void chiHaPochiBersaglioColpisceSoloQuantiNeHa() {
        Personaggio mago = pronto(TipoPersonaggio.MAGO, 1);
        Gruppo miei = gruppo(true, mago);
        miei.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        Personaggio[] nemici = new Personaggio[mago.getBersagli() + 2];
        for (int i = 0; i < nemici.length; i++) {
            nemici[i] = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        }
        Gruppo molti = gruppo(false, nemici);

        List<Personaggio> colpiti = SceltaIncantesimo.bersagli(mago, nemici[0], TipoIncantesimo.FUOCO, ContestoTattico.per(mago, miei, molti));

        assertEquals(mago.getBersagli(), colpiti.size(), "non più dei bersagli che l'attore ha");
        assertSame(nemici[0], colpiti.get(0));
    }

    @Test
    void unIncantesimoSingoloSiMisuraSenzaAltriBersagli() {
        Personaggio mago = pronto(TipoPersonaggio.MAGO, 5);
        Gruppo miei = gruppo(true, mago);
        miei.addIncantesimi(TipoIncantesimo.MORTE, 1);
        ContestoTattico contesto = ContestoTattico.per(mago, miei, mostri);

        TipoIncantesimo scelto = SceltaIncantesimo.scegli(AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.PIU_ADATTO_ESPERTO, false),
                mago, goblin, contesto);

        assertEquals(TipoIncantesimo.MORTE, scelto);
    }

    @Test
    void unOrdinamentoSenzaMisuraNonSiMisura() throws Exception {
        Method misura = Ordinamenti.class.getDeclaredMethod("misura", Personaggio.class, OrdinamentoMD.class);
        misura.setAccessible(true);
        InvocationTargetException errore = assertThrows(InvocationTargetException.class,
                () -> misura.invoke(null, goblin, OrdinamentoMD.di(TipoOrdinamento.PRIMO)));
        assertTrue(errore.getCause() instanceof IllegalStateException);
    }
}
