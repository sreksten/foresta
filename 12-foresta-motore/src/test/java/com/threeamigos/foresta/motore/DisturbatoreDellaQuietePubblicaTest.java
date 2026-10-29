package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.principali.DisturbatoreDellaQuietePubblica;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ogni eremita si conta una volta sola, anche se l'automa rifà i controlli della missione (per esempio dopo la risposta a una domanda).
 */
class DisturbatoreDellaQuietePubblicaTest {

    @Test
    void lStessoEremitaNonContaDueVolte() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(307)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            DisturbatoreDellaQuietePubblica missione = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(DisturbatoreDellaQuietePubblica.class::isInstance).map(DisturbatoreDellaQuietePubblica.class::cast)
                    .findFirst().orElseThrow(AssertionError::new);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            avversari.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.EREMITA, 1));

            missione.controllaInLocazione();
            missione.controllaInLocazione();
            missione.controllaInLocazione();
            assertTrue(missione.isAttiva());
            assertTrue(missione.getDescrizione().endsWith("Ne mancano nove."), missione.getDescrizione());

            // Un altro eremita (un altro incontro) conta di nuovo, e l'altro no
            avversari.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.EREMITA, 1));
            missione.controllaInLocazione();
            missione.controllaInLocazione();
            assertTrue(missione.getDescrizione().endsWith("Ne mancano otto."), missione.getDescrizione());
            assertEquals(1, partita.testi().stream().filter(t -> t.startsWith("La quiete del primo Eremita")).count());
        }
    }
}
