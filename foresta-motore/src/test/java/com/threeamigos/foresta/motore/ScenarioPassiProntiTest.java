package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I passi pronti di MissioneAPassi su una partita vera (passi_missioni.md, §2): gli eventi di gioco arrivano dal
 * bus, come quando si combatte e si raccoglie.
 */
class ScenarioPassiProntiTest {

    /**
     * "Sconfiggi due goblin, raccogli tre pietre preziose e torna a riscuotere": INCARICO (dialogo, si attiva) →
     * CACCIA → RACCOLTA → RITORNO al punto di partenza → PREMIO (15 monete) → fine.
     */
    static class CacciaAiGoblin extends MissioneAPassi {

        CacciaAiGoblin() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "INCARICO";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            switch (id) {
                case "INCARICO":
                    return dialogo(MomentoControllo.IN_LOCAZIONE, () -> "Un mercante vi chiede di liberare la strada dai goblin.")
                            .esegui(this::attivaMissione).poi("CACCIA");
                case "CACCIA":
                    return sconfiggi(MomentoControllo.POST_LOCAZIONE, TipoPersonaggio.GOBLIN, 2).poi("RACCOLTA");
                case "RACCOLTA":
                    return raccogli(MomentoControllo.POST_LOCAZIONE, TipoOggetto.PIETRA_PREZIOSA, 3).poi("RITORNO");
                case "RITORNO":
                    return tornaAlPuntoDiPartenza(MomentoControllo.PRE_LOCAZIONE).poi("PREMIO");
                default:
                    return ricompensa(MomentoControllo.IN_LOCAZIONE, 15, () -> "Il mercante paga 15 monete.").poi(Passo.FINE);
            }
        }
    }

    @Test
    void laCacciaContaGliAvversariEGliOggettiDalBusEPagaAlRitorno() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(51)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            CacciaAiGoblin missione = new CacciaAiGoblin();
            RegistroMissioni.getMissionePrincipale().aggiungiMissione(missione);
            CoordinateMD nyena = partita.gruppo().getCoordinate();

            // Un goblin sconfitto prima di accettare l'incarico non conta
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            missione.controllaInLocazione();
            assertTrue(missione.isAttiva());
            assertEquals(nyena, missione.getPuntoDiPartenza());
            assertEquals("CACCIA", missione.getPassoCorrente());

            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.TROLL));
            missione.controllaPostLocazione();
            assertEquals("CACCIA", missione.getPassoCorrente(), "un goblin solo, e il troll non conta");
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
            missione.controllaPostLocazione();
            assertEquals("RACCOLTA", missione.getPassoCorrente());

            partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.PIETRA_PREZIOSA, 2, null, false));
            missione.controllaPostLocazione();
            assertEquals("RACCOLTA", missione.getPassoCorrente());
            partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.PIETRA_PREZIOSA, 1, null, true));
            missione.controllaPostLocazione();
            assertEquals("RITORNO", missione.getPassoCorrente());

            // Altrove non si riscuote; tornati a Nyena, sì
            partita.gruppo().setCoordinate(new CoordinateMD(nyena.getX() == 0 ? 1 : nyena.getX() - 1, nyena.getY()));
            missione.controllaPreLocazione();
            assertEquals("RITORNO", missione.getPassoCorrente());
            partita.gruppo().setCoordinate(nyena);
            missione.controllaPreLocazione();
            int monete = partita.gruppo().getMonete();
            missione.controllaInLocazione();
            assertEquals(monete + 15, partita.gruppo().getMonete());
            assertTrue(missione.isCompleta());
            assertTrue(partita.testi().contains("Il mercante paga 15 monete."), String.valueOf(partita.testi()));
        }
    }
}
