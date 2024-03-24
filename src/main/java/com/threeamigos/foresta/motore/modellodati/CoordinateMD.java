package com.threeamigos.foresta.motore.modellodati;

public class CoordinateMD {
	
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
		return ForestaMD.MAX_DIMENSIONE * x + y;
	}
}
