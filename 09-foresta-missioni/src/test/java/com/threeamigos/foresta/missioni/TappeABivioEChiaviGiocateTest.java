package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.incarichi.MissioneATappe;
import com.threeamigos.foresta.missioni.richieste.TappeRichieste;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni a tappe con le due piste o con le tre chiavi, giocate da molti giocatori automatici in mondi diversi (anche con le città che
 * cadono a metà strada): è la parte di MissioneATappe in cui i posti dipendono gli uni dagli altri.
 */
class TappeABivioEChiaviGiocateTest {

    private static final TipoLocazione[] CITTA = {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA,
            TipoLocazione.CITTA_FLEENA};

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private static String riga(Predicate<TappeRichieste> forma) {
        for (int i = 0; i < 3000; i++) {
            String riga = ProduttoreDiMissioniCasuale.rigaDiTappe(false);
            if (forma.test(TappeRichieste.da(riga))) {
                return riga;
            }
        }
        throw new IllegalStateException("Nessuna riga di quella forma");
    }

    private static void gioca(String riga, long seme, int cadute, Map<String, String> inattesi) {
        MondoDiProva.nuovo(seme);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        MissioneATappe missione = new MissioneATappe();
        missione.getModelloDati().setId("TAPPE" + seme);
        missione.aggiungiProprieta("PARAMETRO_RIGA", riga);
        RegistroMissioni.aggiungiMissioneSecondaria(missione);
        GiocatoreAutomatico giocatore = new GiocatoreAutomatico(seme);
        giocatore.gioca(60);
        for (int i = 0; i < cadute; i++) {
            Foresta.setCittaDistrutta(CITTA[i]);
            giocatore.gioca(20);
        }
        giocatore.getErrori().forEach((k, v) -> {
            if (!GiocatoreAutomaticoTest.isRumoreNoto(k)) {
                inattesi.put(k, v);
            }
        });
    }

    @Test
    void ConLeDuePiste() {
        Map<String, String> inattesi = new TreeMap<>();
        String riga = riga(r -> r.getBivio().isPresent());
        for (int seme = 0; seme < 12; seme++) {
            gioca(riga, 900 + seme, seme % 4, inattesi);
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void ConLeTreChiavi() {
        Map<String, String> inattesi = new TreeMap<>();
        String riga = riga(TappeRichieste::hasChiavi);
        String conScambio = riga(r -> r.hasChiavi() && r.getCustodi().stream().anyMatch(TappeRichieste.Custode::isInScambio));
        for (int seme = 0; seme < 12; seme++) {
            gioca(seme % 2 == 0 ? riga : conScambio, 950 + seme, seme % 4, inattesi);
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    // Le righe scritte a mano, con più tappe prima del bivio o delle chiavi e con due tappe in città (la grammatica non ne dà)
    private static final String TESTA = "CHIAVE=PROVA;TIPO=RINTRACCIAMENTO;ASPETTO=LOCANDIERE;MANDANTE=il locandiere;MONETE=40;"
            + "TITOLO=Sulle tracce di Gualtiero;RICHIESTA=Gualtiero è sparito.;BATTUTA=Da dove cominciamo?;RISPOSTA=Cominciate %DA_PROSSIMO%.;";
    private static final String CODA = "RINGRAZIAMENTO=Grazie.;RICORDO=Qui fu trovato Gualtiero.";
    private static final String DUE_CITTA = TESTA
            + "TAPPA_1=CITTA:Qualcuno lo ha visto: è andato %A_PROSSIMO%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa./Io sento le chiacchiere.;"
            + "TAPPA_2=CITTA:Un altro lo ha visto: è andato %A_PROSSIMO%.;SCENA_2=Il gruppo chiede ancora./Qualcuno sa.;"
            + "ARRIVO=ROVINE SCHELETRO 2:Gualtiero è qui, prigioniero.;" + CODA;
    private static final String BIVIO_DOPO_DUE_TAPPE = TESTA
            + "TAPPA_1=CITTA:Qualcuno lo ha visto: è andato %A_PROSSIMO%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa.;"
            + "TAPPA_2=ROVINE:Due voci: %PISTA_A% oppure %PISTA_B%.;SCENA_2=Il gruppo cerca./Qualcuno sa.;"
            + "PISTA_A=BOSCO:Un cacciatore ricorda un uomo diretto verso %PROSSIMO%.;SCENA_A=Il bosco è fitto./Cerchiamo segni.;"
            + "PISTA_B=GROTTA GOBLIN 3:Una grotta vuota: la strada giusta porta %A_ALTRA%.;SCENA_B=La grotta sa di pietra./Attenti.;GIUSTA=A;"
            + "ARRIVO=RADURA:Gualtiero è qui.;" + CODA;
    private static final String CHIAVI_DOPO_DUE_TAPPE = "CHIAVE=PROVA;TIPO=CACCIA_AL_TESORO;ASPETTO=MAGO;MANDANTE=il mago;MONETE=60;TITOLO=Le tre chiavi;"
            + "RICHIESTA=Una volta custodisce %OGGETTO%.;BATTUTA=E dove sono le chiavi?;RISPOSTA=Cominciate %DA_PROSSIMO%.;"
            + "TAPPA_1=CITTA:Un oste ricorda: è andato %A_PROSSIMO%.;SCENA_1=Il gruppo chiede./Cerchiamo.;"
            + "TAPPA_2=ROVINE:Una lapide dice i tre luoghi: %POSTO_1%, %POSTO_2% e %POSTO_3%.;SCENA_2=Il gruppo si muove fra i muri./Cerchiamo.;"
            + "CHIAVI=3;OGGETTO_CHIAVE_1=la chiave di ferro;OGGETTO_CHIAVE_2=il sigillo d'osso;OGGETTO_CHIAVE_3=la chiave a tre denti;"
            + "CUSTODE_1=BOSCO LADRO 3 SI:Il capo %CAPO% porta la chiave al collo.;CUSTODE_2=ROVINE SCHELETRO 4 NO:Scheletri senza sonno.;"
            + "CUSTODE_3=RADURA SCAMBIO 0 NOME_CAMPIONE:Il cavaliere %CAPO% vuole un medaglione.;"
            + "SCAMBIO_3=ROVINE SCHELETRO 3:il medaglione/Il medaglione è fra le ossa di una cripta.;"
            + "ARRIVO=TEMPIO:Dietro l'altare trovate %OGGETTO%.;" + CODA;

    @Test
    void ConPiuTappePrimaDelBivioODelleChiaviEDueCittaDiFila() {
        Map<String, String> inattesi = new TreeMap<>();
        int seme = 0;
        for (String riga : new String[] {DUE_CITTA, BIVIO_DOPO_DUE_TAPPE, CHIAVI_DOPO_DUE_TAPPE}) {
            assertNotNull(TappeRichieste.da(riga));
            for (int giro = 0; giro < 8; giro++) {
                gioca(riga, 1000 + seme++, giro % 4, inattesi);
            }
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void ConUnGruppoDiUnSoloPersonaggio() {
        Map<String, String> inattesi = new TreeMap<>();
        for (int giro = 0; giro < 3; giro++) {
            MondoDiProva.nuovo(1100 + giro);
            for (com.threeamigos.foresta.personaggi.Personaggio p : new java.util.ArrayList<>(
                    com.threeamigos.foresta.gruppi.GruppoGiocatore.getIstanza().getPersonaggiVivi())) {
                if (p != com.threeamigos.foresta.gruppi.GruppoGiocatore.getIstanza().getCapo()) {
                    p.subSalute(100000, null, com.threeamigos.foresta.personaggi.Personaggio.NotificaFerite.NO,
                            com.threeamigos.foresta.personaggi.Personaggio.NotificaMorte.NO);
                }
            }
            RegistroMissioni.reimposta();
            RegistroMissioni.registrati();
            MissioneATappe missione = new MissioneATappe();
            missione.getModelloDati().setId("TAPPE_SOLO" + giro);
            missione.aggiungiProprieta("PARAMETRO_RIGA", giro == 0 ? DUE_CITTA : giro == 1 ? BIVIO_DOPO_DUE_TAPPE : CHIAVI_DOPO_DUE_TAPPE);
            RegistroMissioni.aggiungiMissioneSecondaria(missione);
            GiocatoreAutomatico giocatore = new GiocatoreAutomatico(1100 + giro);
            giocatore.gioca(80);
            giocatore.getErrori().forEach((k, v) -> {
                if (!GiocatoreAutomaticoTest.isRumoreNoto(k)) {
                    inattesi.put(k, v);
                }
            });
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }
}
