package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.IndagineRichiesta;
import com.threeamigos.foresta.missioni.LIndagine;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'indagine: gli indizi in posti segnati sulla mappa uno alla volta, poi la scelta del colpevole; se è quello giusto
 * lo si sconfigge nel suo nascondiglio e si torna a riscuotere, se è un innocente la missione fallisce.
 */
class ScenarioIndagineTest {

    private static final String CAMPANE = "CHIAVE=CAMPANE_DI_PROVA;TIPO=TESTIMONI;ASPETTO=QUALUNQUE;MANDANTE=il sacerdote;"
            + "MONETE=35;TITOLO=Le campane;RICHIESTA=Hanno rubato le campane.;BATTUTA=Pesano?;RISPOSTA=Tanto.;"
            + "INDIZIO_1=LOCANDA:Volavano verso le rovine.;INDIZIO_2=TEMPIO:Ali di pietra.;DOMANDA=Chi è stato?;"
            + "SOSPETTI=Il campanaro/I gargoyle/Le arpie;COLPEVOLE=2;NEMICO=GARGOYLE;NUMERO=3;LUOGO=ROVINE;"
            + "SMASCHERAMENTO=Sono i gargoyle.;ERRORE=Vi siete sbagliati.;VITTORIA=Le campane sono salve.;"
            + "RINGRAZIAMENTO=Grazie.;RICORDO=Qui c'erano le campane.";

    @Test
    void ogniIndagineSiLegge() {
        Set<String> chiavi = new HashSet<>();
        for (int i = 0; i < 600; i++) {
            IndagineRichiesta indagine = IndagineRichiesta.da(ProduttoreDiTestiCasuale.rigaDiMissioni("INDAGINE"));
            if (!indagine.isConCapo()) {
                assertFalse(indagine.getRiga().contains("%CAPO%"), indagine.getRiga());
            }
            chiavi.add(indagine.getChiave());
        }
        assertTrue(chiavi.size() >= 13, String.valueOf(chiavi));

        IndagineRichiesta campane = IndagineRichiesta.da(CAMPANE);
        assertEquals(Arrays.asList("Il campanaro", "I gargoyle", "Le arpie"), campane.getSospetti());
        assertEquals(2, campane.getIndizi().size());
        assertEquals(ClassiLocazione.TEMPIO, campane.getIndizi().get(1).getLuogo());
        assertEquals("Ali di pietra.", campane.getIndizi().get(1).getTesto());
        assertThrows(IllegalArgumentException.class, () -> IndagineRichiesta.da(CAMPANE.replace("COLPEVOLE=2", "COLPEVOLE=4")));
        assertThrows(IllegalArgumentException.class, () -> IndagineRichiesta.da(CAMPANE.replace("SOSPETTI=Il campanaro/I gargoyle/Le arpie", "SOSPETTI=Il campanaro")));
        assertThrows(IllegalArgumentException.class, () -> IndagineRichiesta.da(CAMPANE.replace("INDIZIO_2=TEMPIO:", "INDIZIO_2=CITTA_NYENA:")));
        assertThrows(IllegalArgumentException.class, () -> IndagineRichiesta.da(CAMPANE.replace("INDIZIO_2=TEMPIO:", "INDIZIO_2=")));
    }

    @Test
    void gliIndiziPortanoAiGargoyleCheVannoSconfittiNelleRovine() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(201)) {
            LIndagine indagine = prendiLIncarico(partita);
            raccogliGliIndizi(partita, indagine);
            assertEquals("Hai trovato tutti gli indizi. Chi è stato? Indizio 1: Volavano verso le rovine. Indizio 2: Ali di pietra.",
                    indagine.getDescrizione());

            Passo accusa = indagine.getDomandaDaPorre(Passo.MomentoControllo.IN_LOCAZIONE);
            assertNotNull(accusa);
            assertEquals("Chi è stato?", accusa.getDomanda());
            indagine.rispondi("2");
            indagine.controllaInLocazione();
            assertEquals("CATTURA", indagine.getPassoCorrente());
            assertTrue(partita.testi().contains("Sono i gargoyle. Il nascondiglio è segnato sulla mappa."), String.valueOf(partita.testi()));
            CoordinateMD nascondiglio = indagine.getPosto();
            assertEquals(ClassiLocazione.ROVINE, Foresta.getLocazione(nascondiglio));
            assertTrue(Foresta.isLocazioneConosciuta(nascondiglio));
            assertEquals(3, RegistroMissioni.getIncontroMissione(nascondiglio).orElseThrow(AssertionError::new).size());

            partita.gruppo().setCoordinate(nascondiglio);
            for (int i = 0; i < 3; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GARGOYLE));
            }
            indagine.controllaPostLocazione();
            assertEquals("RITORNO", indagine.getPassoCorrente());
            assertTrue(partita.testi().contains("Le campane sono salve. Il sacerdote aspetta a Nyena."), String.valueOf(partita.testi()));

            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA));
            int monete = partita.gruppo().getMonete();
            indagine.controllaPreLocazione();
            indagine.segnaIntermezzoPassoMostrato("RITORNO");
            indagine.controllaInLocazione();
            assertEquals(monete + 35, partita.gruppo().getMonete());
            assertTrue(indagine.isCompleta());
            assertEquals("Qui c'erano le campane.", indagine.getRicordoDellaLocazione());
            assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof LIndagine && m != indagine));
        }
    }

    @Test
    void accusareUnInnocenteFaFallireLaMissione() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(202)) {
            LIndagine indagine = prendiLIncarico(partita);
            raccogliGliIndizi(partita, indagine);
            indagine.rispondi("3");
            indagine.controllaInLocazione();
            assertTrue(indagine.isFallita());
            assertTrue(partita.testi().contains("Vi siete sbagliati."), String.valueOf(partita.testi()));
            assertNull(indagine.getRicordoDellaLocazione());
        }
    }

    private static LIndagine prendiLIncarico(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
        LIndagine indagine = RegistroMissioni.getTutteLeMissioni().stream().filter(LIndagine.class::isInstance)
                .map(LIndagine.class::cast).findFirst().orElseThrow(AssertionError::new);
        indagine.aggiungiProprieta("PARAMETRO_" + LIndagine.INDAGINE, CAMPANE);
        indagine.controllaPreLocazione();
        indagine.segnaIntermezzoPassoMostrato("INCARICO");
        indagine.controllaInLocazione();
        assertTrue(indagine.isAttiva());
        return indagine;
    }

    /**
     * Il primo indizio in una locanda, il secondo in un tempio: ciascuno segnato sulla mappa quando tocca a lui.
     */
    private static void raccogliGliIndizi(PartitaDiTest partita, LIndagine indagine) {
        assertEquals("INDIZIO_1", indagine.getPassoCorrente());
        assertTrue(indagine.getDescrizione().startsWith("Il sacerdote di Nyena ti ha chiesto di indagare: cerca il primo indizio"),
                indagine.getDescrizione());
        assertTrue(partita.testi().contains("Il primo indizio va cercato in una locanda: il posto è segnato sulla mappa."),
                String.valueOf(partita.testi()));
        CoordinateMD locanda = indagine.getPosto();
        assertEquals(ClassiLocazione.LOCANDA, Foresta.getLocazione(locanda));
        assertTrue(Foresta.isLocazioneConosciuta(locanda));

        // Altrove l'indizio non c'è
        indagine.controllaInLocazione();
        assertEquals("INDIZIO_1", indagine.getPassoCorrente());

        partita.gruppo().setCoordinate(locanda);
        indagine.controllaInLocazione();
        assertEquals("INDIZIO_2", indagine.getPassoCorrente());
        assertTrue(partita.testi().contains("Volavano verso le rovine."), String.valueOf(partita.testi()));
        assertTrue(indagine.getDescrizione().endsWith("Indizio 1: Volavano verso le rovine."), indagine.getDescrizione());
        CoordinateMD tempio = indagine.getPosto();
        assertEquals(ClassiLocazione.TEMPIO, Foresta.getLocazione(tempio));

        partita.gruppo().setCoordinate(tempio);
        indagine.controllaInLocazione();
        assertEquals(LIndagine.ACCUSA, indagine.getPassoCorrente());
        assertTrue(partita.testi().contains("Ali di pietra."), String.valueOf(partita.testi()));
    }
}
