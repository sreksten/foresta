package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il troll: creatura umanoide e gigantesca, corrompibile ma non amichevole. È immune agli incantesimi di Terra.
 */
public class Troll extends PersonaggioBase implements Personaggio {

	public Troll(int livello) {
		super(TipoPersonaggio.TROLL, livello);
	}

}
