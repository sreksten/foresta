package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I tipi di locazione della Foresta: quali sono botteghe, e i nomi propri delle uniche, letti da tipo_locazione_it.properties.
 */
class TipoLocazioneTest {

    @Test
    void leBottegheDellaForestaSonoQuattro() {
        Set<TipoLocazione> botteghe = EnumSet.of(TipoLocazione.ALCHIMISTA, TipoLocazione.ARMAIOLO,
                TipoLocazione.VENDITORE_DI_PERGAMENE, TipoLocazione.INCANTATORE);
        for (TipoLocazione locazione : TipoLocazione.values()) {
            assertEquals(botteghe.contains(locazione), locazione.isBottega(), locazione.name());
        }
    }

    @Test
    void iNomiDelleLocazioniUnicheSonoNelFile() {
        for (TipoLocazione locazione : TipoLocazione.values()) {
            if (locazione.isLocazioneUnica()) {
                assertTrue(locazione.getNomeProprio() != null && !locazione.getNomeProprio().isEmpty(), "manca il nome di " + locazione);
            } else {
                assertNull(locazione.getNomeProprio(), locazione + " non è unica e non ha un nome proprio");
            }
            if (locazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
                assertTrue(locazione.getNomeProprio().endsWith(locazione.getNomeDellaCitta()), locazione.getNomeProprio());
            } else {
                assertNull(locazione.getNomeDellaCitta());
            }
        }
    }

    @Test
    void neiFileCiSonoSoloIVociDelleLocazioniUniche() throws IOException {
        // Il nome proprio per le nove uniche, quello della città per le quattro città
        String nomeDelFile = Testi.nomeDelFile(TipoLocazione.class) + "_it.properties";
        Properties testi = new Properties();
        try (InputStream flusso = TipoLocazione.class.getResourceAsStream(nomeDelFile)) {
            assertNotNull(flusso, "manca il file " + nomeDelFile);
            testi.load(new InputStreamReader(flusso, StandardCharsets.UTF_8));
        }
        Set<String> costanti = new HashSet<>();
        for (TipoLocazione locazione : TipoLocazione.values()) {
            costanti.add(locazione.name());
        }
        Set<String> voci = new HashSet<>(Arrays.asList("NOME_PROPRIO", "NOME_DELLA_CITTA"));
        for (String chiave : testi.stringPropertyNames()) {
            int punto = chiave.indexOf('.');
            assertTrue(punto > 0 && costanti.contains(chiave.substring(0, punto)), "costante sconosciuta in " + chiave);
            assertTrue(voci.contains(chiave.substring(punto + 1)), "voce sconosciuta in " + chiave);
            assertFalse(testi.getProperty(chiave).trim().isEmpty(), "testo vuoto in " + chiave);
        }
        assertEquals(9 + 4, testi.size(), "numero di voci");
    }
}
