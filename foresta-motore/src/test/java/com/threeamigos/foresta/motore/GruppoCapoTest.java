package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Chi esce dal gruppo cambia il capo solo se era lui il capo.
 */
class GruppoCapoTest {

    @Test
    void ilCapoSpostatoRestaCapoSeEsceUnAltro() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(294)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            GruppoGiocatore gruppo = partita.gruppo();
            Personaggio capo = gruppo.getCapo();
            Personaggio secondo = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 3);
            Personaggio terzo = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 3);
            gruppo.aggiungiPersonaggio(secondo);
            gruppo.aggiungiPersonaggio(terzo);

            assertTrue(gruppo.spostaPersonaggio(capo, 1));
            gruppo.rimuoviPersonaggio(terzo);
            assertSame(capo, gruppo.getCapo());

            gruppo.rimuoviPersonaggio(capo);
            assertSame(gruppo.getPersonaggi().get(0), gruppo.getCapo());
        }
    }
}
