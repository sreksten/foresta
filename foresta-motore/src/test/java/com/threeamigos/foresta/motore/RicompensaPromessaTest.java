package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.CacciaAiGoblin;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La ricompensa di un incarico è quella pattuita all'inizio (la paga base per il livello del mondo di quando si è offerto): i testi
 * promettono quella cifra, e quella si paga a fine lavoro anche se nel frattempo il livello del mondo sale.
 */
class RicompensaPromessaTest {

    @Test
    void laCifraPromessaELaCifraPagataAncheSeIlLivelloSale() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(306)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            ModelloDati.getIstanza().getStatisticheMD().setLivello(3);
            CacciaAiGoblin caccia = RegistroMissioni.getTutteLeMissioni().stream().filter(CacciaAiGoblin.class::isInstance)
                    .map(CacciaAiGoblin.class::cast).findFirst().orElseThrow(AssertionError::new);
            caccia.controllaPreLocazione();
            caccia.segnaIntermezzoPassoMostrato("INCARICO");
            caccia.controllaInLocazione();
            assertTrue(caccia.isAttiva());
            // 15 monete di base per il livello 3 del mondo
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("pagherà 45 monete")), String.valueOf(partita.testi()));

            // Il mondo sale di livello, ma la cifra pattuita non cambia
            ModelloDati.getIstanza().getStatisticheMD().setLivello(6);
            for (int i = 0; i < CacciaAiGoblin.GOBLIN_DA_SCONFIGGERE; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            }
            caccia.controllaPostLocazione();
            int monete = partita.gruppo().getMonete();
            caccia.controllaPreLocazione();
            caccia.controllaInLocazione();

            assertEquals(monete + 45, partita.gruppo().getMonete());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("paga le 45 monete promesse")), String.valueOf(partita.testi()));
        }
    }
}
