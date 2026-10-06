package com.threeamigos.foresta.ui;

import java.awt.image.BufferedImage;

/**
 * Ponte verso i membri protected di DisegnatoreMappa, per un test di riproduzione
 * bug che vive nel pacchetto motore (dove si può chiamare Foresta.reimposta()).
 */
public class AccessoMappaGeneralePerTest {

	public static BufferedImage mappaGenerale() {
		return DisegnatoreMappa.ottieniMappaGenerale();
	}

	public static int larghezzaIcona() {
		return DisegnatoreMappa.LARGHEZZA_ICONA;
	}

	public static int altezzaIcona() {
		return DisegnatoreMappa.ALTEZZA_ICONA;
	}
}
