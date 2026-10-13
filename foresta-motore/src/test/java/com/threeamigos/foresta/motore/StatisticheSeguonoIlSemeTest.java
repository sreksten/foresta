package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Le statistiche di un personaggio nuovo si tirano con {@link Dado}: con lo stesso seme escono sempre uguali (prima il lanciatore
 * usava un generatore suo, e i test che dipendevano dalle statistiche erano instabili).
 */
class StatisticheSeguonoIlSemeTest {

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    private static int[] statistiche(long seme, TipoPersonaggio classe) {
        Dado.impostaSeme(seme);
        Personaggio p = FabbricaPersonaggi.crea(classe, 5);
        return new int[] {p.getForza(), p.getDestrezza(), p.getCostituzione(), p.getIntelligenza(), p.getSaggezza(), p.getCarisma(), p.getFortuna()};
    }

    @Test
    void conLoStessoSemeLeStatisticheSonoLeStesse() {
        for (long seme = 1; seme <= 20; seme++) {
            assertEquals(java.util.Arrays.toString(statistiche(seme, TipoPersonaggio.LADRO)),
                    java.util.Arrays.toString(statistiche(seme, TipoPersonaggio.LADRO)), "seme " + seme);
        }
    }
}
