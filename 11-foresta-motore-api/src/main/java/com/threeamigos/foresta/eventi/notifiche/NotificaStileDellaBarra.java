package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.StileDellaBarra;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Il giocatore ha scelto com'è la barra dei comandi (nella schermata delle opzioni): l'interfaccia la cambia subito.
 */
public class NotificaStileDellaBarra extends EventoBase {

	private final StileDellaBarra stile;

	public NotificaStileDellaBarra(StileDellaBarra stile) {
		super(TipoEvento.NOTIFICA_STILE_DELLA_BARRA);
		this.stile = stile;
	}

	public StileDellaBarra getStile() {
		return stile;
	}
}
