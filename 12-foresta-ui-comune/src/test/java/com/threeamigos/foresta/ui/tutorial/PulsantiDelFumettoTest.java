package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Punto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dove stanno i pulsanti e quando un punto è su di loro. Che cosa fa la pressione lo dice lo schermo (pollicePremuto, annullaPremuto): qui si prova
 * solo che fuori dai pulsanti, o senza fumetto, non succede niente.
 */
class PulsantiDelFumettoTest {

	private static final Punto FUMETTO = new Punto(100, 200);
	private static final int LARGHEZZA = 300;
	private static final int ALTEZZA = 150;
	private static final int LATO = 32;

	@Test
	void senzaFumettoNonCiSonoPulsanti() {
		PulsantiDelFumetto pulsanti = new PulsantiDelFumetto(null);
		assertNull(pulsanti.getPollice());
		assertNull(pulsanti.getAnnulla());
		assertFalse(pulsanti.suiPulsanti(FUMETTO.x + LARGHEZZA - 20, FUMETTO.y + ALTEZZA - 20));
		assertFalse(pulsanti.premi(FUMETTO.x + LARGHEZZA - 20, FUMETTO.y + ALTEZZA - 20));
	}

	@Test
	void stannoDoveDiceLaDisposizioneESiRiconosconoAlTocco() {
		PulsantiDelFumetto pulsanti = new PulsantiDelFumetto(null);
		pulsanti.posiziona(FUMETTO, LARGHEZZA, ALTEZZA, LATO);
		Area pollice = DisposizioneDelFumetto.pollice(FUMETTO, LARGHEZZA, ALTEZZA, LATO);
		Area annulla = DisposizioneDelFumetto.annulla(FUMETTO, LARGHEZZA, ALTEZZA, LATO);
		assertEquals(pollice.getX(), pulsanti.getPollice().getX());
		assertEquals(pollice.getY(), pulsanti.getPollice().getY());
		assertEquals(annulla.getX(), pulsanti.getAnnulla().getX());
		assertTrue(pulsanti.suiPulsanti(pollice.getX() + 1, pollice.getY() + 1));
		assertTrue(pulsanti.suiPulsanti(annulla.getX() + 1, annulla.getY() + 1));
		assertFalse(pulsanti.suiPulsanti(FUMETTO.x + 1, FUMETTO.y + 1));
		assertFalse(pulsanti.premi(FUMETTO.x + 1, FUMETTO.y + 1));
	}

	@Test
	void nascostiNonSiPremonoPiu() {
		PulsantiDelFumetto pulsanti = new PulsantiDelFumetto(null);
		pulsanti.posiziona(FUMETTO, LARGHEZZA, ALTEZZA, LATO);
		Area pollice = pulsanti.getPollice();
		pulsanti.nascondi();
		assertFalse(pulsanti.suiPulsanti(pollice.getX() + 1, pollice.getY() + 1));
		assertFalse(pulsanti.premi(pollice.getX() + 1, pollice.getY() + 1));
	}
}
