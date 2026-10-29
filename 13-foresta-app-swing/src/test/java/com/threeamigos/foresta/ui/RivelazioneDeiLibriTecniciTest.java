package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.ui.sprite.SpriteRivelazioneArtefatto;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * La rivelazione grafica dei libri tecnici trovati in un cofano (con l'aspetto del libro) si costruisce e si disegna.
 */
class RivelazioneDeiLibriTecniciTest {

	@Test
	void laRivelazioneDeiDueLibriSiCostruisceESiDisegna() {
		for (String chiave : new String[] {"THE_C_PROGRAMMING_LANGUAGE", "MANUALE_UNIX_SYSTEM_V"}) {
			Artefatto libro = LibroTecnico.getLibro(chiave).orElseThrow(AssertionError::new)
					.costruisci(new GeneratoreArtefattiTabelle(new Random(1)), 4);
			SpriteRivelazioneArtefatto sprite = new SpriteRivelazioneArtefatto(libro, 4, new Rectangle(0, 0, 1000, 700), new Point(100, 100));
			BufferedImage immagine = new BufferedImage(1000, 700, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = immagine.createGraphics();
			sprite.anima(g);
			sprite.disegna(g, 1.5f);
			g.dispose();
		}
	}
}
