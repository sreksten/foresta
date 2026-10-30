package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.principali.CronacheDiUnFegatoEroico;
import com.threeamigos.foresta.missioni.principali.DisturbatoreDellaQuietePubblica;
import com.threeamigos.foresta.missioni.principali.IlGiroDelleSetteChiese;
import com.threeamigos.foresta.missioni.principali.NessunBoccaleLasciatoIndietro;
import com.threeamigos.foresta.missioni.principali.SconfiggiIlDrago;
import com.threeamigos.foresta.missioni.principali.VisitaLocanda;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni principali negli stati che una partita a caso non incontra: eremiti già contati, locande perdute, tappe fallite, boccali
 * contati uno per uno, templi ripuliti fino al settimo, castello del Drago senza un bosco dove sorgere.
 */
class PrincipaliScenariTest {

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(81);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    @SuppressWarnings("unchecked")
    private static <M extends Missione> M daRegistro(Class<M> classe) {
        for (Missione m : RegistroMissioni.getTutteLeMissioni()) {
            if (classe.isInstance(m)) {
                return (M) m;
            }
        }
        throw new IllegalStateException(classe.getSimpleName());
    }

    private static List<CoordinateMD> caselle(TipoLocazione tipo) {
        List<CoordinateMD> trovate = new ArrayList<>();
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                if (Foresta.getLocazione(x, y) == tipo) {
                    trovate.add(new CoordinateMD(x, y));
                }
            }
        }
        return trovate;
    }

    // ---- Gli eremiti

    @Test
    void unEremitaGiaContatoNonSiContaDueVolte() {
        DisturbatoreDellaQuietePubblica disturbatore = daRegistro(DisturbatoreDellaQuietePubblica.class);
        GruppoAvversario.getIstanza().reimposta();
        GruppoAvversario.getIstanza().aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.EREMITA, 1));
        disturbatore.controllaPreLocazione();
        disturbatore.controllaPostLocazione();
        disturbatore.controllaInLocazione();
        assertTrue(disturbatore.isAttiva());
        String contati = disturbatore.ottieniProprieta("EREMITI_CONTATI");
        disturbatore.controllaInLocazione();
        assertEquals(contati, disturbatore.ottieniProprieta("EREMITI_CONTATI"), "lo stesso eremita non conta due volte");
        // Una lista salvata ma vuota è una lista senza eremiti
        disturbatore.aggiungiProprieta("EREMITI_CONTATI", "");
        GruppoAvversario.getIstanza().reimposta();
        disturbatore.controllaInLocazione();
        assertEquals("", disturbatore.ottieniProprieta("EREMITI_CONTATI"));
    }

    // ---- Le locande

    @Test
    void laVisitaAUnaLocandaControllaLaCitta() {
        VisitaLocanda visita = new VisitaLocanda();
        visita.getModelloDati().setId("VISITA");
        assertThrows(IllegalArgumentException.class, () -> visita.setCitta(TipoLocazione.BOSCO));
        // Senza città (a una tappa nata da sola, e già attiva) non c'è niente da segnare né da controllare
        visita.attivaMissione();
        visita.controllaPreLocazione();
        visita.controllaPostLocazione();
        assertFalse(visita.isCompleta());
    }

    @Test
    void laLocandaDiUnaCittaSenzaNomeEUnaCittaDistrutta() {
        CoordinateMD ruuna = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_RUUNA);
        Foresta.getLocazioneMD(ruuna).rimuoviProprieta(Locande.LOCANDA_NOME_IN_CITTA);
        VisitaLocanda senzaNome = new VisitaLocanda();
        senzaNome.getModelloDati().setId("SENZA_NOME");
        senzaNome.setCitta(TipoLocazione.CITTA_RUUNA);
        assertNull(Locande.getNome(Foresta.getLocazioneMD(ruuna)));
        assertFalse(senzaNome.getDescrizione().isEmpty());
        senzaNome.controllaPreLocazione();
        senzaNome.controllaPreLocazione();
        senzaNome.controllaPostLocazione();
        assertFalse(senzaNome.isCompleta(), "nessuno ha ancora bevuto in questa locanda");

        VisitaLocanda bevuta = new VisitaLocanda();
        bevuta.getModelloDati().setId("BEVUTA");
        bevuta.setCitta(TipoLocazione.CITTA_MALGAARD);
        bevuta.controllaPreLocazione();
        Foresta.getLocazioneMD(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_MALGAARD)).aggiungiProprieta(Locande.LOCANDA_VISITATA, "SI");
        bevuta.controllaPostLocazione();
        assertTrue(bevuta.isCompleta());
        bevuta.controllaPreLocazione();
        bevuta.controllaPostLocazione();

        VisitaLocanda perduta = new VisitaLocanda();
        perduta.getModelloDati().setId("PERDUTA");
        perduta.setCitta(TipoLocazione.CITTA_FLEENA);
        perduta.controllaPreLocazione();
        Foresta.setCittaDistrutta(TipoLocazione.CITTA_FLEENA);
        perduta.controllaPreLocazione();
        assertTrue(perduta.isFallita());
    }

    @Test
    void laLocandaDiUnaCittaNonCostruitaEPerduta() throws Exception {
        java.lang.reflect.Method dati = Foresta.class.getDeclaredMethod("getForestaMD");
        dati.setAccessible(true);
        java.lang.reflect.Field uniche = ForestaMD.class.getDeclaredField("locazioniUniche");
        uniche.setAccessible(true);
        ((Map<?, ?>) uniche.get(dati.invoke(null))).remove(TipoLocazione.CITTA_FLEENA);
        VisitaLocanda visita = new VisitaLocanda();
        visita.getModelloDati().setId("NON_COSTRUITA");
        visita.setCitta(TipoLocazione.CITTA_FLEENA);
        assertTrue(visita.getNome().contains("locanda perduta"));
        visita.controllaPostLocazione();
        assertFalse(visita.isCompleta());
    }

    @Test
    void leCronacheFalliscononSeUnaTappaFallisce() {
        CronacheDiUnFegatoEroico cronache = daRegistro(CronacheDiUnFegatoEroico.class);
        assertFalse(cronache.getMissioniSecondarie().isEmpty());
        cronache.controllaPostLocazione();
        cronache.attivaMissione();
        cronache.getMissioniSecondarie().get(0).fallisciMissione();
        cronache.controllaPostLocazione();
        assertTrue(cronache.isFallita());
        cronache.controllaPostLocazione();
        cronache.controllaInLocazione();
    }

    @Test
    void ilFegatoContaLeLocandeUnaAdUna() {
        NessunBoccaleLasciatoIndietro boccali = daRegistro(NessunBoccaleLasciatoIndietro.class);
        boccali.controllaPostLocazione();
        assertTrue(boccali.isAttiva());
        List<CoordinateMD> locande = caselle(TipoLocazione.LOCANDA);
        for (int x = 0; locande.size() < 10; x++) {
            CoordinateMD nuova = new CoordinateMD(x, 0);
            Foresta.impostaLocazione(nuova, TipoLocazione.LOCANDA);
            locande.add(nuova);
        }
        for (int i = 0; i < 9; i++) {
            Foresta.getLocazioneMD(locande.get(i)).aggiungiProprieta(Locande.LOCANDA_VISITATA, "SI");
        }
        boccali.controllaPostLocazione();
        assertTrue(boccali.getDescrizione().contains("Ne manca una"), boccali.getDescrizione());
        Foresta.getLocazioneMD(locande.get(9)).aggiungiProprieta(Locande.LOCANDA_VISITATA, "SI");
        boccali.controllaPostLocazione();
        assertTrue(boccali.isCompleta());
    }

    // ---- I templi

    @Test
    void ilGiroDelleSetteChieseContaITempliFinoAlSettimo() {
        IlGiroDelleSetteChiese giro = daRegistro(IlGiroDelleSetteChiese.class);
        List<CoordinateMD> templi = caselle(TipoLocazione.TEMPIO);
        for (int x = 0; templi.size() < 7; x++) {
            CoordinateMD nuovo = new CoordinateMD(x, 1);
            Foresta.impostaLocazione(nuovo, TipoLocazione.TEMPIO);
            templi.add(nuovo);
        }
        GruppoAvversario.getIstanza().reimposta();
        Foresta.setPosizioneDelGruppo(templi.get(0));
        giro.controllaPostLocazione();
        assertTrue(giro.isAttiva());
        for (int i = 1; i < 6; i++) {
            Foresta.setPosizioneDelGruppo(templi.get(i));
            giro.controllaPostLocazione();
        }
        assertTrue(giro.getDescrizione().contains("Ne manca uno"), giro.getDescrizione());
        Foresta.setPosizioneDelGruppo(templi.get(6));
        giro.controllaPostLocazione();
        assertNotEquals("TEMPLI", giro.getPassoCorrente(), "sette templi ripuliti: il giro è finito");
        assertFalse(giro.getDescrizione().isEmpty());
    }

    // ---- Il Drago

    @Test
    void ilCastelloDelDragoNonSorgeSeNonCeUnBoscoLibero() {
        SconfiggiIlDrago drago = daRegistro(SconfiggiIlDrago.class);
        for (Missione m : RegistroMissioni.getTutteLeMissioni()) {
            if (m.getClass().getSimpleName().startsWith("Sconfiggi") && m != drago) {
                m.completaMissione();
            }
        }
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                TipoLocazione tipo = Foresta.getLocazione(x, y);
                if (tipo == TipoLocazione.BOSCO || tipo == TipoLocazione.PALUDE) {
                    Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.RADURA);
                }
            }
        }
        drago.controllaPostLocazione();
        assertFalse(drago.isCompleta());
    }
}
