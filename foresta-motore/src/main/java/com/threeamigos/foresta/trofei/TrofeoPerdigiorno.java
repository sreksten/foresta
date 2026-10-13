package com.threeamigos.foresta.trofei;

import com.threeamigos.foresta.motore.RegistroTrofei;
import com.threeamigos.foresta.tipi.TipoTrofeo;

import java.util.Arrays;

/**
 * Vinto quando sono stati vinti tutti gli altri trofei: non conta nessun evento, guarda solo il registro.
 */
public class TrofeoPerdigiorno implements Trofeo {

	@Override
	public TipoTrofeo getTipo() {
		return TipoTrofeo.PERDIGIORNO;
	}

	@Override
	public void registrati() {
		// Nessun evento da contare
	}

	@Override
	public boolean isMeritato() {
		return Arrays.stream(TipoTrofeo.values())
				.filter(trofeo -> trofeo != TipoTrofeo.PERDIGIORNO)
				.allMatch(RegistroTrofei::isVinto);
	}

	/**
	 * Tutti gli altri trofei.
	 */
	@Override
	public int getObiettivo() {
		return TipoTrofeo.values().length - 1;
	}

	/**
	 * Quanti degli altri trofei sono stati vinti.
	 */
	@Override
	public int getProgresso() {
		return (int) Arrays.stream(TipoTrofeo.values())
				.filter(trofeo -> trofeo != TipoTrofeo.PERDIGIORNO)
				.filter(RegistroTrofei::isVinto)
				.count();
	}
}
