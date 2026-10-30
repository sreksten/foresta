package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.PassiSinteticiTest.Sintetica;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.incarichi.MissioneATappe;
import com.threeamigos.foresta.missioni.leggende.ConLeggendario;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDelLocandiere;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MissioneATappe e le leggende nei casi che non si incontrano giocando: un posto che manca, un mondo senza spazio per l'ultimo posto, un
 * gruppo di uno che ascolta la scena, il bivio visto da dentro, le leggende che aspettano. Le domande private si fanno con la reflection.
 */
class MissioneATappeCasiTest {

    private static final MomentoControllo PRE = MomentoControllo.PRE_LOCAZIONE;

    private static final String TESTA = "CHIAVE=PROVA;TIPO=RINTRACCIAMENTO;ASPETTO=LOCANDIERE;MANDANTE=il locandiere;MONETE=40;"
            + "TITOLO=Sulle tracce di Gualtiero;RICHIESTA=Gualtiero è sparito.;BATTUTA=Da dove cominciamo?;RISPOSTA=Cominciate %DA_PROSSIMO%.;";
    private static final String CODA = "RINGRAZIAMENTO=Grazie.;RICORDO=Qui fu trovato Gualtiero.";
    private static final String CON_COMPAGNO = TESTA
            + "TAPPA_1=CITTA:Qualcuno lo ha visto: è andato %A_PROSSIMO%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa./Io sento le chiacchiere.;"
            + "ARRIVO=ROVINE SCHELETRO 2:Gualtiero è qui, prigioniero.;" + CODA;
    private static final String BIVIO = TESTA
            + "TAPPA_1=CITTA:Due voci: %PISTA_A% oppure %PISTA_B%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa.;"
            + "PISTA_A=BOSCO:Un cacciatore ricorda un uomo diretto verso %PROSSIMO%.;SCENA_A=Il bosco è fitto./Cerchiamo segni.;"
            + "PISTA_B=GROTTA GOBLIN 3:Una grotta vuota: la strada giusta porta %A_ALTRA%.;SCENA_B=La grotta sa di pietra./Attenti.;GIUSTA=A;"
            + "ARRIVO=RADURA:Gualtiero è qui.;" + CODA;
    private static final String CHIAVI = "CHIAVE=PROVA;TIPO=CACCIA_AL_TESORO;ASPETTO=MAGO;MANDANTE=il mago;MONETE=60;TITOLO=Le tre chiavi;"
            + "RICHIESTA=Una volta custodisce %OGGETTO%.;BATTUTA=E dove sono le chiavi?;RISPOSTA=Cominciate %DA_PROSSIMO%.;"
            + "TAPPA_1=ROVINE:Una lapide dice i tre luoghi: %POSTO_1%, %POSTO_2% e %POSTO_3%.;SCENA_1=Il gruppo si muove fra i muri./Cerchiamo.;"
            + "CHIAVI=3;OGGETTO_CHIAVE_1=la chiave di ferro;OGGETTO_CHIAVE_2=il sigillo d'osso;OGGETTO_CHIAVE_3=la chiave a tre denti;"
            + "CUSTODE_1=BOSCO LADRO 3 SI:Il capo %CAPO% porta la chiave al collo.;CUSTODE_2=ROVINE SCHELETRO 4 NO:Scheletri senza sonno.;"
            + "CUSTODE_3=RADURA SCAMBIO 0 NOME_CAMPIONE:Il cavaliere %CAPO% vuole un medaglione.;"
            + "SCAMBIO_3=ROVINE SCHELETRO 3:il medaglione/Il medaglione è fra le ossa di una cripta.;"
            + "ARRIVO=TEMPIO:Dietro l'altare trovate %OGGETTO%.;" + CODA;

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(131);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        FabbricaMissioni.registra(MissionePredefinita.MISSIONE_DI_PROVA, Sintetica::new);
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

    private MissioneATappe tappe(String riga, String id) {
        MissioneATappe m = registra(new MissioneATappe(), id);
        m.aggiungiProprieta("PARAMETRO_RIGA", riga);
        return m;
    }

    private static Object invoca(Object oggetto, String nome, Object... argomenti) throws Exception {
        for (Class<?> c = oggetto.getClass(); c != null; c = c.getSuperclass()) {
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

    private static void tuttoRadureTranne(TipoLocazione tipo, CoordinateMD dove) {
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                TipoLocazione t = Foresta.getLocazione(x, y);
                if (t.getCategoriaLocazione() != CategoriaLocazione.CITTA && t.getCategoriaLocazione() != CategoriaLocazione.CASTELLO) {
                    Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.RADURA);
                }
            }
        }
        Foresta.impostaLocazione(dove, tipo);
    }

    // ---- I testi che nominano il posto dopo

    @Test
    void iTestiSiRisolvonoSoloPerIPostiGiaScelti() throws Exception {
        MissioneATappe m = tappe(CON_COMPAGNO, "TAPPE");
        String senzaPosto = (String) invoca(m, "testo", "Vai " + com.threeamigos.foresta.missioni.richieste.TappeRichieste.A_PROSSIMO + ".", 2);
        assertTrue(senzaPosto.contains(com.threeamigos.foresta.missioni.richieste.TappeRichieste.A_PROSSIMO), "il posto 2 non è ancora scelto");
        String oltre = (String) invoca(m, "testo", "Vai " + com.threeamigos.foresta.missioni.richieste.TappeRichieste.A_PROSSIMO + ".", 99);
        assertTrue(oltre.contains(com.threeamigos.foresta.missioni.richieste.TappeRichieste.A_PROSSIMO), "il posto 99 non esiste");
        m.aggiungiProprieta("POSTO_2", "5,5");
        assertFalse(((String) invoca(m, "testo", "Vai " + com.threeamigos.foresta.missioni.richieste.TappeRichieste.A_PROSSIMO + ".", 2))
                .contains(com.threeamigos.foresta.missioni.richieste.TappeRichieste.A_PROSSIMO));
    }

    @Test
    void ilBivioSiDescriveAnchePrimaDiAverScelto_ilSecondoPosto() {
        MissioneATappe m = tappe(BIVIO, "TAPPE_BIVIO");
        m.attivaMissione();
        m.aggiungiProprieta("PASSO_CORRENTE", "BIVIO");
        assertFalse(m.getDescrizione().isEmpty());
        m.aggiungiProprieta("POSTO_2", "5,5");
        assertFalse(m.getDescrizione().isEmpty());
        m.aggiungiProprieta("POSTO_3", "8,8");
        assertTrue(m.getDescrizione().contains("Le piste sono due"), m.getDescrizione());
    }

    @Test
    void nelBivioUnaPistaConGuardiaNonRendeSicuroIlPosto() {
        MissioneATappe m = tappe(BIVIO, "TAPPE_BIVIO");
        m.aggiungiProprieta("POSTO_2", "5,5");
        m.aggiungiProprieta("POSTO_3", "8,8");
        m.aggiungiProprieta("CITTA_DEL_POSTO_1", TipoLocazione.CITTA_RUUNA.name());
        Foresta.setPosizioneDelGruppo(new CoordinateMD(5, 5));
        AccessoAiPassi.eseguiAOgniControllo(m, "BIVIO");
        assertTrue(RegistroMissioni.isDaSopprimere(new CoordinateMD(5, 5)), "la pista A non ha guardia: è sicura");
        Foresta.setPosizioneDelGruppo(new CoordinateMD(8, 8));
        AccessoAiPassi.eseguiAOgniControllo(m, "BIVIO");
        assertFalse(RegistroMissioni.isDaSopprimere(new CoordinateMD(8, 8)), "la pista B ha tre goblin di guardia");
        m.aggiungiProprieta("PISTA_FALSA_VISTA", "S");
        AccessoAiPassi.eseguiAOgniControllo(m, "BIVIO");
        assertFalse(AccessoAiPassi.isConcluso(m, "BIVIO"), "la pista falsa già vista non conta più");
    }

    @Test
    void leTappeNonTrovanoPostoPerLeDuePiste() throws Exception {
        // Un solo bosco in tutta la mappa: la prima pista lo prende, per la seconda (una grotta) non resta niente da costruire
        tuttoRadureTranne(TipoLocazione.BOSCO, new CoordinateMD(10, 10));
        MissioneATappe m = tappe(BIVIO, "TAPPE_BIVIO");
        m.aggiungiProprieta("CITTA_DEL_POSTO_1", TipoLocazione.CITTA_RUUNA.name());
        m.aggiungiProprieta("LIVELLO_DELL_INCARICO", "1");
        Foresta.setPosizioneDelGruppo(new CoordinateMD(0, 0));
        m.attivaMissione();
        invoca(m, "concludiIlPosto", 1);
        assertTrue(m.isFallita(), "la pista si interrompe: non c'è dove cercare");
    }

    @Test
    void senzaSpazioPerL_ultimoPostoLeChiaviNonBastano() throws Exception {
        tuttoRadureTranne(TipoLocazione.RADURA, new CoordinateMD(10, 10));
        MissioneATappe m = tappe(CHIAVI, "TAPPE_CHIAVI");
        m.attivaMissione();
        for (int i = 1; i <= 3; i++) {
            Sintetica chiave = new Sintetica();
            chiave.getModelloDati().setId("CHIAVE_" + i);
            m.aggiungiMissione(chiave);
            chiave.passo("A", s -> Passo.quando(PRE, () -> false).poi(Passo.FINE));
            chiave.completaMissione();
        }
        m.aggiungiProprieta("AFFIDATE_CHIAVI", "CHIAVE_1,CHIAVE_2,CHIAVE_3");
        assertTrue(m.sonoRiusciteLeAffidate("CHIAVI"));
        invoca(m, "apriLUltimoPosto");
        assertTrue(m.isFallita(), "le chiavi ci sono, ma l'ultimo posto, un tempio, non si può costruire da nessuna parte");
    }

    @Test
    void unGruppoDiUnoAscoltaLaSceneDaSolo() throws Exception {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        for (Personaggio p : new java.util.ArrayList<>(gruppo.getPersonaggiVivi())) {
            if (p != gruppo.getCapo()) {
                p.subSalute(100000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            }
        }
        MissioneATappe m = tappe(CON_COMPAGNO, "TAPPE_SCENA");
        m.aggiungiProprieta("CITTA_DEL_POSTO_1", TipoLocazione.CITTA_RUUNA.name());
        assertFalse(((java.util.List<?>) invoca(m, "pagineDellaTappa", 1)).isEmpty());
    }

    // ---- Le leggende che aspettano

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

    @Test
    void laLeggendaSiRaccontaSoloQuandoTuttoLoPermette() {
        LaLeggendaDellArmaiolo armaiolo = registra(new LaLeggendaDellArmaiolo(), "ARMAIOLO");
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        assertTrue(AccessoAiPassi.isConcluso(armaiolo, "INCARICO"));
        armaiolo.aggiungiProprieta("DISPONIBILE_DALLE", "99999999");
        assertFalse(AccessoAiPassi.isConcluso(armaiolo, "INCARICO"), "non è ancora passata la pausa");
        armaiolo.rimuoviProprieta("DISPONIBILE_DALLE");
        Foresta.setPosizioneDelGruppo(new CoordinateMD(0, 0));
        assertFalse(AccessoAiPassi.isConcluso(armaiolo, "INCARICO"), "il gruppo non è in una città");
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        LaLeggendaDelLocandiere altra = registra(new LaLeggendaDelLocandiere(), "LOCANDIERE");
        altra.aggiungiProprieta("RACCONTATA_ALLE", String.valueOf(com.threeamigos.foresta.statistiche.LineaTemporale.getGiorno() * 24L
                + com.threeamigos.foresta.statistiche.LineaTemporale.getOra()));
        assertFalse(AccessoAiPassi.isConcluso(armaiolo, "INCARICO"), "un'altra leggenda è stata appena raccontata");
        altra.rimuoviProprieta("RACCONTATA_ALLE");
        assertTrue(AccessoAiPassi.isConcluso(armaiolo, "INCARICO"));
        // Finiti i leggendari del catalogo, una leggenda senza il suo non si racconta; con il suo sì
        int i = 0;
        for (OggettoLeggendario leggendario : CatalogoLeggendari.getTuttiILeggendari().values()) {
            registra(new ConUnLeggendario(leggendario), "USCITO_" + i++);
        }
        assertFalse(AccessoAiPassi.isConcluso(armaiolo, "INCARICO"));
        armaiolo.aggiungiProprieta("LEGGENDARIO", CatalogoLeggendari.getTuttiILeggendari().values().iterator().next().getRiga());
        assertTrue(AccessoAiPassi.isConcluso(armaiolo, "INCARICO"));
    }

    @Test
    void unaPistaInCittaNonSiRendeSicuraComeUnPostoSenzaGuardia() {
        String bivioInCitta = BIVIO.replace("PISTA_A=BOSCO:", "PISTA_A=CITTA:");
        MissioneATappe m = tappe(bivioInCitta, "TAPPE_BIVIO_IN_CITTA");
        m.aggiungiProprieta("CITTA_DEL_POSTO_2", TipoLocazione.CITTA_RUUNA.name());
        m.aggiungiProprieta("POSTO_2", "5,5");
        m.aggiungiProprieta("POSTO_3", "8,8");
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_RUUNA));
        AccessoAiPassi.eseguiAOgniControllo(m, "BIVIO");
        assertFalse(RegistroMissioni.isDaSopprimere(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_RUUNA)));
    }

    @Test
    void ilMietitoreSiRaccontaSoloInUnaCittaALuiDisponibile() {
        com.threeamigos.foresta.missioni.leggende.LaFalceDelTristoMietitore falce =
                registra(new com.threeamigos.foresta.missioni.leggende.LaFalceDelTristoMietitore(), "FALCE");
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        assertTrue(AccessoAiPassi.isConcluso(falce, "INCARICO"));
        falce.aggiungiProprieta("DISPONIBILE_DALLE", "99999999");
        assertFalse(AccessoAiPassi.isConcluso(falce, "INCARICO"));
        falce.rimuoviProprieta("DISPONIBILE_DALLE");
        Foresta.setPosizioneDelGruppo(new CoordinateMD(0, 0));
        assertFalse(AccessoAiPassi.isConcluso(falce, "INCARICO"));
    }
}
