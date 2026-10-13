package com.threeamigos.foresta.modellodati;

public class CoordinateMD {

	// Per l'hashCode: con lati della foresta fino a questa misura due caselle non hanno mai lo stesso hash
	private static final int MAX_DIMENSIONE_LATO_FORESTA = 80;
	
	private int x;
	private int y;
	
	public CoordinateMD() {
	}

	public CoordinateMD(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
	}
	
	public int getX() {
		return x;
	}
	
	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}
	
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
	
	@Override
	public int hashCode() {
		return MAX_DIMENSIONE_LATO_FORESTA * x + y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
