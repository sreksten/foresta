package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;

/**
 * Che cosa disegnare in una schermata senza partita (i messaggi, le statistiche, gli slot di salvataggio, la sconfitta, la vittoria, la classifica): lo
 * sfondo e gli elementi, nelle coordinate dello schermo (l'angolo in alto a sinistra è (0, 0)).
 */
public final class DisposizioneDiSchermata {

	/**
	 * Lo sfondo della schermata
	 */
	public enum Sfondo {
		NESSUNO,
		/**
		 * L'ombra del drago, centrata (<code>fondi/OmbraDelDrago.png</code>, con lo zoom)
		 */
		OMBRA_DEL_DRAGO,
		/**
		 * L'immagine del trionfo, centrata (<code>fondi/Trionfo.png</code>, con lo zoom)
		 */
		TRIONFO
	}

	private final Sfondo sfondo;
	private final ContenutoDiColonna contenuto;

	public DisposizioneDiSchermata(Sfondo sfondo, ContenutoDiColonna contenuto) {
		this.sfondo = sfondo;
		this.contenuto = contenuto;
	}

	/**
	 * La stessa schermata con un altro sfondo
	 */
	public DisposizioneDiSchermata conSfondo(Sfondo nuovo) {
		return new DisposizioneDiSchermata(nuovo, contenuto);
	}

	public Sfondo getSfondo() {
		return sfondo;
	}

	public ContenutoDiColonna getContenuto() {
		return contenuto;
	}
}
