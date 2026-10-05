package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.LOggettoSmarrito;
import com.threeamigos.foresta.missioni.OggettoSmarrito;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'oggetto smarrito: si cerca a non più di due passi da un posto segnato sulla mappa, e si riporta a chi l'ha perso.
 */
class ScenarioOggettoSmarritoTest {

    private static final String FEDE = "F;fede nuziale;il mugnaio;ROVINE;15;Al ritorno la fede non c'era più.;E se se ne accorge?;Meglio di no.;Eccola!";

    @Test
    void ogniOggettoSmarritoSiLegge() {
        Set<String> oggetti = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            OggettoSmarrito smarrito = OggettoSmarrito.da(ProduttoreDiTestiCasuale.oggettoSmarrito());
            assertFalse("aeiou".contains(smarrito.getOggetto().substring(0, 1)), smarrito.getOggetto());
            assertFalse(smarrito.getPosti().isEmpty());
            assertFalse(smarrito.getRingraziamento().isEmpty());
            oggetti.add(smarrito.getOggetto());
        }
        assertTrue(oggetti.size() >= 8, String.valueOf(oggetti));

        assertEquals("la fede nuziale", OggettoSmarrito.da(FEDE).getOggettoConArticolo());
        assertEquals("persa", OggettoSmarrito.da(FEDE).getPerso());
        assertThrows(IllegalArgumentException.class, () -> OggettoSmarrito.da("F;a;b;CASTELLO_DRAGO;3;c;d;e;f"));
        assertThrows(IllegalArgumentException.class, () -> OggettoSmarrito.da("F;a;b;ROVINE;3;c;d;e"));
    }

    @Test
    void laFedeDelMugnaioStaADuePassiDalleRovineEVaRiportata() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(191)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            LOggettoSmarrito smarrito = prendiLIncarico();

            CoordinateMD posto = smarrito.getPosto();
            CoordinateMD casella = smarrito.getCasella();
            assertEquals(ClassiLocazione.ROVINE, Foresta.getLocazione(posto));
            assertTrue(Foresta.isLocazioneConosciuta(posto));
            assertTrue(Math.abs(casella.getX() - posto.getX()) + Math.abs(casella.getY() - posto.getY()) <= LOggettoSmarrito.RAGGIO);
            ClassiLocazione classe = Foresta.getLocazione(casella);
            assertTrue(classe.getTipoLocazione() == ClassiLocazione.TipoLocazione.STANDARD && classe != ClassiLocazione.LOCANDA, String.valueOf(classe));
            assertEquals("La fede nuziale del mugnaio", smarrito.getNome());
            assertTrue(smarrito.getDescrizione().startsWith("Cerca la fede nuziale che il mugnaio di Nyena ha perso vicino"), smarrito.getDescrizione());

            // La fede sta in quella casella, anche se già visitata, e in nessun'altra
            assertTrue(smarrito.getOggettoInLocazione(casella, classe, true).isPresent());
            CoordinateMD altrove = new CoordinateMD(casella.getX() + LOggettoSmarrito.RAGGIO * 2 + 1, casella.getY());
            for (int i = 0; i < 50; i++) {
                assertFalse(smarrito.getOggettoInLocazione(altrove, classe, false).isPresent());
            }

            // Niente ripiego, anche dopo giorni
            LineaTemporale.aggiungiOre(200);
            smarrito.controllaPreLocazione();
            assertNull(smarrito.getRipiego(smarrito.getOggettoDaCercare()));

            new OggettoMissione(smarrito.getId(), LOggettoSmarrito.OGGETTO, smarrito.getSmarrito().getNome(), 1).prendi(partita.gruppo(), null);
            smarrito.controllaPostLocazione();
            assertEquals("Hai trovato la fede nuziale: riportala al mugnaio di Nyena.", smarrito.getDescrizione());

            int monete = partita.gruppo().getMonete();
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA));
            smarrito.controllaPreLocazione();
            smarrito.segnaIntermezzoPassoMostrato("RITORNO");
            smarrito.controllaInLocazione();
            assertEquals(monete + 15, partita.gruppo().getMonete());
            List<String> testi = partita.testi();
            assertTrue(testi.contains("La fede nuziale torna al mugnaio."), String.valueOf(testi));
            assertTrue(smarrito.isCompleta());
            assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof LOggettoSmarrito && m != smarrito));
        }
    }

    private static LOggettoSmarrito prendiLIncarico() {
        LOggettoSmarrito smarrito = RegistroMissioni.getTutteLeMissioni().stream().filter(LOggettoSmarrito.class::isInstance)
                .map(LOggettoSmarrito.class::cast).findFirst().orElseThrow(AssertionError::new);
        smarrito.aggiungiProprieta("PARAMETRO_" + LOggettoSmarrito.SMARRITO, FEDE);
        smarrito.controllaPreLocazione();
        smarrito.segnaIntermezzoPassoMostrato("INCARICO");
        smarrito.controllaInLocazione();
        assertTrue(smarrito.isAttiva());
        return smarrito;
    }
}
