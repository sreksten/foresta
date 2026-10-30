package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.PassiSinteticiTest.Sintetica;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.incarichi.IlNegozioDellAlchimista;
import com.threeamigos.foresta.missioni.incarichi.IlTorneo;
import com.threeamigos.foresta.missioni.incarichi.IncaricoInCitta;
import com.threeamigos.foresta.missioni.incarichi.SceltaDegliIncarichi;
import com.threeamigos.foresta.missioni.principali.CronacheDiUnFegatoEroico;
import com.threeamigos.foresta.missioni.storie.IlViaggioDellaMoglie;
import com.threeamigos.foresta.missioni.storie.IlViandante;
import com.threeamigos.foresta.missioni.storie.LaLealta;
import com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Passi e condizioni di inizio delle storie in situazioni precise: una visita non tranquilla perché un'altra missione sta per mostrare un
 * intermezzo, una locanda con meno visite del dovuto, un bardo già affidato, un sorteggio che cambia idea.
 */
class ScenariDiPassoTest {

    private static final MomentoControllo PRE = MomentoControllo.PRE_LOCAZIONE;

    private CoordinateMD locanda;

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(121);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        FabbricaMissioni.registra(com.threeamigos.foresta.tipi.MissionePredefinita.MISSIONE_DI_PROVA, Sintetica::new);
        for (int x = 0; x < MondoDiProva.LATO && locanda == null; x++) {
            for (int y = 0; y < MondoDiProva.LATO && locanda == null; y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.LOCANDA) {
                    locanda = new CoordinateMD(x, y);
                }
            }
        }
    }

    @AfterEach
    void ripulisce() {
        SceltaDegliIncarichi.setSorteggioAttivo(true);
        SceltaDegliIncarichi.dimentica();
        MondoDiProva.finito();
    }

    private <M extends Missione> M registra(M m, String id) {
        m.getModelloDati().setId(id);
        RegistroMissioni.aggiungiMissioneSecondaria(m);
        return m;
    }

    /** Un'altra missione che sta per mostrare un intermezzo a inizio locazione: la visita non è tranquilla. */
    private Sintetica chiMostraUnIntermezzo() {
        Sintetica altra = registra(new Sintetica(), "CHI_MOSTRA");
        altra.passo("A", s -> Passo.quando(PRE, () -> true).conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, Collections::emptyList).poi("B"))
                .passo("B", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
        return altra;
    }

    private static boolean concluso(MissioneAPassi m) {
        return m.costruisciPasso(m.getPassoCorrente()).isConcluso();
    }

    private static Object invoca(Object oggetto, String nome, Object... argomenti) throws Exception {
        for (Class<?> c = oggetto == null ? null : oggetto.getClass(); c != null; c = c.getSuperclass()) {
            for (Method metodo : c.getDeclaredMethods()) {
                if (metodo.getName().equals(nome) && metodo.getParameterCount() == argomenti.length) {
                    metodo.setAccessible(true);
                    try {
                        return metodo.invoke(oggetto, argomenti);
                    } catch (InvocationTargetException e) {
                        throw (Exception) e.getCause();
                    }
                }
            }
        }
        throw new NoSuchMethodException(nome);
    }

    // ---- Le visite non tranquille

    @Test
    void ilViandanteSiIncontraSoloNelBoscoEInUnaVisitaTranquilla() throws Exception {
        IlViandante viandante = registra(new IlViandante(), "VIANDANTE");
        assertFalse((Boolean) invoca(viandante, "isIncontroPossibile"), "il gruppo è in una città");
        CoordinateMD bosco = new CoordinateMD(0, 0);
        assertEquals(TipoLocazione.BOSCO, Foresta.getLocazione(bosco));
        Foresta.setPosizioneDelGruppo(bosco);
        assertTrue((Boolean) invoca(viandante, "isIncontroPossibile"));
        chiMostraUnIntermezzo();
        assertFalse((Boolean) invoca(viandante, "isIncontroPossibile"), "un'altra missione sta per mostrare un intermezzo");
        viandante.aggiungiProprieta("PARAMETRO_RIGA", ProduttoreDiMissioniCasuale.rigaDiMissioni("VIANDANTE"));
        assertTrue(viandante.getDescrizione().startsWith("Prima o poi"), "il viandante non si è ancora fatto vivo");
    }

    @Test
    void ilBardoNonViaggiaSeUnAltroStaPerMostrareUnIntermezzo() {
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "9");
        Foresta.setPosizioneDelGruppo(locanda);
        chiMostraUnIntermezzo();
        assertFalse(concluso(bardo));
    }

    @Test
    void ilBardoGiaAffidatoBloccaLaScenaDiBenvenuto() {
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "9");
        Foresta.setPosizioneDelGruppo(locanda);
        bardo.controllaPreLocazione();
        assertNotNull(bardo.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
        assertTrue(bardo.staPerAffidareIlBardo());
    }

    @Test
    void ilViaggioDellaMoglieAspettaLeVisiteDellaLocandaEUnaCittaDiArrivo() throws Exception {
        IlViaggioDellaMoglie moglie = registra(new IlViaggioDellaMoglie(), "MOGLIE");
        NonSparateSulPianista bardo = registra(new NonSparateSulPianista(), "BARDO");
        bardo.aggiungiProprieta("CITTA", TipoLocazione.CITTA_NYENA.name());
        bardo.completaMissione();
        Foresta.setPosizioneDelGruppo(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "0");
        assertFalse(concluso(moglie), "la locanda non ha ancora visite");
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "5");
        chiMostraUnIntermezzo();
        assertFalse(concluso(moglie), "un altro sta per mostrare un intermezzo");
        Method distrutta = IlViaggioDellaMoglie.class.getDeclaredMethod("isCittaDistrutta", TipoLocazione.class);
        distrutta.setAccessible(true);
        assertFalse((Boolean) distrutta.invoke(null, (Object) null));
        assertFalse((Boolean) distrutta.invoke(null, TipoLocazione.CITTA_NYENA));
    }

    @Test
    void ilCompagnoLeale_nonEIlCapoNeUnOspiteATempo() throws Exception {
        LaLealta lealta = registra(new LaLealta(), "LEALTA");
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio ospite = gruppo.getPersonaggiVivi().get(2);
        ospite.getModelloDati().setMotivoNelGruppo(com.threeamigos.foresta.tipi.TipoMotivoNelGruppo.MIETITURA);
        Method candidati = LaLealta.class.getDeclaredMethod("candidati");
        candidati.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Personaggio> chi = (List<Personaggio>) candidati.invoke(null);
        assertFalse(chi.contains(ospite));
        assertFalse(chi.contains(gruppo.getCapo()));
        assertNotNull(lealta);
    }

    // ---- I tornei, le sfide, i sorteggi

    @Test
    void ilTorneoDiceCiSonoPerIPlurali() {
        String riga = LaSfidaDeiCampioni.costruisciUnLeggendario(3).getRiga().replaceAll("BREVE=[^;]*", "BREVE=le Scarpe del Campione");
        IlTorneo torneo = registra(new IlTorneo(), "TORNEO");
        torneo.aggiungiProprieta("LEGGENDARIO", riga);
        torneo.attivaMissione();
        assertTrue(torneo.getDescrizione().contains("ci sono le Scarpe del Campione"), torneo.getDescrizione());
        IlTorneo singolare = registra(new IlTorneo(), "TORNEO_2");
        singolare.aggiungiProprieta("LEGGENDARIO", LaSfidaDeiCampioni.costruisciUnLeggendario(3).getRiga().replaceAll("BREVE=[^;]*", "BREVE=lo Scudo"));
        singolare.attivaMissione();
        assertTrue(singolare.getDescrizione().contains("c'è lo Scudo"), singolare.getDescrizione());
    }

    @Test
    void unIncaricoNonPiuInAttesaNonSiOffre() throws Exception {
        IlNegozioDellAlchimista negozio = registra(new IlNegozioDellAlchimista(), "NEGOZIO");
        assertTrue((Boolean) invoca(negozio, "isInAttesaDiOffrirsi"));
        negozio.aggiungiProprieta("PASSO_CORRENTE", "ACCETTAZIONE");
        assertFalse((Boolean) invoca(negozio, "isInAttesaDiOffrirsi"));
        negozio.aggiungiProprieta("PASSO_CORRENTE", "INCARICO");
        negozio.attivaMissione();
        assertFalse((Boolean) invoca(negozio, "isInAttesaDiOffrirsi"));
    }

    @Test
    void ilSorteggioSiRifaSeLoScelto_nonEPiuPronto() throws Exception {
        IlNegozioDellAlchimista negozio = registra(new IlNegozioDellAlchimista(), "NEGOZIO");
        negozio.aggiungiProprieta("CITTA_ASSEGNATA", TipoLocazione.CITTA_NYENA.name());
        negozio.aggiungiProprieta("CITTA", TipoLocazione.CITTA_NYENA.name());
        negozio.aggiungiProprieta("LOCAZIONE_OCCUPATA", "5,5,1");
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        SceltaDegliIncarichi.dimentica();
        Method tocca = SceltaDegliIncarichi.class.getDeclaredMethod("tocca", IncaricoInCitta.class);
        tocca.setAccessible(true);
        tocca.invoke(null, negozio);
        Field scelto = SceltaDegliIncarichi.class.getDeclaredField("scelto");
        scelto.setAccessible(true);
        IncaricoInCitta primo = (IncaricoInCitta) scelto.get(null);
        assertNotNull(primo, "un incarico pronto c'è");
        // Nella stessa visita lo scelto smette di essere pronto: si sceglie di nuovo
        primo.aggiungiProprieta("DISPONIBILE_DALLE", String.valueOf(LineaTemporale.getGiorno() * 24L + LineaTemporale.getOra() + 1000));
        tocca.invoke(null, negozio);
        assertNotSame(primo, scelto.get(null));
    }

    @Test
    void leCronacheSenzaTappeOCompleteNonFannoPiuNiente() {
        CronacheDiUnFegatoEroico cronache = new CronacheDiUnFegatoEroico();
        cronache.getModelloDati().setId("CRONACHE_VUOTE");
        cronache.sostituisciMissioniSecondarie(Collections.emptyList());
        cronache.controllaPostLocazione();
        assertFalse(cronache.isCompleta());
        CronacheDiUnFegatoEroico vere = RegistroMissioni.getTutteLeMissioni().stream()
                .filter(CronacheDiUnFegatoEroico.class::isInstance).map(CronacheDiUnFegatoEroico.class::cast).findFirst().get();
        vere.completaMissione();
        vere.controllaPostLocazione();
        assertTrue(vere.isCompleta());
    }

    @Test
    void leLeggendeEIlMietitoreSiRaccontanoSoloAUnaVisitaTranquilla() {
        com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo armaiolo =
                registra(new com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo(), "ARMAIOLO");
        com.threeamigos.foresta.missioni.leggende.LaFalceDelTristoMietitore falce =
                registra(new com.threeamigos.foresta.missioni.leggende.LaFalceDelTristoMietitore(), "FALCE");
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        assertTrue(concluso(armaiolo));
        assertTrue(concluso(falce));
        chiMostraUnIntermezzo();
        assertFalse(concluso(armaiolo));
        assertFalse(concluso(falce));
    }

    @Test
    void ilLeggendarioDelTempioSiRecuperaSoloSeNessunAltroLHaPreso() {
        com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo leggenda =
                registra(new com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo(), "ARMAIOLO");
        leggenda.aggiungiProprieta("LEGGENDARIO", LaSfidaDeiCampioni.costruisciUnLeggendario(2).getRiga());
        CoordinateMD tempio = new CoordinateMD(4, 12);
        RegistroMissioni.occupaLocazione(tempio, leggenda);
        Foresta.setPosizioneDelGruppo(tempio);
        leggenda.aggiungiProprieta("PASSO_CORRENTE", "RECUPERO");
        assertTrue(concluso(leggenda), "il tempio è vuoto: il leggendario è già stato preso");
        com.threeamigos.foresta.mondo.RegistroArtefatti.addArtefattoInLocazione(
                com.threeamigos.foresta.artefatti.GeneratoreArtefatti.istanza().generaArtefatto(com.threeamigos.foresta.tipi.TipoArtefatto.SPADA, 1), tempio);
        assertFalse(concluso(leggenda), "l'artefatto è ancora nel tempio");
    }
}
