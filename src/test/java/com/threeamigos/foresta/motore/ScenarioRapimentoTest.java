package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.IlRapimento;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il rapimento: la banda di goblin in una grotta, l'ostaggio liberato che viaggia con il gruppo come
 * ospite vulnerabile, e il ritorno in città.
 */
class ScenarioRapimentoTest {

    private static IlRapimento prendiLIncarico(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        IlRapimento rapimento = RegistroMissioni.getTutteLeMissioni().stream().filter(IlRapimento.class::isInstance)
                .map(IlRapimento.class::cast).findFirst().orElseThrow(AssertionError::new);
        // Come a una visita tranquilla della città
        rapimento.controllaPreLocazione();
        rapimento.segnaIntermezzoPassoMostrato("INCARICO");
        rapimento.controllaInLocazione();
        assertTrue(rapimento.isAttiva());
        return rapimento;
    }

    private static Personaggio liberaArmando(PartitaDiTest partita, IlRapimento rapimento) {
        CoordinateMD covo = rapimento.getCovo();
        partita.gruppo().setCoordinate(covo);
        for (int i = 0; i < IlRapimento.RAPITORI; i++) {
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GOBLIN));
        }
        rapimento.controllaPostLocazione();
        assertEquals("VIAGGIO", rapimento.getPassoCorrente());
        return rapimento.getScortato().orElseThrow(() -> new AssertionError("Armando non è con il gruppo"));
    }

    @Test
    void armandoLiberatoTornaACasaEPagaSuaMoglie() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(131)) {
            IlRapimento rapimento = prendiLIncarico(partita);
            CoordinateMD covo = rapimento.getCovo();
            assertEquals(TipoLocazione.GROTTA, Foresta.getLocazione(covo));
            assertTrue(Foresta.isLocazioneConosciuta(covo));
            assertTrue(Foresta.getCoordinateDaSegnalare().contains(covo));
            List<Personaggio> banda = RegistroMissioni.getIncontroMissione(covo).orElseThrow(AssertionError::new);
            assertEquals(4, banda.size());
            assertEquals(rapimento.getCapobanda(), banda.get(0).getNome());

            Personaggio armando = liberaArmando(partita, rapimento);
            assertEquals(rapimento.getOstaggio(), armando.getNome());
            assertTrue(partita.gruppo().isOspiteVulnerabile(armando));
            assertFalse(partita.gruppo().getPersonaggi().contains(armando));

            CoordinateMD nyena = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA);
            partita.gruppo().setCoordinate(nyena);
            int monete = partita.gruppo().getMonete();
            rapimento.controllaPreLocazione();
            assertTrue(partita.gruppo().getOspiti().isEmpty(), "Armando è a casa");
            assertEquals("RITORNO", rapimento.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            rapimento.segnaIntermezzoPassoMostrato("RITORNO");
            rapimento.controllaInLocazione();
            assertEquals(monete + 35, partita.gruppo().getMonete());
            assertTrue(rapimento.isCompleta());
        }
    }

    @Test
    void seArmandoMuorePerStradaBisognaDirloASuaMoglieEPoiLaMissioneFallisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(132)) {
            IlRapimento rapimento = prendiLIncarico(partita);
            Personaggio armando = liberaArmando(partita, rapimento);

            // Muore lontano dalla città: la missione resta aperta, e c'è da portare la notizia
            armando.muore("un goblin rimasto indietro");
            rapimento.controllaPreLocazione();
            assertFalse(rapimento.isFallita());
            assertEquals("LUTTO", rapimento.getPassoCorrente());
            assertTrue(partita.gruppo().getOspiti().isEmpty(), "Armando non viaggia più con il gruppo");
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith(rapimento.getOstaggio() + " non ce l'ha fatta")), String.valueOf(partita.testi()));
            assertTrue(rapimento.getDescrizione().contains("dare la notizia"), rapimento.getDescrizione());

            // In città la scena triste, poi la missione fallisce, senza monete
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            int monete = partita.gruppo().getMonete();
            rapimento.controllaPreLocazione();
            assertEquals("LUTTO", rapimento.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE));
            assertFalse(rapimento.isFallita(), "prima la scena");
            rapimento.segnaIntermezzoPassoMostrato("LUTTO");
            rapimento.controllaInLocazione();
            assertTrue(rapimento.isFallita());
            assertEquals(monete, partita.gruppo().getMonete());
            assertTrue(partita.testi().contains("La moglie di " + rapimento.getOstaggio() + " chiude la porta senza dire una parola."));
        }
    }
}
