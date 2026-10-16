package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Non sparate sul pianista: alla terza visita a una locanda il locandiere affida al gruppo un bardo ubriaco,
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
                if (Foresta.getLocazione(x, y) == TipoLocazione.LOCANDA) {
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
    void allaTerzaVisitaIlBardoSiAffidaAlGruppoEArrivaACasa() {
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
            assertEquals(pianista.getBardo(), ugolino.getNome());
            assertEquals(TipoPersonaggio.BARDO_DELLE_LOCANDE, ugolino.getClasse());
            assertEquals(RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome(), ugolino.getNome(), "è il bardo della locanda, con il suo nome");
            assertFalse(partita.gruppo().isOspiteVulnerabile(ugolino), "è troppo ubriaco perché i mostri se ne curino");

            // La città è la più vicina alla locanda
            TipoLocazione citta = pianista.getCitta();
            assertNotNull(citta);
            CoordinateMD casa = Foresta.getCoordinateLocazioneUnica(citta);
            for (TipoLocazione altra : TipoLocazione.values()) {
                CoordinateMD coordinate = altra.getCategoria() == CategoriaLocazione.CITTA
                        ? Foresta.getCoordinateLocazioneUnica(altra) : null;
                if (coordinate != null) {
                    assertTrue(distanza(locanda, casa) <= distanza(locanda, coordinate), altra + " è più vicina di " + citta);
                }
            }

            partita.gruppo().setCoordinate(casa);
            int monete = partita.gruppo().getMonete();
            pianista.controllaPreLocazione();
            assertEquals("VIAGGIO", pianista.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertFalse(partita.gruppo().getOspiti().isEmpty(), "il bardo c'è ancora per la scena");
            pianista.segnaIntermezzoPassoMostrato("VIAGGIO");
            pianista.controllaInLocazione();
            assertEquals(monete + 20, partita.gruppo().getMonete());
            assertTrue(pianista.isCompleta());
            String moglie = RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome();
            assertTrue(partita.testi().contains(moglie + ", la moglie di " + pianista.getBardo() + ", paga 20 monete e trascina il marito in casa per un orecchio."),
                    String.valueOf(partita.testi()));
            assertTrue(partita.gruppo().getOspiti().isEmpty(), "il bardo è a casa");
            assertEquals(1, RegistroMissioni.getTutteLeMissioni().stream().filter(NonSparateSulPianista.class::isInstance).count(),
                    "la missione si fa una volta sola: non ne lascia una nuova");
        }
    }

    @Test
    void ilBardoNonSiPuoFerireNePerStradaNeInCombattimento() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(152)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = pianista();
            entraNellaLocanda(partita, pianista, unaLocandaNelBosco(), 2);
            Personaggio ugolino = pianista.getScortato().orElseThrow(AssertionError::new);

            assertFalse(partita.gruppo().isOspiteVulnerabile(ugolino));
            // Gli avversari non lo scelgono mai come bersaglio
            assertTrue(partita.gruppo().getOspitiBersaglio().isEmpty(), "un avversario ha attaccato il bardo ubriaco");
            assertTrue(ugolino.isVivo());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("i mostri non si curano nemmeno di lui")), String.valueOf(partita.testi()));
        }
    }

    private static int distanza(CoordinateMD a, CoordinateMD b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
