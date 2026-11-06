package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.ui.Finestra;

/**
 * La schermata degli intermezzi (vedi {@link ScenaDellIntermezzo}) come la comanda lo schermo del gioco: la implementano Swing e
 * libGDX, che la disegnano.
 */
public interface SchermataIntermezzo extends Finestra {

	/** Mostra la pagina, dalla sua comparsa */
	void mostra(PaginaIntermezzo pagina);

	/** La pagina riparte da quei secondi dalla sua comparsa: dal fumetto successivo */
	void saltaA(double secondi);

	/** Libera le immagini della pagina: si torna al gioco */
	void svuota();
}
