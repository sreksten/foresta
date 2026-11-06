package com.threeamigos.foresta.ui.immagini;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * La composizione di un'icona: sfondo, ombra nera del disegno spostata di un pixel in basso a destra, disegno.
 */
class TipoIconaTest {
	/**
	 * Un'immagine che si può modificare, per preparare i dati della prova
	 */
	static final class Tela {
		private final int larghezza;
		private final int altezza;
		private final int[] pixel;

		Tela(int larghezza, int altezza) {
			this.larghezza = larghezza;
			this.altezza = altezza;
			this.pixel = new int[larghezza * altezza];
		}

		void setRGB(int x, int y, int argb) {
			pixel[y * larghezza + x] = argb;
		}

		ImmagineDiPixel immagine() {
			return new ImmagineDiPixel(larghezza, altezza, pixel.clone());
		}
	}


	private static final int VERDE = 0xFF008000;
	private static final int ROSSO = 0xFFFF0000;
	private static final int NERO = 0xFF000000;

	private static Tela tinta(int larghezza, int altezza, int argb) {
		Tela immagine = new Tela(larghezza, altezza);
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				immagine.setRGB(x, y, argb);
			}
		}
		return immagine;
	}

	@Test
	void ilDisegnoHaLOmbraNeraUnPixelInBassoADestra() {
		Tela disegno = tinta(2, 2, 0x00000000);
		disegno.setRGB(0, 0, ROSSO);
		ImmagineDiPixel composta = ComposizioneDelleIcone.componi(tinta(4, 4, VERDE).immagine(), disegno.immagine());
		assertEquals(4, composta.getLarghezza());
		assertEquals(4, composta.getAltezza());
		assertEquals(ROSSO, composta.getArgb(0, 0));
		assertEquals(NERO, composta.getArgb(1, 1));
		// Il resto è sfondo: dove il disegno è trasparente non c'è ombra
		assertEquals(VERDE, composta.getArgb(1, 0));
		assertEquals(VERDE, composta.getArgb(0, 1));
		assertEquals(VERDE, composta.getArgb(2, 2));
		assertEquals(VERDE, composta.getArgb(3, 3));
	}

	@Test
	void ilDisegnoSiSovrapponeAllOmbraDelPixelVicino() {
		// Due pixel affiancati: l'ombra del primo cade sotto al secondo, che sta sopra
		Tela disegno = tinta(2, 1, ROSSO);
		ImmagineDiPixel composta = ComposizioneDelleIcone.componi(tinta(3, 3, VERDE).immagine(), disegno.immagine());
		assertEquals(ROSSO, composta.getArgb(0, 0));
		assertEquals(ROSSO, composta.getArgb(1, 0));
		assertEquals(NERO, composta.getArgb(1, 1));
		assertEquals(NERO, composta.getArgb(2, 1));
		assertEquals(VERDE, composta.getArgb(0, 1));
	}

	@Test
	void unOmbraCheEsceDallaSfondoVieneTagliata() {
		// Un disegno largo quanto lo sfondo: l'ombra dell'ultima colonna e dell'ultima riga cade fuori
		ImmagineDiPixel composta = ComposizioneDelleIcone.componi(tinta(2, 2, VERDE).immagine(), tinta(2, 2, ROSSO).immagine());
		assertEquals(2, composta.getLarghezza());
		assertEquals(ROSSO, composta.getArgb(0, 0));
		assertEquals(ROSSO, composta.getArgb(1, 1));
	}

	@Test
	void lOmbraNonCopreLoSfondoSeIlDisegnoENelCentro() {
		Tela disegno = tinta(3, 3, 0x00000000);
		disegno.setRGB(1, 1, ROSSO);
		ImmagineDiPixel composta = ComposizioneDelleIcone.componi(tinta(3, 3, VERDE).immagine(), disegno.immagine());
		assertEquals(ROSSO, composta.getArgb(1, 1));
		assertEquals(NERO, composta.getArgb(2, 2));
		assertEquals(VERDE, composta.getArgb(0, 0));
	}
}
