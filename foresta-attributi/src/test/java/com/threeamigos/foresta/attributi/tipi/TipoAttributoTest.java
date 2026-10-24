package com.threeamigos.foresta.attributi.tipi;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TipoAttributoTest {

    @Test
    void unAttributoEPrimarioOSecondarioSecondoLaSuaCategoria() {
        for (TipoAttributo attributo : TipoAttributo.values()) {
            assertEquals(attributo.getCategoriaAttributo() == CategoriaAttributo.PRIMARIO, attributo.isPrimario(), attributo.name());
            assertEquals(attributo.getCategoriaAttributo() == CategoriaAttributo.SECONDARIO, attributo.isSecondario(), attributo.name());
        }
        assertTrue(TipoAttributo.FORZA.isPrimario());
    }

    @Test
    void ogniAttributoHaNomeEDescrizioneDalFileDeiTesti() {
        for (TipoAttributo attributo : TipoAttributo.values()) {
            assertEquals(Testi.testoOCostante(attributo, "NOME"), attributo.getNome(), attributo.name());
            assertEquals(Testi.testoOCostante(attributo, "DESCRIZIONE"), attributo.getDescrizione(), attributo.name());
            assertNotEquals(attributo.name(), attributo.getNome(), "manca il nome di " + attributo);
            assertFalse(attributo.getDescrizione().trim().isEmpty(), attributo.name());
        }
        assertEquals("Forza", TipoAttributo.FORZA.getNome());
    }
}
