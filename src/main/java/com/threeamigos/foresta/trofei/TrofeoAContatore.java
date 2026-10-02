package com.threeamigos.foresta.trofei;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.motore.RegistroTrofei;
import com.threeamigos.foresta.motore.tipi.TipoTrofeo;

import java.util.function.ToIntFunction;

/**
 * Un trofeo che si vince accumulando, partita dopo partita, quanto vale un certo evento
 * interno fino a una soglia: i pasti in locanda, i goblin sconfitti, le amicizie strette...
 *
 * @param <E> l'evento interno da contare
 */
public class TrofeoAContatore<E> implements Trofeo {

	private final TipoTrofeo tipo;
	private final Class<E> evento;
	private final ToIntFunction<E> valore;
	private final int soglia;

	/**
	 * @param tipo   il trofeo
	 * @param evento l'evento interno che lo fa avanzare
	 * @param valore di quanto lo fa avanzare ogni evento (anche zero, per gli eventi che non contano)
	 * @param soglia il progresso con cui si vince
	 */
	TrofeoAContatore(TipoTrofeo tipo, Class<E> evento, ToIntFunction<E> valore, int soglia) {
		this.tipo = tipo;
		this.evento = evento;
		this.valore = valore;
		this.soglia = soglia;
	}

	@Override
	public TipoTrofeo getTipo() {
		return tipo;
	}

	@Override
	public void registrati() {
		BusEventi.iscriviti(evento, e -> RegistroTrofei.incrementaProgresso(tipo, valore.applyAsInt(e)));
	}

	@Override
	public boolean isMeritato() {
		return RegistroTrofei.getProgresso(tipo) >= soglia;
	}
}
