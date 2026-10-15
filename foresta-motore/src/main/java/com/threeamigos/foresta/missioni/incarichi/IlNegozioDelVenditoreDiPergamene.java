package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.missioni.AspettoDelMandante;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Un parente del venditore di pergamene apre un negozio di pergamene nelle rovine disinfestate dal gruppo (vedi IlNegozio).
 */
public class IlNegozioDelVenditoreDiPergamene extends IlNegozio {

	public IlNegozioDelVenditoreDiPergamene() {
		super(ClasseMissione.IL_NEGOZIO_DEL_VENDITORE_DI_PERGAMENE, "Il negozio del venditore di pergamene", TipoLocazione.VENDITORE_DI_PERGAMENE, PersonaggioNoto.VENDITORE_DI_PERGAMENE_DELLA_FORESTA, AspettoDelMandante.VENDITORE_DI_PERGAMENE,
				"il venditore di pergamene");
	}
}
