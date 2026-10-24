package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Quando un effetto a tempo finisce il messaggio riporta il nome com'è: l'iniziale minuscola è dell'articolo ("Per il guerriero"),
 * mai di un nome proprio.
 */
class FineModificatoreATempoTest {

    @Test
    void ilNomeProprioMantieneLInizialeMaiuscolaQuandoLEffettoFinisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(1)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio capo = partita.gruppo().getCapo();
            capo.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 1, "prova",
                    UnitaDurataModificatore.TURNI, 1, BersaglioModificatore.PORTATORE));
            capo.rimuoviIModificatoriTemporaneiRimovibili();
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("Per Arsenio non vale più")), partita.testi().toString());
        }
    }
}
