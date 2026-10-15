package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.ui.sviluppo.FinestraDiProva;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La finestra di prova costruita su una partita vera. Si salta dove non c'è uno schermo.
 */
class FinestraDiProvaTest {

    @Test
    void ogniPersonaggioHaIlSuoTabPoiGruppoStatisticheMissioniEDadi() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            FinestraDiProva finestra = null;
            try {
                finestra = new FinestraDiProva(new VistaPartitaMotore(), new StrumentiDiSviluppoMotore());
            } catch (RuntimeException e) {
                Assumptions.assumeFalse(e.getClass().getSimpleName().equals("HeadlessException"), "Nessuno schermo");
                throw e;
            }
            finestra.aggiornaOra();
            List<String> titoli = finestra.getTitoliDeiTab();
            int personaggi = partita.gruppo().getPersonaggi().size();
            assertTrue(personaggi >= 1);
            assertEquals(personaggi + 5, titoli.size());
            assertEquals(partita.gruppo().getPersonaggi().get(0).getNome(), titoli.get(0));
            assertEquals("Gruppo", titoli.get(personaggi));
            assertEquals("Statistiche", titoli.get(personaggi + 1));
            assertEquals("Dadi", titoli.get(personaggi + 4));
        }
    }
}
