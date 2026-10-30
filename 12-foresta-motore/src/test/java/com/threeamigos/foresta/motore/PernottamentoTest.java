package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La notte del gruppo: quel che si racconta all'accampamento secondo le ore che restano fino al mattino, da soli o in gruppo, e la città
 * in cui si dormiva che il Drago ha già distrutto.
 */
class PernottamentoTest {

    private static PartitaDiTest partitaInUnBosco(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                    if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO) {
                        Foresta.setPosizioneDelGruppo(new CoordinateMD(x, y));
                        return;
                    }
                }
            }
            throw new IllegalStateException("nessun bosco nella Foresta");
        });
        return partita;
    }

    private static void portaOrologioAlle(int ora) {
        LineaTemporale.aggiungiOre((ora - LineaTemporale.getOra() + 24) % 24);
        assertEquals(ora, LineaTemporale.getOra());
    }

    // La frase della notte, quella che racconta come si dorme: le altre sono quelle dell'ingresso nella locazione
    private static String primaFrase(PartitaDiTest partita) {
        return partita.eventi().tutti(NotificaTestoFrase.class).stream().map(NotificaTestoFrase::getMessaggio)
                .filter(m -> m.contains("addormentare") || m.contains("accamparsi")).findFirst().orElse("");
    }

    @Test
    void all_apertoIlRaccontoDipendeDalleOreCheRestano() {
        String[] attese = {"Ma un'ora sola", "Ma due ore sono insufficienti", "Ma tre ore sono troppo poche", "Ma quattro ore", "Cinque ore sono appena sufficienti"};
        for (int ore = 1; ore <= 5; ore++) {
            try (PartitaDiTest partita = partitaInUnBosco(20 + ore)) {
                portaOrologioAlle(8 - ore);

                Pernottamento.pernotta(partita.gruppo(), TipoRiposo.ALL_APERTO_CON_FUOCO);

                assertTrue(primaFrase(partita).contains(attese[ore - 1]), primaFrase(partita));
            }
        }
    }

    @Test
    void daSoliSiRaccontaDelCapoEInGruppoDelGruppo() {
        try (PartitaDiTest partita = partitaInUnBosco(31)) {
            portaOrologioAlle(22);
            Pernottamento.pernotta(partita.gruppo(), TipoRiposo.ALL_APERTO_CON_FUOCO);
            assertTrue(primaFrase(partita).contains("Arsenio ha riposato abbastanza"), primaFrase(partita));
        }
        try (PartitaDiTest partita = partitaInUnBosco(32)) {
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Bruno", 1));
            portaOrologioAlle(22);
            Pernottamento.pernotta(partita.gruppo(), TipoRiposo.ALL_APERTO_CON_FUOCO);
            assertTrue(primaFrase(partita).contains("Il gruppo ha riposato abbastanza"), primaFrase(partita));
        }
    }

    @Test
    void seIlCapoMuoreNelSonnoNonScorronoPiuITurni() {
        try (PartitaDiTest partita = partitaInUnBosco(33)) {
            Personaggio capo = partita.gruppo().getCapo();
            capo.subSalute(100000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            assertFalse(capo.isVivo());
            portaOrologioAlle(22);

            Pernottamento.pernotta(partita.gruppo(), TipoRiposo.ALL_APERTO_CON_FUOCO);

            assertEquals(8, LineaTemporale.getOra());
        }
    }

    @Test
    void dormendoInUnaCittaGiaDistruttaNonCEUnNuovoEvento() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(34)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_RUUNA));
            Foresta.setCittaDistrutta(TipoLocazione.CITTA_RUUNA);
            portaOrologioAlle(22);

            Pernottamento.pernotta(partita.gruppo(), TipoRiposo.AL_COPERTO);

            assertTrue(primaFrase(partita).contains("tepore delle coperte"), primaFrase(partita));
            assertNull(LineaTemporale.getEvento());
        }
    }
}
