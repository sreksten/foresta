package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gli slot di salvataggio stanno uno ogni 100 pixel; su uno schermo basso (il telefono, alto 505) si avvicinano, perché anche il quinto, il salvataggio
 * automatico, si veda (layout_telefono.md §5).
 */
class SlotDiSalvataggioSulTelefonoTest {

	@Test
	void sulloSchermoDiSempreGliSlotStannoDoveStavano() {
		SchermateSenzaPartita schermate = new SchermateSenzaPartita(1022, 732, null, null);
		for (int id = 1; id <= 5; id++) {
			assertEquals(50 + 100 * id, schermate.getCoordinataY(id));
		}
	}

	@Test
	void sulTelefonoAncheIlQuintoSlotStaSulloSchermo() {
		SchermateSenzaPartita schermate = new SchermateSenzaPartita(770, 505, null, null);
		assertTrue(schermate.getCoordinataY(5) + LayoutDelTestoGrande.ALTEZZA_RIGA <= 505);
		assertTrue(schermate.getCoordinataY(1) > 50 + LayoutDelTestoGrande.ALTEZZA_RIGA, "sotto la scritta in cima");
	}
}
