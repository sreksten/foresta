package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.NonSparateSulPianista;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Non sparate sul pianista: alla terza visita a una locanda il locandiere affida al gruppo il bardo Ugolino, ubriaco,
 * da riportare a casa nella città più vicina.
 */
class ScenarioNonSparateSulPianistaTest {

    private static NonSparateSulPianista pianista() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(NonSparateSulPianista.class::isInstance)
                .map(NonSparateSulPianista.class::cast).findFirst().orElseThrow(AssertionError::new);
    }

    private static CoordinateMD unaLocandaNelBosco() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == ClassiLocazione.LOCANDA) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna locanda nel bosco");
    }

    /**
     * Il gruppo entra nella locanda, che ha già visitato tante volte; la missione si controlla come all'ingresso.
     */
    private static void entraNellaLocanda(PartitaDiTest partita, NonSparateSulPianista pianista, CoordinateMD locanda, int visitePrecedenti) {
        partita.gruppo().setCoordinate(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locanda.LOCANDA_VISITE, String.valueOf(visitePrecedenti));
        pianista.controllaPreLocazione();
        if ("ACCETTAZIONE".equals(pianista.getPassoCorrente())) {
            pianista.segnaIntermezzoPassoMostrato("INCARICO");
            pianista.controllaInLocazione();
        }
    }

    @Test
    void allaTerzaVisitaUgolinoSiAffidaAlGruppoEArrivaACasa() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(151)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = pianista();
            CoordinateMD locanda = unaLocandaNelBosco();

            entraNellaLocanda(partita, pianista, locanda, 1);
            assertFalse(pianista.isAttiva(), "alla seconda visita il locandiere non chiede niente");

            entraNellaLocanda(partita, pianista, locanda, 2);
            assertTrue(pianista.isAttiva());
            assertEquals("VIAGGIO", pianista.getPassoCorrente());
            Personaggio ugolino = pianista.getScortato().orElseThrow(AssertionError::new);
            assertEquals(NonSparateSulPianista.UGOLINO, ugolino.getNome());
            assertEquals(ClassePersonaggio.BARDO, ugolino.getClasse());
            assertTrue(partita.gruppo().isOspiteVulnerabile(ugolino));

            // La città è la più vicina alla locanda
            ClassiLocazione citta = pianista.getCitta();
            assertNotNull(citta);
            CoordinateMD casa = Foresta.getCoordinateLocazioneUnica(citta);
            for (ClassiLocazione altra : ClassiLocazione.values()) {
                CoordinateMD coordinate = altra.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA
                        ? Foresta.getCoordinateLocazioneUnica(altra) : null;
                if (coordinate != null) {
                    assertTrue(distanza(locanda, casa) <= distanza(locanda, coordinate), altra + " è più vicina di " + citta);
                }
            }

            partita.gruppo().setCoordinate(casa);
            int monete = partita.gruppo().getMonete();
            pianista.controllaPreLocazione();
            assertEquals("VIAGGIO", pianista.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertTrue(partita.gruppo().getOspiti().isEmpty(), "Ugolino è a casa");
            pianista.segnaIntermezzoPassoMostrato("VIAGGIO");
            pianista.controllaInLocazione();
            assertEquals(monete + 20, partita.gruppo().getMonete());
            assertTrue(pianista.isCompleta());
        }
    }

    @Test
    void seUgolinoMuorePerStradaLaMissioneFallisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(152)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = pianista();
            entraNellaLocanda(partita, pianista, unaLocandaNelBosco(), 2);
            Personaggio ugolino = pianista.getScortato().orElseThrow(AssertionError::new);

            ugolino.muore("un lupo affamato");
            pianista.controllaPreLocazione();
            assertTrue(pianista.isFallita());
            assertTrue(partita.gruppo().getOspiti().isEmpty());
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("Ugolino il bardo non ce l'ha fatta")), String.valueOf(partita.testi()));
        }
    }

    private static int distanza(CoordinateMD a, CoordinateMD b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
