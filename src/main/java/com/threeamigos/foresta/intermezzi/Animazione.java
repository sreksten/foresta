package com.threeamigos.foresta.intermezzi;

/**
 * Le immagini animate fornite da una classe della UI, che produce il fotogramma giusto
 * per ogni istante (disegnandolo, componendo più immagini, ecc.). Il motore le nomina
 * soltanto; l'associazione con la classe che le disegna sta in ui.AnimazioniIntermezzo.
 * Per aggiungerne una: una costante qui, una classe che implementa ui.AnimazioneImmagine,
 * una riga in ui.AnimazioniIntermezzo.
 */
public enum Animazione {

	/** Un fuoco da campo che scoppietta. */
	FUOCO_DA_CAMPO
}
