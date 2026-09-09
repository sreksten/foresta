package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Stefano Reksten
 */
public class TestGenerazionePersonaggi {

    @Test
    public void testValoriMedi() {

        for (ClassePersonaggio classe : ClassePersonaggio.values()) {
            Personaggio p = classe.getIstanza(1);
            assertNotNull(p);
        }
    }

}
