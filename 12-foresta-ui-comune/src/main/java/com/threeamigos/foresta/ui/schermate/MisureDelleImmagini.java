package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Misura;

/**
 * Quanto sono grandi le immagini, come le tiene chi disegna (Swing, libGDX): le schermate le chiedono per impaginare senza conoscere il toolkit grafico.
 */
public interface MisureDelleImmagini {

	/**
	 * L'immagine del personaggio di quella classe, già con lo zoom (specchiata ha la stessa misura)
	 */
	Misura personaggio(TipoPersonaggio classe);

	/**
	 * Un'immagine per nome, alla sua dimensione originale (senza zoom)
	 */
	Misura immagine(String risorsa);
}
