package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratoreDiNuvoleTest {

	private static int pixelVisibili(ImmagineDiPixel immagine) {
		int visibili = 0;
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				if ((immagine.getArgb(x, y) >>> 24) != 0) {
					visibili++;
				}
			}
		}
		return visibili;
	}

	@Test
	void laNuvolaHaLaMisuraChiestaEPixelSemitrasparentiBianchi() {
		ImmagineDiPixel nuvola = GeneratoreDiNuvole.genera(120, 60, new Random(1));
		assertEquals(120, nuvola.getLarghezza());
		assertEquals(60, nuvola.getAltezza());
		assertTrue(pixelVisibili(nuvola) > 500, "ci sono pixel disegnati");
		for (int y = 0; y < 60; y++) {
			for (int x = 0; x < 120; x++) {
				int argb = nuvola.getArgb(x, y);
				if ((argb >>> 24) != 0) {
					// Bianchi o appena più freddi, mai opachi del tutto (l'alfa massima è quella del centro, sommata fra soffi sovrapposti)
					assertTrue(((argb >> 16) & 0xFF) >= 240 && ((argb >> 8) & 0xFF) >= 240 && (argb & 0xFF) >= 240);
				}
			}
		}
	}

	@Test
	void unaNuvolaMinuscolaNonDaErrori() {
		assertEquals(1, GeneratoreDiNuvole.genera(1, 1, new Random(2)).getLarghezza());
		assertEquals(3, GeneratoreDiNuvole.genera(3, 1, new Random(3)).getLarghezza());
	}

	@Test
	void laSfumaturaVaDalCentroOpacoAlBordoTrasparente() {
		assertEquals(180, GeneratoreDiNuvole.sfumatura(0.0) >>> 24);
		assertEquals(90, GeneratoreDiNuvole.sfumatura(0.8) >>> 24);
		assertEquals(0, GeneratoreDiNuvole.sfumatura(1.0) >>> 24);
		assertEquals(0, GeneratoreDiNuvole.sfumatura(2.0) >>> 24);
		assertEquals(135, GeneratoreDiNuvole.sfumatura(0.4) >>> 24);
	}

	@Test
	void sopraUnPixelTrasparenteResta() {
		assertEquals(0xB4FFFFFF, GeneratoreDiNuvole.sopra(0xB4FFFFFF, 0));
		assertEquals(0, GeneratoreDiNuvole.sopra(0, 0));
	}

	@Test
	void laNuvolaSiMuoveVersoDestraERientraDaSinistra() {
		ImmagineDiPixel disegno = new ImmagineDiPixel(10, 5, new int[50]);
		Nuvola nuvola = new Nuvola(disegno, 90, 0, 20, new Area(0, 0, 100, 40));
		nuvola.aggiorna(0.25f);
		assertEquals(95, nuvola.getX());
		nuvola.aggiorna(1f);
		assertEquals(-10, nuvola.getX(), "esce a destra e rientra appena prima del bordo sinistro");
		assertTrue(nuvola.getY() >= 0 && nuvola.getY() <= 35);
	}
}
