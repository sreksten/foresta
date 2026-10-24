package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Una notte passata al coperto (in una locanda o in città) fa passare il tempo come una notte all'accampamento: l'orologio arriva al mattino
 * e gli eventi del giorno (il drago) hanno la loro occasione.
 */
class NotteAlCopertoTest {

    private static PartitaDiTest partita(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    @Test
    void laNotteAlCopertoPortaLOrologioAlMattinoSeguente() {
        try (PartitaDiTest partita = partita(303)) {
            int giorno = LineaTemporale.getGiorno();
            LineaTemporale.aggiungiOre(12 - LineaTemporale.getOra());
            assertEquals(12, LineaTemporale.getOra());

            partita.gruppo().pernotta(TipoRiposo.AL_COPERTO);

            assertEquals(8, LineaTemporale.getOra());
            assertEquals(giorno + 1, LineaTemporale.getGiorno());
        }
    }

    @Test
    void laNotteAlCopertoPuoFarFinireIlTempo() {
        try (PartitaDiTest partita = partita(304)) {
            LineaTemporale.aggiungiOre(24 * (40 - LineaTemporale.getGiorno()) + 12 - LineaTemporale.getOra());
            assertEquals(40, LineaTemporale.getGiorno());
            assertFalse(LineaTemporale.isGiocoFinito());

            partita.gruppo().pernotta(TipoRiposo.AL_COPERTO);

            assertEquals(41, LineaTemporale.getGiorno());
            assertTrue(LineaTemporale.isGiocoFinito());
        }
    }

    @Test
    void laNotteInUnaLocandaFuoriCittaNonFaCrollareIlGioco() {
        try (PartitaDiTest partita = partita(305)) {
            partita.gruppo().setCoordinate(unaLocanda());
            assertEquals(TipoLocazione.LOCANDA, partita.gruppo().getTipoLocazioneCorrente());
            int giorno = LineaTemporale.getGiorno();

            partita.gruppo().pernotta(TipoRiposo.AL_COPERTO);

            assertEquals(giorno + 1, LineaTemporale.getGiorno());
        }
    }

    private static CoordinateMD unaLocanda() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.LOCANDA) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("Nessuna locanda nella Foresta");
    }
}
