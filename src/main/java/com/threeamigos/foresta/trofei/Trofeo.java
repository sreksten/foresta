package com.threeamigos.foresta.trofei;

import com.threeamigos.foresta.motore.RegistroTrofei;
import com.threeamigos.foresta.motore.tipi.TipoTrofeo;

/**
 * Il trigger di un trofeo: come una missione ha un innesco, ma non scade mai e una
 * volta vinto vale per tutte le partite successive. Viene controllato a fine
 * locazione, solo finché il trofeo non è ancora stato vinto.
 */
public interface Trofeo {

	TipoTrofeo getTipo();

	/**
	 * Si iscrive agli eventi che fanno avanzare il trofeo (vedi RegistroTrofei.incrementaProgresso).
	 */
	void registrati();

	/**
	 * @return true se il trofeo è stato meritato
	 */
	boolean isMeritato();

	/**
	 * Quanto serve per vincerlo: 100 goblin, 50 missioni, 1 per un boss.
	 */
	int getObiettivo();

	/**
	 * A che punto è, da 0 a {@link #getObiettivo()}: quello che il registro ha accumulato, partita dopo partita.
	 */
	default int getProgresso() {
		return Math.min(getObiettivo(), RegistroTrofei.getProgresso(getTipo()));
	}
}
