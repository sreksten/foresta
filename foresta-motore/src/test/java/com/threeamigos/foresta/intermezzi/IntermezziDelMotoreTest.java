package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.TipoIntermezzo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Con gli intermezzi registrati dal motore, la fabbrica sa costruire l'intermezzo di ogni TipoIntermezzo.
 */
class IntermezziDelMotoreTest {

    @Test
    void ogniTipoDiIntermezzoHaIlSuoIntermezzo() {
        IntermezziDelMotore.registra();
        for (TipoIntermezzo tipo : TipoIntermezzo.values()) {
            assertTrue(FabbricaIntermezzi.isRegistrato(tipo), tipo.name());
        }
    }
}
