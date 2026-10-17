package com.threeamigos.foresta.modellodati;

/**
 * Una posizione sulla mappa della Foresta: <code>x</code> è la colonna e <code>y</code> la riga di una casella.
 * Dato puro, senza logica: due coordinate con gli stessi valori sono uguali ({@link #equals(Object)}) e hanno lo
 * stesso {@link #hashCode()}, quindi si possono usare come chiavi di una mappa (come fanno i registri).
 *
 * @author Stefano Reksten
 */
public class CoordinateMD {

	private int x;
	private int y;

	public CoordinateMD() {
	}

	public CoordinateMD(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	/**
	 * La forma "(x, y)", comoda nei messaggi di log.
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	/**
	 * Due coordinate sono uguali se hanno la stessa colonna e la stessa riga.
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (this.getClass() != object.getClass()) {
			return false;
		}
		CoordinateMD coordinate = (CoordinateMD)object;
		return x == coordinate.x && y == coordinate.y;
	}

	/**
	 * Combina colonna e riga come Objects.hash, senza dipendere dalla misura della foresta: due coordinate uguali hanno
	 * lo stesso valore, e due diverse lo condividono di rado (le mappe le distinguono comunque con equals).
	 */
	@Override
	public int hashCode() {
		return 31 * x + y;
	}

}
