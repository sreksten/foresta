package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il mago giocante, al maschile: umanoide, corrompibile e amichevole. Vive di magia: ha il dardo arcano come abilità
 * innata, che non consuma pergamene. Il femminile è la {@link Maga}.
 */
public class Mago extends PersonaggioBase implements Personaggio {

	public Mago(int livello) {
		super(TipoPersonaggio.MAGO, livello);
	}

	public Mago(String nome, int livello) {
		super(nome, TipoPersonaggio.MAGO, livello);
	}

}
