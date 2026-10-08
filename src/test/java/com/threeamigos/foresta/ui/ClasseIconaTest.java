package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * La composizione di un'icona: sfondo, ombra nera del disegno spostata di un pixel in basso a destra, disegno.
 */
class ClasseIconaTest {

	private static final int VERDE = 0xFF008000;
	private static final int ROSSO = 0xFFFF0000;
	private static final int NERO = 0xFF000000;

	private static BufferedImage tinta(int larghezza, int altezza, int argb) {
		BufferedImage immagine = new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				immagine.setRGB(x, y, argb);
			}
		}
		return immagine;
	}

	@Test
	void ilDisegnoHaLOmbraNeraUnPixelInBassoADestra() {
		BufferedImage disegno = tinta(2, 2, 0x00000000);
		disegno.setRGB(0, 0, ROSSO);
		BufferedImage composta = ClasseIcona.componi(tinta(4, 4, VERDE), disegno);
		assertEquals(4, composta.getWidth());
		assertEquals(4, composta.getHeight());
		assertEquals(ROSSO, composta.getRGB(0, 0));
		assertEquals(NERO, composta.getRGB(1, 1));
		// Il resto è sfondo: dove il disegno è trasparente non c'è ombra
		assertEquals(VERDE, composta.getRGB(1, 0));
		assertEquals(VERDE, composta.getRGB(0, 1));
		assertEquals(VERDE, composta.getRGB(2, 2));
		assertEquals(VERDE, composta.getRGB(3, 3));
	}

	@Test
	void ilDisegnoSiSovrapponeAllOmbraDelPixelVicino() {
		// Due pixel affiancati: l'ombra del primo cade sotto al secondo, che sta sopra
		BufferedImage disegno = tinta(2, 1, ROSSO);
		BufferedImage composta = ClasseIcona.componi(tinta(3, 3, VERDE), disegno);
		assertEquals(ROSSO, composta.getRGB(0, 0));
		assertEquals(ROSSO, composta.getRGB(1, 0));
		assertEquals(NERO, composta.getRGB(1, 1));
		assertEquals(NERO, composta.getRGB(2, 1));
		assertEquals(VERDE, composta.getRGB(0, 1));
	}

	@Test
	void unOmbraCheEsceDallaSfondoVieneTagliata() {
		// Un disegno largo quanto lo sfondo: l'ombra dell'ultima colonna e dell'ultima riga cade fuori
		BufferedImage composta = ClasseIcona.componi(tinta(2, 2, VERDE), tinta(2, 2, ROSSO));
		assertEquals(2, composta.getWidth());
		assertEquals(ROSSO, composta.getRGB(0, 0));
		assertEquals(ROSSO, composta.getRGB(1, 1));
	}

	@Test
	void lOmbraNonCopreLoSfondoSeIlDisegnoENelCentro() {
		BufferedImage disegno = tinta(3, 3, 0x00000000);
		disegno.setRGB(1, 1, ROSSO);
		BufferedImage composta = ClasseIcona.componi(tinta(3, 3, VERDE), disegno);
		assertEquals(ROSSO, composta.getRGB(1, 1));
		assertEquals(NERO, composta.getRGB(2, 2));
		assertEquals(VERDE, composta.getRGB(0, 0));
	}
}
