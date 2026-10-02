package com.threeamigos.foresta.trofei;

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
}
