package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il troll: creatura umanoide e gigantesca, corrompibile ma non amichevole. La terra non lo scalfisce, incantesimi compresi (vedi TipoPersonaggio).
 */
public class Troll extends PersonaggioBase {

	public Troll(int livello) {
		super(TipoPersonaggio.TROLL, livello);
	}

}
