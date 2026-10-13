package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoDecisioneTattica;
import com.threeamigos.foresta.SnifferBusEventi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SnifferBusEventiTest {

    @Test
    void loSnifferScriveLaDecisioneNelLogConIlSuoTipoPerIlGrep() {
        java.io.ByteArrayOutputStream catturato = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originale = System.out;
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(609)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            new SnifferBusEventi();
            System.setOut(new java.io.PrintStream(catturato, true));
            Personaggio capo = partita.gruppo().getCapo();
            com.threeamigos.foresta.eventi.BusEventi.pubblica(new InternoDecisioneTattica(capo, null, TipoAzione.CANTO_DI_VALORE, 2,
                    Arrays.asList("voce 1: disattivata", "voce 3: CANTO_DI_VALORE su tutto il gruppo")));
        } finally {
            System.setOut(originale);
        }
        String riga = new String(catturato.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(riga.contains("INTERNO_DECISIONE_TATTICA"), riga);
        assertTrue(riga.contains("decide CANTO_DI_VALORE (voce 3)"), riga);
        assertTrue(riga.contains("voce 1: disattivata | voce 3: CANTO_DI_VALORE su tutto il gruppo"), riga);
    }
}
