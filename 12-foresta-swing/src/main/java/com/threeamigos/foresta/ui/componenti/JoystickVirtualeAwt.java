package com.threeamigos.foresta.ui.componenti;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Il disegno in Swing del {@link JoystickVirtuale} (nel modulo comune): il disco con la zona centrale piena e la periferica che sfuma, e da attivo
 * l'indicatore del puntatore.
 */
public final class JoystickVirtualeAwt {

	private static final int ALFA_ZONA_CENTRALE = 200;
	private static final Color BLU = new Color(40, 110, 255);
	private static final Color BLU_PIENO = new Color(BLU.getRed(), BLU.getGreen(), BLU.getBlue(), ALFA_ZONA_CENTRALE);
	private static final Color BLU_TRASPARENTE = new Color(BLU.getRed(), BLU.getGreen(), BLU.getBlue(), 0);
	private static final Color BLU_CHIARO = new Color(170, 210, 255, 220);

	private JoystickVirtualeAwt() {
	}

	/**
	 * Disegna il disco: pieno e blu nella zona centrale, dello stesso blu che sfuma fino alla trasparenza all'esterno;
	 * da attivo mostra anche dove sta il puntatore, tenuto dentro il disco.
	 */
	public static void disegna(Graphics2D graphics, JoystickVirtuale joystick) {
		int centroX = joystick.getCentroX();
		int centroY = joystick.getCentroY();
		int raggio = JoystickVirtuale.RAGGIO;
		int raggioIndicatore = JoystickVirtuale.RAGGIO_INDICATORE;
		Paint paintOriginale = graphics.getPaint();
		Object antialiasing = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		float frazioneZonaCentrale = (float) JoystickVirtuale.RAGGIO_ZONA_CENTRALE / raggio;
		graphics.setPaint(new RadialGradientPaint(centroX, centroY, raggio, new float[] {0f, frazioneZonaCentrale, 1f},
				new Color[] {BLU_PIENO, BLU_PIENO, BLU_TRASPARENTE}));
		graphics.fill(new Ellipse2D.Double(centroX - raggio, centroY - raggio, 2 * raggio, 2 * raggio));

		// Il bordo della zona centrale, appena più chiaro: dice dove finisce la zona morta
		graphics.setPaint(BLU_CHIARO);
		graphics.draw(new Ellipse2D.Double(centroX - JoystickVirtuale.RAGGIO_ZONA_CENTRALE, centroY - JoystickVirtuale.RAGGIO_ZONA_CENTRALE,
				2 * JoystickVirtuale.RAGGIO_ZONA_CENTRALE, 2 * JoystickVirtuale.RAGGIO_ZONA_CENTRALE));

		if (joystick.isAttivo()) {
			double[] scostamento = joystick.getScostamentoDellIndicatore();
			graphics.setPaint(BLU_CHIARO);
			graphics.fill(new Ellipse2D.Double(centroX + scostamento[0] - raggioIndicatore, centroY + scostamento[1] - raggioIndicatore,
					2 * raggioIndicatore, 2 * raggioIndicatore));
		}

		graphics.setPaint(paintOriginale);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				antialiasing != null ? antialiasing : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	}
}
