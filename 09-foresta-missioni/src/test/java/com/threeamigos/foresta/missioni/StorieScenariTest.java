package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.PassiSinteticiTest.Sintetica;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.incarichi.IlTorneo;
import com.threeamigos.foresta.missioni.incarichi.MissioneATappe;
import com.threeamigos.foresta.missioni.leggende.ConLeggendario;
import com.threeamigos.foresta.missioni.leggende.LaLeggenda;
import com.threeamigos.foresta.missioni.leggende.PescaLeggendaria;
import com.threeamigos.foresta.missioni.storie.IlCampione;
import com.threeamigos.foresta.missioni.storie.IlViaggioDellaMoglie;
import com.threeamigos.foresta.missioni.storie.LaBenedizione;
import com.threeamigos.foresta.missioni.storie.LaLealta;
import com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le storie del gioco portate a mano negli stati che il giocatore automatico non incontra: città distrutte a viaggio cominciato,
 * leggendari tutti già usciti, nomi di leggendari al plurale, gruppi di un solo personaggio, locande con poche visite.
 */
class StorieScenariTest {

    private static final MomentoControllo PRE = MomentoControllo.PRE_LOCAZIONE;

    private CoordinateMD locanda;
    private CoordinateMD tempio;

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(71);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        FabbricaMissioni.registra(MissionePredefinita.MISSIONE_DI_PROVA, Sintetica::new);
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                TipoLocazione tipo = Foresta.getLocazione(x, y);
                if (tipo == TipoLocazione.LOCANDA && locanda == null) {
                    locanda = new CoordinateMD(x, y);
                }
                if (tipo == TipoLocazione.TEMPIO && tempio == null) {
                    tempio = new CoordinateMD(x, y);
                }
            }
        }
        assertNotNull(locanda);
        assertNotNull(tempio);
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private <M extends Missione> M registra(M m, String id) {
        m.getModelloDati().setId(id);
        RegistroMissioni.aggiungiMissioneSecondaria(m);
        return m;
    }

    /** Un leggendario con un nome al plurale, per le frasi che cambiano verbo. */
    private static String rigaPlurale() {
        String riga = LaSfidaDeiCampioni.costruisciUnLeggendario(3).getRiga();
        return riga.replaceAll("BREVE=[^;]*", "BREVE=i Guanti dei Campioni");
    }

    private static final class ConUnLeggendario extends Sintetica implements ConLeggendario {
        private final OggettoLeggendario leggendario;

        ConUnLeggendario(OggettoLeggendario leggendario) {
            this.leggendario = leggendario;
            passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        }

        @Override
        public OggettoLeggendario getLeggendario() {
            return leggendario;
        }
    }

    /** Ogni leggendario del catalogo è già uscito in una missione della partita. */
    private void tuttiILeggendariGiaUsciti() {
        int i = 0;
        for (OggettoLeggendario leggendario : CatalogoLeggendari.getTuttiILeggendari().values()) {
            registra(new ConUnLeggendario(leggendario), "USCITO_" + i++);
        }
        assertFalse(PescaLeggendaria.restaUnLeggendario(PescaLeggendaria.giaPescati(null)));
    }

    // ---- Leggendari

    @Test
    void quandoIlCatalogoFinisceLaSfidaDeiCampioniNeCostruisceUnoNuovo() {
        tuttiILeggendariGiaUsciti();
        LaSfidaDeiCampioni sfida = new LaSfidaDeiCampioni();
        sfida.getModelloDati().setId("SFIDA");
        sfida.avviaDaSola();
        assertNotNull(sfida.getLeggendario());
        assertTrue(sfida.getLeggendario().getChiave().startsWith("CAMPIONI_"));
    }

    @Test
    void quandoIlCatalogoFinisceNonSiBandisconoPiuTorneiNeLeggende() throws Exception {
        tuttiILeggendariGiaUsciti();
        IlTorneo torneo = registra(new IlTorneo(), "TORNEO");
        assertFalse(torneo.getDescrizione().isEmpty());
        torneo.controllaPreLocazione();
        assertFalse(torneo.isAttiva());
        LaLeggenda leggenda = registra(new com.threeamigos.foresta.missioni.leggende.LaLeggendaDelLocandiere(), "LEGGENDA");
        leggenda.controllaPreLocazione();
        assertFalse(leggenda.isAttiva());
        MissioneATappe tappe = registra(new MissioneATappe(), "TAPPE");
        // La riga si pesca per tentativi: quelle di un leggendario si scartano, perché non ne resta nessuno da mettere in palio
        java.lang.reflect.Method pesca = MissioneATappe.class.getDeclaredMethod("pescaLaRiga");
        pesca.setAccessible(true);
        for (int i = 0; i < 60; i++) {
            String riga = (String) pesca.invoke(tappe);
            assertFalse(com.threeamigos.foresta.missioni.richieste.TappeRichieste.da(riga).isDiUnLeggendario(), riga);
        }
    }

    @Test
    void ilNomeAlPluraleCambiaLeFrasi() {
        IlTorneo torneo = registra(new IlTorneo(), "TORNEO");
        torneo.aggiungiProprieta("LEGGENDARIO", rigaPlurale());
        torneo.attivaMissione();
        assertTrue(torneo.getDescrizione().contains("ci sono i Guanti dei Campioni"), torneo.getDescrizione());
        LaLeggenda leggenda = registra(new com.threeamigos.foresta.missioni.leggende.LaLeggendaDelLocandiere(), "LEGGENDA");
        leggenda.aggiungiProprieta("LEGGENDARIO", rigaPlurale());
        assertFalse(((MissioneAPassi) leggenda).costruisciPasso(((MissioneAPassi) leggenda).passoIniziale()).getPagine().isEmpty());
        leggenda.aggiungiProprieta("LEGGENDARIO", LaSfidaDeiCampioni.costruisciUnLeggendario(2).getRiga());
        assertFalse(((MissioneAPassi) leggenda).costruisciPasso(((MissioneAPassi) leggenda).passoIniziale()).getPagine().isEmpty());
    }

    @Test
    void iGuardianiDelleLeggendeCrescono() {
        assertEquals(com.threeamigos.foresta.tipi.TipoPersonaggio.HOBGOBLIN, LaLeggenda.guardianiDi("QUALUNQUE", 1).getClasse());
        assertEquals(com.threeamigos.foresta.tipi.TipoPersonaggio.TROLL, LaLeggenda.guardianiDi("QUALUNQUE", 3).getClasse());
        assertEquals(com.threeamigos.foresta.tipi.TipoPersonaggio.VIVERNA, LaLeggenda.guardianiDi("QUALUNQUE", 5).getClasse());
        assertEquals(com.threeamigos.foresta.tipi.TipoPersonaggio.CHIMERA_DRAGO, LaLeggenda.guardianiDi("QUALUNQUE", 8).getClasse());
        assertEquals(com.threeamigos.foresta.tipi.TipoPersonaggio.VIVERNA, LaLeggenda.guardianiDi("SPADA_DI_ROMY_JONA", 1).getClasse());
    }

    // ---- Il bardo

    @Test
    void ilViaggioDelBardoFalliscePerLaCittaCaduta() {
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        bardo.attivaMissione();
        bardo.aggiungiProprieta("CITTA", TipoLocazione.CITTA_RUUNA.name());
        bardo.aggiungiProprieta("PASSO_CORRENTE", "VIAGGIO");
        Foresta.setCittaDistrutta(TipoLocazione.CITTA_RUUNA);
        bardo.controllaPreLocazione();
        assertTrue(bardo.isFallita());
    }

    @Test
    void ilLocandiereAffidaIlBardoSoloSeCeUnaCittaEUnaVisitaTranquilla() {
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "9");
        Foresta.setPosizioneDelGruppo(locanda);
        // Un'altra missione sta per mostrare un intermezzo a inizio locazione: la visita non è tranquilla
        Sintetica altra = registra(new Sintetica(), "ALTRA");
        altra.passo("A", s -> Passo.quando(PRE, () -> true).conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, Collections::emptyList)
                .poi("B")).passo("B", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        bardo.controllaPreLocazione();
        assertEquals("INCARICO", bardo.getPassoCorrente());
    }

    @Test
    void senzaPiuCittaIlBardoNonHaDoveEssereRiportato() {
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "9");
        Foresta.setPosizioneDelGruppo(locanda);
        for (TipoLocazione citta : new TipoLocazione[] {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA,
                TipoLocazione.CITTA_FLEENA}) {
            Foresta.setCittaDistrutta(citta);
        }
        bardo.controllaPreLocazione();
        assertEquals("INCARICO", bardo.getPassoCorrente(), "non c'è più una città dove riportarlo");
    }

    @Test
    void laScenaDelLocandiereCedeIlPostoSoloQuandoStaPerAffidareIlBardo() {
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        assertFalse(bardo.bloccaIntermezzo(com.threeamigos.foresta.tipi.TipoIntermezzo.INTERMEZZO_INTRODUTTIVO));
        assertFalse(bardo.bloccaIntermezzo(com.threeamigos.foresta.tipi.TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA));
        bardo.aggiungiProprieta("PASSO_CORRENTE", "ACCETTAZIONE");
        Foresta.setPosizioneDelGruppo(locanda);
        assertFalse(bardo.staPerAffidareIlBardo(), "la locanda non è ancora quella del locandiere");
        bardo.aggiungiProprieta("LOCANDA", locanda.getX() + "," + locanda.getY());
        assertTrue(bardo.staPerAffidareIlBardo());
        assertTrue(bardo.bloccaIntermezzo(com.threeamigos.foresta.tipi.TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA));
        bardo.aggiungiProprieta("PASSO_CORRENTE", "VIAGGIO");
        assertFalse(bardo.staPerAffidareIlBardo());
    }

    @Test
    void ilViaggioDellaMoglieFallisceSeCadonoLeCittaDelViaggio() throws Exception {
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        bardo.aggiungiProprieta("CITTA", TipoLocazione.CITTA_NYENA.name());
        bardo.completaMissione();
        IlViaggioDellaMoglie moglie = registra(new IlViaggioDellaMoglie(), "MOGLIE");
        moglie.aggiungiProprieta("CITTA_DI_PARTENZA", TipoLocazione.CITTA_NYENA.name());
        moglie.aggiungiProprieta("CITTA_DI_ARRIVO", TipoLocazione.CITTA_MALGAARD.name());
        moglie.attivaMissione();
        moglie.aggiungiProprieta("PASSO_CORRENTE", "PRELEVAMENTO");
        Foresta.setCittaDistrutta(TipoLocazione.CITTA_NYENA);
        moglie.controllaPreLocazione();
        assertTrue(moglie.isFallita());
        IlViaggioDellaMoglie viaggio = registra(new IlViaggioDellaMoglie(), "MOGLIE_2");
        viaggio.aggiungiProprieta("CITTA_DI_PARTENZA", TipoLocazione.CITTA_RUUNA.name());
        viaggio.aggiungiProprieta("CITTA_DI_ARRIVO", TipoLocazione.CITTA_MALGAARD.name());
        viaggio.attivaMissione();
        viaggio.aggiungiProprieta("PASSO_CORRENTE", "VIAGGIO");
        Foresta.setCittaDistrutta(TipoLocazione.CITTA_MALGAARD);
        viaggio.controllaPreLocazione();
        assertTrue(viaggio.isFallita());
        java.lang.reflect.Method nome = IlViaggioDellaMoglie.class.getDeclaredMethod("getNomeCitta", TipoLocazione.class);
        nome.setAccessible(true);
        assertEquals("La città", nome.invoke(null, (Object) null));
        assertFalse(((String) nome.invoke(null, TipoLocazione.CITTA_RUUNA)).isEmpty());
    }

    @Test
    void ilRitornoDelBardoInUnaLocandaSenzaVisiteNonScatta() {
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        bardo.completaMissione();
        IlViaggioDellaMoglie moglie = registra(new IlViaggioDellaMoglie(), "MOGLIE");
        Foresta.setPosizioneDelGruppo(locanda);
        moglie.controllaPreLocazione();
        assertEquals("INCARICO", moglie.getPassoCorrente(), "il bardo non ha ancora una città: nessun viaggio");
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "1");
        moglie.controllaPreLocazione();
    }

    // ---- La benedizione

    @Test
    void laBenedizioneVaAlCompagnoUnicoEDaIlPremio() {
        LaBenedizione benedizione = registra(new LaBenedizione(), "BENEDIZIONE");
        benedizione.attivaMissione();
        for (String passo : new String[] {"TEMPIO", "ARRIVO", "SCELTA", "DIRETTA", "INCONTRO"}) {
            benedizione.aggiungiProprieta("PASSO_CORRENTE", passo);
            assertFalse(benedizione.getDescrizione().isEmpty());
        }
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        int vivi = 0;
        for (Personaggio p : gruppo.getPersonaggiVivi()) {
            if (vivi++ > 0) {
                p.subSalute(100000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            }
        }
        assertEquals(1, gruppo.getPersonaggiVivi().size());
        RegistroMissioni.occupaLocazione(tempio, benedizione);
        Foresta.setPosizioneDelGruppo(tempio);
        benedizione.aggiungiProprieta("PASSO_CORRENTE", "ARRIVO");
        benedizione.controllaPreLocazione();
        assertNotNull(benedizione.getBenedetto());
        assertNotNull(benedizione.ottieniProprieta("PREMIO_DATO"));
    }

    @Test
    void laBenedizioneNonSiOffreInUnaLocandaConPocheVisiteONonTranquilla() {
        LaBenedizione benedizione = registra(new LaBenedizione(), "BENEDIZIONE");
        Foresta.setPosizioneDelGruppo(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "9");
        Sintetica altra = registra(new Sintetica(), "ALTRA");
        altra.passo("A", s -> Passo.quando(PRE, () -> true).conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, Collections::emptyList)
                .poi("B")).passo("B", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        benedizione.controllaPreLocazione();
        assertEquals("INCONTRO", benedizione.getPassoCorrente(), "un'altra missione sta per mostrare un intermezzo");
        benedizione.aggiungiProprieta("PASSO_CORRENTE", "FAVORE");
        benedizione.controllaInLocazione();
    }

    // ---- La lealtà e il campione

    @Test
    void ilCompagnoDellaLealtaDevEssereVivoELaSuaStoriaSiRicorda() {
        LaLealta lealta = registra(new LaLealta(), "LEALTA");
        lealta.aggiungiProprieta("COMPAGNO", "nessuno");
        assertFalse(lealta.getCompagno().isPresent());
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio morto = gruppo.getPersonaggiVivi().get(1);
        lealta.aggiungiProprieta("COMPAGNO", morto.getModelloDati().getUuid());
        assertTrue(lealta.getCompagno().isPresent());
        morto.subSalute(100000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(lealta.getCompagno().isPresent());
        lealta.controllaPreLocazione();
        lealta.controllaInLocazione();
    }

    @Test
    void ilCampioneFallitoNonLasciaRicordi() {
        IlCampione campione = new IlCampione();
        campione.getModelloDati().setId("CAMPIONE");
        assertNotNull(campione.getRicordoDellaLocazione());
        campione.fallisciMissione();
        assertNull(campione.getRicordoDellaLocazione());
        assertNotNull(ProduttoreDiMissioniCasuale.nomeCapobanda());
    }
}
