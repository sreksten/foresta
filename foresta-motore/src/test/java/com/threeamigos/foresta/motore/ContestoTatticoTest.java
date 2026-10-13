package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.Rose;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il contesto di un personaggio ricavato dai gruppi della partita (ContestoTattico.per)
 */
class ContestoTatticoTest {

    @Test
    void ilContestoSaDiCheLatoEUnPersonaggioEChiSonoGliAlleatiEGliAvversari() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(501)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio compagno = new Guerriero("Compagno", 1);
            partita.gruppo().aggiungiPersonaggio(compagno);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
            avversari.aggiungiPersonaggio(goblin);
            Personaggio ostaggio = new com.threeamigos.foresta.personaggi.Viandante("Ottone", 1);
            partita.gruppo().aggiungiOspite(ostaggio, true);

            ContestoTattico mio = ContestoTattico.per(compagno);
            assertTrue(mio.isAttoreDelGiocatore());
            assertEquals(partita.gruppo().getPersonaggi(), Rose.di(TipoRosa.ALLEATI).candidati(mio));
            assertEquals(Collections.singletonList(goblin), Rose.di(TipoRosa.AVVERSARI).candidati(mio));
            assertEquals(Collections.singletonList(ostaggio), Rose.di(TipoRosa.OSPITI).candidati(mio));

            ContestoTattico delMostro = ContestoTattico.per(goblin);
            assertFalse(delMostro.isAttoreDelGiocatore());
            assertEquals(Collections.singletonList(goblin), Rose.di(TipoRosa.ALLEATI).candidati(delMostro));
            assertEquals(partita.gruppo().getPersonaggi(), Rose.di(TipoRosa.AVVERSARI).candidati(delMostro));
            assertEquals(partita.gruppo().getPersonaggi().size() + 1, Rose.di(TipoRosa.AVVERSARI_E_OSPITI).candidati(delMostro).size());
        }
    }
}
