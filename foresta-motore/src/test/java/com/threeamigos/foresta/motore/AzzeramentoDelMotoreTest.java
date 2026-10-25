package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Dopo StatoDelMotore.azzera() il vecchio gruppo e il vecchio automa non ascoltano più il bus: gli eventi non li gestiscono due volte.
 */
class AzzeramentoDelMotoreTest {

    @Test
    void ilVecchioGruppoDelGiocatoreNonAscoltaPiu() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(308)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            GruppoGiocatore vecchio = GruppoGiocatore.getIstanza();
            Cassa.addMonete(1000);
            int pozioniDelVecchio = vecchio.getPozioniSalute();

            StatoDelMotore.azzera();
            GruppoGiocatore nuovo = GruppoGiocatore.getIstanza();
            Cassa.addMonete(1000);
            int pozioniDelNuovo = nuovo.getPozioniSalute();
            BusEventi.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.POZIONE_SALUTE, null, null));

            assertEquals(pozioniDelNuovo + 1, nuovo.getPozioniSalute(), "il gruppo nuovo gestisce l'acquisto");
            assertEquals(pozioniDelVecchio, vecchio.getPozioniSalute(), "quello vecchio non lo vede");
        }
    }

    @Test
    void ilVecchioAutomaDopoAzzeraNonRispondePiuAiComandi() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(309)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            partita.assertStato(Stato.IN_LOCAZIONE);

            StatoDelMotore.azzera();
            partita.comando(Comando.LOCANDA);

            partita.assertStato(Stato.IN_LOCAZIONE);
        }
    }
}
