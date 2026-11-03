package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Testi;

/**
 * Un parente del venditore di pergamene apre un negozio di pergamene nelle rovine disinfestate dal gruppo (vedi IlNegozio).
 */
public class IlNegozioDelVenditoreDiPergamene extends IlNegozio {

	public IlNegozioDelVenditoreDiPergamene() {
		super(MissionePredefinita.IL_NEGOZIO_DEL_VENDITORE_DI_PERGAMENE, "IL_NEGOZIO_DEL_VENDITORE_DI_PERGAMENE_TITOLO", TipoLocazione.VENDITORE_DI_PERGAMENE, PersonaggioNoto.VENDITORE_DI_PERGAMENE_DELLA_FORESTA, AspettoDelMandante.VENDITORE_DI_PERGAMENE,
				ProduttoreDiMissioniCasuale.testoDellaMissione("RUOLO_VENDITORE_DI_PERGAMENE"));
	}
}
