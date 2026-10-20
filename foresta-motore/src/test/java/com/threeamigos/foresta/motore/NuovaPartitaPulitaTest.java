package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.MessaggioMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Una partita nuova nella stessa sessione non eredita gli intermezzi già visti né i messaggi e le notizie della precedente.
 */
class NuovaPartitaPulitaTest {

    @Test
    void reimpostareLaForestaAzzeraIntermezziENotizie() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(305)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            ModelloDati modello = ModelloDati.getIstanza();
            modello.getIntermezziMD().aggiungiScattato("INTRODUZIONE_DI_UNA_PARTITA_VECCHIA");
            modello.getNotizieMD().aggiungiMessaggio(new MessaggioMD("Un messaggio vecchio", false));
            assertTrue(modello.getIntermezziMD().isScattato("INTRODUZIONE_DI_UNA_PARTITA_VECCHIA"));

            Foresta.reimposta();

            assertFalse(ModelloDati.getIstanza().getIntermezziMD().isScattato("INTRODUZIONE_DI_UNA_PARTITA_VECCHIA"));
            assertTrue(ModelloDati.getIstanza().getNotizieMD().getUltimiMessaggi().isEmpty());
        }
    }
}
