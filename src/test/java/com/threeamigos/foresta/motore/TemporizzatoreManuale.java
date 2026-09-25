package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.Temporizzatore;

/**
 * Un temporizzatore per i test: non scatta mai da solo. Ricorda se l'automa lo ha avviato e con che periodo,
 * e fa arrivare un impulso solo quando il test chiama {@link #scatta()}.
 */
final class TemporizzatoreManuale implements Temporizzatore {

	private Temporizzabile temporizzabile;
	private boolean attivo;
	private int periodo;

	@Override
	public void setTemporizzabile(Temporizzabile temporizzabile) {
		this.temporizzabile = temporizzabile;
	}

	@Override
	public void inizia(int millisecondi) {
		attivo = true;
		periodo = millisecondi;
	}

	@Override
	public void iniziaDopo(int millisecondi) {
		inizia(millisecondi);
	}

	@Override
	public void termina() {
		attivo = false;
	}

	boolean isAttivo() {
		return attivo;
	}

	int getPeriodo() {
		return periodo;
	}

	/**
	 * Un impulso, come se fosse passato un periodo. Va chiamato solo a temporizzatore attivo.
	 */
	void scatta() {
		if (!attivo) {
			throw new IllegalStateException("Il temporizzatore non è attivo: l'automa non aspetta impulsi");
		}
		temporizzabile.tick();
	}
}
