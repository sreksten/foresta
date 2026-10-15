package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.missioni.AspettoDelMandante;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Un parente dell'incantatore apre un laboratorio di incantamenti nelle rovine disinfestate dal gruppo (vedi IlNegozio).
 */
public class IlNegozioDellIncantatore extends IlNegozio {

	public IlNegozioDellIncantatore() {
		super(ClasseMissione.IL_NEGOZIO_DELL_INCANTATORE, "Il negozio dell'incantatore", TipoLocazione.INCANTATORE, PersonaggioNoto.INCANTATORE_DELLA_FORESTA, AspettoDelMandante.INCANTATORE,
				"l'incantatore");
	}
}
