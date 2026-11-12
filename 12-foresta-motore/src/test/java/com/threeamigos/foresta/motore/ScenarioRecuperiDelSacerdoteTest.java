package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.LocandaDelRacconto;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.storie.GliSchinieriDelPellegrino;
import com.threeamigos.foresta.missioni.storie.IlBastoneDelSantone;
import com.threeamigos.foresta.missioni.storie.LaSpadaDellAcquasantiera;
import com.threeamigos.foresta.missioni.storie.RecuperoDelSacerdote;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni di recupero del sacerdote: in città un sacerdote, o una sacerdotessa, racconta la storia di un oggetto sacro finito nel posto
 * sbagliato, il posto si segna sulla mappa, e il gruppo riprende l'oggetto e lo tiene. Si offrono una alla volta, nell'ordine.
 */
class ScenarioRecuperiDelSacerdoteTest {

    private static <T extends RecuperoDelSacerdote> T la(Class<T> classe) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(classe::isInstance).map(classe::cast).findFirst()
                .orElseThrow(AssertionError::new);
    }

    /**
     * La partita comincia in città, con tutte le missioni rimandate tranne quella da provare; le missioni del sacerdote che vengono prima
     * sono già finite.
     */
    private static void inCittaCon(PartitaDiTest partita, Class<? extends RecuperoDelSacerdote> classe) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
            partita.rimandaLeMissioniCheSiOffrono();
            for (Class<? extends RecuperoDelSacerdote> precedente : Arrays.asList(LaSpadaDellAcquasantiera.class, IlBastoneDelSantone.class,
                    GliSchinieriDelPellegrino.class)) {
                if (precedente == classe) {
                    break;
                }
                la(precedente).completaMissione();
            }
            partita.rendiDisponibile(la(classe));
            partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA);
            partita.eventi().ascolta(NotificaPaginaIntermezzo.class);
        });
    }

    /**
     * Esce dalla città ed entra nella casella, venendo da quella accanto.
     */
    private static void entraIn(PartitaDiTest partita, CoordinateMD destinazione) {
        partita.comando(Comando.ESCI_DA_CITTA);
        boolean daSotto = destinazione.getY() + 1 < Foresta.getDimensioneY();
        Foresta.setPosizioneDelGruppo(new CoordinateMD(destinazione.getX(), destinazione.getY() + (daSotto ? 1 : -1)));
        partita.assertStato(Stato.SCELTA_DIREZIONE);
        partita.comando(daSotto ? Comando.NORD : Comando.SUD).comando(Comando.NUMERO_1);
        assertEquals(destinazione, Foresta.getPosizioneDelGruppo());
    }

    /**
     * Gli avversari della casella cadono tutti, e lo scontro finisce.
     */
    private static void vinciLoScontro(PartitaDiTest partita) {
        partita.gruppo().getCapo().getModelloDati().set(TipoAttributo.SALUTE, 1_000_000);
        GruppoAvversario.getIstanza().getPersonaggiVivi().forEach(avversario -> avversario.muore("è caduto"));
        if (partita.stato() != Stato.IN_COMBATTIMENTO) {
            partita.comando(Comando.COMBATTIMENTO);
        }
        for (int giri = 0; partita.stato() == Stato.IN_COMBATTIMENTO && giri < 50; giri++) {
            partita.scatta();
        }
    }

    private static boolean haIlLeggendario(PartitaDiTest partita, RecuperoDelSacerdote missione) {
        String nome = missione.getLeggendario().getNome();
        return partita.gruppo().getInventario().stream().anyMatch(artefatto -> nome.equals(artefatto.getNome()));
    }

    private static void senzaTrattiniBassi(PartitaDiTest partita) {
        assertTrue(partita.testi().stream().noneMatch(t -> t.contains("_")),
                "un testo con un trattino basso: " + partita.testi().stream().filter(t -> t.contains("_")).collect(Collectors.toList()));
    }

    @Test
    void inCittaIlSacerdoteRaccontaDellaSpadaESegnaLaGrottaDeiGoblinLavandai() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(401)) {
            inCittaCon(partita, LaSpadaDellAcquasantiera.class);
            LaSpadaDellAcquasantiera missione = la(LaSpadaDellAcquasantiera.class);
            assertTrue(missione.isAttiva());
            assertEquals("La spada dell'acquasantiera", missione.getNome());
            assertTrue(Arrays.asList(TipoPersonaggio.SACERDOTE, TipoPersonaggio.SACERDOTESSA).contains(missione.getSacerdote()));
            assertFalse(partita.eventi().tutti(NotificaPaginaIntermezzo.class).isEmpty(), "la scena del racconto");
            CoordinateMD grotta = missione.getPostoDellaStoria();
            assertEquals(TipoLocazione.GROTTA, Foresta.getLocazione(grotta));
            assertTrue(Foresta.isLocazioneConosciuta(grotta), "la grotta è sulla mappa");
            List<Personaggio> incontro = missione.getIncontroInLocazione(grotta).orElseThrow(AssertionError::new);
            assertEquals(Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.GOBLIN, TipoPersonaggio.HOBGOBLIN, TipoPersonaggio.GOBLIN,
                    TipoPersonaggio.GOBLIN), incontro.stream().map(Personaggio::getClasse).collect(Collectors.toList()));
            assertEquals(missione.getNomeDelCapo(), incontro.get(2).getNome());
            senzaTrattiniBassi(partita);
        }
    }

    @Test
    void sconfittiIGoblinLaSpadaEDelGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(402)) {
            inCittaCon(partita, LaSpadaDellAcquasantiera.class);
            LaSpadaDellAcquasantiera missione = la(LaSpadaDellAcquasantiera.class);
            entraIn(partita, missione.getPostoDellaStoria());
            vinciLoScontro(partita);
            assertTrue(missione.isCompleta(), partita.stato() + " " + missione.getPassoCorrente() + " " + partita.testi());
            assertTrue(haIlLeggendario(partita, missione));
            assertEquals("SANTA_IGNORANZA", partita.gruppo().getInventario().stream()
                    .filter(artefatto -> missione.getLeggendario().getNome().equals(artefatto.getNome()))
                    .findFirst().orElseThrow(AssertionError::new).getModelloDati().getSetLeggendario());
            senzaTrattiniBassi(partita);
        }
    }

    @Test
    void ilBastoneSiRiportaDalSacerdoteDopoAverSconfittoIFantasmi() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(403)) {
            inCittaCon(partita, IlBastoneDelSantone.class);
            IlBastoneDelSantone missione = la(IlBastoneDelSantone.class);
            assertTrue(missione.isAttiva());
            CoordinateMD rovine = missione.getPostoDellaStoria();
            assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(rovine));
            List<Personaggio> incontro = missione.getIncontroInLocazione(rovine).orElseThrow(AssertionError::new);
            assertEquals(Arrays.asList(TipoPersonaggio.FANTASMA), incontro.stream().map(Personaggio::getClasse).collect(Collectors.toList()));
            assertEquals(missione.getNomeDelCapo(), incontro.get(0).getNome());
            entraIn(partita, rovine);
            vinciLoScontro(partita);
            assertFalse(missione.isCompleta(), "il bastone si consegna tornando dal sacerdote");
            assertFalse(haIlLeggendario(partita, missione));
            // Di ritorno in città, dove il sacerdote ha raccontato la storia
            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            missione.controllaInLocazione();
            assertTrue(missione.isCompleta(), missione.getPassoCorrente());
            assertTrue(haIlLeggendario(partita, missione));
            senzaTrattiniBassi(partita);
        }
    }

    @Test
    void ilPellegrinoSiTrovaNelBoscoESiAccompagnaInCittaDoveLasciaGliSchinieri() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(404)) {
            inCittaCon(partita, GliSchinieriDelPellegrino.class);
            GliSchinieriDelPellegrino missione = la(GliSchinieriDelPellegrino.class);
            assertTrue(missione.isAttiva());
            CoordinateMD bosco = missione.getPostoDellaStoria();
            assertEquals(TipoLocazione.BOSCO, Foresta.getLocazione(bosco));
            entraIn(partita, bosco);
            vinciLoScontro(partita);
            String pellegrino = missione.getNomeDelPellegrino();
            assertTrue(partita.gruppo().getOspiti().stream().anyMatch(ospite -> pellegrino.equals(ospite.getNome())),
                    missione.getPassoCorrente() + " " + partita.testi());
            // In città il pellegrino saluta e lascia gli schinieri
            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            missione.controllaPreLocazione();
            assertTrue(missione.isCompleta(), missione.getPassoCorrente());
            assertTrue(haIlLeggendario(partita, missione));
            assertTrue(partita.gruppo().getOspiti().stream().noneMatch(ospite -> pellegrino.equals(ospite.getNome())));
            senzaTrattiniBassi(partita);
        }
    }

    @Test
    void siOffronoUnaAllaVoltaNellOrdine() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(405)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, partita::rimandaLeMissioniCheSiOffrono);
            assertTrue(la(LaSpadaDellAcquasantiera.class).puoAncoraOffrirsi());
            assertFalse(la(IlBastoneDelSantone.class).puoAncoraOffrirsi(), "prima la spada");
            assertFalse(la(GliSchinieriDelPellegrino.class).puoAncoraOffrirsi(), "prima la spada e il bastone");
            la(LaSpadaDellAcquasantiera.class).fallisciMissione();
            assertTrue(la(IlBastoneDelSantone.class).puoAncoraOffrirsi(), "anche una missione fallita è finita");
            assertFalse(la(GliSchinieriDelPellegrino.class).puoAncoraOffrirsi());
        }
    }

    @Test
    void inLocandaSiOffronoDallaSecondaVisitaDopoIlBenvenuto() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(406)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, partita::rimandaLeMissioniCheSiOffrono);
            CoordinateMD locanda = null;
            for (int y = 0; y < Foresta.getDimensioneY() && locanda == null; y++) {
                for (int x = 0; x < Foresta.getDimensioneX() && locanda == null; x++) {
                    if (Foresta.getLocazione(new CoordinateMD(x, y)) == TipoLocazione.LOCANDA) {
                        locanda = new CoordinateMD(x, y);
                    }
                }
            }
            assertNotNull(locanda, "una locanda nel bosco");
            Foresta.setPosizioneDelGruppo(locanda);
            Foresta.getLocazioneMD(locanda).aggiungiProprieta(com.threeamigos.foresta.intermezzi.Locande.LOCANDA_VISITE, "0");
            assertFalse(LocandaDelRacconto.isQui(), "alla prima visita c'è il benvenuto della locanda");
            Foresta.getLocazioneMD(locanda).aggiungiProprieta(com.threeamigos.foresta.intermezzi.Locande.LOCANDA_VISITE, "1");
            assertTrue(LocandaDelRacconto.isQui(), "alla seconda visita");
            // Se alla prima visita c'era il pianista, il benvenuto è rimandato: alla seconda c'è lui, e il racconto aspetta
            Foresta.getLocazioneMD(locanda).aggiungiProprieta(com.threeamigos.foresta.intermezzi.Locande.LOCANDA_BENVENUTO_RIMANDATO, "S");
            assertFalse(LocandaDelRacconto.isQui(), "prima il benvenuto rimandato");
        }
    }
}
