package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.IlRapimentoDiArmando;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il rapimento di Armando: la banda di Ghignazzo in una grotta, Armando liberato che viaggia con il gruppo come
 * ospite vulnerabile, e il ritorno in città.
 */
class ScenarioRapimentoDiArmandoTest {

    private static IlRapimentoDiArmando prendiLIncarico(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
        IlRapimentoDiArmando rapimento = RegistroMissioni.getTutteLeMissioni().stream().filter(IlRapimentoDiArmando.class::isInstance)
                .map(IlRapimentoDiArmando.class::cast).findFirst().orElseThrow(AssertionError::new);
        // Come a una visita tranquilla della città
        rapimento.controllaPreLocazione();
        rapimento.segnaIntermezzoPassoMostrato("INCARICO");
        rapimento.controllaInLocazione();
        assertTrue(rapimento.isAttiva());
        return rapimento;
    }

    private static Personaggio liberaArmando(PartitaDiTest partita, IlRapimentoDiArmando rapimento) {
        CoordinateMD covo = rapimento.getCovo();
        partita.gruppo().setCoordinate(covo);
        for (int i = 0; i < IlRapimentoDiArmando.RAPITORI.getNumero(); i++) {
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GOBLIN));
        }
        rapimento.controllaPostLocazione();
        assertEquals("VIAGGIO", rapimento.getPassoCorrente());
        return rapimento.getScortato().orElseThrow(() -> new AssertionError("Armando non è con il gruppo"));
    }

    @Test
    void armandoLiberatoTornaACasaEPagaSuaMoglie() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(131)) {
            IlRapimentoDiArmando rapimento = prendiLIncarico(partita);
            CoordinateMD covo = rapimento.getCovo();
            assertEquals(ClassiLocazione.GROTTA, Foresta.getLocazione(covo));
            assertTrue(Foresta.isLocazioneConosciuta(covo));
            assertTrue(Foresta.getCoordinateDaSegnalare().contains(covo));
            List<Personaggio> banda = RegistroMissioni.getIncontroMissione(covo).orElseThrow(AssertionError::new);
            assertEquals(4, banda.size());
            assertEquals("Ghignazzo", banda.get(0).getNome());

            Personaggio armando = liberaArmando(partita, rapimento);
            assertEquals(IlRapimentoDiArmando.ARMANDO, armando.getNome());
            assertTrue(partita.gruppo().isOspiteVulnerabile(armando));
            assertFalse(partita.gruppo().getPersonaggi().contains(armando));

            CoordinateMD nyena = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA);
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
    void seArmandoMuorePerStradaLaMissioneFallisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(132)) {
            IlRapimentoDiArmando rapimento = prendiLIncarico(partita);
            Personaggio armando = liberaArmando(partita, rapimento);

            armando.muore("un goblin rimasto indietro");
            rapimento.controllaPreLocazione();
            assertTrue(rapimento.isFallita());
            assertTrue(partita.gruppo().getOspiti().isEmpty());
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("Armando non ce l'ha fatta")), String.valueOf(partita.testi()));
        }
    }
}
