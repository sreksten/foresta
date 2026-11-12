package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Il giocatore ha scelto dove sta la barra dei comandi (nella schermata delle opzioni): l'interfaccia la sposta subito.
 */
public class NotificaPosizioneDellaBarra extends EventoBase {

	private final PosizioneDellaBarra posizione;

	public NotificaPosizioneDellaBarra(PosizioneDellaBarra posizione) {
		super(TipoEvento.NOTIFICA_POSIZIONE_DELLA_BARRA);
		this.posizione = posizione;
	}

	public PosizioneDellaBarra getPosizione() {
		return posizione;
	}
}
