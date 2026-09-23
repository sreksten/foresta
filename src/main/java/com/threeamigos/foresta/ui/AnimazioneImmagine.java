package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.intermezzi.StatoElemento;

import java.awt.image.BufferedImage;

/**
 * Una classe che fornisce l'immagine di un elemento di intermezzo istante per istante:
 * così un'animazione può essere disegnata via codice, composta da più immagini o
 * scegliere il fotogramma in base a come si sta muovendo l'elemento.
 * <p>
 * Un'istanza viene creata per ogni elemento che la usa e tenuta finché dura
 * l'intermezzo, quindi può preparare i fotogrammi una volta sola nel costruttore.
 * Il metodo è chiamato a ogni frame: non deve allocare immagini nuove ogni volta.
 */
public interface AnimazioneImmagine {

	/**
	 * @param secondi secondi trascorsi dall'inizio della pagina
	 * @param stato   lo stato dell'elemento in quell'istante (posizione, scala, opacità,
	 *                verso), oppure null se l'immagine è usata come sfondo
	 * @return il fotogramma da disegnare, alla sua dimensione originale
	 */
	BufferedImage getFotogramma(double secondi, StatoElemento stato);
}
