package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Un parente dell'alchimista apre una bottega da alchimista nelle rovine disinfestate dal gruppo (vedi IlNegozio).
 */
public class IlNegozioDellAlchimista extends IlNegozio {

	public IlNegozioDellAlchimista() {
		super(MissionePredefinita.IL_NEGOZIO_DELL_ALCHIMISTA, "Il negozio dell'alchimista", TipoLocazione.ALCHIMISTA, PersonaggioNoto.ALCHIMISTA_DELLA_FORESTA, AspettoDelMandante.ALCHIMISTA,
				"l'alchimista");
	}
}
