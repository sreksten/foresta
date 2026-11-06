package com.threeamigos.foresta.ui;

/**
 * Un rettangolo di schermo (angolo in alto a sinistra, larghezza e altezza), senza toolkit: dove sta una {@link Finestra}. Contiene i punti da
 * x compreso a x + larghezza escluso, e lo stesso per y, come <code>java.awt.Rectangle</code>.
 */
public final class Area {

	private final int x;
	private final int y;
	private final int larghezza;
	private final int altezza;

	public Area(int x, int y, int larghezza, int altezza) {
		this.x = x;
		this.y = y;
		this.larghezza = larghezza;
		this.altezza = altezza;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	public boolean contiene(int px, int py) {
		return px >= x && px < x + larghezza && py >= y && py < y + altezza;
	}
}
