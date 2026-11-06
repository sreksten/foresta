package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

/**
 * Dove sta l'intestazione di un riquadro della schermata di gioco, come quelle delle due liste dell'inventario: scritta grigia chiara con l'ombra nera
 * dietro, centrata sul bordo superiore della cornice (non toglie spazio al contenuto). Il disegno è di chi usa queste posizioni (Swing, libGDX).
 */
public final class PosizioneDellIntestazione {

	/**
	 * Di quanti pixel l'ombra nera è spostata in basso e a destra
	 */
	public static final int SCOSTAMENTO_OMBRA = 2;

	private PosizioneDellIntestazione() {
	}

	/**
	 * La x della scritta larga <code>larghezzaTesto</code>, centrata sulla cornice larga <code>larghezzaCornice</code> che comincia in xCornice
	 */
	public static int x(int xCornice, int larghezzaCornice, int larghezzaTesto) {
		return xCornice + larghezzaCornice / 2 - larghezzaTesto / 2;
	}

	/**
	 * La y della scritta, per una cornice che comincia in yCornice: il corpo del testo sta dove stava, le righe in più per gli accenti salgono sopra
	 */
	public static int y(int yCornice) {
		return yCornice + Finestra.SPACING - DoomdarkFont.medio().getRigheInCima();
	}
}
