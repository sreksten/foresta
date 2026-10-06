package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.CoordinateMD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * I quattro quadranti della Foresta, per sparpagliare le città e i castelli: uno per quadrante. Il nord è in alto,
 * dove la y è più piccola; con una dimensione dispari la metà in più va a sud o a est.
 */
public enum Quadrante {

	NORD_OVEST(false, false),
	NORD_EST(true, false),
	SUD_OVEST(false, true),
	SUD_EST(true, true);

	private final boolean est;
	private final boolean sud;

	Quadrante(boolean est, boolean sud) {
		this.est = est;
		this.sud = sud;
	}

	public int getMinX() {
		return est ? Foresta.getDimensioneX() / 2 : 0;
	}

	/**
	 * Esclusa.
	 */
	public int getMaxX() {
		return est ? Foresta.getDimensioneX() : Foresta.getDimensioneX() / 2;
	}

	public int getMinY() {
		return sud ? Foresta.getDimensioneY() / 2 : 0;
	}

	/**
	 * Esclusa.
	 */
	public int getMaxY() {
		return sud ? Foresta.getDimensioneY() : Foresta.getDimensioneY() / 2;
	}

	public boolean contiene(CoordinateMD coordinate) {
		return coordinate.getX() >= getMinX() && coordinate.getX() < getMaxX()
				&& coordinate.getY() >= getMinY() && coordinate.getY() < getMaxY();
	}

	/**
	 * Una casella a caso del quadrante.
	 */
	public CoordinateMD getCoordinateACaso() {
		return new CoordinateMD(getMinX() + Dado.tiraAncheAUnaFaccia(getMaxX() - getMinX()) - 1,
				getMinY() + Dado.tiraAncheAUnaFaccia(getMaxY() - getMinY()) - 1);
	}

	public static Quadrante di(CoordinateMD coordinate) {
		for (Quadrante quadrante : values()) {
			if (quadrante.contiene(coordinate)) {
				return quadrante;
			}
		}
		throw new IllegalArgumentException("Coordinate fuori dalla Foresta: " + coordinate);
	}

	/**
	 * I quattro quadranti in ordine casuale.
	 */
	public static List<Quadrante> inOrdineCasuale() {
		List<Quadrante> daPescare = new ArrayList<>(Arrays.asList(values()));
		List<Quadrante> pescati = new ArrayList<>();
		while (!daPescare.isEmpty()) {
			pescati.add(daPescare.remove(Dado.tiraAncheAUnaFaccia(daPescare.size()) - 1));
		}
		return pescati;
	}
}
