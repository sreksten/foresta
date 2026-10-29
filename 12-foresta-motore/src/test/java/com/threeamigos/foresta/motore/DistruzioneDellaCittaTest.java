package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Direzioni;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il Drago distrugge una città dal giorno 25 (Nyena): chi è lontano vede una colonna di fumo, chi dormiva in città scampa per un pelo, chi ci
 * arriva trova solo le rovine.
 */
class DistruzioneDellaCittaTest {

    private static PartitaDiTest inNyena(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        return partita;
    }

    private static void alGiorno(int giorno) {
        ModelloDati.getIstanza().getLineaTemporaleMD().setGiorno(giorno);
    }

    @Test
    void chiDormivaInCittaScampaPerUnPeloEIlTestoCompareAlRisveglio() {
        try (PartitaDiTest partita = inNyena(315)) {
            alGiorno(24);
            LineaTemporale.aggiungiOre(12 - LineaTemporale.getOra());

            Pernottamento.pernotta(partita.gruppo(), TipoRiposo.AL_COPERTO);

            assertTrue(Foresta.isCittaDistrutta(TipoLocazione.CITTA_NYENA));
            List<String> testi = partita.testi();
            int fuga = indiceCheContiene(testi, "Un boato scuote Nyena prima dell'alba");
            int alba = indiceCheContiene(testi, "Il sole sorge e l'avventura ricomincia.");
            assertTrue(fuga >= 0, String.valueOf(testi));
            assertTrue(fuga < alba, "il boato viene prima dell'alba: " + testi);
            assertTrue(testi.get(fuga).contains("riesce a fuggire per un pelo"), testi.get(fuga));
            assertNull(LineaTemporale.getEvento(), "il testo è già stato mostrato");
        }
    }

    @Test
    void chiArrivaDovePrimaSorgevaLaCittaTrovaSoloLeRovine() {
        try (PartitaDiTest partita = inNyena(316)) {
            alGiorno(25);

            LineaTemporale.eventi(partita.gruppo());

            assertTrue(Foresta.isCittaDistrutta(TipoLocazione.CITTA_NYENA));
            String evento = LineaTemporale.getEvento();
            assertTrue(evento.startsWith("Dove sorgeva Nyena") && evento.contains("trova solo le rovine"), evento);
        }
    }

    @Test
    void chiELontanoVedeUnaColonnaDiFumo() {
        try (PartitaDiTest partita = inNyena(317)) {
            CoordinateMD nyena = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA);
            Foresta.setPosizioneDelGruppo(new CoordinateMD(nyena.getX() == 0 ? 3 : nyena.getX() - 3, nyena.getY()));
            alGiorno(25);

            LineaTemporale.eventi(partita.gruppo());

            String evento = LineaTemporale.getEvento();
            assertTrue(evento.contains("vede levarsi una colonna di fumo"), evento);
            assertFalse(evento.contains("proprio qui"), evento);
        }
    }

    @Test
    void aDistanzaZeroNonSiDiceZeroOreDiCammino() {
        assertEquals("proprio qui.", Direzioni.getDirezione(3, 3, 3, 3));
        assertTrue(Direzioni.getDirezione(3, 3, 3, 4).contains("un'ora"), Direzioni.getDirezione(3, 3, 3, 4));
    }

    private static int indiceCheContiene(List<String> testi, String frammento) {
        for (int i = 0; i < testi.size(); i++) {
            if (testi.get(i).contains(frammento)) {
                return i;
            }
        }
        return -1;
    }
}
