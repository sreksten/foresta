package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.ui.Finestra;

import java.awt.*;

/**
 * Una barra delle icone che Swing sa disegnare (la classica e il Dock): il modello è del modulo comune, qui c'è il disegno.
 */
public interface BarraIconeDisegnabile extends Finestra {

	/**
	 * Disegna la barra: lo sfondo e le icone visibili.
	 */
	void disegna(Graphics2D graphics);

	/**
	 * Ad aiuto acceso, il cartiglio con la descrizione del comando sotto il mouse, sopra tutto il resto: va chiamato dopo aver disegnato lo schermo intero,
	 * grande larghezzaSchermo x altezzaSchermo.
	 */
	void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo);

	/**
	 * Dispone le icone dei comandi possibili
	 */
	void impostaAzioni();

	boolean inTransizione();

	boolean contiene(int x, int y);
}
