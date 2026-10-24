package com.threeamigos.foresta.danni.tipi;

import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TipoDannoTest {

    @Test
    void gliEffettiDiUnTipoDiDannoNonSiCambianoDaFuori() {
        TipoDanno danno = null;
        for (TipoDanno tipo : TipoDanno.values()) {
            if (tipo.hasEffettiDiStato()) {
                danno = tipo;
                break;
            }
        }
        assertNotNull(danno, "almeno un tipo di danno causa degli stati");
        TipoEffettoDiStato[] prima = danno.getEffetti();
        TipoEffettoDiStato[] copia = danno.getEffetti();
        assertNotSame(prima, copia);

        // Chi riceve l'array lo può cambiare senza toccare l'enum
        Arrays.fill(prima, null);

        assertArrayEquals(copia, danno.getEffetti());
        assertNotNull(danno.getEffetti()[0]);
    }

    @Test
    void unTipoDiDannoHaEffettiDiStatoSeNeElencaQualcuno() {
        for (TipoDanno tipo : TipoDanno.values()) {
            assertEquals(tipo.getEffetti().length > 0, tipo.hasEffettiDiStato(), tipo.name());
            assertNotNull(tipo.getCategoriaDanno(), tipo.name());
        }
        assertTrue(Arrays.asList(TipoDanno.SONICO.getEffetti()).contains(TipoEffettoDiStato.ASSORDATO));
    }

    @Test
    void ogniDannoHaIlNomeDalFileDeiTesti() {
        for (TipoDanno tipo : TipoDanno.values()) {
            assertEquals(Testi.testoOCostante(tipo, "NOME"), tipo.getNome(), tipo.name());
            assertNotEquals(tipo.name(), tipo.getNome(), "manca il nome di " + tipo);
        }
        assertEquals("Fuoco", TipoDanno.FUOCO.getNome());
    }
}
