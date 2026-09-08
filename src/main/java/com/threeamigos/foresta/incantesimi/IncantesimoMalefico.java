package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Arma;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

public interface IncantesimoMalefico extends Incantesimo, Arma {

	/**
	 * Tipo di danno
	 */
	TipoDanno getTipoDanno();

	/**
	 * Quanti danni fa di base
	 */
	int getDanni();

}
