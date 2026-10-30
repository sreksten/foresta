package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.DovePartono;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.VoceMissione;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Altri casi della finestra di prova: la magia che non supera il massimo, chi è già vivo, le missioni finite o fallite e le missioni
 * forzate che partono nel posto giusto (la locanda) o non partono in quello sbagliato (un bosco per un incarico in città).
 */
class StrumentiDiSviluppoCasiRimastiTest {

    private final StrumentiDiSviluppoMotore strumenti = new StrumentiDiSviluppoMotore();

    private static PartitaDiTest partitaInCitta() {
        PartitaDiTest partita = PartitaDiTest.nuova(7);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    private static CoordinateMD primaCasellaDi(TipoLocazione tipo) {
        for (int y = 0; y < Foresta.getDimensioneY(); y++) {
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                CoordinateMD coordinate = new CoordinateMD(x, y);
                if (Foresta.getLocazione(coordinate) == tipo) {
                    return coordinate;
                }
            }
        }
        throw new AssertionError("nella mappa non c'è nessuna casella " + tipo);
    }

    private static Missione missione(MissionePredefinita classe) {
        return RegistroMissioni.getMissioniNonCompletate().stream().filter(m -> m.getClasse() == classe).findFirst().get();
    }

    private VoceMissione voce(DovePartono dove, MissionePredefinita classe) {
        return strumenti.getMissioni(dove).stream().filter(v -> v.getClasse() == classe).findFirst().get();
    }

    @Test
    void gliAttributiModificabiliSonoUnaListaFissaELaMagiaNonSuperaIlMassimo() {
        try (PartitaDiTest partita = partitaInCitta()) {
            assertTrue(strumenti.getAttributiModificabili().contains(TipoAttributo.MAGIA));
            Personaggio capo = partita.gruppo().getPersonaggi().get(0);
            String uuid = capo.getModelloDati().getUuid();

            strumenti.impostaAttributo(uuid, TipoAttributo.MAGIA, 100000);

            assertEquals(capo.getMagiaMassima(), strumenti.getValoreBase(uuid, TipoAttributo.MAGIA));
        }
    }

    @Test
    void renderVivoUnPersonaggioGiaVivoNonFaNiente() {
        try (PartitaDiTest partita = partitaInCitta()) {
            Personaggio capo = partita.gruppo().getPersonaggi().get(0);

            strumenti.impostaVivo(capo.getModelloDati().getUuid(), true);

            assertTrue(capo.isVivo());
        }
    }

    @Test
    void unaMissioneCompletataOFallitaSiDiceNellaVoce() {
        try (PartitaDiTest partita = partitaInCitta()) {
            // Le missioni completate o fallite escono dall'elenco di quelle da fare: la voce dice solo che sono finite
            Missione goblin = missione(MissionePredefinita.CACCIA_AI_GOBLIN);
            RegistroMissioni.fallisciMissione(goblin);
            assertEquals("finita", voce(DovePartono.CITTA, MissionePredefinita.CACCIA_AI_GOBLIN).getStato());
            Missione armaiolo = missione(MissionePredefinita.IL_NEGOZIO_DELL_ARMAIOLO);
            RegistroMissioni.completaMissione(armaiolo);
            assertEquals("finita", voce(DovePartono.CITTA, MissionePredefinita.IL_NEGOZIO_DELL_ARMAIOLO).getStato());
            assertFalse(voce(DovePartono.CITTA, MissionePredefinita.IL_NEGOZIO_DELL_ARMAIOLO).isForzabile());
            assertNotNull(voce(DovePartono.CITTA, MissionePredefinita.IL_NEGOZIO_DELL_ARMAIOLO).getNome());

            // Una missione ancora nell'elenco ma segnata come fallita o completata (qui con le proprietà, come se un controllo non l'avesse
            // ancora spostata) lo dice
            Missione forgia = missione(MissionePredefinita.LA_FORGIA);
            forgia.aggiungiProprieta("FALLITA", "S");
            assertEquals("fallita", voce(DovePartono.CITTA, MissionePredefinita.LA_FORGIA).getStato());
            Missione pianista = missione(MissionePredefinita.NON_SPARATE_SUL_PIANISTA);
            pianista.aggiungiProprieta("COMPLETA", "S");
            assertEquals("completata", voce(DovePartono.LOCANDA, MissionePredefinita.NON_SPARATE_SUL_PIANISTA).getStato());
        }
    }

    @Test
    void unaMissioneForzataPartePerLaLocandaSoloInUnaLocanda() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi leggenda = (MissioneAPassi) missione(MissionePredefinita.LA_LEGGENDA_DEL_LOCANDIERE);
            String passoIniziale = leggenda.getPassoCorrente();
            strumenti.forzaPartenza(MissionePredefinita.LA_LEGGENDA_DEL_LOCANDIERE);
            Foresta.setPosizioneDelGruppo(primaCasellaDi(TipoLocazione.LOCANDA));

            leggenda.controllaPreLocazione();

            assertNotEquals(passoIniziale, leggenda.getPassoCorrente());
        }
    }

    @Test
    void unIncaricoInCittaForzatoNonPartePerStradaInUnBosco() {
        try (PartitaDiTest partita = partitaInCitta()) {
            MissioneAPassi goblin = (MissioneAPassi) missione(MissionePredefinita.CACCIA_AI_GOBLIN);
            String passoIniziale = goblin.getPassoCorrente();
            strumenti.forzaPartenza(MissionePredefinita.CACCIA_AI_GOBLIN);
            Foresta.setPosizioneDelGruppo(primaCasellaDi(TipoLocazione.BOSCO));

            goblin.controllaPreLocazione();

            assertEquals(passoIniziale, goblin.getPassoCorrente());
        }
    }
}
