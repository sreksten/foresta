package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.CacciaAiGoblin;
import com.threeamigos.foresta.missioni.IncaricoInCitta;
import com.threeamigos.foresta.missioni.LaTagliaSuSgranf;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RecuperaIlMedaglione;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli incarichi in città ripetibili, finiti, ne lasciano uno nuovo uguale, che si prende dopo una pausa; le storie
 * delle città e gli incarichi con un personaggio dal nome proprio no.
 */
class ScenarioIncarichiRipetutiTest {

    private static <T extends Missione> List<T> tutte(Class<T> tipo) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(tipo::cast).collect(Collectors.toList());
    }

    /**
     * Come a una visita tranquilla della città in cui si trova il gruppo.
     */
    private static void offri(IncaricoInCitta incarico) {
        incarico.controllaPreLocazione();
        if ("ACCETTAZIONE".equals(incarico.getPassoCorrente())) {
            incarico.segnaIntermezzoPassoMostrato("INCARICO");
            incarico.controllaInLocazione();
        }
    }

    @Test
    void unaCacciaFinitaNeLasciaUnaNuovaCheSiPrendeDopoLaPausa() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(141)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            CacciaAiGoblin prima = tutte(CacciaAiGoblin.class).get(0);
            offri(prima);
            assertTrue(prima.isAttiva());
            prima.completaMissione();

            List<CacciaAiGoblin> cacce = tutte(CacciaAiGoblin.class);
            assertEquals(2, cacce.size(), "ne è nata una nuova");
            CacciaAiGoblin seconda = cacce.stream().filter(c -> c != prima).findFirst().orElseThrow(AssertionError::new);
            assertFalse(seconda.isAttiva());
            assertNotEquals(prima.getId(), seconda.getId());

            // Prima della pausa non si offre, neanche a una visita tranquilla
            offri(seconda);
            assertEquals("INCARICO", seconda.getPassoCorrente());

            // Il nuovo incarico si salva con la partita
            GestoreSalvataggi.salva(Comando.NUMERO_2);
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_2));
            CacciaAiGoblin riletta = tutte(CacciaAiGoblin.class).stream().filter(c -> c.getId().equals(seconda.getId()))
                    .findFirst().orElseThrow(() -> new AssertionError("il nuovo incarico dopo il caricamento"));

            LineaTemporale.aggiungiOre(IncaricoInCitta.ORE_FRA_UN_INCARICO_E_L_ALTRO);
            offri(riletta);
            assertTrue(riletta.isAttiva(), "dopo la pausa si offre");
            assertEquals(ClassiLocazione.CITTA_NYENA, riletta.getCitta());
        }
    }

    @Test
    void ancheUnIncaricoFallitoSiRipeteMaUnaVoltaSola() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(142)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            CacciaAiGoblin caccia = tutte(CacciaAiGoblin.class).get(0);
            offri(caccia);
            caccia.fallisciMissione();
            caccia.fallisciMissione();
            assertEquals(2, tutte(CacciaAiGoblin.class).size());
        }
    }

    @Test
    void leStorieDelleCittaEGliIncarichiConUnNomeProprioNonSiRipetono() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(143)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_FLEENA));
            RecuperaIlMedaglione medaglione = tutte(RecuperaIlMedaglione.class).get(0);
            assertTrue(medaglione.isAttiva(), "la storia di Fleena parte alla prima visita");
            medaglione.completaMissione();
            assertEquals(1, tutte(RecuperaIlMedaglione.class).size());

            LaTagliaSuSgranf taglia = tutte(LaTagliaSuSgranf.class).get(0);
            taglia.completaMissione();
            assertEquals(1, tutte(LaTagliaSuSgranf.class).size());
        }
    }
}
