package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il druido giocante, al maschile: l'incantatore della natura, che usa bastone e lancia ma niente armatura né scudo.
 * Ha innate le radici (Terra), lo sciame (Veleno), la pelle di corteccia e la rigenerazione (vedi il pacchetto
 * {@code incantesimi}). Il femminile è la {@link Druida}, che ha lo stesso ruolo.
 */
public class Druido extends PersonaggioBase implements Personaggio {

	public Druido(int livello) {
		super(TipoPersonaggio.DRUIDO, livello);
	}

	public Druido(String nome, int livello) {
		super(nome, TipoPersonaggio.DRUIDO, livello);
	}

}
