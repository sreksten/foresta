package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Testi;

/**
 * Un parente dell'alchimista apre una bottega da alchimista nelle rovine disinfestate dal gruppo (vedi IlNegozio).
 */
public class IlNegozioDellAlchimista extends IlNegozio {

	public IlNegozioDellAlchimista() {
		super(MissionePredefinita.IL_NEGOZIO_DELL_ALCHIMISTA, "IL_NEGOZIO_DELL_ALCHIMISTA_TITOLO", TipoLocazione.ALCHIMISTA, PersonaggioNoto.ALCHIMISTA_DELLA_FORESTA, AspettoDelMandante.ALCHIMISTA,
				ProduttoreDiMissioniCasuale.testoDellaMissione("RUOLO_ALCHIMISTA"));
	}
}
