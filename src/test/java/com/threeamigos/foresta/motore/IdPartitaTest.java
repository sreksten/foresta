package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
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
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            id = Statistiche.getIdPartita();
            assertNotNull(id);
            GestoreSalvataggi.salva(Comando.NUMERO_2);
            Statistiche.reimposta();
            assertNotEquals(id, Statistiche.getIdPartita(), "una partita nuova ha un altro identificativo");
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_2));
            assertEquals(id, Statistiche.getIdPartita(), "ricaricata, è la stessa partita");
        }
    }
}
