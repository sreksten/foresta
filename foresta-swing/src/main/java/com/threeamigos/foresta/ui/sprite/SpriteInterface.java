package com.threeamigos.foresta.ui.sprite;

import java.awt.*;

/**
 * Uno sprite: qualcosa che compare per un po' sopra la schermata e si anima da solo finché non ha finito.
 */
public interface SpriteInterface {

	/**
	 * Disegna lo sprite per l'istante corrente e fa avanzare la sua animazione.
	 */
	void anima(Graphics2D g);

	/**
	 * Dice se lo sprite è ancora in vita: finita l'animazione non lo è più e chi lo tiene può toglierlo.
	 */
	boolean isAttivo();

}
