package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un'ondata di avversari che arriva in una locazione quando quelli di prima sono stati sconfitti tutti (vedi
 * GruppoAvversario.setOndateSuccessive e LocazioneBase): gli avversari, e che cosa si scrive quando arrivano.
 */
public final class Ondata {

	private final List<Personaggio> avversari;
	private final String arrivo;

	public Ondata(List<Personaggio> avversari, String arrivo) {
		this.avversari = new ArrayList<>(avversari);
		this.arrivo = arrivo;
	}

	public List<Personaggio> getAvversari() {
		return Collections.unmodifiableList(avversari);
	}

	/**
	 * Che cosa si scrive quando l'ondata arriva: "Dalle tombe si alzano altri scheletri!".
	 */
	public String getArrivo() {
		return arrivo;
	}
}
