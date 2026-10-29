package com.threeamigos.foresta.strumenti;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModalitaDiProvaTest {

    private boolean precedente;

    @BeforeEach
    void ricordaLoStatoPrecedente() {
        precedente = ModalitaDiProva.isAttiva();
    }

    @AfterEach
    void ripristinaLoStatoPrecedente() {
        ModalitaDiProva.setAttiva(precedente);
    }

    @Test
    void siAccendeESiSpegne() {
        ModalitaDiProva.setAttiva(true);
        assertTrue(ModalitaDiProva.isAttiva());
        ModalitaDiProva.setAttiva(false);
        assertFalse(ModalitaDiProva.isAttiva());
    }
}
