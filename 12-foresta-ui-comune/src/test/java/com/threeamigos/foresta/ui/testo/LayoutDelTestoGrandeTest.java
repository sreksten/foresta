package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutDelTestoGrandeTest {

	@Test
	void unTestoCortoStaSuUnaRigaEUnoLungoVaACapo() {
		assertEquals(Arrays.asList("UN TESTO"), LayoutDelTestoGrande.righe("UN TESTO", 1000));
		assertTrue(LayoutDelTestoGrande.righe("UN TESTO MOLTO LUNGO CHE NON STA", LayoutDelTestoGrande.larghezza("UN TESTO")).size() > 1);
	}

	@Test
	void laLarghezzaEQuellaDeiCaratteriPiuLoSpazioFraDiLoro() {
		int atteso = 0;
		for (char c : "AB 1".toCharArray()) {
			atteso += LayoutDelTestoGrande.larghezzaCarattere(c) + LayoutDelTestoGrande.SPAZIO_FRA_CARATTERI;
		}
		assertEquals(atteso, LayoutDelTestoGrande.larghezza("AB 1"));
	}

	@Test
	void leMinuscoleSiScrivonoConLeMaiuscole() {
		assertEquals(LayoutDelTestoGrande.larghezza("ABC"), LayoutDelTestoGrande.larghezza("abc"));
	}

	@Test
	void limmagineHaLaLarghezzaChiestaEPixelDisegnati() {
		ImmagineDiPixel immagine = LayoutDelTestoGrande.immagine("12", 80, true);
		assertEquals(80, immagine.getLarghezza());
		assertEquals(LayoutDelTestoGrande.altezzaConAccenti(), immagine.getAltezza());
		int visibili = 0;
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				if ((immagine.getArgb(x, y) >>> 24) != 0) {
					visibili++;
				}
			}
		}
		assertTrue(visibili > 20);
	}

	@Test
	void lAloneAllargaDiRaggioPerLatoEScuriscePiano() {
		ImmagineDiPixel immagine = LayoutDelTestoGrande.immagine("1", 20, true);
		ImmagineDiPixel conAlone = LayoutDelTestoGrande.conAlone(immagine, 6);
		assertEquals(immagine.getLarghezza() + 12, conAlone.getLarghezza());
		assertEquals(immagine.getAltezza() + 12, conAlone.getAltezza());
		// Nell'angolo, lontano dalle lettere, non c'è alone
		assertEquals(0, conAlone.getArgb(0, 0) >>> 24);
		ImmagineDiPixel scura = LayoutDelTestoGrande.scura(new ImmagineDiPixel(1, 1, new int[]{0xFFC8C8C8}));
		assertEquals(0xFF3C3C3C, scura.getArgb(0, 0));
	}
}
