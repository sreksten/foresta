package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.testo.CaratteriDelFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisposizioneDeiGlifiTest {

	@Test
	void iGlifiVannoACapoQuandoLaRigaEPiena() {
		DisposizioneDeiGlifi d = DisposizioneDeiGlifi.di(new int[]{10, 10, 10}, 5, 25);
		// Un pixel di margine: 1 + 10 + 1 + 10 = 22, il terzo non sta nei 25 pixel e va sotto
		assertEquals(1, d.getPosto(0).x);
		assertEquals(12, d.getPosto(1).x);
		assertEquals(1, d.getPosto(2).x);
		assertEquals(1, d.getPosto(0).y);
		assertEquals(1, d.getPosto(1).y);
		assertEquals(7, d.getPosto(2).y);
		assertEquals(23, d.getLarghezza());
		assertEquals(13, d.getAltezza());
	}

	@Test
	void unGlifoPiuLargoDellaPaginaNonEntra() {
		assertThrows(IllegalArgumentException.class, () -> DisposizioneDeiGlifi.di(new int[]{30}, 5, 25));
	}

	@Test
	void iGlifiVeriDeiDueFontStannoInUnaPaginaDaMilleVentiquattroSenzaSovrapporsi() {
		for (DoomdarkFont font : new DoomdarkFont[]{DoomdarkFont.piccolo(), DoomdarkFont.medio()}) {
			StringBuilder caratteri = new StringBuilder();
			for (String riga : CaratteriDelFont.RIGHE) {
				for (char c : riga.toCharArray()) {
					if (font.supportaIlCarattere(c)) {
						caratteri.append(c);
					}
				}
			}
			int[] larghezze = new int[caratteri.length()];
			for (int i = 0; i < larghezze.length; i++) {
				larghezze[i] = font.getGlyphWidth(caratteri.charAt(i));
			}
			DisposizioneDeiGlifi d = DisposizioneDeiGlifi.di(larghezze, font.getHeight(), 1024);
			assertTrue(d.getLarghezza() <= 1024 && d.getAltezza() <= 1024, "pagina " + d.getLarghezza() + "x" + d.getAltezza());
			boolean[][] occupato = new boolean[d.getAltezza()][d.getLarghezza()];
			for (int i = 0; i < larghezze.length; i++) {
				DisposizioneDeiGlifi.Posto p = d.getPosto(i);
				for (int y = p.y; y < p.y + font.getHeight(); y++) {
					for (int x = p.x; x < p.x + larghezze[i]; x++) {
						assertTrue(!occupato[y][x], "il glifo " + caratteri.charAt(i) + " si sovrappone");
						occupato[y][x] = true;
					}
				}
			}
		}
	}
}
