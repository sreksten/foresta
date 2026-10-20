package com.threeamigos.foresta.modellodati;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le proprietà di locazioni e missioni nei salvataggi: un campo "chiave§valore" per proprietà, in cui il valore può
 * contenere il separatore o essere vuoto e la chiave, scritta dal codice, non può contenere i caratteri riservati.
 */
class MappaProprietaTest {

    private static Map<String, String> rileggi(String riga) throws IOException {
        Map<String, String> riletta = new HashMap<>();
        MappaProprieta.leggi(new LettoreCampi(riga), riletta);
        return riletta;
    }

    @Test
    void ogniProprietaEUnCampoChiaveValore() throws IOException {
        Map<String, String> proprieta = new LinkedHashMap<>();
        proprieta.put("NOME", "Il Boccale Rovesciato");
        proprieta.put("VISITATA", "S");

        String scritto = MappaProprieta.salva(proprieta);

        assertEquals("NOME§Il Boccale Rovesciato|VISITATA§S", scritto);
        assertEquals(proprieta, rileggi(scritto));
    }

    @Test
    void unaMappaVuotaNonScriveNiente() throws IOException {
        assertEquals("", MappaProprieta.salva(Collections.emptyMap()));
        assertTrue(rileggi("").isEmpty());
    }

    @Test
    void ilValorePuoContenereIlSeparatoreOEssereVuoto() throws IOException {
        Map<String, String> proprieta = new LinkedHashMap<>();
        proprieta.put("NOTA", "uno§due");
        proprieta.put("VUOTA", "");

        assertEquals(proprieta, rileggi(MappaProprieta.salva(proprieta)));
    }

    @Test
    void unCampoSenzaSeparatoreEUnaProprietaConIlValoreVuoto() throws IOException {
        assertEquals(Collections.singletonMap("VISITATA", ""), rileggi("VISITATA"));
    }

    @Test
    void iCampiVuotiNonSonoProprietaConLaChiaveVuota() throws IOException {
        Map<String, String> riletta = rileggi("A§1||B§2|");
        assertEquals(2, riletta.size(), String.valueOf(riletta));
        assertEquals("1", riletta.get("A"));
        assertEquals("2", riletta.get("B"));
        assertFalse(riletta.containsKey(""));
    }

    @Test
    void rileggendoSiDimenticaQuelCheCEraPrima() throws IOException {
        Map<String, String> destinazione = new HashMap<>();
        destinazione.put("VECCHIA", "S");
        MappaProprieta.leggi(new LettoreCampi("NUOVA§S"), destinazione);
        assertEquals(Collections.singletonMap("NUOVA", "S"), destinazione);
    }

    @Test
    void unaChiaveConUnCarattereRiservatoEUnErroreDiProgrammazione() {
        for (String chiave : new String[]{"CON§SEPARATORE", "CON|PIPE", "CON\nA_CAPO", "CON\rRITORNO"}) {
            IllegalStateException errore = assertThrows(IllegalStateException.class,
                    () -> MappaProprieta.salva(Collections.singletonMap(chiave, "valore")), chiave);
            assertTrue(errore.getMessage().contains(chiave), errore.getMessage());
        }
    }

    @Test
    void unValoreNullSiRifiutaEUnaProprietaSiTogliendoLa() {
        MissioneMD missione = new MissioneMD();
        LocazioneMD locazione = new LocazioneMD();

        IllegalArgumentException eccezioneMissione = assertThrows(IllegalArgumentException.class, () -> missione.aggiungiProprieta("TEMPO", null));
        assertTrue(eccezioneMissione.getMessage().contains("TEMPO"), eccezioneMissione.getMessage());
        assertThrows(IllegalArgumentException.class, () -> locazione.aggiungiProprieta("TEMPO", null));
        assertNull(missione.ottieniProprieta("TEMPO"));
        assertNull(locazione.ottieniProprieta("TEMPO"));

        // Il valore vuoto invece vale, e la pulizia dei campi resta
        missione.aggiungiProprieta("VUOTO", "");
        missione.aggiungiProprieta("PIPE", "a|b");
        assertEquals("", missione.ottieniProprieta("VUOTO"));
        assertEquals("ab", missione.ottieniProprieta("PIPE"));
    }
}
