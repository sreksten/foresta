package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.missioni.richieste.TappeRichieste.Custode;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.ClasseDiPosto;
import com.threeamigos.foresta.tipi.TipoMissione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni a tappe (vedi missione_a_tappe.md e tappe.txt): la lettura di una riga a mano, i controlli sulle righe sbagliate e la lettura
 * di tutte le righe che la grammatica sa produrre.
 */
class TappeRichiesteTest {

    private static final String PERSONA = "CHIAVE=PROVA;TIPO=RINTRACCIAMENTO;ASPETTO=LOCANDIERE;MANDANTE=il locandiere;MONETE=40;"
            + "TITOLO=Sulle tracce di Gualtiero;RICHIESTA=Gualtiero è sparito.;BATTUTA=Da dove cominciamo?;"
            + "RISPOSTA=Cominciate %DA_PROSSIMO%.;"
            + "TAPPA_1=CITTA:Qualcuno lo ha visto: è andato %A_PROSSIMO%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa./Io sento le chiacchiere.;"
            + "ARRIVO=ROVINE SCHELETRO 2:Gualtiero è qui, prigioniero.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui fu trovato Gualtiero.";

    private static final String BIVIO = "CHIAVE=PROVA;TIPO=RINTRACCIAMENTO;ASPETTO=LOCANDIERE;MANDANTE=il locandiere;MONETE=40;"
            + "TITOLO=Due piste;RICHIESTA=Gualtiero è sparito.;BATTUTA=Da dove cominciamo?;RISPOSTA=Cominciate %DA_PROSSIMO%.;"
            + "TAPPA_1=CITTA:Due voci: %PISTA_A% oppure %PISTA_B%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa.;"
            + "PISTA_A=BOSCO:Un cacciatore ricorda un uomo diretto verso %PROSSIMO%.;SCENA_A=Il bosco è fitto./Cerchiamo segni.;"
            + "PISTA_B=GROTTA GOBLIN 3:Una grotta vuota: la strada giusta porta %A_ALTRA%.;SCENA_B=La grotta sa di pietra./Attenti.;GIUSTA=A;"
            + "ARRIVO=RADURA:Gualtiero è qui.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui fu trovato Gualtiero.";

    private static final String FARSA = "CHIAVE=SGARBO_DI_Ubaldo;TIPO=VENDETTA;ASPETTO=QUALUNQUE;MANDANTE=un mugnaio;MONETE=20;"
            + "TITOLO=Una questione di portiera;RICHIESTA=Dovete andare a punire Ubaldo. Vi darò %MONETE% monete.;BATTUTA=Che cosa vi ha fatto?;"
            + "RISPOSTA=In sogno mi ha rigato la portiera della macchina.;BATTUTA_2=Che cos'è una macchina?;"
            + "RISPOSTA_2=Non lo so, l'ho sognata. Cominciate %DA_PROSSIMO%.;"
            + "ARRIVO=BOSCO:Ubaldo è qui, e ripara una staccionata.;RINGRAZIAMENTO=La portiera è a posto.;RICORDO=Qui Ubaldo fu punito.";

    private static final String CHIAVI = "CHIAVE=PROVA;TIPO=CACCIA_AL_TESORO;ASPETTO=MAGO;MANDANTE=il mago;MONETE=60;TITOLO=Le tre chiavi;"
            + "RICHIESTA=Una volta custodisce %OGGETTO%.;BATTUTA=E dove sono le chiavi?;RISPOSTA=Cominciate %DA_PROSSIMO%.;"
            + "TAPPA_1=ROVINE:Una lapide dice i tre luoghi: %POSTO_1%, %POSTO_2% e %POSTO_3%.;SCENA_1=Il gruppo si muove fra i muri./Cerchiamo.;"
            + "CHIAVI=3;OGGETTO_CHIAVE_1=la chiave di ferro;OGGETTO_CHIAVE_2=il sigillo d'osso;OGGETTO_CHIAVE_3=la chiave a tre denti;"
            + "CUSTODE_1=BOSCO LADRO 3 SI:Il capo %CAPO% porta la chiave al collo.;CUSTODE_2=ROVINE SCHELETRO 4 NO:Scheletri senza sonno.;"
            + "CUSTODE_3=RADURA SCAMBIO 0 NOME_CAMPIONE:Il cavaliere %CAPO% vuole un medaglione.;"
            + "SCAMBIO_3=ROVINE SCHELETRO 3:il medaglione/Il medaglione è fra le ossa di una cripta.;"
            + "ARRIVO=TEMPIO:Dietro l'altare trovate %OGGETTO%.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui si aprì la volta.";

    private static void assertRifiutata(String riga) {
        assertThrows(IllegalArgumentException.class, () -> TappeRichieste.da(riga), riga);
    }

    @Test
    void unaPersonaDaCercareSiLegge() {
        TappeRichieste r = TappeRichieste.da(PERSONA);
        assertEquals(TipoMissione.RINTRACCIAMENTO, r.getTipo());
        assertFalse(r.isFarsa());
        assertFalse(r.isDiUnLeggendario());
        assertEquals(1, r.getTappe().size());
        assertSame(ClasseDiPosto.CITTA, r.getTappe().get(0).getPosto().getClasse());
        assertFalse(r.getTappe().get(0).getPosto().hasGuardia());
        assertEquals("Il gruppo chiede in giro.", r.getTappe().get(0).getScena().getNarrazione());
        assertEquals("Qualcuno sa.", r.getTappe().get(0).getScena().getBattutaDelCapo());
        assertEquals("Io sento le chiacchiere.", r.getTappe().get(0).getScena().getBattutaDiUnCompagno().get());
        assertSame(ClasseDiPosto.ROVINE, r.getArrivo().getPosto().getClasse());
        assertSame(TipoPersonaggio.SCHELETRO, r.getArrivo().getPosto().getGuardia().get());
        assertEquals(2, r.getArrivo().getPosto().getNumero());
        assertEquals(2, r.getNumeroDiPosti());
        assertTrue(r.getTappe().get(0).getInformazione().contains(TappeRichieste.A_PROSSIMO));
    }

    @Test
    void ilCompagnoENelleScenePuoMancare() {
        TappeRichieste r = TappeRichieste.da(BIVIO);
        assertFalse(r.getBivio().get().getPistaA().getScena().getBattutaDiUnCompagno().isPresent());
    }

    @Test
    void laSceltaFraDuePisteHaUnaGiustaEUnVicoloCieco() {
        TappeRichieste r = TappeRichieste.da(BIVIO);
        TappeRichieste.Bivio bivio = r.getBivio().get();
        assertSame(bivio.getPistaA(), bivio.getPistaGiusta());
        assertSame(bivio.getPistaB(), bivio.getPistaFalsa());
        assertTrue(bivio.getPistaFalsa().getInformazione().contains(TappeRichieste.A_ALTRA));
        assertTrue(bivio.getPistaFalsa().getPosto().hasGuardia(), "anche il vicolo cieco può avere una guardia");
        assertEquals(1 + 2 + 1, r.getNumeroDiPosti());
    }

    @Test
    void unaFarsaHaUnSecondoScambioEAlPiuDuePosti() {
        TappeRichieste r = TappeRichieste.da(FARSA);
        assertTrue(r.isFarsa());
        assertEquals("Che cos'è una macchina?", r.getSecondaBattutaDelCapo().get());
        assertTrue(r.getUltimaRisposta().contains(TappeRichieste.DA_PROSSIMO));
        assertTrue(r.getTappe().isEmpty());
        assertEquals(1, r.getNumeroDiPosti());
        assertRifiutata(FARSA.replace("ARRIVO=", "TAPPA_1=CITTA:Un oste: %A_PROSSIMO%.;SCENA_1=Il gruppo chiede./Sapete?;"
                + "TAPPA_2=LOCANDA:Un avventore: %A_PROSSIMO%.;SCENA_2=Il gruppo ascolta./Cosa?;ARRIVO="));
    }

    @Test
    void leTreChiaviHannoTreCustodiEUnoPuoVolerUnoScambio() {
        TappeRichieste r = TappeRichieste.da(CHIAVI);
        assertTrue(r.hasChiavi());
        assertEquals(3, r.getCustodi().size());
        assertEquals(3, r.getOggettiChiave().size());
        Custode ladri = r.getCustodi().get(0);
        assertSame(TipoPersonaggio.LADRO, ladri.getNemico().get());
        assertTrue(ladri.isConCapo());
        assertFalse(r.getCustodi().get(1).isConCapo());
        Custode scambio = r.getCustodi().get(2);
        assertTrue(scambio.isInScambio());
        assertFalse(scambio.getNemico().isPresent());
        assertEquals("il medaglione", scambio.getScambio().get().getOggetto());
        assertSame(TipoPersonaggio.SCHELETRO, scambio.getScambio().get().getPosto().getGuardia().get());
        assertTrue(scambio.getScambio().get().getTesto().startsWith("Il medaglione"));
    }

    @Test
    void ilLeggendarioSiDichiaraConOggetto() {
        assertTrue(TappeRichieste.da(PERSONA.replace("TIPO=RINTRACCIAMENTO;", "TIPO=CACCIA_AL_TESORO;OGGETTO=LEGGENDARIO;")).isDiUnLeggendario());
        assertRifiutata(PERSONA.replace("TIPO=RINTRACCIAMENTO;", "TIPO=CACCIA_AL_TESORO;OGGETTO=ARMA;"));
    }

    @Test
    void leRigheSbagliateSiRifiutano() {
        assertRifiutata(PERSONA + ";CAMPO_INVENTATO=1");
        assertRifiutata(PERSONA.replace("CHIAVE=PROVA;", ""));
        assertRifiutata(PERSONA.replace("MONETE=40", "MONETE=0"));
        assertRifiutata(PERSONA.replace("TIPO=RINTRACCIAMENTO", "TIPO=INVENTATO"));
        assertRifiutata(PERSONA.replace("ASPETTO=LOCANDIERE", "ASPETTO=NANO"));
        assertRifiutata(PERSONA.replace("SCENA_1=Il gruppo chiede in giro./Qualcuno sa./Io sento le chiacchiere.;", ""));
        assertRifiutata(PERSONA.replace("Il gruppo chiede in giro./Qualcuno sa./Io sento le chiacchiere.", "Solo una parte."));
        assertRifiutata(PERSONA.replace("Il gruppo chiede in giro./Qualcuno sa./Io sento le chiacchiere.", "Una//Tre."));
        assertRifiutata(PERSONA.replace("TAPPA_1=CITTA:", "TAPPA_2=CITTA:").replace("SCENA_1=", "SCENA_2="));
        assertRifiutata(PERSONA.replace("TAPPA_1=CITTA:", "TAPPA_1=VILLAGGIO:"));
        assertRifiutata(PERSONA.replace("TAPPA_1=CITTA:", "TAPPA_1=CITTA LADRO 2:"));
        assertRifiutata(PERSONA.replace("ARRIVO=ROVINE SCHELETRO 2:", "ARRIVO=ROVINE DRAGHETTO 2:"));
        assertRifiutata(PERSONA.replace("ARRIVO=ROVINE SCHELETRO 2:", "ARRIVO=ROVINE SCHELETRO 0:"));
        assertRifiutata(PERSONA.replace("ARRIVO=ROVINE SCHELETRO 2:", "ARRIVO=ROVINE SCHELETRO:"));
        assertRifiutata(PERSONA.replace("ARRIVO=ROVINE SCHELETRO 2:Gualtiero è qui, prigioniero.;", ""));
        assertRifiutata(PERSONA.replace("Cominciate %DA_PROSSIMO%.", "Cominciate da qui."));
        assertRifiutata(PERSONA.replace("%A_PROSSIMO%", "%A_QUALCHE_PARTE%"));
    }

    @Test
    void leDuePisteSiControllano() {
        assertRifiutata(BIVIO.replace("GIUSTA=A;", ""));
        assertRifiutata(BIVIO.replace("GIUSTA=A;", "GIUSTA=C;"));
        assertRifiutata(BIVIO.replace("PISTA_B=GROTTA GOBLIN 3", "PISTA_B=BOSCO"));
        assertRifiutata(BIVIO.replace("la strada giusta porta %A_ALTRA%", "la strada giusta è un'altra"));
        assertRifiutata(BIVIO.replace("diretto verso %PROSSIMO%", "diretto verso %A_ALTRA%"));
        assertRifiutata(BIVIO.replace("SCENA_B=La grotta sa di pietra./Attenti.;", ""));
        assertRifiutata(BIVIO.replace("TAPPA_1=CITTA:Due voci: %PISTA_A% oppure %PISTA_B%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa.;", ""));
        assertRifiutata(PERSONA.replace("ARRIVO=", "GIUSTA=A;ARRIVO="));
    }

    @Test
    void leChiaviSiControllano() {
        assertRifiutata(CHIAVI.replace("CHIAVI=3;", "CHIAVI=2;"));
        assertRifiutata(CHIAVI.replace("OGGETTO_CHIAVE_2=il sigillo d'osso", "OGGETTO_CHIAVE_2=la chiave di ferro"));
        assertRifiutata(CHIAVI.replace("SCAMBIO_3=ROVINE SCHELETRO 3:il medaglione/Il medaglione è fra le ossa di una cripta.;", ""));
        assertRifiutata(CHIAVI.replace("SCAMBIO_3=ROVINE SCHELETRO 3:", "SCAMBIO_3=ROVINE:"));
        assertRifiutata(CHIAVI.replace("il medaglione/Il medaglione", "il medaglione e basta"));
        assertRifiutata(CHIAVI.replace("CUSTODE_2=ROVINE SCHELETRO 4 NO:Scheletri senza sonno.;",
                "CUSTODE_2=ROVINE SCAMBIO 0 NO:Vuole un oggetto.;SCAMBIO_2=GROTTA GOBLIN 2:l'accetta/L'accetta è in una grotta.;"));
        assertRifiutata(CHIAVI.replace("CUSTODE_1=BOSCO LADRO 3 SI:", "CUSTODE_1=BOSCO LADRO 3:"));
        assertRifiutata(CHIAVI.replace("CUSTODE_1=BOSCO LADRO 3 SI:", "CUSTODE_1=BOSCO LADRO 0 SI:"));
        assertRifiutata(PERSONA.replace("ARRIVO=", "CUSTODE_1=BOSCO LADRO 3 SI:Il capo.;ARRIVO="));
        assertRifiutata(CHIAVI.replace("ARRIVO=", "PISTA_A=BOSCO:x;ARRIVO="));
    }

    @Test
    void ogniRigaDellaGrammaticaSiLegge() {
        int[] tipi = new int[4];
        int scambi = 0;
        int prigionieri = 0;
        int farse = 0;
        Set<TipoMissione> tipiDiMissione = EnumSet.noneOf(TipoMissione.class);
        for (int i = 0; i < 2000; i++) {
            boolean farsa = i % 3 == 0;
            TappeRichieste r = TappeRichieste.da(ProduttoreDiMissioniCasuale.rigaDiTappe(farsa));
            assertEquals(farsa, r.isFarsa(), r.getRiga());
            assertTrue(r.getTappe().size() <= TappeRichieste.TAPPE_MASSIME);
            assertTrue(r.getMonete() > 0);
            tipiDiMissione.add(r.getTipo());
            if (farsa) {
                farse++;
                assertTrue(r.getNumeroDiPosti() <= TappeRichieste.POSTI_MASSIMI_DI_UNA_FARSA, r.getRiga());
                assertTrue(r.getMonete() <= 30, "le farse pagano poco: " + r.getMonete());
                assertTrue(r.getRichiesta().contains(TappeRichieste.MONETE + " monete"), "la paga è un segnaposto: " + r.getRiga());
                assertFalse(r.getRichiesta().matches(".*\\d+ monete.*"), r.getRiga());
            }
            tipi[r.getBivio().isPresent() ? 0 : r.hasChiavi() ? 1 : r.isDiUnLeggendario() ? 2 : farsa ? 3 : 2] += 1;
            if (r.hasChiavi()) {
                assertEquals(TappeRichieste.CUSTODI, r.getCustodi().size());
                assertTrue(r.getCustodi().stream().filter(Custode::isInScambio).count() <= 1);
                scambi += r.getCustodi().stream().anyMatch(Custode::isInScambio) ? 1 : 0;
            }
            prigionieri += r.getArrivo().getPosto().hasGuardia() && !r.isDiUnLeggendario() && !r.isFarsa() ? 1 : 0;
        }
        assertTrue(tipi[0] > 0, "due piste");
        assertTrue(tipi[1] > 0, "chiavi");
        assertTrue(tipi[2] > 0, "persone e leggendari");
        assertTrue(farse > 600);
        assertTrue(scambi > 0, "almeno una riga con uno scambio");
        assertTrue(prigionieri > 0, "almeno una persona prigioniera");
        assertTrue(tipiDiMissione.containsAll(EnumSet.of(TipoMissione.RINTRACCIAMENTO, TipoMissione.CACCIA_AL_TESORO, TipoMissione.VENDETTA,
                TipoMissione.RECUPERO)), tipiDiMissione.toString());
    }
}
