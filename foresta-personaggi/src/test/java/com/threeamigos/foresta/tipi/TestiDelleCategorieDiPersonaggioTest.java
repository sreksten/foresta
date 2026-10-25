package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il nome di ogni categoria di personaggio, letto da categoria_personaggio_it.properties: ogni costante ha il suo, e nel file non ce ne
 * sono per costanti che non esistono.
 */
class TestiDelleCategorieDiPersonaggioTest {

    @Test
    void ogniCategoriaHaIlSuoNome() {
        for (CategoriaPersonaggio categoria : CategoriaPersonaggio.values()) {
            assertTrue(Testi.testo(categoria, "NOME").filter(t -> !t.trim().isEmpty()).isPresent(), "manca " + categoria + ".NOME");
        }
    }

    @Test
    void nelFileNonCiSonoVociPerCostantiOVociCheNonEsistono() throws IOException {
        String nomeDelFile = Testi.nomeDelFile(CategoriaPersonaggio.class) + "_it.properties";
        Properties testi = new Properties();
        try (InputStream flusso = CategoriaPersonaggio.class.getResourceAsStream(nomeDelFile)) {
            assertNotNull(flusso, "manca il file " + nomeDelFile);
            testi.load(new InputStreamReader(flusso, StandardCharsets.UTF_8));
        }
        Set<String> costanti = new HashSet<>();
        for (CategoriaPersonaggio categoria : CategoriaPersonaggio.values()) {
            costanti.add(categoria.name());
        }
        for (String chiave : testi.stringPropertyNames()) {
            int punto = chiave.indexOf('.');
            assertTrue(punto > 0 && costanti.contains(chiave.substring(0, punto)), "costante sconosciuta in " + chiave);
            assertEquals("NOME", chiave.substring(punto + 1), "voce sconosciuta in " + chiave);
            assertFalse(testi.getProperty(chiave).trim().isEmpty(), "testo vuoto in " + chiave);
        }
        assertEquals(CategoriaPersonaggio.values().length, testi.size());
    }
}
