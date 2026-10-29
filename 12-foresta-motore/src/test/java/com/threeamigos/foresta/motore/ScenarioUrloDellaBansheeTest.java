package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.canti.CantoDelBardo;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.IntermezzoUrloDellaBanshee;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.storie.IlViaggioDellaMoglie;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il bardo della locanda insegna al gruppo l'Urlo della Banshee, la prima volta che entra in una locanda nel bosco dopo
 * aver completato con successo il viaggio della moglie.
 */
class ScenarioUrloDellaBansheeTest {

    private static NonSparateSulPianista pianista() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(NonSparateSulPianista.class::isInstance)
                .map(NonSparateSulPianista.class::cast).findFirst().orElseThrow(AssertionError::new);
    }

    private static IlViaggioDellaMoglie viaggio() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(IlViaggioDellaMoglie.class::isInstance)
                .map(IlViaggioDellaMoglie.class::cast).findFirst().orElseThrow(AssertionError::new);
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

    /** Il gruppo riporta a casa il bardo. */
    private static TipoLocazione riportaACasaIlBardo(PartitaDiTest partita, CoordinateMD locanda) {
        NonSparateSulPianista pianista = pianista();
        Foresta.setPosizioneDelGruppo(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "2");
        pianista.controllaPreLocazione();
        pianista.segnaIntermezzoPassoMostrato("INCARICO");
        pianista.controllaInLocazione();
        TipoLocazione casa = pianista.getCitta();
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(casa));
        pianista.controllaPreLocazione();
        pianista.segnaIntermezzoPassoMostrato("VIAGGIO");
        pianista.controllaInLocazione();
        assertTrue(pianista.isCompleta());
        return casa;
    }

    /** Il gruppo accompagna la moglie fino all'altra città, a fine viaggio. */
    private static void accompagnaLaMoglie(PartitaDiTest partita, CoordinateMD locanda) {
        IlViaggioDellaMoglie viaggio = viaggio();
        Foresta.setPosizioneDelGruppo(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "1");
        viaggio.controllaPreLocazione();
        viaggio.segnaIntermezzoPassoMostrato("INCARICO");
        viaggio.controllaInLocazione();
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(viaggio.getCittaDiPartenza()));
        viaggio.controllaPreLocazione();
        viaggio.segnaIntermezzoPassoMostrato("PRELEVAMENTO");
        viaggio.controllaInLocazione();
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(viaggio.getCittaDiArrivo()));
        viaggio.controllaPreLocazione();
        viaggio.controllaInLocazione();
        assertTrue(viaggio.isCompleta());
    }

    private static Intermezzo prossimoIntermezzoNellaLocanda(PartitaDiTest partita, CoordinateMD locanda) {
        Foresta.setPosizioneDelGruppo(locanda);
        RegistroIntermezzi.nuovoMomento();
        return RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE);
    }

    @Test
    void ilBardoInsegnaL_UrloDopoIlViaggioDellaMoglie() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(160)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            CoordinateMD locanda = unaLocandaNelBosco();
            assertFalse(CantoDelBardo.isUrloDellaBansheeAppreso());
            // La locanda è già stata visitata: alla prima visita scatta il benvenuto, che ha la precedenza
            Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);

            // Prima del viaggio non scatta, nemmeno dopo aver riportato a casa il bardo
            Intermezzo prima = prossimoIntermezzoNellaLocanda(partita, locanda);
            assertFalse(prima instanceof IntermezzoUrloDellaBanshee);
            riportaACasaIlBardo(partita, locanda);
            Intermezzo dopoIlPianista = prossimoIntermezzoNellaLocanda(partita, locanda);
            assertFalse(dopoIlPianista instanceof IntermezzoUrloDellaBanshee);

            accompagnaLaMoglie(partita, locanda);

            // In città no: scatta solo in una locanda nel bosco
            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            RegistroIntermezzi.nuovoMomento();
            assertFalse(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE) instanceof IntermezzoUrloDellaBanshee);

            // La prima volta in una locanda nel bosco, dopo il viaggio, il bardo insegna il canto
            Intermezzo intermezzo = prossimoIntermezzoNellaLocanda(partita, locanda);
            assertInstanceOf(IntermezzoUrloDellaBanshee.class, intermezzo, String.valueOf(intermezzo));
            assertFalse(intermezzo.isDiRipiego(), "non è un intermezzo di ripiego");
            assertEquals(TipoIntermezzo.INTERMEZZO_URLO_DELLA_BANSHEE.name(), intermezzo.getId());
            assertFalse(intermezzo.getPagine().isEmpty(), "la scena si costruisce");

            RegistroIntermezzi.segnaScattato(intermezzo);

            assertTrue(CantoDelBardo.isUrloDellaBansheeAppreso());
            Bardo bardo = new Bardo("Orfeo", 5);
            bardo.addMagia(1000);
            assertTrue(CantoDelBardo.cantiPossibili(bardo).contains(TipoCanto.URLO_DELLA_BANSHEE));
            // Una volta sola
            assertFalse(prossimoIntermezzoNellaLocanda(partita, locanda) instanceof IntermezzoUrloDellaBanshee);
        }
    }

    @Test
    void seIlViaggioFallisceIlBardoNonInsegnaNiente() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(161)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            CoordinateMD locanda = unaLocandaNelBosco();
            Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);
            riportaACasaIlBardo(partita, locanda);
            IlViaggioDellaMoglie viaggio = viaggio();
            Foresta.setPosizioneDelGruppo(locanda);
            Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locande.LOCANDA_VISITE, "1");
            viaggio.controllaPreLocazione();
            viaggio.segnaIntermezzoPassoMostrato("INCARICO");
            viaggio.controllaInLocazione();
            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(viaggio.getCittaDiPartenza()));
            viaggio.controllaPreLocazione();
            viaggio.segnaIntermezzoPassoMostrato("PRELEVAMENTO");
            viaggio.controllaInLocazione();
            Foresta.setCittaDistrutta(viaggio.getCittaDiArrivo());
            viaggio.controllaPreLocazione();
            assertTrue(viaggio.isFallita());

            assertFalse(prossimoIntermezzoNellaLocanda(partita, locanda) instanceof IntermezzoUrloDellaBanshee);
            assertFalse(CantoDelBardo.isUrloDellaBansheeAppreso());
        }
    }
}
