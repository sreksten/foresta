package com.threeamigos.foresta.ui.sprite;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * L'alfabeto grande ha solo le maiuscole: un annuncio col nome di una missione in maiuscole e minuscole ("Defeat the
 * Dragon") deve venire come lo stesso nome tutto maiuscolo, non perdere le minuscole.
 */
class SpriteAnnuncioGlobaleMinuscoleTest {

	private static BufferedImage immagine(String titolo, String descrizione) {
		return new SpriteAnnuncioGlobale(titolo, descrizione, 1024, 768).buildImage();
	}

	@Test
	void leMinuscoleSiDisegnanoComeLeMaiuscole() {
		BufferedImage misto = immagine("New mission", "Defeat the Dragon");
		BufferedImage maiuscolo = immagine("NEW MISSION", "DEFEAT THE DRAGON");
		assertEquals(maiuscolo.getWidth(), misto.getWidth());
		assertEquals(maiuscolo.getHeight(), misto.getHeight());
		for (int y = 0; y < misto.getHeight(); y++) {
			for (int x = 0; x < misto.getWidth(); x++) {
				assertEquals(maiuscolo.getRGB(x, y), misto.getRGB(x, y), "pixel " + x + "," + y);
			}
		}
	}
}
