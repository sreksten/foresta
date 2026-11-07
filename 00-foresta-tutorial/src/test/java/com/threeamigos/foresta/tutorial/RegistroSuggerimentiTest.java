package com.threeamigos.foresta.tutorial;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroSuggerimentiTest {

	@AfterEach
	void riaccende() {
		RegistroSuggerimenti.setAttivo(true);
	}

	@Test
	void ilTutorialPartePerDefaultAcceso() {
		assertTrue(RegistroSuggerimenti.isAttivo());
	}

	@Test
	void siAccendeESiSpegne() {
		RegistroSuggerimenti.setAttivo(false);
		assertFalse(RegistroSuggerimenti.isAttivo());
		RegistroSuggerimenti.setAttivo(true);
		assertTrue(RegistroSuggerimenti.isAttivo());
	}
}
