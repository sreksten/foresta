package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Testi;

/**
 * Un parente dell'incantatore apre un laboratorio di incantamenti nelle rovine disinfestate dal gruppo (vedi IlNegozio).
 */
public class IlNegozioDellIncantatore extends IlNegozio {

	public IlNegozioDellIncantatore() {
		super(MissionePredefinita.IL_NEGOZIO_DELL_INCANTATORE, "IL_NEGOZIO_DELL_INCANTATORE_TITOLO", TipoLocazione.INCANTATORE, PersonaggioNoto.INCANTATORE_DELLA_FORESTA, AspettoDelMandante.INCANTATORE,
				Testi.formatta("missioni", "Ruolo.incantatore"));
	}
}
