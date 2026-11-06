package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Chi aveva chiesto un testo al Prompt può smettere di aspettarlo, ma solo se il Prompt aspetta ancora proprio la sua risposta.
 */
class PromptAnnullaTest {

    @Test
    void annullaChiudeSoloLaRichiestaGiusta() {
        Prompt prompt = new Prompt();
        Consumer<String> mia = testo -> { };
        Consumer<String> altra = testo -> { };
        prompt.getCampo().mostra("nome", mia);
        assertTrue(prompt.isVisible());

        prompt.getCampo().annulla(altra);
        assertTrue(prompt.isVisible(), "un'altra richiesta non chiude questa");

        prompt.getCampo().annulla(mia);
        assertFalse(prompt.isVisible());

        // Già chiuso: non fa niente, e non riapre
        prompt.getCampo().annulla(mia);
        assertFalse(prompt.isVisible());
    }
}
