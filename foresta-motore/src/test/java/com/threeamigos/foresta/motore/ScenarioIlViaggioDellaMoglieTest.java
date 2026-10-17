package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.IntermezzoLocandaVisitaSuccessiva;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.storie.IlViaggioDellaMoglie;
import com.threeamigos.foresta.missioni.IntermezzoDiPasso;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.missioni.RegistroMissioni;

/**
 * Il viaggio della moglie: dopo aver riportato a casa il bardo (vedi NonSparateSulPianista), rientrando in una locanda
 * il bardo chiede al gruppo di accompagnare sua moglie dalla città di prima a un'altra città. La moglie viaggia come
 * ospite non vulnerabile.
 */
class ScenarioIlViaggioDellaMoglieTest {

    private static NonSparateSulPianista pianista() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(NonSparateSulPianista.class::isInstance)
                .map(NonSparateSulPianista.class::cast).findFirst().orElseThrow(AssertionError::new);
    }

    private static IlViaggioDellaMoglie viaggio() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(IlViaggioDellaMoglie.class::isInstance)
                .map(IlViaggioDellaMoglie.class::cast).findFirst().orElseThrow(AssertionError::new);
    }

    private static long viaggi() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(IlViaggioDellaMoglie.class::isInstance).count();
    }

    private static CoordinateMD unaLocandaNelBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.LOCANDA) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna locanda nel bosco");
    }

    /**
     * Il gruppo riporta a casa il bardo, come in ScenarioNonSparateSulPianistaTest: la missione del pianista è completata.
     */
    private static TipoLocazione riportaACasaIlBardo(PartitaDiTest partita, CoordinateMD locanda) {
        NonSparateSulPianista pianista = pianista();
        partita.gruppo().setCoordinate(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locanda.LOCANDA_VISITE, "2");
        pianista.controllaPreLocazione();
        pianista.segnaIntermezzoPassoMostrato("INCARICO");
        pianista.controllaInLocazione();
        assertEquals("VIAGGIO", pianista.getPassoCorrente());
        TipoLocazione casa = pianista.getCitta();
        partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(casa));
        pianista.controllaPreLocazione();
        pianista.segnaIntermezzoPassoMostrato("VIAGGIO");
        pianista.controllaInLocazione();
        assertTrue(pianista.isCompleta());
        return casa;
    }

    private static void rientraNellaLocanda(PartitaDiTest partita, CoordinateMD locanda, int visitePrecedenti) {
        partita.gruppo().setCoordinate(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locanda.LOCANDA_VISITE, String.valueOf(visitePrecedenti));
        viaggio().controllaPreLocazione();
    }

    @Test
    void dopoIlPianistaIlBardoAffidaLaMoglieEIlGruppoLaAccompagnaInUnAltraCitta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(153)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            CoordinateMD locanda = unaLocandaNelBosco();
            IlViaggioDellaMoglie viaggio = viaggio();
            String moglie = RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome();
            assertEquals("Il viaggio di " + moglie, viaggio.getNome());

            // Finché il bardo non è a casa il viaggio non parte, anche rientrando in una locanda già vista
            rientraNellaLocanda(partita, locanda, 3);
            assertFalse(viaggio.isAttiva());
            assertNull(viaggio.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));

            TipoLocazione partenza = riportaACasaIlBardo(partita, locanda);

            // Alla prima visita alla locanda scatta il benvenuto, non il viaggio
            rientraNellaLocanda(partita, locanda, 0);
            assertNull(viaggio.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertNull(viaggio.getCittaDiPartenza());

            // Rientrando (non è la prima visita) il bardo ringrazia e chiede
            rientraNellaLocanda(partita, locanda, 1);
            assertEquals("INCARICO", viaggio.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertEquals(partenza, viaggio.getCittaDiPartenza(), "parte dalla città dove il gruppo ha riportato il bardo");
            assertNotNull(viaggio.getCittaDiArrivo());
            assertNotEquals(partenza, viaggio.getCittaDiArrivo());
            assertFalse(new IntermezzoDiPasso(viaggio, "INCARICO").getPagine().isEmpty(), "la scena del bardo si costruisce");
            viaggio.segnaIntermezzoPassoMostrato("INCARICO");
            viaggio.controllaInLocazione();
            assertTrue(viaggio.isAttiva());
            assertEquals("PRELEVAMENTO", viaggio.getPassoCorrente());
            assertTrue(viaggio.getDescrizione().contains(moglie), viaggio.getDescrizione());

            // Nella città di partenza la moglie si unisce al gruppo, e non si può ferire
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(partenza));
            viaggio.controllaPreLocazione();
            assertEquals("PRELEVAMENTO", viaggio.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertFalse(new IntermezzoDiPasso(viaggio, "PRELEVAMENTO").getPagine().isEmpty(), "la scena della moglie si costruisce");
            viaggio.segnaIntermezzoPassoMostrato("PRELEVAMENTO");
            viaggio.controllaInLocazione();
            assertEquals("VIAGGIO", viaggio.getPassoCorrente());
            Personaggio signora = viaggio.getScortato().orElseThrow(AssertionError::new);
            assertEquals(TipoPersonaggio.MOGLIE_DEL_BARDO, signora.getClasse());
            assertEquals(moglie, signora.getNome());
            assertFalse(partita.gruppo().isOspiteVulnerabile(signora));
            for (int i = 0; i < 500; i++) {
                assertTrue(partita.gruppo().getOspitiBersaglio().isEmpty(), "un avversario ha attaccato la moglie del bardo");
            }

            // All'arrivo paga e si separa dal gruppo
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(viaggio.getCittaDiArrivo()));
            int monete = partita.gruppo().getMonete();
            viaggio.controllaPreLocazione();
            viaggio.controllaInLocazione();
            assertTrue(viaggio.isCompleta());
            assertEquals(monete + 25, partita.gruppo().getMonete());
            assertTrue(partita.gruppo().getOspiti().isEmpty(), "la moglie è arrivata");
            assertEquals(1, viaggi(), "il viaggio si fa una volta sola: completato, non ne lascia uno nuovo");
        }
    }

    @Test
    void siFallisceSeLaCittaDiArrivoVieneDistrutta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(154)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            CoordinateMD locanda = unaLocandaNelBosco();
            riportaACasaIlBardo(partita, locanda);
            IlViaggioDellaMoglie viaggio = viaggio();
            rientraNellaLocanda(partita, locanda, 1);
            viaggio.segnaIntermezzoPassoMostrato("INCARICO");
            viaggio.controllaInLocazione();
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(viaggio.getCittaDiPartenza()));
            viaggio.controllaPreLocazione();
            viaggio.segnaIntermezzoPassoMostrato("PRELEVAMENTO");
            viaggio.controllaInLocazione();
            assertEquals("VIAGGIO", viaggio.getPassoCorrente());

            LineaTemporale.setCittaDistrutta(viaggio.getCittaDiArrivo());
            viaggio.controllaPreLocazione();
            assertTrue(viaggio.isFallita());
            assertTrue(partita.gruppo().getOspiti().isEmpty());
            assertEquals(1, viaggi(), "il viaggio si fa una volta sola: fallito, non ne lascia uno nuovo");
        }
    }

    @Test
    void laVisitaSuccessivaAllaLocandaAspettaCheIlViaggioAbbiaParlato() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(155)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
            CoordinateMD locanda = unaLocandaNelBosco();
            riportaACasaIlBardo(partita, locanda);
            IlViaggioDellaMoglie viaggio = viaggio();

            LocazioneMD sede = Foresta.getLocazioneMD(locanda);
            sede.aggiungiProprieta(Locanda.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);
            rientraNellaLocanda(partita, locanda, 1);
            assertEquals("INCARICO", viaggio.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));

            // Il viaggio ha la precedenza: la visita successiva (di ripiego) non scatta, e aspetta
            RegistroIntermezzi.nuovoMomento();
            Intermezzo primo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE);
            assertNotNull(primo);
            assertFalse(primo instanceof IntermezzoLocandaVisitaSuccessiva, "la visita successiva dovrebbe aspettare: " + primo.getId());

            // Mostrato quello del viaggio, nella visita seguente non c'è altro e la visita successiva si vede
            viaggio.segnaIntermezzoPassoMostrato("INCARICO");
            RegistroIntermezzi.nuovoMomento();
            Intermezzo dopo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE);
            assertNotNull(dopo);
            assertTrue(dopo instanceof IntermezzoLocandaVisitaSuccessiva, String.valueOf(dopo.getId()));
        }
    }
}
