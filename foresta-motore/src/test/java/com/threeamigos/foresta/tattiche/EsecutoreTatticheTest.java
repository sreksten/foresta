package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoDecisioneTattica;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.tattiche.*;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.tipi.TipoDanno;
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
 * L'esecutore delle decisioni delle tattiche, da solo (senza una locazione): i gruppi sono fatti sul momento, come nelle simulazioni.
 */
class EsecutoreTatticheTest {

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

    private Personaggio mago;
    private Personaggio guerriero;
    private Personaggio goblin;
    private Personaggio strega;
    private Gruppo miei;
    private Gruppo mostri;
    private final List<String> uccisioni = new ArrayList<>();
    private EsecutoreTattiche esecutore;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        StatoDelCombattimento.getIstanza().reimposta();
        mago = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1);
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        mago.addMagia(mago.getMagiaMassima());
        miei = gruppo(true, mago, guerriero);
        mostri = gruppo(false, goblin, strega);
        esecutore = new EsecutoreTattiche(miei, mostri, StatoDelCombattimento.getIstanza(), (uccisore, vittima) -> uccisioni.add(uccisore.getNome() + ">" + vittima.getNome()));
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private static SelettoreMD su(TipoRosa rosa, TipoOrdinamento ordinamento, FiltroMD... filtri) {
        return new SelettoreMD(rosa, Arrays.asList(filtri), OrdinamentoMD.di(ordinamento));
    }

    @Test
    void unMostroCheUccideUnMioPersonaggioNonDaEsperienzaNeStatistiche() {
        guerriero.subSalute(guerriero.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        TatticaMD attacca = TatticaMD.di(VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
        // Un goblin contro un guerriero con 1 punto: prima o poi un colpo va a segno
        for (int i = 0; i < 200 && guerriero.isVivo(); i++) {
            esecutore.agisci(goblin, attacca);
        }
        assertFalse(guerriero.isVivo());
        assertEquals(Collections.emptyList(), uccisioni, "il registro degli uccisi riguarda solo gli avversari messi fuori combattimento dai miei");
    }

    @Test
    void leRadiciDelDruidoRegistranoGliAvversariCheUccidono() {
        // Un'abilità colpisce più avversari insieme: chi cade conta come sconfitto (bestiario, trofei, punti), come con un incantesimo
        Personaggio druido = FabbricaPersonaggi.crea(TipoPersonaggio.DRUIDO, 1);
        druido.addMagia(druido.getMagiaMassima());
        Gruppo druidi = gruppo(true, druido);
        Gruppo nemici = gruppo(false, goblin, strega);
        goblin.subSalute(goblin.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        TatticaMD radici = TatticaMD.di(VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), AzioneMD.di(TipoAzione.RADICI)));
        EsecutoreTattiche conIDruidi = new EsecutoreTattiche(druidi, nemici, StatoDelCombattimento.getIstanza(),
                (uccisore, vittima) -> uccisioni.add(uccisore.getNome() + ">" + vittima.getNome()));
        for (int i = 0; i < 50 && goblin.isVivo(); i++) {
            Decisione d = conIDruidi.agisci(druido, radici);
            assertNotNull(d);
            assertEquals(TipoAzione.RADICI, d.getTipoEseguito());
        }
        assertFalse(goblin.isVivo());
        assertEquals(1, uccisioni.stream().filter(u -> u.endsWith(">" + goblin.getNome())).count(), uccisioni.toString());
    }

    @Test
    void unMioAttaccoCheUccideIlPrimoPassaAlProssimoELFermaQuandoFinisconoGliAvversari() {
        goblin.subSalute(goblin.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        strega.subSalute(strega.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        TatticaMD attacca = TatticaMD.di(VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
        for (int i = 0; i < 300 && !mostri.getPersonaggiVivi().isEmpty(); i++) {
            Decisione d = esecutore.agisci(guerriero, attacca);
            if (mostri.getPersonaggiVivi().isEmpty()) {
                assertNotNull(d);
            }
        }
        assertTrue(mostri.getPersonaggiVivi().isEmpty());
        assertEquals(2, uccisioni.size(), uccisioni.toString());
        // Senza più avversari non c'è niente da fare
        assertNull(esecutore.agisci(guerriero, attacca));
    }

    @Test
    void unIncantesimoConsumaLaPergamenaELaMagiaEColpisceSoloIlBersaglioDiUnMostro() {
        miei.addIncantesimi(TipoIncantesimo.FULMINE, 1);
        int magia = mago.getMagia();
        TatticaMD lancia = TatticaMD.di(VoceMD.su(su(TipoRosa.AVVERSARI, TipoOrdinamento.PRIMO), AzioneMD.di(TipoAzione.FULMINE)));
        List<InternoDecisioneTattica> decisioni = new ArrayList<>();
        BusEventi.impostaConsegna(Runnable::run);
        BusEventi.iscriviti(InternoDecisioneTattica.class, decisioni::add);
        try {
            Decisione d = esecutore.agisci(mago, lancia);
            assertEquals(TipoAzione.FULMINE, d.getTipoEseguito());
        } finally {
            BusEventi.impostaConsegna(null);
        }
        assertEquals(0, miei.getIncantesimi(TipoIncantesimo.FULMINE), "la pergamena si consuma");
        assertTrue(mago.getMagia() < magia, "e costa MAGIA");
        assertEquals(1, decisioni.size());
        assertSame(mago, decisioni.get(0).getPersonaggio());
        assertEquals(1, StatoDelCombattimento.getIstanza().getUsiDellaVoce(mago, "0"));
    }

    @Test
    void unMostroConUnaPozioneDelSuoGruppoLaBeve() {
        mostri.addPozioniSalute(1);
        goblin.subSalute(goblin.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int salute = goblin.getSalute();
        TatticaMD bevi = TatticaMD.di(VoceMD.su(su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO,
                FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, TipoConfronto.MINORE, 80)), AzioneMD.di(TipoAzione.POZIONE_SALUTE)));
        Decisione d = esecutore.agisci(goblin, bevi);
        assertEquals(TipoAzione.POZIONE_SALUTE, d.getTipoEseguito());
        assertEquals(0, mostri.getPozioniSalute());
        assertTrue(goblin.getSalute() > salute);
    }

    @Test
    void ilCambioDArmaNonFaNienteSeIlGruppoNonEQuelloDelGiocatore() {
        TatticaMD cambia = TatticaMD.di(new VoceMD(su(TipoRosa.SE_STESSO, TipoOrdinamento.PRIMO), Collections.emptyList(),
                Collections.singletonList(AzioneMD.cambiaArma(TipoDanno.SACRO))));
        // Non è applicabile (il gruppo non è un GruppoGiocatore): ripiego sull'attacco, senza eccezioni
        Decisione d = esecutore.agisci(guerriero, cambia);
        assertTrue(d.isRipiego());
        assertEquals(TipoAzione.ATTACCO_FISICO, d.getTipoEseguito());
    }
}
