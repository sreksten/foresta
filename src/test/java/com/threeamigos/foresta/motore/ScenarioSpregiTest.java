package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lo spregio per un tentativo di amicizia o di corruzione fallito (vedi LocazioneBase.subisciUnoSpregio): monete o
 * preziosi persi da 1 a 3, un attacco dell'avversario che non può uccidere e non toglie più del 20% della salute di
 * quel momento, un incantesimo perso, o niente. La corruzione ha gli stessi spregi anche se mancano le monete.
 */
class ScenarioSpregiTest {

    /**
     * Un capo che non ha il carisma per sbaragliare il dado a 12, davanti all'Idra (senza la modalità di prova, che gli dà
     * oggetti che cambiano i numeri).
     */
    private static PartitaDiTest davantiAll_Idra() {
        PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(25);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
        partita.assertStato(Stato.IN_LOCAZIONE);
        assertTrue(partita.gruppo().getCapo().getCarisma() < 12, "precondizione: il tiro più alto del dado fa fallire");
        return partita;
    }

    @Test
    void conUnaAmiciziaFallitaSiPerdonoDa1A3Monete() {
        try (PartitaDiTest partita = davantiAll_Idra()) {
            int monete = partita.gruppo().getMonete();
            // Il dado a 12 dice 12 (fallisce), lo spregio è il 3 (monete), ne perde 2
            Dado.trucca(12, 3, 2);
            partita.comando(Comando.AMICIZIA);
            assertEquals(monete - 2, partita.gruppo().getMonete());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("non riesce a stringere amicizia, ma in una breve colluttazione perde alcune monete")),
                    String.valueOf(partita.testi()));
        }
    }

    @Test
    void ilMassimoDiMoneteEPreziosiPersiEDiTre() {
        try (PartitaDiTest partita = davantiAll_Idra()) {
            int monete = partita.gruppo().getMonete();
            Dado.trucca(12, 3, 3);
            partita.comando(Comando.AMICIZIA);
            assertEquals(monete - 3, partita.gruppo().getMonete());
        }
        try (PartitaDiTest partita = davantiAll_Idra()) {
            partita.gruppo().addPreziosi(10);
            int preziosi = partita.gruppo().getPreziosi();
            Dado.trucca(12, 4, 3);
            partita.comando(Comando.AMICIZIA);
            assertEquals(preziosi - 3, partita.gruppo().getPreziosi());
        }
    }

    @Test
    void unAttaccoNonTogliePiuDelVentiPerCentoDellaSaluteDiAdessoENonUccide() {
        try (PartitaDiTest partita = davantiAll_Idra()) {
            Personaggio capo = partita.gruppo().getCapo();
            int salute = capo.getSalute();
            // Fallisce, lo spregio è l'attacco (2) e il colpo arriva (1 a un dado a 100): il resto del danno è a caso
            Dado.trucca(12, 2, 1);
            partita.comando(Comando.AMICIZIA);
            assertTrue(capo.isVivo());
            assertTrue(capo.getSalute() >= 1);
            assertTrue(salute - capo.getSalute() <= Math.max(1, salute * 20 / 100), "tolti " + (salute - capo.getSalute()) + " su " + salute);
        }
    }

    @Test
    void conUnSoloPuntoDiSaluteUnAttaccoNonFaNiente() {
        try (PartitaDiTest partita = davantiAll_Idra()) {
            Personaggio capo = partita.gruppo().getCapo();
            capo.subSalute(capo.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            assertEquals(1, capo.getSalute());
            Dado.trucca(12, 2, 1);
            partita.comando(Comando.AMICIZIA);
            assertTrue(capo.isVivo());
            assertEquals(1, capo.getSalute());
        }
    }

    @Test
    void ilColpoMancatoNonFaNienteEIlMessaggioEQuelloSemplice() {
        try (PartitaDiTest partita = davantiAll_Idra()) {
            Personaggio capo = partita.gruppo().getCapo();
            int salute = capo.getSalute();
            // 100 al dado a 100: il colpo non arriva
            Dado.trucca(12, 2, 100);
            partita.comando(Comando.AMICIZIA);
            assertEquals(salute, capo.getSalute());
            assertTrue(partita.testi().stream().anyMatch(t -> t.endsWith("non riesce a stringere amicizia.")), String.valueOf(partita.testi()));
        }
    }

    @Test
    void laCorruzioneFallitaHaGliStessiSpregiESiDiceCheCeUnaConseguenza() {
        try (PartitaDiTest partita = davantiAll_Idra()) {
            int monete = partita.gruppo().getMonete();
            // Il dado a 10 dice 1 (non riesce: serve più di 3), lo spregio è il 3 (monete), ne perde 2
            Dado.trucca(1, 3, 2);
            partita.comando(Comando.CORRUZIONE);
            assertEquals(monete - 2, partita.gruppo().getMonete());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("non ha avuto successo, e in una breve colluttazione perde alcune monete")),
                    String.valueOf(partita.testi()));
        }
    }

    @Test
    void laCorruzioneSenzaMoneteFallisceSempreEPureConLoSpregio() {
        try (PartitaDiTest partita = davantiAll_Idra()) {
            // Una moneta sola: non bastano per pagare (2 a testa), quindi non c'è nemmeno il dado; lo spregio è il 3 (monete)
            partita.gruppo().subMonete(partita.gruppo().getMonete() - 1);
            Dado.trucca(3, 2);
            partita.comando(Comando.CORRUZIONE);
            assertEquals(0, partita.gruppo().getMonete());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("non ha avuto successo, e in una breve colluttazione")),
                    String.valueOf(partita.testi()));
        }
    }
}
