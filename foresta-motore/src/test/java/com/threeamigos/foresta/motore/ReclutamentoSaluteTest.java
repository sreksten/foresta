package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Un compagno che entra a un livello più alto di quello a cui è nato non porta la salute e la magia del livello 1: ne ha la stessa frazione che
 * aveva (di solito tutta) rispetto ai nuovi massimi.
 */
class ReclutamentoSaluteTest {

    @Test
    void chiEntraAUnLivelloAltoNonPortaLaSaluteDelLivelloUno() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(301)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio mago = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1);
            int saluteMassimaAl1 = mago.getSaluteMassima();
            double frazioneSalute = (double) mago.getSalute() / saluteMassimaAl1;
            double frazioneMagia = (double) mago.getMagia() / mago.getMagiaMassima();

            mago.impostaLivelloDopoReclutamento(9);

            assertEquals(9, mago.getLivello());
            assertTrue(mago.getSaluteMassima() > saluteMassimaAl1);
            assertTrue(mago.getSalute() > saluteMassimaAl1, "la salute non è rimasta quella del livello 1: " + mago.getSalute());
            assertEquals(frazioneSalute, (double) mago.getSalute() / mago.getSaluteMassima(), 0.02d);
            assertEquals(frazioneMagia, (double) mago.getMagia() / mago.getMagiaMassima(), 0.02d);
        }
    }

    @Test
    void chiEraFeritoRestaFeritoInProporzione() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(302)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
            guerriero.subSalute(guerriero.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            double frazione = (double) guerriero.getSalute() / guerriero.getSaluteMassima();

            guerriero.impostaLivelloDopoReclutamento(9);

            assertEquals(frazione, (double) guerriero.getSalute() / guerriero.getSaluteMassima(), 0.02d);
        }
    }
}
