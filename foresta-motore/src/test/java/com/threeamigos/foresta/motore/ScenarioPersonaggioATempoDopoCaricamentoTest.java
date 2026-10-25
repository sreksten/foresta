package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Gigante;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Un personaggio a tempo non ha un nome: dopo un salvataggio e un caricamento deve restare un PNG, altrimenti chiederne
 * il nome (per annunciare che lascia il gruppo, o che risorge) lancia un'eccezione.
 */
class ScenarioPersonaggioATempoDopoCaricamentoTest {

    @Test
    void dopoUnCaricamentoIlPersonaggioATempoSiPuNominareELascia() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(241)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Gigante gigante = new Gigante(1);
            gigante.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_MERCENARIO);
            gigante.setTempo(1);
            partita.gruppo().aggiungiPersonaggio(gigante);
            assertTrue(partita.salva(Comando.NUMERO_1));
            assertTrue(partita.leggi(Comando.NUMERO_1));

            Personaggio ricaricato = partita.gruppo().getPersonaggio(1);
            assertTrue(ricaricato.isATempo());
            assertTrue(ricaricato.isPNG());
            assertEquals(TipoMotivoNelGruppo.AIUTO_MERCENARIO, ricaricato.getMotivoNelGruppo());

            partita.gruppo().rimuoviPersonaggio(ricaricato);
            assertEquals(1, partita.gruppo().getNumeroPersonaggi());
            assertTrue(partita.testi().contains("Il Gigante lascia il gruppo."), partita.testi().toString());
        }
    }
}
