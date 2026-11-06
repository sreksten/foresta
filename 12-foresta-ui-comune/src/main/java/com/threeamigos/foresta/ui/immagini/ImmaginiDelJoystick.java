package com.threeamigos.foresta.ui.immagini;

import com.threeamigos.foresta.ui.componenti.JoystickVirtuale;

/**
 * Le immagini del {@link JoystickVirtuale}, calcolate pixel per pixel senza toolkit: il disco (pieno e blu nella zona centrale, dello stesso blu che sfuma fino
 * alla trasparenza all'esterno, con il bordo della zona centrale appena più chiaro) e l'indicatore di dove sta il puntatore. Il disco è quadrato, con il
 * centro del joystick al centro: l'angolo in alto a sinistra sta a (centroX - RAGGIO, centroY - RAGGIO).
 */
public final class ImmaginiDelJoystick {

	private static final int ALFA_ZONA_CENTRALE = 200;
	private static final int BLU = 0x286EFF;
	private static final int BLU_CHIARO = 0xAAD2FF;
	private static final int ALFA_BLU_CHIARO = 220;

	private ImmaginiDelJoystick() {
	}

	public static ImmagineDiPixel disco() {
		int raggio = JoystickVirtuale.RAGGIO;
		int raggioCentrale = JoystickVirtuale.RAGGIO_ZONA_CENTRALE;
		int lato = 2 * raggio + 1;
		int[] argb = new int[lato * lato];
		for (int y = 0; y < lato; y++) {
			for (int x = 0; x < lato; x++) {
				double distanza = Math.hypot(x - raggio, y - raggio);
				int alfa;
				if (distanza <= raggioCentrale) {
					alfa = ALFA_ZONA_CENTRALE;
				} else if (distanza < raggio) {
					alfa = (int) Math.round(ALFA_ZONA_CENTRALE * (1 - (distanza - raggioCentrale) / (raggio - raggioCentrale)));
				} else {
					alfa = 0;
				}
				int pixel = (alfa << 24) | BLU;
				// Il bordo della zona centrale, appena più chiaro: dice dove finisce la zona morta
				if (Math.abs(distanza - raggioCentrale) < 0.75) {
					pixel = (ALFA_BLU_CHIARO << 24) | BLU_CHIARO;
				}
				argb[y * lato + x] = pixel;
			}
		}
		return new ImmagineDiPixel(lato, lato, argb);
	}

	/**
	 * Il cerchio che segna il puntatore; il suo centro è al centro dell'immagine.
	 */
	public static ImmagineDiPixel indicatore() {
		int raggio = JoystickVirtuale.RAGGIO_INDICATORE;
		int lato = 2 * raggio + 1;
		int[] argb = new int[lato * lato];
		for (int y = 0; y < lato; y++) {
			for (int x = 0; x < lato; x++) {
				if (Math.hypot(x - raggio, y - raggio) <= raggio) {
					argb[y * lato + x] = (ALFA_BLU_CHIARO << 24) | BLU_CHIARO;
				}
			}
		}
		return new ImmagineDiPixel(lato, lato, argb);
	}
}
