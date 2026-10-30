package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.MissioneATappe;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La missione a tappe: la pista parte da una città, passa a un'altra e finisce in un posto con la guardia; poi si torna a riscuotere.
 */
class ScenarioMissioneATappeTest {

    private static final String PERSONA = "CHIAVE=PROVA;TIPO=RINTRACCIAMENTO;ASPETTO=LOCANDIERE;MANDANTE=il locandiere;MONETE=40;"
            + "TITOLO=Sulle tracce di Gualtiero;RICHIESTA=Gualtiero è sparito.;BATTUTA=Da dove cominciamo?;"
            + "RISPOSTA=Cominciate %DA_PROSSIMO%.;"
            + "TAPPA_1=CITTA:Qualcuno lo ha visto: è andato %A_PROSSIMO%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa./Io sento le chiacchiere.;"
            + "ARRIVO=ROVINE SCHELETRO 2:Gualtiero è qui, prigioniero.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui fu trovato Gualtiero.";

    private static final String FARSA = "CHIAVE=SGARBO_DI_Ubaldo;TIPO=VENDETTA;ASPETTO=QUALUNQUE;MANDANTE=un mugnaio;MONETE=20;"
            + "TITOLO=Una questione di portiera;RICHIESTA=Dovete andare a punire Ubaldo. Vi darò %MONETE% monete.;BATTUTA=Che cosa vi ha fatto?;"
            + "RISPOSTA=In sogno mi ha rigato la portiera della macchina.;BATTUTA_2=Che cos'è una macchina?;"
            + "RISPOSTA_2=Non lo so, l'ho sognata. Cominciate %DA_PROSSIMO%.;"
            + "ARRIVO=BOSCO:Ubaldo è qui, e ripara una staccionata.;RINGRAZIAMENTO=La portiera è a posto.;RICORDO=Qui Ubaldo fu punito.";

    @Test
    void dallaCittaAlleRovineEPoiDalMandante() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(401)) {
            MissioneATappe missione = prendiLIncarico(partita, PERSONA);
            assertEquals("TAPPA_1", missione.getPassoCorrente());
            assertTrue(missione.getDescrizione().contains("Sulle tracce di Gualtiero"), missione.getDescrizione());

            // La prima città è segnata sulla mappa e non è quella del mandante
            CoordinateMD prima = coordinate(missione, 1);
            assertNotEquals(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA), prima);
            assertTrue(Foresta.isLocazioneConosciuta(prima));

            // Altrove non succede niente
            missione.controllaPreLocazione();
            assertEquals("TAPPA_1", missione.getPassoCorrente());

            Foresta.setPosizioneDelGruppo(prima);
            missione.controllaPreLocazione();
            missione.segnaIntermezzoPassoMostrato("TAPPA_1");
            missione.controllaInLocazione();
            assertEquals("ARRIVO", missione.getPassoCorrente());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("Qualcuno lo ha visto: è andato")), String.valueOf(partita.testi()));
            CoordinateMD rovine = coordinate(missione, 2);
            assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(rovine));
            assertTrue(Foresta.isLocazioneConosciuta(rovine));

            Foresta.setPosizioneDelGruppo(rovine);
            assertEquals(2, RegistroMissioni.getIncontroMissione(rovine).orElseThrow(AssertionError::new).size());
            for (int i = 0; i < 2; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.SCHELETRO));
            }
            missione.controllaPostLocazione();
            assertEquals("RITORNO", missione.getPassoCorrente());

            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            int monete = Cassa.getMonete();
            missione.controllaPreLocazione();
            missione.segnaIntermezzoPassoMostrato("RITORNO");
            missione.controllaInLocazione();
            assertTrue(missione.isCompleta());
            assertTrue(Cassa.getMonete() > monete, "la paga è stata versata");
            assertEquals("Qui fu trovato Gualtiero.", missione.getRicordoDellaLocazione());
        }
    }

    private static final String BIVIO = "CHIAVE=PROVA;TIPO=RINTRACCIAMENTO;ASPETTO=LOCANDIERE;MANDANTE=il locandiere;MONETE=40;"
            + "TITOLO=Due piste;RICHIESTA=Gualtiero è sparito.;BATTUTA=Da dove cominciamo?;RISPOSTA=Cominciate %DA_PROSSIMO%.;"
            + "TAPPA_1=CITTA:Due voci: %PISTA_A% oppure %PISTA_B%.;SCENA_1=Il gruppo chiede in giro./Qualcuno sa.;"
            + "PISTA_A=BOSCO:Un cacciatore ricorda un uomo diretto verso %PROSSIMO%.;SCENA_A=Il bosco è fitto./Cerchiamo segni.;"
            + "PISTA_B=GROTTA GOBLIN 3:Una grotta vuota: la strada giusta porta %A_ALTRA%.;SCENA_B=La grotta sa di pietra./Attenti.;GIUSTA=A;"
            + "ARRIVO=RADURA:Gualtiero è qui.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui fu trovato Gualtiero.";

    @Test
    void ilBivioPortaPrimaNelVicoloCiecoEPoiNellaPistaGiusta() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(404)) {
            MissioneATappe missione = prendiLIncarico(partita, BIVIO);
            Foresta.setPosizioneDelGruppo(coordinate(missione, 1));
            missione.controllaPreLocazione();
            missione.segnaIntermezzoPassoMostrato("TAPPA_1");
            missione.controllaInLocazione();
            assertEquals("BIVIO", missione.getPassoCorrente());
            CoordinateMD giusta = coordinate(missione, 2);
            CoordinateMD falsa = coordinate(missione, 3);
            assertEquals(TipoLocazione.BOSCO, Foresta.getLocazione(giusta));
            assertEquals(TipoLocazione.GROTTA, Foresta.getLocazione(falsa));
            assertTrue(Foresta.isLocazioneConosciuta(giusta) && Foresta.isLocazioneConosciuta(falsa));
            assertTrue(missione.getDescrizione().contains("Le piste sono due"), missione.getDescrizione());

            // Il vicolo cieco: la grotta, con i suoi goblin
            Foresta.setPosizioneDelGruppo(falsa);
            missione.controllaPreLocazione();
            assertEquals("PISTA_B", missione.getPassoCorrente());
            assertEquals(3, RegistroMissioni.getIncontroMissione(falsa).orElseThrow(AssertionError::new).size());
            for (int i = 0; i < 3; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            }
            missione.controllaPostLocazione();
            assertEquals("BIVIO", missione.getPassoCorrente());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("la strada giusta porta a")), String.valueOf(partita.testi()));
            assertFalse(missione.isFallita());

            // La pista giusta
            Foresta.setPosizioneDelGruppo(giusta);
            missione.controllaPreLocazione();
            assertEquals("PISTA_A", missione.getPassoCorrente());
            missione.controllaPostLocazione();
            assertEquals("ARRIVO", missione.getPassoCorrente());
            assertEquals(TipoLocazione.RADURA, Foresta.getLocazione(coordinate(missione, 4)));
        }
    }

    private static final String CHIAVI = "CHIAVE=PROVA;TIPO=CACCIA_AL_TESORO;ASPETTO=MAGO;MANDANTE=il mago;MONETE=60;TITOLO=Le tre chiavi;"
            + "RICHIESTA=Una volta antica si apre con tre chiavi.;BATTUTA=E dove sono le chiavi?;RISPOSTA=Cominciate %DA_PROSSIMO%.;"
            + "TAPPA_1=ROVINE:Una lapide dice i tre luoghi: %POSTO_1%, %POSTO_2% e %POSTO_3%.;SCENA_1=Il gruppo si muove fra i muri./Cerchiamo.;"
            + "CHIAVI=3;OGGETTO_CHIAVE_1=la chiave di ferro;OGGETTO_CHIAVE_2=il sigillo d'osso;OGGETTO_CHIAVE_3=la chiave a tre denti;"
            + "CUSTODE_1=BOSCO LADRO 3 SI:Il capo %CAPO% porta la chiave al collo.;CUSTODE_2=ROVINE SCHELETRO 4 NO:Scheletri senza sonno.;"
            + "CUSTODE_3=RADURA SCAMBIO 0 NOME_CAMPIONE:Il cavaliere %CAPO% vuole un medaglione.;"
            + "SCAMBIO_3=ROVINE SCHELETRO 3:il medaglione/Il medaglione è fra le ossa di una cripta.;"
            + "ARRIVO=TEMPIO:Dietro l'altare c'è la volta.;RINGRAZIAMENTO=Grazie.;RICORDO=Qui si aprì la volta.";

    @Test
    void leTreChiaviSiPrendonoInOrdineLibero() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(405)) {
            MissioneATappe missione = prendiLIncarico(partita, CHIAVI);
            Foresta.setPosizioneDelGruppo(coordinate(missione, 1));
            missione.controllaPreLocazione();
            missione.segnaIntermezzoPassoMostrato("TAPPA_1");
            missione.controllaInLocazione();
            assertEquals("CHIAVI", missione.getPassoCorrente());
            missione.controllaPostLocazione();
            assertEquals("ATTESA_DELLE_CHIAVI", missione.getPassoCorrente());
            assertEquals(3, missione.getMissioniAffidate("CHIAVI").size());
            assertTrue(missione.getDescrizione().contains("(ne hai 0)"), missione.getDescrizione());
            assertFalse(partita.testi().stream().anyMatch(t -> t.contains("%")), String.valueOf(partita.testi()));

            // Il custode in scambio per primo: chiede, l'oggetto è nelle rovine, si riporta
            CoordinateMD radura = coordinate(missione, 4);
            CoordinateMD bosco = coordinate(missione, 2);
            CoordinateMD rovine = coordinate(missione, 3);
            CoordinateMD cripta = coordinate(missione, 5);
            assertEquals(TipoLocazione.RADURA, Foresta.getLocazione(radura));
            assertEquals(TipoLocazione.BOSCO, Foresta.getLocazione(bosco));
            assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(rovine));
            assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(cripta));
            assertEquals(4, missione.getMissioniAffidate("CHIAVI").size() + 1);

            Foresta.setPosizioneDelGruppo(radura);
            pre(missione);
            dentro(missione);
            Foresta.setPosizioneDelGruppo(cripta);
            assertEquals(3, RegistroMissioni.getIncontroMissione(cripta).orElseThrow(AssertionError::new).size());
            for (int i = 0; i < 3; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.SCHELETRO));
            }
            post(missione);
            Foresta.setPosizioneDelGruppo(radura);
            pre(missione);
            dentro(missione);
            assertTrue(missione.getDescrizione().contains("(ne hai 1)"), missione.getDescrizione());

            // Il bosco con i ladri
            Foresta.setPosizioneDelGruppo(bosco);
            assertEquals(3, RegistroMissioni.getIncontroMissione(bosco).orElseThrow(AssertionError::new).size() - 1 + 1);
            for (int i = 0; i < 3; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.LADRO));
            }
            post(missione);
            // Le rovine con gli scheletri
            Foresta.setPosizioneDelGruppo(rovine);
            for (int i = 0; i < 4; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.SCHELETRO));
            }
            post(missione);
            post(missione);
            assertEquals("ARRIVO", missione.getPassoCorrente());
            CoordinateMD tempio = coordinate(missione, 6);
            assertEquals(TipoLocazione.TEMPIO, Foresta.getLocazione(tempio));

            Foresta.setPosizioneDelGruppo(tempio);
            pre(missione);
            dentro(missione);
            assertEquals("RITORNO", missione.getPassoCorrente());
        }
    }

    @Test
    void ogniRigaDellaGrammaticaSiOffreESiDescrive() {
        for (int i = 0; i < 120; i++) {
            String riga = ProduttoreDiMissioniCasuale.rigaDiTappe(i % 3 == 0);
            try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(500 + i)) {
                MissioneATappe missione = prendiLIncarico(partita, riga);
                assertNotNull(missione.getNome());
                assertFalse(missione.getDescrizione().contains("%"), riga + " -> " + missione.getDescrizione());
                assertFalse(partita.testi().stream().anyMatch(t -> t.contains("%")), riga + " -> " + partita.testi());
                assertFalse(missione.isFallita(), riga);
            }
        }
    }

    @Test
    void laFarsaHaUnSoloPostoESiRiscuoteLaPagaVera() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(402)) {
            MissioneATappe missione = prendiLIncarico(partita, FARSA);
            assertEquals("ARRIVO", missione.getPassoCorrente());
            CoordinateMD bosco = coordinate(missione, 1);
            assertTrue(Foresta.isLocazioneConosciuta(bosco));
            assertFalse(partita.testi().stream().anyMatch(t -> t.contains("%")), String.valueOf(partita.testi()));

            Foresta.setPosizioneDelGruppo(bosco);
            missione.controllaPreLocazione();
            missione.controllaInLocazione();
            assertEquals("RITORNO", missione.getPassoCorrente());

            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            missione.controllaPreLocazione();
            missione.segnaIntermezzoPassoMostrato("RITORNO");
            missione.controllaInLocazione();
            assertTrue(missione.isCompleta());
        }
    }

    @Test
    void laMissioneRicordaLaRigaDopoIlSalvataggio() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(403)) {
            MissioneATappe missione = prendiLIncarico(partita, PERSONA);
            assertEquals("Sulle tracce di Gualtiero", missione.getNome());
            assertEquals(PERSONA, missione.getRichiesta().getRiga());
        }
    }

    private static void pre(MissioneATappe madre) {
        madre.controllaPreLocazione();
        madre.getMissioniAffidate("CHIAVI").forEach(com.threeamigos.foresta.missioni.Missione::controllaPreLocazione);
    }

    private static void dentro(MissioneATappe madre) {
        madre.controllaInLocazione();
        madre.getMissioniAffidate("CHIAVI").forEach(com.threeamigos.foresta.missioni.Missione::controllaInLocazione);
    }

    private static void post(MissioneATappe madre) {
        madre.controllaPostLocazione();
        madre.getMissioniAffidate("CHIAVI").forEach(com.threeamigos.foresta.missioni.Missione::controllaPostLocazione);
    }

    private static CoordinateMD coordinate(MissioneATappe missione, int posto) {
        String[] parti = missione.ottieniProprieta("POSTO_" + posto).split(",");
        return new CoordinateMD(Integer.parseInt(parti[0]), Integer.parseInt(parti[1]));
    }

    private static MissioneATappe prendiLIncarico(PartitaDiTest partita, String riga) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        MissioneATappe missione = RegistroMissioni.getTutteLeMissioni().stream().filter(MissioneATappe.class::isInstance)
                .map(MissioneATappe.class::cast).findFirst().orElseThrow(AssertionError::new);
        missione.aggiungiProprieta("PARAMETRO_RIGA", riga);
        missione.controllaPreLocazione();
        missione.segnaIntermezzoPassoMostrato("INCARICO");
        missione.controllaInLocazione();
        assertTrue(missione.isAttiva());
        return missione;
    }
}
