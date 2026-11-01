package com.threeamigos.foresta.ui.componenti;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Un joystick analogico a schermo, per muovere un puntatore col dito (o col mouse) quando non c'è altro modo di
 * arrivare ovunque: un disco luminoso in basso a destra, con una zona centrale piena e una periferica che sfuma verso
 * l'esterno. Premuto il disco il joystick si attiva e, finché il tasto resta premuto, la distanza del puntatore dalla
 * zona centrale dà la velocità: nessuna dentro la zona centrale (la zona morta), poi sempre di più fino al massimo al
 * bordo del disco, oltre il quale non cresce. La direzione è quella del puntatore rispetto al centro.
 * <p>
 * La classe non muove niente: dice con che velocità, e chi la usa sposta il suo puntatore.
 */
public final class JoystickVirtuale {

	/**
	 * Il raggio del disco, in pixel.
	 */
	public static final int RAGGIO = 100;
	/**
	 * Il raggio della zona centrale, quella che non muove: un terzo del disco.
	 */
	public static final int RAGGIO_ZONA_CENTRALE = RAGGIO / 3;
	/**
	 * La velocità al bordo del disco, in pixel al secondo.
	 */
	public static final double VELOCITA_MASSIMA = 600;
	private static final int MARGINE = 20;

	private static final int ALFA_ZONA_CENTRALE = 200;
	private static final Color BLU = new Color(40, 110, 255);
	private static final Color BLU_PIENO = new Color(BLU.getRed(), BLU.getGreen(), BLU.getBlue(), ALFA_ZONA_CENTRALE);
	private static final Color BLU_TRASPARENTE = new Color(BLU.getRed(), BLU.getGreen(), BLU.getBlue(), 0);
	private static final Color BLU_CHIARO = new Color(170, 210, 255, 220);
	private static final int RAGGIO_INDICATORE = 7;

	private final int centroX;
	private final int centroY;
	private boolean attivo;
	private int xPuntatore;
	private int yPuntatore;

	/**
	 * Un joystick nell'angolo in basso a destra di un'area larga {@code larghezza} e alta {@code altezza}.
	 */
	public JoystickVirtuale(int larghezza, int altezza) {
		this.centroX = larghezza - MARGINE - RAGGIO;
		this.centroY = altezza - MARGINE - RAGGIO;
	}

	/**
	 * Se il punto sta nel disco, zona periferica compresa.
	 */
	public boolean contiene(int x, int y) {
		long dx = (long) x - centroX;
		long dy = (long) y - centroY;
		return dx * dx + dy * dy <= (long) RAGGIO * RAGGIO;
	}

	/**
	 * Attiva il joystick, se il punto sta nel disco; il puntatore è lì.
	 *
	 * @return se si è attivato
	 */
	public boolean attiva(int x, int y) {
		if (!contiene(x, y)) {
			return false;
		}
		attivo = true;
		xPuntatore = x;
		yPuntatore = y;
		return true;
	}

	/**
	 * Dove sta il puntatore (il dito o il mouse), anche fuori dal disco; non fa niente se il joystick non è attivo.
	 */
	public void sposta(int x, int y) {
		if (attivo) {
			xPuntatore = x;
			yPuntatore = y;
		}
	}

	public void rilascia() {
		attivo = false;
	}

	public boolean isAttivo() {
		return attivo;
	}

	/**
	 * Quanto va forte il joystick, da 0 (puntatore nella zona centrale, o joystick non attivo) a 1 (al bordo del disco
	 * o oltre), crescendo in modo lineare in mezzo.
	 */
	public double getIntensita() {
		if (!attivo) {
			return 0;
		}
		double distanza = Math.hypot(xPuntatore - centroX, yPuntatore - centroY);
		if (distanza <= RAGGIO_ZONA_CENTRALE) {
			return 0;
		}
		return Math.min(1, (distanza - RAGGIO_ZONA_CENTRALE) / (RAGGIO - RAGGIO_ZONA_CENTRALE));
	}

	/**
	 * La velocità sull'asse x, in pixel al secondo: positiva verso destra.
	 */
	public double getVelocitaX() {
		return velocita(xPuntatore - centroX);
	}

	/**
	 * La velocità sull'asse y, in pixel al secondo: positiva verso il basso.
	 */
	public double getVelocitaY() {
		return velocita(yPuntatore - centroY);
	}

	private double velocita(int scostamento) {
		double intensita = getIntensita();
		if (intensita == 0) {
			return 0;
		}
		double distanza = Math.hypot(xPuntatore - centroX, yPuntatore - centroY);
		return intensita * VELOCITA_MASSIMA * scostamento / distanza;
	}

	/**
	 * Disegna il disco: pieno e blu nella zona centrale, dello stesso blu che sfuma fino alla trasparenza all'esterno;
	 * da attivo mostra anche dove sta il puntatore, tenuto dentro il disco.
	 */
	public void disegna(Graphics2D graphics) {
		Paint paintOriginale = graphics.getPaint();
		Object antialiasing = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		float frazioneZonaCentrale = (float) RAGGIO_ZONA_CENTRALE / RAGGIO;
		graphics.setPaint(new RadialGradientPaint(centroX, centroY, RAGGIO, new float[] {0f, frazioneZonaCentrale, 1f},
				new Color[] {BLU_PIENO, BLU_PIENO, BLU_TRASPARENTE}));
		graphics.fill(new Ellipse2D.Double(centroX - RAGGIO, centroY - RAGGIO, 2 * RAGGIO, 2 * RAGGIO));

		// Il bordo della zona centrale, appena più chiaro: dice dove finisce la zona morta
		graphics.setPaint(BLU_CHIARO);
		graphics.draw(new Ellipse2D.Double(centroX - RAGGIO_ZONA_CENTRALE, centroY - RAGGIO_ZONA_CENTRALE,
				2 * RAGGIO_ZONA_CENTRALE, 2 * RAGGIO_ZONA_CENTRALE));

		if (attivo) {
			double dx = xPuntatore - centroX;
			double dy = yPuntatore - centroY;
			double distanza = Math.hypot(dx, dy);
			if (distanza > RAGGIO) {
				dx = dx * RAGGIO / distanza;
				dy = dy * RAGGIO / distanza;
			}
			graphics.setPaint(BLU_CHIARO);
			graphics.fill(new Ellipse2D.Double(centroX + dx - RAGGIO_INDICATORE, centroY + dy - RAGGIO_INDICATORE,
					2 * RAGGIO_INDICATORE, 2 * RAGGIO_INDICATORE));
		}

		graphics.setPaint(paintOriginale);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				antialiasing != null ? antialiasing : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	}

	int getCentroX() {
		return centroX;
	}

	int getCentroY() {
		return centroY;
	}
}
