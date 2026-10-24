package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.IncantamentoMD;
import com.threeamigos.foresta.danni.tipi.TipoDanno;

import java.util.Collection;
import java.util.Collections;

/**
 * Un incantesimo che fa del male al bersaglio: ha un tipo di danno e dei danni base, e si comporta come un'arma ({@link Arma}) senza incantamenti.
 */
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
	default boolean isMagica() {
		return true;
	}

	@Override
	default boolean isIncantata() {
		return false;
	}

	@Override
	default Collection<IncantamentoMD> getIncantamenti() {
		return Collections.emptyList();
	}

}
