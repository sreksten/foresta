package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tutte le missioni giocate dal giocatore automatico in mondi estremi: ogni livello del mondo, nessun posto dove costruire, tutte le città
 * distrutte, boschi tutti custoditi da artefatti. Nessuna deve rompersi, salvo il rifiuto esplicito di IlNegozio quando non c'è posto.
 */
class MondiEstremiTest {

    private static final TipoLocazione[] CITTA = {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA,
            TipoLocazione.CITTA_FLEENA};

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private static boolean isRifiutoAtteso(String errore, String messaggio) {
        return GiocatoreAutomaticoTest.isRumoreNoto(errore) || messaggio.startsWith("Nessun posto per le rovine");
    }

    private static void gioca(long seme, int livello, boolean senzaPosti, boolean cittaDistrutte, boolean boschiCustoditi, Map<String, String> inattesi) {
        MondoDiProva.nuovo(seme);
        MondoDiProva.statistiche.setLivello(livello);
        if (senzaPosti) {
            for (int x = 0; x < MondoDiProva.LATO; x++) {
                for (int y = 0; y < MondoDiProva.LATO; y++) {
                    TipoLocazione tipo = Foresta.getLocazione(x, y);
                    if (tipo.getCategoriaLocazione() != CategoriaLocazione.CITTA && tipo.getCategoriaLocazione() != CategoriaLocazione.CASTELLO
                            && tipo != TipoLocazione.LOCANDA) {
                        Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.RADURA);
                    }
                }
            }
        }
        if (boschiCustoditi) {
            for (int x = 0; x < MondoDiProva.LATO; x++) {
                for (int y = 0; y < MondoDiProva.LATO; y++) {
                    if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO && (x + y) % 2 == 0) {
                        RegistroArtefatti.addArtefattoInLocazione(GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 1),
                                new CoordinateMD(x, y));
                    }
                }
            }
        }
        if (cittaDistrutte) {
            for (TipoLocazione citta : CITTA) {
                Foresta.setCittaDistrutta(citta);
            }
        }
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        GiocatoreAutomatico g = new GiocatoreAutomatico(seme);
        g.gioca(70);
        g.getErrori().forEach((k, v) -> {
            if (!isRifiutoAtteso(k, v)) {
                inattesi.put(k, v);
            }
        });
    }

    @Test
    void ognLivelloDelMondo() {
        Map<String, String> inattesi = new TreeMap<>();
        int seme = 700;
        for (int livello : new int[] {1, 2, 3, 4, 5, 6, 7, 9, 12}) {
            gioca(++seme, livello, false, false, false, inattesi);
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void senzaPostiPerCostruire() {
        Map<String, String> inattesi = new TreeMap<>();
        for (int seme = 720; seme < 726; seme++) {
            gioca(seme, 1 + seme % 5, true, false, false, inattesi);
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void conTutteLeCittaDistrutte() {
        Map<String, String> inattesi = new TreeMap<>();
        for (int seme = 740; seme < 746; seme++) {
            gioca(seme, 1 + seme % 5, false, true, false, inattesi);
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void coiBoschiCustoditiDagliArtefatti() {
        Map<String, String> inattesi = new TreeMap<>();
        for (int seme = 760; seme < 766; seme++) {
            gioca(seme, 1 + seme % 5, false, false, true, inattesi);
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void senzaPostiEConLeCittaDistrutte() {
        Map<String, String> inattesi = new TreeMap<>();
        for (int seme = 780; seme < 785; seme++) {
            gioca(seme, 1 + seme % 5, true, true, true, inattesi);
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    /** I dati della Foresta in uso: per togliere al gruppo la sua casella, che l'API pubblica non lascia togliere. */
    private static ForestaMD datiDellaForesta() throws ReflectiveOperationException {
        java.lang.reflect.Method dati = Foresta.class.getDeclaredMethod("getForestaMD");
        dati.setAccessible(true);
        return (ForestaMD) dati.invoke(null);
    }

    @Test
    void senzaUnaCasellaPerIlGruppoLeMissioniPercorronoICoseSenzaRompersi() throws Exception {
        for (int seme = 820; seme < 824; seme++) {
            MondoDiProva.nuovo(seme);
            RegistroMissioni.reimposta();
            RegistroMissioni.registrati();
            GiocatoreAutomatico g = new GiocatoreAutomatico(seme);
            g.gioca(20);
            datiDellaForesta().setPosizioneDelGruppo(null);
            g.percorriTutto();
            datiDellaForesta().setPosizioneDelGruppo(new CoordinateMD(0, 0));
        }
    }

    /** Un mondo con solo le prime città: le altre non sono mai state costruite. */
    private static void poche(long seme, int citta, Map<String, String> inattesi) throws Exception {
        MondoDiProva.nuovo(seme);
        ForestaMD dati = datiDellaForesta();
        java.lang.reflect.Field uniche = ForestaMD.class.getDeclaredField("locazioniUniche");
        uniche.setAccessible(true);
        for (int i = citta; i < CITTA.length; i++) {
            ((Map<?, ?>) uniche.get(dati)).remove(CITTA[i]);
        }
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        GiocatoreAutomatico g = new GiocatoreAutomatico(seme);
        g.gioca(60);
        g.percorriTutto();
        g.getErrori().forEach((k, v) -> {
            if (!isRifiutoAtteso(k, v)) {
                inattesi.put(k, v);
            }
        });
    }

    @Test
    void conPocheCittaCostruite() throws Exception {
        Map<String, String> inattesi = new TreeMap<>();
        for (int citta = 1; citta <= 3; citta++) {
            for (int seme = 0; seme < 3; seme++) {
                poche(840 + citta * 10 + seme, citta, inattesi);
            }
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void leCittaCadonoDopoCheLeMissioniHannoScelto() {
        Map<String, String> inattesi = new TreeMap<>();
        for (int seme = 800; seme < 806; seme++) {
            MondoDiProva.nuovo(seme);
            RegistroMissioni.reimposta();
            RegistroMissioni.registrati();
            GiocatoreAutomatico g = new GiocatoreAutomatico(seme);
            g.gioca(60);
            for (TipoLocazione citta : CITTA) {
                Foresta.setCittaDistrutta(citta);
            }
            g.percorriTutto();
            g.gioca(30);
            g.getErrori().forEach((k, v) -> {
                if (!isRifiutoAtteso(k, v)) {
                    inattesi.put(k, v);
                }
            });
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }
}
