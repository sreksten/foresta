package com.threeamigos.foresta.trofei;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.bestiario.RegistroBestiario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.interni.InternoAmiciziaStretta;
import com.threeamigos.foresta.eventi.interni.InternoArtefattoTrovato;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoCorruzioneRiuscita;
import com.threeamigos.foresta.eventi.interni.InternoEremitiIncontrati;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoLocazioneVisitata;
import com.threeamigos.foresta.eventi.interni.InternoMissioneCompletata;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.interni.InternoPastoConsumatoInLocanda;
import com.threeamigos.foresta.eventi.interni.InternoScheletroRisvegliato;
import com.threeamigos.foresta.eventi.interni.InternoSpadaNascostaImpugnata;
import com.threeamigos.foresta.eventi.interni.InternoTrofeoAcquisito;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneIncantatura;
import com.threeamigos.foresta.interfacce.GestoreBestiario;
import com.threeamigos.foresta.interfacce.GestoreTrofei;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.BestiarioMD;
import com.threeamigos.foresta.modellodati.TrofeiMD;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.RegolaTrofeo;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I trofei: ognuno avanza con i suoi eventi, il progresso conta solo a fine locazione, e un trofeo vinto vale per sempre.
 */
class RegistroTrofeiTest {

    /**
     * Il "file" dei trofei, in memoria: il testo che verrebbe scritto su disco.
     */
    private static final class GestoreInMemoria implements GestoreTrofei {
        private String contenuto = "";
        private int salvataggi;

        void conVinti(TipoTrofeo... vinti) {
            TrofeiMD trofeiMD = new TrofeiMD();
            for (TipoTrofeo trofeo : vinti) {
                trofeiMD.aggiungiVinto(trofeo);
            }
            salva(trofeiMD);
            salvataggi = 0;
        }

        @Override
        public boolean carica(TrofeiMD trofeiMD) {
            try {
                trofeiMD.leggi(new BufferedReader(new StringReader(contenuto)));
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        public boolean salva(TrofeiMD trofeiMD) {
            StringWriter scrittura = new StringWriter();
            try (PrintWriter writer = new PrintWriter(scrittura)) {
                trofeiMD.salva(writer);
            } catch (IOException e) {
                return false;
            }
            contenuto = scrittura.toString();
            salvataggi++;
            return true;
        }
    }

    private GestoreInMemoria gestore;
    private final List<TipoTrofeo> annunciati = new ArrayList<>();

    @BeforeEach
    void preparaIRegistri() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        RegistroBestiario.impostaGestoreBestiario(new GestoreBestiario() {
            @Override
            public boolean carica(BestiarioMD bestiarioMD) {
                return true;
            }

            @Override
            public boolean salva(BestiarioMD bestiarioMD) {
                return true;
            }
        });
        gestore = new GestoreInMemoria();
        annunciati.clear();
        ricarica();
    }

    // Il registro si rilegge dal "file" e si iscrive di nuovo agli eventi
    private void ricarica() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        RegistroTrofei.impostaGestoreTrofei(gestore);
        RegistroTrofei.registrati();
        BusEventi.iscriviti(InternoTrofeoAcquisito.class, evento -> annunciati.add(evento.getTrofeo()));
    }

    @AfterEach
    void azzeraIlBus() {
        BusEventi.impostaConsegna(null);
        BusEventi.azzera();
    }

    private static void fineLocazione() {
        BusEventi.pubblica(new InternoFineLocazione());
    }

    private static Missione missione(MissionePredefinita classe) {
        return (Missione) Proxy.newProxyInstance(Missione.class.getClassLoader(), new Class<?>[]{Missione.class},
                (proxy, metodo, argomenti) -> {
                    if (metodo.getName().equals("getClasse")) {
                        return classe;
                    }
                    throw new UnsupportedOperationException(metodo.getName());
                });
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(0.1);
        return Artefatto.di(md);
    }

    private static TipoArtefatto unIngrediente() {
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            if (tipo.isIngrediente()) {
                return tipo;
            }
        }
        throw new AssertionError("nessun ingrediente");
    }

    private static MissionePredefinita unaMissioneQualunque() {
        for (MissionePredefinita classe : MissionePredefinita.values()) {
            if (classe != MissionePredefinita.LA_SFIDA_DEI_CAMPIONI) {
                return classe;
            }
        }
        throw new AssertionError();
    }

    @Test
    void ilProgressoContaSoloAFineLocazione() {
        BusEventi.pubblica(new InternoPastoConsumatoInLocanda());
        assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE), "è ancora in sospeso");
        fineLocazione();
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));
        assertEquals(1, gestore.salvataggi, "la novità si salva");

        fineLocazione();
        assertEquals(1, gestore.salvataggi, "senza novità non si riscrive niente");
    }

    @Test
    void sePartitaFinisceSenzaLocazioneIlSospesoSiScarta() {
        BusEventi.pubblica(new InternoPastoConsumatoInLocanda());
        RegistroTrofei.scartaIlProgressoInSospeso();
        fineLocazione();
        assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));
    }

    @Test
    void ilProgressoSiRicordaDaUnaPartitaAllAltra() {
        BusEventi.pubblica(new InternoPastoConsumatoInLocanda());
        BusEventi.pubblica(new InternoPastoConsumatoInLocanda());
        fineLocazione();

        ricarica();

        assertEquals(2, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));
    }

    @Test
    void unTrofeoVintoNonContaPiuENonSiAnnunciaDiNuovo() {
        BusEventi.pubblica(new InternoScheletroRisvegliato());
        fineLocazione();
        assertTrue(RegistroTrofei.isVinto(TipoTrofeo.NECROMANTE));
        assertEquals(Collections.singletonList(TipoTrofeo.NECROMANTE), annunciati);

        RegistroTrofei.incrementaProgresso(TipoTrofeo.NECROMANTE, 5);
        BusEventi.pubblica(new InternoScheletroRisvegliato());
        fineLocazione();
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.NECROMANTE));
        assertEquals(1, annunciati.size());

        // Quantità nulla o negativa non fa niente
        RegistroTrofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, 0);
        RegistroTrofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, -3);
        fineLocazione();
        assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));
    }

    @Test
    void senzaUnGestoreIProgressiRestanoInMemoria() throws Exception {
        java.lang.reflect.Field campo = RegistroTrofei.class.getDeclaredField("gestoreTrofei");
        campo.setAccessible(true);
        campo.set(null, null);

        BusEventi.pubblica(new InternoScheletroRisvegliato());
        fineLocazione();

        assertTrue(RegistroTrofei.isVinto(TipoTrofeo.NECROMANTE));
        assertEquals(0, gestore.salvataggi);
    }

    @Test
    void leMissioniCompletateContanoTutteMaLaSfidaDeiCampioniFaIlReDiCartaForbiciESasso() {
        BusEventi.pubblica(new InternoMissioneCompletata(missione(unaMissioneQualunque())));
        fineLocazione();
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.CACCIATORE_DI_TAGLIE));
        assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.RE_DI_CARTA_FORBICI_E_SASSO));

        BusEventi.pubblica(new InternoMissioneCompletata(missione(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI)));
        fineLocazione();
        assertEquals(2, RegistroTrofei.getProgresso(TipoTrofeo.CACCIATORE_DI_TAGLIE));
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.RE_DI_CARTA_FORBICI_E_SASSO));
    }

    @Test
    void leVisiteContanoSoloIlLoroTipoDiLocazione() {
        BusEventi.pubblica(new InternoLocazioneVisitata(TipoLocazione.TEMPIO));
        BusEventi.pubblica(new InternoLocazioneVisitata(TipoLocazione.GROTTA));
        BusEventi.pubblica(new InternoLocazioneVisitata(TipoLocazione.GROTTA));
        BusEventi.pubblica(new InternoLocazioneVisitata(TipoLocazione.BOSCO));
        fineLocazione();
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.TUTTI_I_SANTI));
        assertEquals(2, RegistroTrofei.getProgresso(TipoTrofeo.SPELEOLOGO));
    }

    @Test
    void gliEremitiContanoPerQuantiSonoStatiIncontrati() {
        BusEventi.pubblica(new InternoEremitiIncontrati(3));
        fineLocazione();
        assertEquals(3, RegistroTrofei.getProgresso(TipoTrofeo.VERO_ROMPISCATOLE));
    }

    @Test
    void amiciziaECorruzioneContanoUnaVoltaPerVolta() {
        BusEventi.pubblica(new InternoAmiciziaStretta(Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.SCHELETRO)));
        BusEventi.pubblica(new InternoCorruzioneRiuscita(Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.SCHELETRO)));
        fineLocazione();
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.AMICO_DI_TUTTI));
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.CORRUTTORE));
    }

    @Test
    void soloIGoblinFannoAvanzareLAmmazzagoblin() {
        BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
        BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.SCHELETRO));
        fineLocazione();
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.AMMAZZAGOBLIN));
    }

    @Test
    void iBossSiVinconoSconfiggendoliUnaVolta() {
        TipoTrofeo[] trofei = {TipoTrofeo.UCCIDI_IL_DRAGO, TipoTrofeo.UCCIDI_LA_STREGA, TipoTrofeo.UCCIDI_IL_LICH,
                TipoTrofeo.UCCIDI_L_IDRA, TipoTrofeo.UCCIDI_IL_MINOTAURO_GIGANTE};
        TipoPersonaggio[] boss = {TipoPersonaggio.DRAGO, TipoPersonaggio.STREGA, TipoPersonaggio.LICH,
                TipoPersonaggio.IDRA, TipoPersonaggio.MINOTAURO_GIGANTE};
        for (int i = 0; i < boss.length; i++) {
            assertFalse(RegistroTrofei.isVinto(trofei[i]));
            BusEventi.pubblica(new InternoAvversarioSconfitto(boss[i]));
            fineLocazione();
            assertTrue(RegistroTrofei.isVinto(trofei[i]), trofei[i].name());
        }
    }

    @Test
    void ilBottinoContaSoloSeCustoditoTranneIFofani() {
        BusEventi.pubblica(new InternoOggettoRaccolto(TipoBottino.MONETA, 5, null, true));
        BusEventi.pubblica(new InternoOggettoRaccolto(TipoBottino.MONETA, 7, null, false));
        BusEventi.pubblica(new InternoOggettoRaccolto(TipoBottino.PIETRA_PREZIOSA, 2, null, true));
        BusEventi.pubblica(new InternoOggettoRaccolto(TipoBottino.CORONA, 4, null, true));
        BusEventi.pubblica(new InternoOggettoRaccolto(TipoBottino.COFANO, 3, null, false));
        fineLocazione();
        assertEquals(5, RegistroTrofei.getProgresso(TipoTrofeo.RAPINATORE));
        assertEquals(2, RegistroTrofei.getProgresso(TipoTrofeo.LADRO_DI_PREZIOSI));
        assertEquals(4, RegistroTrofei.getProgresso(TipoTrofeo.ARSENIO_LUPIN));
        assertEquals(3, RegistroTrofei.getProgresso(TipoTrofeo.ESPERTO_SCASSINATORE));
    }

    @Test
    void gliAcquistiDagliArmaioliDistinguonoILivelliELIngredienti() {
        BusEventi.pubblica(new NotificaApprovazioneAcquistoArtefatto(null, artefatto(TipoArtefatto.SPADA, 1), "r1"));
        BusEventi.pubblica(new NotificaApprovazioneAcquistoArtefatto(null, artefatto(TipoArtefatto.SPADA, 3), "r2"));
        BusEventi.pubblica(new NotificaApprovazioneAcquistoArtefatto(null, artefatto(TipoArtefatto.SPADA, 6), "r3"));
        BusEventi.pubblica(new NotificaApprovazioneAcquistoArtefatto(null, artefatto(unIngrediente(), 6), "r4"));
        fineLocazione();
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.RIGATTIERE));
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.COLLEZIONISTA));
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.STUDIOSO));
    }

    @Test
    void gliArtefattiDegliAvversariContanoSoloSeCustoditi() {
        BusEventi.pubblica(new InternoArtefattoTrovato(artefatto(TipoArtefatto.SPADA, 2), true));
        BusEventi.pubblica(new InternoArtefattoTrovato(artefatto(TipoArtefatto.SPADA, 5), true));
        BusEventi.pubblica(new InternoArtefattoTrovato(artefatto(TipoArtefatto.SPADA, 5), false));
        fineLocazione();
        assertEquals(2, RegistroTrofei.getProgresso(TipoTrofeo.CACCIATORE_DI_TESORI));
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.ESPERTO_CACCIATORE_DI_TESORI));
    }

    @Test
    void gliAcquistiDiConsumabiliEDiIncantature() {
        BusEventi.pubblica(new NotificaApprovazioneAcquistoConsumabile(
                new ComandoAcquistoConsumabile(TipoConsumabile.INCANTESIMO, TipoIncantesimo.FUOCO, "u"), 10));
        BusEventi.pubblica(new NotificaApprovazioneAcquistoConsumabile(
                new ComandoAcquistoConsumabile(TipoConsumabile.MAPPA_COMPLETA_FORESTA, null, "u"), 10));
        BusEventi.pubblica(new NotificaApprovazioneIncantatura(artefatto(TipoArtefatto.SPADA, 2), 30));
        fineLocazione();
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.BOMBAROLO));
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.CARTOGRAFO));
        assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.TRAFFICONE));
    }

    @Test
    void laSpadaNascostaImpugnataFaIlTrovarobe() {
        BusEventi.pubblica(new InternoSpadaNascostaImpugnata());
        fineLocazione();
        assertTrue(RegistroTrofei.isVinto(TipoTrofeo.TROVAROBE));
    }

    @Test
    void ilProgressoDiUnTrofeoNonSuperaIlSuoObiettivo() {
        for (int i = 0; i < 3; i++) {
            BusEventi.pubblica(new InternoScheletroRisvegliato());
        }
        fineLocazione();
        assertEquals(3, RegistroTrofei.getProgresso(TipoTrofeo.NECROMANTE));
        Trofeo trofeo = RegolaTrofeo.di(TipoTrofeo.NECROMANTE);
        assertEquals(TipoTrofeo.NECROMANTE, trofeo.getTipo());
        assertEquals(1, trofeo.getObiettivo());
        assertEquals(1, trofeo.getProgresso());
    }

    @Test
    void ilPerdigiornoSiVinceQuandoSonoVintiTuttiGliAltri() {
        Set<TipoTrofeo> altri = EnumSet.allOf(TipoTrofeo.class);
        altri.remove(TipoTrofeo.PERDIGIORNO);
        altri.remove(TipoTrofeo.ENCICLOPEDISTA);
        gestore.conVinti(altri.toArray(new TipoTrofeo[0]));
        ricarica();
        Trofeo perdigiorno = RegolaTrofeo.di(TipoTrofeo.PERDIGIORNO);
        assertEquals(TipoTrofeo.PERDIGIORNO, perdigiorno.getTipo());
        assertEquals(TipoTrofeo.values().length - 1, perdigiorno.getObiettivo());
        assertEquals(TipoTrofeo.values().length - 2, perdigiorno.getProgresso());
        fineLocazione();
        assertFalse(RegistroTrofei.isVinto(TipoTrofeo.PERDIGIORNO), "manca l'enciclopedista");

        // Anche l'ultimo, ed è il Perdigiorno a chiudere la serie
        RegistroBestiario.impostaGestoreBestiario(new GestoreBestiario() {
            @Override
            public boolean carica(BestiarioMD bestiarioMD) {
                TipoPersonaggio.getVociDelBestiario().forEach(bestiarioMD::sblocca);
                return true;
            }

            @Override
            public boolean salva(BestiarioMD bestiarioMD) {
                return true;
            }
        });
        fineLocazione();
        assertTrue(RegistroTrofei.isVinto(TipoTrofeo.ENCICLOPEDISTA));
        assertTrue(RegistroTrofei.isVinto(TipoTrofeo.PERDIGIORNO));
        assertEquals(Arrays.asList(TipoTrofeo.ENCICLOPEDISTA, TipoTrofeo.PERDIGIORNO), annunciati);
    }

    @Test
    void lEnciclopedistaGuardaLeVociDelBestiario() {
        Trofeo enciclopedista = RegolaTrofeo.di(TipoTrofeo.ENCICLOPEDISTA);
        enciclopedista.registrati();
        assertEquals(TipoTrofeo.ENCICLOPEDISTA, enciclopedista.getTipo());
        assertEquals(RegistroBestiario.getNumeroVoci(), enciclopedista.getObiettivo());
        assertEquals(0, enciclopedista.getProgresso());
        assertFalse(enciclopedista.isMeritato());
    }
}
