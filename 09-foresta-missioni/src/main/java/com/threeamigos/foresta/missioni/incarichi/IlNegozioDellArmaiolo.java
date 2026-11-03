package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Testi;

/**
 * Un parente dell'armaiolo apre una bottega da armaiolo nelle rovine disinfestate dal gruppo (vedi IlNegozio).
 */
public class IlNegozioDellArmaiolo extends IlNegozio {

	public IlNegozioDellArmaiolo() {
		super(MissionePredefinita.IL_NEGOZIO_DELL_ARMAIOLO, "IL_NEGOZIO_DELL_ARMAIOLO_TITOLO", TipoLocazione.ARMAIOLO, PersonaggioNoto.ARMAIOLO_DELLA_FORESTA, AspettoDelMandante.ARMAIOLO,
				ProduttoreDiMissioniCasuale.testoDellaMissione("RUOLO_ARMAIOLO"));
	}
}
