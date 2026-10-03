package com.threeamigos.foresta.missioni;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Che cosa costa il passo COSTRUISCI (vedi {@link MissioneAPassi#costruisci}): gli oggetti di missione raccolti
 * prima, che si consumano, le monete e le ore di gioco che ci vogliono.
 * <pre>
 * Costruzione.con(TRONCHI).conMonete(10).inOre(6);
 * </pre>
 */
public final class Costruzione {

	private final List<OggettiDaRaccogliere> materiali = new ArrayList<>();
	private int monete;
	private int ore;

	private Costruzione() {
	}

	public static Costruzione senzaMateriali() {
		return new Costruzione();
	}

	/**
	 * Servono quegli oggetti, nella loro quantità: sono quelli di un passo di raccolta precedente.
	 */
	public static Costruzione con(OggettiDaRaccogliere... materiali) {
		Costruzione costruzione = new Costruzione();
		for (OggettiDaRaccogliere materiale : materiali) {
			costruzione.materiali.add(Objects.requireNonNull(materiale));
		}
		return costruzione;
	}

	public Costruzione conMonete(int monete) {
		if (monete < 0) {
			throw new IllegalArgumentException("Monete negative: " + monete);
		}
		this.monete = monete;
		return this;
	}

	public Costruzione inOre(int ore) {
		if (ore < 0) {
			throw new IllegalArgumentException("Ore negative: " + ore);
		}
		this.ore = ore;
		return this;
	}

	public List<OggettiDaRaccogliere> getMateriali() {
		return Collections.unmodifiableList(materiali);
	}

	public int getMonete() {
		return monete;
	}

	public int getOre() {
		return ore;
	}
}
