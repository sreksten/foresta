package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.EnumMap;
import java.util.Map;

/**
 * La rosa che corrisponde a ogni {@link TipoRosa}.
 */
public final class Rose {

	private static final Map<TipoRosa, Rosa> ROSE = new EnumMap<>(TipoRosa.class);

	static {
		registra(new RosaSeStesso());
		registra(new RosaAlleati());
		registra(new RosaAlleatiCaduti());
		registra(new RosaAvversari());
		registra(new RosaOspiti());
		registra(new RosaAlleatiEOspiti());
		registra(new RosaAvversariEOspiti());
	}

	private Rose() {
	}

	private static void registra(Rosa rosa) {
		ROSE.put(rosa.getTipo(), rosa);
	}

	public static Rosa di(TipoRosa tipo) {
		return ROSE.get(tipo);
	}
}
