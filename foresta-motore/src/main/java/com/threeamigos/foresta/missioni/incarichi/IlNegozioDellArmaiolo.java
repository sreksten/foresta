package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.missioni.AspettoDelMandante;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Un parente dell'armaiolo apre una bottega da armaiolo nelle rovine disinfestate dal gruppo (vedi IlNegozio).
 */
public class IlNegozioDellArmaiolo extends IlNegozio {

	public IlNegozioDellArmaiolo() {
		super(MissionePredefinita.IL_NEGOZIO_DELL_ARMAIOLO, "Il negozio dell'armaiolo", TipoLocazione.ARMAIOLO, PersonaggioNoto.ARMAIOLO_DELLA_FORESTA, AspettoDelMandante.ARMAIOLO,
				"l'armaiolo");
	}
}
