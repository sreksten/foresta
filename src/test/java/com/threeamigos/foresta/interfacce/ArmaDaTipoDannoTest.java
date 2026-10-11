package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.CatalogoLeggendari;
import com.threeamigos.foresta.tipi.TipoDanno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un'arma "dà" un tipo di danno se è il suo, o se è quello di uno dei suoi incantamenti.
 */
class ArmaDaTipoDannoTest {

    @BeforeEach
    void preparaModello() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    private static Arma arma(TipoDanno tipo, IncantamentoMD... incantamenti) {
        return new Arma() {
            @Override public int getDanni() { return 10; }
            @Override public int getLivello() { return 1; }
            @Override public TipoDanno getTipoDanno() { return tipo; }
            @Override public boolean isIncantata() { return incantamenti.length > 0; }
            @Override public Collection<IncantamentoMD> getIncantamenti() { return Arrays.asList(incantamenti); }
        };
    }

    @Test
    void daIlSuoTipoDanno() {
        Arma spada = arma(TipoDanno.TAGLIENTE);
        assertTrue(spada.daTipoDanno(TipoDanno.TAGLIENTE));
        assertFalse(spada.daTipoDanno(TipoDanno.SACRO));
    }

    @Test
    void daAncheQuelloDeiSuoiIncantamenti() {
        Arma spada = arma(TipoDanno.TAGLIENTE,
                new IncantamentoMD("Luce", TipoDanno.SACRO, 5, 0.1d), new IncantamentoMD("Fiamma", TipoDanno.FUOCO, 5, 0.1d));
        assertTrue(spada.daTipoDanno(TipoDanno.TAGLIENTE));
        assertTrue(spada.daTipoDanno(TipoDanno.SACRO));
        assertTrue(spada.daTipoDanno(TipoDanno.FUOCO));
        assertFalse(spada.daTipoDanno(TipoDanno.GELO));
    }

    @Test
    void laSpadaDellaMorteDaIlNecroticoPerIlSuoIncantamento() {
        OggettoLeggendario leggendario = CatalogoLeggendari.getLeggendario("SPADA_DI_ROMY_JONA").orElseThrow(AssertionError::new);
        Arma spada = (Arma) leggendario.costruisci();
        assertTrue(spada.daTipoDanno(TipoDanno.NECROTICO));
        assertFalse(spada.daTipoDanno(TipoDanno.SACRO));
    }
}
