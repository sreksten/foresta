package com.threeamigos.foresta.ui.immagini;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecchiareImmaginiTest {

	@Test
	void specchiareRovesciaLeColonneESpecchiandoDueVolteSiTornaAllOriginale() {
		BufferedImage originale = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
		originale.setRGB(0, 0, 0xFFFF0000);
		originale.setRGB(2, 1, 0xFF00FF00);
		BufferedImage specchiata = ImmaginiAwt.specchia(originale);
		assertEquals(0xFFFF0000, specchiata.getRGB(2, 0));
		assertEquals(0xFF00FF00, specchiata.getRGB(0, 1));
		assertEquals(0, specchiata.getRGB(0, 0));
		BufferedImage ritorno = ImmaginiAwt.specchia(specchiata);
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 3; x++) {
				assertEquals(originale.getRGB(x, y), ritorno.getRGB(x, y));
			}
		}
	}
}
