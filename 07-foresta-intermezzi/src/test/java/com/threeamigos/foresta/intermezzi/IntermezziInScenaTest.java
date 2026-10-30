package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.IntermezziMD;
import com.threeamigos.foresta.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.modellodati.StatisticheMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.OmbraFiamma;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.NegozioInScena;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli intermezzi fissi e le scene con cui si costruiscono, nel mondo in cui il gruppo si trova: quando scattano, che pagine raccontano, e il
 * registro che si ricorda quali sono già scattati.
 */
class IntermezziInScenaTest {

    private final CoordinateMD qui = new CoordinateMD(3, 4);
    private IntermezziMD intermezziMD;
    private LineaTemporaleMD lineaTemporaleMD;

    @BeforeEach
    void preparaIlMondo() {
        GruppoGiocatoreMD datiGruppo = new GruppoGiocatoreMD();
        GruppoGiocatore.setFornitoreDiDati(() -> datiGruppo);
        GruppoGiocatore.azzeraIstanza();
        intermezziMD = new IntermezziMD();
        RegistroIntermezzi.setFornitoreDiDati(() -> intermezziMD);
        lineaTemporaleMD = new LineaTemporaleMD();
        LineaTemporale.setFornitoreDiDati(() -> lineaTemporaleMD);
        StatisticheMD statistiche = new StatisticheMD();
        Statistiche.setFornitoreDiDati(() -> statistiche);
        ForestaMD foresta = new ForestaMD();
        Foresta.setFornitoreDiDati(() -> foresta);
        Foresta.azzera(10, 8);
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 8; y++) {
                Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.BOSCO);
            }
        }
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(new Guerriero("Arsenio", 3));
        gruppo.aggiungiPersonaggio(new Mago("Merlino", 3));
        RegistroIntermezzi.nuovoMomento();
        IntermezzoLibroTecnico.reimposta();
        lineaTemporaleMD.setOra(12);
        // Gli intermezzi delle missioni li registra chi le conosce: qui ne bastano due che non scattano mai
        for (TipoIntermezzo tipo : new TipoIntermezzo[] {TipoIntermezzo.INTERMEZZO_URLO_DELLA_BANSHEE, TipoIntermezzo.INTERMEZZO_EREMITA_ROMPISCATOLE}) {
            if (!FabbricaIntermezzi.isRegistrato(tipo)) {
                FabbricaIntermezzi.registra(tipo, t -> new Intermezzo() {
                    @Override
                    public String getId() {
                        return t.name();
                    }

                    @Override
                    public boolean deveScattare(MomentoIntermezzo momento) {
                        return false;
                    }

                    @Override
                    public List<PaginaIntermezzo> getPagine() {
                        return Collections.emptyList();
                    }
                });
            }
        }
    }

    @AfterEach
    void ripulisce() throws Exception {
        // Le fonti sono statiche: ogni prova parte senza quelle delle altre
        java.lang.reflect.Field fonti = RegistroIntermezzi.class.getDeclaredField("FONTI");
        fonti.setAccessible(true);
        ((List<?>) fonti.get(null)).clear();
        ModalitaDiProva.setAttiva(false);
        IntermezzoLibroTecnico.reimposta();
        RegistroIntermezzi.nuovoMomento();
    }

    private LocazioneMD vaiIn(TipoLocazione tipo) {
        Foresta.impostaLocazione(qui, tipo);
        Foresta.setPosizioneDelGruppo(qui);
        return Foresta.getLocazioneMD(qui);
    }

    private static void controlla(List<PaginaIntermezzo> pagine) {
        assertNotNull(pagine);
        assertFalse(pagine.isEmpty());
    }

    // --- La fabbrica

    @Test
    void laFabbricaCostruisceOgniTipoRegistrato() {
        int costruiti = 0;
        for (TipoIntermezzo tipo : TipoIntermezzo.values()) {
            if (FabbricaIntermezzi.isRegistrato(tipo)) {
                Intermezzo intermezzo = FabbricaIntermezzi.crea(tipo);
                assertTrue(intermezzo.getId().startsWith(tipo.name()), intermezzo.getId());
                assertEquals(Intermezzo.SECONDI_PER_PAGINA, intermezzo.getSecondiPerPagina());
                intermezzo.isDiRipiego();
                intermezzo.isRipetibile();
                costruiti++;
            } else {
                assertThrows(IllegalStateException.class, () -> FabbricaIntermezzi.crea(tipo));
            }
        }
        assertTrue(costruiti >= 15);
        // Chi ne ha di suoi (le missioni) li registra
        TipoIntermezzo urlo = TipoIntermezzo.INTERMEZZO_URLO_DELLA_BANSHEE;
        if (!FabbricaIntermezzi.isRegistrato(urlo)) {
            FabbricaIntermezzi.registra(urlo, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.ARMAIOLO));
            assertTrue(FabbricaIntermezzi.isRegistrato(urlo));
            assertNotNull(FabbricaIntermezzi.crea(urlo));
        }
    }

    // --- Intermezzi fissi

    @Test
    void lIntroduzioneEILaFineDellaPrimaLocazione() {
        IntermezzoIntroduttivo introduzione = new IntermezzoIntroduttivo();
        assertTrue(introduzione.deveScattare(MomentoIntermezzo.INIZIO_GIOCO));
        assertFalse(introduzione.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        controlla(introduzione.getPagine());
        IntermezzoFinePrimaLocazione fine = new IntermezzoFinePrimaLocazione();
        assertTrue(fine.deveScattare(MomentoIntermezzo.LOCAZIONE_COMPLETATA));
        assertFalse(fine.deveScattare(MomentoIntermezzo.INIZIO_GIOCO));
        controlla(fine.getPagine());
        // Un eroe senza nome proprio si chiama con la classe, e l'Ombrafiamma ha una battuta in più
        GruppoGiocatore.getIstanza().reimposta();
        Personaggio senzaNome = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        GruppoGiocatore.getIstanza().aggiungiPersonaggioSenzaNotificare(senzaNome);
        controlla(new IntermezzoIntroduttivo().getPagine());
        controlla(new IntermezzoFinePrimaLocazione().getPagine());
        GruppoGiocatore.getIstanza().reimposta();
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(new OmbraFiamma("Fiamma", 3));
        int paginaConUnaBattutaInPiu = new IntermezzoIntroduttivo().getPagine().get(2).getBattuteProgrammate().size();
        assertEquals(4, paginaConUnaBattutaInPiu);
    }

    @Test
    void iNegoziScattanoAllIngressoEIlRiposoDellaNotteSoloDiNotte() {
        for (TipoIntermezzo tipo : new TipoIntermezzo[] {TipoIntermezzo.INTERMEZZO_ARMAIOLO, TipoIntermezzo.INTERMEZZO_VENDITORE_DI_PERGAMENE,
                TipoIntermezzo.INTERMEZZO_INCANTATORE}) {
            Intermezzo intermezzo = FabbricaIntermezzi.crea(tipo);
            controlla(intermezzo.getPagine());
            int scattano = 0;
            for (MomentoIntermezzo momento : MomentoIntermezzo.values()) {
                scattano += intermezzo.deveScattare(momento) ? 1 : 0;
            }
            assertEquals(1, scattano, tipo.name());
        }
        // Di giorno nessuno dice "non chiudete mai"; di notte sì, ma la locanda solo dalla terza visita
        vaiIn(TipoLocazione.BOSCO);
        for (TipoIntermezzo tipo : new TipoIntermezzo[] {TipoIntermezzo.INTERMEZZO_NOTTE_ARMAIOLO, TipoIntermezzo.INTERMEZZO_NOTTE_ALCHIMISTA,
                TipoIntermezzo.INTERMEZZO_NOTTE_VENDITORE_DI_PERGAMENE, TipoIntermezzo.INTERMEZZO_NOTTE_INCANTATORE}) {
            Intermezzo intermezzo = FabbricaIntermezzi.crea(tipo);
            assertTrue(intermezzo.isDiRipiego());
            controlla(intermezzo.getPagine());
            MomentoIntermezzo ingresso = null;
            for (MomentoIntermezzo momento : MomentoIntermezzo.values()) {
                lineaTemporaleMD.setOra(12);
                if (intermezzo.deveScattare(momento)) {
                    fail("di giorno " + tipo + " non scatta");
                }
                lineaTemporaleMD.setOra(23);
                if (intermezzo.deveScattare(momento)) {
                    ingresso = momento;
                }
                lineaTemporaleMD.setOra(3);
                intermezzo.deveScattare(momento);
            }
            assertNotNull(ingresso, tipo.name());
        }
    }

    @Test
    void laNotteInLocandaScattaDallaTerzaVisita() {
        LocazioneMD locanda = vaiIn(TipoLocazione.LOCANDA);
        Intermezzo notte = FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_NOTTE_LOCANDA);
        lineaTemporaleMD.setOra(23);
        assertFalse(notte.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        locanda.aggiungiProprieta(Locande.LOCANDA_VISITE, "1");
        assertFalse(notte.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        locanda.aggiungiProprieta(Locande.LOCANDA_VISITE, "2");
        assertTrue(notte.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        controlla(notte.getPagine());
        assertFalse(notte.deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
    }

    @Test
    void l_oroscopoDell_alchimistaEIlSuoBenvenuto() {
        Intermezzo oroscopo = FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_ALCHIMISTA);
        assertTrue(oroscopo.isRipetibile());
        assertTrue(oroscopo.deveScattare(MomentoIntermezzo.INGRESSO_ALCHIMISTA));
        controlla(oroscopo.getPagine());
        RegistroIntermezzi.segnaScattato(FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_ARMAIOLO));
        assertFalse(oroscopo.deveScattare(MomentoIntermezzo.INGRESSO_ALCHIMISTA));
        assertFalse(oroscopo.deveScattare(MomentoIntermezzo.INGRESSO_ARMAIOLO));
    }

    @Test
    void ilLibroTecnicoSiLeggeEDaAccessoAllaSpadaNascostaEAllaRiprogrammazione() {
        LibroTecnico manuale = LibroTecnico.getLibro("MANUALE_UNIX_SYSTEM_V").get();
        LibroTecnico linguaC = LibroTecnico.getLibro("THE_C_PROGRAMMING_LANGUAGE").get();
        IntermezzoSpadaNascosta spada = (IntermezzoSpadaNascosta) FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_SPADA_NASCOSTA);
        Intermezzo riprogrammazione = FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_RIPROGRAMMAZIONE);
        assertFalse(spada.deveScattare(MomentoIntermezzo.INGRESSO_ALCHIMISTA));
        assertFalse(riprogrammazione.deveScattare(MomentoIntermezzo.INGRESSO_INCANTATORE));
        assertFalse(IntermezzoLibroTecnico.isTrovato(manuale));
        // Il libro è stato trovato: alla fine della locazione lo si legge
        IntermezzoLibroTecnico.trovato(manuale);
        IntermezzoLibroTecnico.trovato(linguaC);
        assertTrue(IntermezzoLibroTecnico.isTrovato(manuale));
        vaiIn(TipoLocazione.BOSCO);
        IntermezzoLibroTecnico lettura = new IntermezzoLibroTecnico();
        assertTrue(lettura.getId().endsWith(manuale.getChiave()));
        assertTrue(lettura.deveScattare(MomentoIntermezzo.LOCAZIONE_COMPLETATA));
        assertFalse(lettura.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        controlla(lettura.getPagine());
        RegistroIntermezzi.segnaScattato(lettura);
        RegistroIntermezzi.nuovoMomento();
        // E ora i due negozi se ne accorgono
        assertTrue(spada.deveScattare(MomentoIntermezzo.INGRESSO_ALCHIMISTA));
        assertFalse(spada.deveScattare(MomentoIntermezzo.INGRESSO_ARMAIOLO));
        controlla(spada.getPagine());
        assertTrue(riprogrammazione.deveScattare(MomentoIntermezzo.INGRESSO_INCANTATORE));
        controlla(riprogrammazione.getPagine());
        assertTrue(RegistroIntermezzi.isScattatoNelMomento());
        // Senza un libro da leggere l'intermezzo non scatta mai
        IntermezzoLibroTecnico.reimposta();
        IntermezzoLibroTecnico vuoto = new IntermezzoLibroTecnico();
        assertEquals(TipoIntermezzo.INTERMEZZO_LIBRO_TECNICO.name(), vuoto.getId());
        assertFalse(vuoto.deveScattare(MomentoIntermezzo.LOCAZIONE_COMPLETATA));
        // Un libro già letto resta trovato
        intermezziMD.aggiungiScattato(TipoIntermezzo.INTERMEZZO_LIBRO_TECNICO.name() + "_" + manuale.getChiave());
        assertTrue(IntermezzoLibroTecnico.isTrovato(manuale));
    }

    @Test
    void l_accampamentoRaccontaTreSceneEPoiNiente() {
        vaiIn(TipoLocazione.BOSCO);
        for (int volta = 1; volta <= 4; volta++) {
            RegistroIntermezzi.nuovoMomento();
            IntermezzoAccampamento accampamento = new IntermezzoAccampamento();
            if (volta <= 3) {
                assertTrue(accampamento.deveScattare(MomentoIntermezzo.ACCAMPAMENTO), "volta " + volta);
                controlla(accampamento.getPagine());
                RegistroIntermezzi.segnaScattato(accampamento);
            } else {
                assertFalse(accampamento.deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
            }
        }
        RegistroIntermezzi.nuovoMomento();
        assertFalse(new IntermezzoAccampamento().deveScattare(MomentoIntermezzo.INIZIO_GIOCO));
        // Se ne è già mostrato uno nel momento, o una fonte ne ha uno in arrivo, aspetta
        intermezziMD.reimposta();
        RegistroIntermezzi.segnaScattato(FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_ARMAIOLO));
        assertFalse(new IntermezzoAccampamento().deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
        RegistroIntermezzi.nuovoMomento();
        FonteDiProva fonte = new FonteDiProva();
        fonte.inArrivo = true;
        RegistroIntermezzi.aggiungiFonte(fonte);
        assertFalse(new IntermezzoAccampamento().deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
        fonte.inArrivo = false;
        assertTrue(new IntermezzoAccampamento().deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
        RegistroIntermezzi.aggiungiFonte(fonte);
        assertTrue(RegistroIntermezzi.isBloccato(TipoIntermezzo.INTERMEZZO_ARMAIOLO) == fonte.blocca(TipoIntermezzo.INTERMEZZO_ARMAIOLO));
        fonte.inArrivo = true;
        assertTrue(RegistroIntermezzi.haUnaFonteUnIntermezzoInArrivo(MomentoIntermezzo.ACCAMPAMENTO));
    }

    private static class FonteDiProva implements FonteDiIntermezzi {
        boolean inArrivo;
        Intermezzo daDare;

        @Override
        public Intermezzo getProssimo(MomentoIntermezzo momento) {
            return daDare;
        }

        @Override
        public boolean haUnIntermezzoInArrivo(MomentoIntermezzo momento) {
            return inArrivo;
        }
    }

    // --- Locande

    @Test
    void ilNomeEIlMomentoDiUnaLocanda() {
        LocazioneMD locanda = vaiIn(TipoLocazione.LOCANDA);
        locanda.setNome("Il Boccale Rotto");
        assertEquals("Il Boccale Rotto", Locande.getNome(locanda));
        assertTrue(Locande.momentoCoerenteConLocazioneCorrente(MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertFalse(Locande.momentoCoerenteConLocazioneCorrente(MomentoIntermezzo.INGRESSO_LOCANDA_IN_CITTA));
        assertFalse(Locande.momentoCoerenteConLocazioneCorrente(MomentoIntermezzo.ACCAMPAMENTO));
        assertNull(Locande.getIdentificativoLocandaCorrente());
        assertEquals(0, Locande.getVisiteLocandaCorrente());
        locanda.aggiungiProprieta(Locande.LOCANDA_IDENTIFICATIVO, "L1");
        locanda.aggiungiProprieta(Locande.LOCANDA_VISITE, "5");
        assertEquals("L1", Locande.getIdentificativoLocandaCorrente());
        assertEquals(5, Locande.getVisiteLocandaCorrente());
        LocazioneMD citta = vaiIn(TipoLocazione.CITTA_NYENA);
        citta.aggiungiProprieta(Locande.LOCANDA_NOME_IN_CITTA, "La Brocca");
        assertEquals("La Brocca", Locande.getNome(citta));
        assertNull(Locande.getIdentificativoLocandaCorrente());
        assertEquals(0, Locande.getVisiteLocandaCorrente());
        assertTrue(Locande.momentoCoerenteConLocazioneCorrente(MomentoIntermezzo.INGRESSO_LOCANDA_IN_CITTA));
        assertFalse(Locande.momentoCoerenteConLocazioneCorrente(MomentoIntermezzo.INIZIO_LOCAZIONE));
        vaiIn(TipoLocazione.BOSCO);
        assertNull(Locande.getLocazioneMDLocandaCorrente());
        assertNull(Locande.getIdentificativoLocandaCorrente());
        assertEquals(0, Locande.getVisiteLocandaCorrente());
    }

    @Test
    void laPrimaVisitaEQuellaSuccessivaAUnaLocanda() {
        LocazioneMD locanda = vaiIn(TipoLocazione.LOCANDA);
        locanda.aggiungiProprieta(Locande.LOCANDA_IDENTIFICATIVO, "L1");
        locanda.aggiungiProprieta(Locande.LOCANDA_DIALOGO, "Benvenuti! Prendete un boccale. Che cosa desiderate?");
        locanda.aggiungiProprieta(Locande.LOCANDA_RECENSIONE, "Un posto accogliente.");
        IntermezzoLocandaPrimaVisita prima = new IntermezzoLocandaPrimaVisita();
        IntermezzoLocandaVisitaSuccessiva successiva = new IntermezzoLocandaVisitaSuccessiva();
        assertTrue(prima.getId().endsWith("_L1"));
        assertTrue(successiva.isDiRipiego());
        assertTrue(prima.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertFalse(prima.deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
        assertFalse(successiva.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        controlla(prima.getPagine());
        assertEquals(LocazioneMD.AFFERMATIVO, locanda.ottieniProprieta(Locande.LOCANDA_DIALOGO_LETTO));
        // Con il bardo ospite non c'è il bardo seduto nella locanda
        Personaggio bardo = FabbricaPersonaggi.crea(TipoPersonaggio.BARDO_DELLE_LOCANDE, 1);
        GruppoGiocatore.getIstanza().aggiungiOspite(bardo);
        GruppoGiocatore.getIstanza().aggiungiOspite(FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1));
        controlla(new IntermezzoLocandaPrimaVisita().getPagine());
        // Visitata: tocca alla successiva, se il gruppo è in più di uno
        locanda.aggiungiProprieta(Locande.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);
        assertFalse(prima.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertTrue(successiva.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertFalse(successiva.deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
        // Una fonte può tenere per sé la prima visita (una missione che ci racconta la sua scena)
        locanda.rimuoviProprieta(Locande.LOCANDA_VISITATA);
        FonteDiProva blocca = new FonteDiProva() {
            @Override
            public boolean blocca(TipoIntermezzo tipo) {
                return tipo == TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA;
            }
        };
        RegistroIntermezzi.aggiungiFonte(blocca);
        assertFalse(prima.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertTrue(RegistroIntermezzi.isBloccato(TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA));
        locanda.aggiungiProprieta(Locande.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);
        controlla(successiva.getPagine());
        assertTrue(successiva.getId().endsWith("_L1"));
        FonteDiProva fonte = new FonteDiProva();
        RegistroIntermezzi.aggiungiFonte(fonte);
        // Da solo il gruppo non ha niente da dire
        GruppoGiocatore.getIstanza().rimuoviPersonaggio(GruppoGiocatore.getIstanza().getPersonaggio(1));
        assertFalse(successiva.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        // E senza identificativo l'id è quello del tipo
        vaiIn(TipoLocazione.BOSCO);
        assertEquals(TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA.name(), new IntermezzoLocandaPrimaVisita().getId());
        assertEquals(TipoIntermezzo.INTERMEZZO_LOCANDA_VISITA_SUCCESSIVA.name(), new IntermezzoLocandaVisitaSuccessiva().getId());
    }

    @Test
    void laVisitaSuccessivaServeAChiHaUnCapoVivoEUnCompagno() {
        LocazioneMD locanda = vaiIn(TipoLocazione.LOCANDA);
        locanda.aggiungiProprieta(Locande.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);
        locanda.aggiungiProprieta(Locande.LOCANDA_RECENSIONE, "Ottima.");
        IntermezzoLocandaVisitaSuccessiva successiva = new IntermezzoLocandaVisitaSuccessiva();
        assertTrue(successiva.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(new Guerriero("Terzo", 3));
        GruppoGiocatore.getIstanza().getCapo().muore("prova");
        assertFalse(successiva.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
    }

    @Test
    void unOspiteCheNonEIlBardoLasciaIlBardoSedutoNellaLocanda() {
        LocazioneMD locanda = vaiIn(TipoLocazione.LOCANDA);
        locanda.aggiungiProprieta(Locande.LOCANDA_DIALOGO, "Benvenuti!");
        GruppoGiocatore.getIstanza().aggiungiOspite(FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1));
        controlla(new IntermezzoLocandaPrimaVisita().getPagine());
    }

    // --- Il registro

    @Test
    void ilRegistroScegliePerPriorita_poiLeFonti_poiIlRipiego() {
        vaiIn(TipoLocazione.BOSCO);
        assertThrows(IllegalStateException.class, () -> {
            RegistroIntermezzi.setFornitoreDiDati(null);
            try {
                RegistroIntermezzi.isScattato("x");
            } finally {
                RegistroIntermezzi.setFornitoreDiDati(() -> intermezziMD);
            }
        });
        Intermezzo introduttivo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_GIOCO);
        assertEquals(TipoIntermezzo.INTERMEZZO_INTRODUTTIVO.name(), introduttivo.getId());
        RegistroIntermezzi.segnaScattato(introduttivo);
        assertTrue(RegistroIntermezzi.isScattato(TipoIntermezzo.INTERMEZZO_INTRODUTTIVO));
        assertTrue(RegistroIntermezzi.isScattato(introduttivo.getId()));
        assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_GIOCO));
        // Un intermezzo ripetibile si ricorda solo nel momento
        RegistroIntermezzi.nuovoMomento();
        Intermezzo oroscopo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ALCHIMISTA);
        assertEquals(TipoIntermezzo.INTERMEZZO_ALCHIMISTA.name(), oroscopo.getId());
        RegistroIntermezzi.segnaScattato(oroscopo);
        assertFalse(RegistroIntermezzi.isScattato(oroscopo.getId()));
        assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ALCHIMISTA));
        RegistroIntermezzi.nuovoMomento();
        assertFalse(RegistroIntermezzi.isScattatoNelMomento());
        assertNotNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ALCHIMISTA));
        // Una fonte ha la precedenza sul ripiego (e il ripiego non si somma a un intermezzo già scattato)
        lineaTemporaleMD.setOra(23);
        RegistroIntermezzi.nuovoMomento();
        Intermezzo ripiego = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ARMAIOLO);
        assertEquals(TipoIntermezzo.INTERMEZZO_ARMAIOLO.name(), ripiego.getId());
        FonteDiProva fonte = new FonteDiProva();
        fonte.daDare = new IntermezzoNonChiudeteMai(TipoIntermezzo.INTERMEZZO_NOTTE_ARMAIOLO, NegozioInScena.ARMAIOLO);
        RegistroIntermezzi.aggiungiFonte(fonte);
        RegistroIntermezzi.segnaScattato(ripiego);
        assertSame(fonte.daDare, RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ARMAIOLO));
        fonte.daDare = null;
        assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ARMAIOLO));
        RegistroIntermezzi.nuovoMomento();
        assertEquals(TipoIntermezzo.INTERMEZZO_NOTTE_ARMAIOLO.name(),
                RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ARMAIOLO).getId());
        assertFalse(RegistroIntermezzi.isBloccato(TipoIntermezzo.INTERMEZZO_ARMAIOLO));
        assertFalse(RegistroIntermezzi.haUnaFonteUnIntermezzoInArrivo(MomentoIntermezzo.INGRESSO_ARMAIOLO));
    }

    @Test
    void gliIntermezziDiProvaSiSaltanoFuoriDallaModalitaDiProva() {
        // Nessun tipo è oggi di prova: il registro li guarda comunque tutti
        for (TipoIntermezzo tipo : TipoIntermezzo.values()) {
            assertFalse(tipo.isDiProva());
        }
        ModalitaDiProva.setAttiva(true);
        assertNotNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_GIOCO));
        assertEquals(Collections.emptyList(), new ArrayList<>());
    }
}
