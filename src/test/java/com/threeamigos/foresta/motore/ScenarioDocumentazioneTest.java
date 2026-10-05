package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.DocumentazioneRichiesta;
import com.threeamigos.foresta.missioni.LaDocumentazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La documentazione: i posti da documentare si segnano sulla mappa uno alla volta; in ognuno si annota che cosa si è
 * trovato; poi si torna dallo studioso a consegnare le note.
 */
class ScenarioDocumentazioneTest {

    private static final String ISCRIZIONI = "CHIAVE=ISCRIZIONI_DI_PROVA;ASPETTO=QUALUNQUE;MANDANTE=lo storico;MONETE=35;"
            + "TITOLO=Le iscrizioni;RICHIESTA=Copiatele.;BATTUTA=Come?;RISPOSTA=Bene.;REPERTO_1=ROVINE:Una corona.;"
            + "REPERTO_2=TEMPIO:Una regina.;RINGRAZIAMENTO=Grazie.";

    @Test
    void ogniDocumentazioneSiLegge() {
        Set<String> chiavi = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            chiavi.add(DocumentazioneRichiesta.da(ProduttoreDiTestiCasuale.rigaDiMissioni("DOCUMENTAZIONE")).getChiave());
        }
        assertTrue(chiavi.size() >= 4, String.valueOf(chiavi));
        assertThrows(IllegalArgumentException.class, () -> DocumentazioneRichiesta.da(ISCRIZIONI.replace("REPERTO_2=TEMPIO:Una regina.;", "")));
        assertThrows(IllegalArgumentException.class, () -> DocumentazioneRichiesta.da(ISCRIZIONI.replace("REPERTO_2=TEMPIO", "REPERTO_2=CITTA_RUUNA")));
    }

    @Test
    void iPostiSiDocumentanoUnoAllaVoltaEPoiSiRiscuote() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(251)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LaDocumentazione documentazione = RegistroMissioni.getTutteLeMissioni().stream().filter(LaDocumentazione.class::isInstance)
                    .map(LaDocumentazione.class::cast).findFirst().orElseThrow(AssertionError::new);
            documentazione.aggiungiProprieta("PARAMETRO_" + LaDocumentazione.DOCUMENTAZIONE, ISCRIZIONI);
            documentazione.controllaPreLocazione();
            documentazione.segnaIntermezzoPassoMostrato("INCARICO");
            documentazione.controllaInLocazione();
            assertTrue(documentazione.isAttiva());
            assertEquals("Lo storico di Nyena ti ha chiesto di documentare due posti: vai in quello segnato sulla mappa.",
                    documentazione.getDescrizione());
            assertTrue(partita.testi().contains("Si comincia fra delle rovine: il posto è segnato sulla mappa."), String.valueOf(partita.testi()));

            CoordinateMD rovine = documentazione.getPosto();
            assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(rovine));
            assertTrue(Foresta.isLocazioneConosciuta(rovine));
            partita.gruppo().setCoordinate(rovine);
            documentazione.controllaInLocazione();
            assertTrue(partita.testi().contains("Una corona."), String.valueOf(partita.testi()));
            assertTrue(documentazione.getDescrizione().endsWith(" 1: Una corona."), documentazione.getDescrizione());

            CoordinateMD tempio = documentazione.getPosto();
            assertEquals(TipoLocazione.TEMPIO, Foresta.getLocazione(tempio));
            partita.gruppo().setCoordinate(tempio);
            documentazione.controllaInLocazione();
            assertEquals("RITORNO", documentazione.getPassoCorrente());
            assertTrue(partita.testi().contains("Una regina. Le note sono complete: lo storico aspetta a Nyena."), String.valueOf(partita.testi()));
            assertEquals("Hai documentato tutto: torna dallo storico a Nyena a consegnare le note. 1: Una corona. 2: Una regina.",
                    documentazione.getDescrizione());

            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            int monete = partita.gruppo().getMonete();
            documentazione.controllaPreLocazione();
            documentazione.segnaIntermezzoPassoMostrato("RITORNO");
            documentazione.controllaInLocazione();
            assertEquals(monete + 35, partita.gruppo().getMonete());
            assertTrue(documentazione.isCompleta());
        }
    }
}
