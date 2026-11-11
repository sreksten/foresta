package com.threeamigos.foresta.tipi;

/**
 * Come si dispone la schermata di gioco (vedi SceltaDelLayout e {@code layout_telefono.md}).
 */
public enum TipoDiLayout {
	/** Il layout di sempre: mappa con la sua cornice, statistiche e tre riquadri impilati; serve uno schermo di almeno 1094×732 dp */
	TABLET,
	/** Il layout dei telefoni: la mappa nella pila, monete, preziosi e punti nel riquadro del gruppo, l'area virtuale allungata come lo schermo */
	TELEFONO
}
