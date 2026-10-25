package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La partita ha un identificativo che sopravvive ai salvataggi: la classifica lo usa per non registrare più volte la
 * stessa partita ricaricata.
 */
class IdPartitaTest {

    @Test
    void lIdentificativoRestaDopoUnCaricamentoECambiaConUnaPartitaNuova() {
        String id;
        try (PartitaDiTest partita = PartitaDiTest.nuova(121)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            id = Statistiche.getIdPartita();
            assertNotNull(id);
            partita.salva(Comando.NUMERO_2);
            Statistiche.reimposta();
            assertNotEquals(id, Statistiche.getIdPartita(), "una partita nuova ha un altro identificativo");
            assertTrue(partita.leggi(Comando.NUMERO_2));
            assertEquals(id, Statistiche.getIdPartita(), "ricaricata, è la stessa partita");
        }
    }
}
