package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Arma;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.oggetti.Incantamento;

import java.util.Collection;
import java.util.Collections;

public interface IncantesimoMalefico extends Incantesimo, Arma {

	/**
	 * Tipo di danno
	 */
	TipoDanno getTipoDanno();

	/**
	 * Quanti danni fa di base
	 */
	int getDanni();

	/**
	 * Un incantesimo di suo è già incantato
	 */
	@Override
	default boolean isIncantata() {
		return false;
	}

	@Override
	default Collection<Incantamento> getIncantamenti() {
		return Collections.emptyList();
	}

}
