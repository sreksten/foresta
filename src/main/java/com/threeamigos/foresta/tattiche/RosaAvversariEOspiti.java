package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gli avversari e poi gli ospiti vulnerabili: vale solo per i mostri, e riproduce la scelta a caso di PersonaggioBase.scegliBersaglio.
 */
final class RosaAvversariEOspiti implements Rosa {

	@Override
	public TipoRosa getTipo() {
		return TipoRosa.AVVERSARI_E_OSPITI;
	}

	@Override
	public List<Personaggio> candidati(ContestoTattico contesto) {
		verificaLato(contesto);
		List<Personaggio> candidati = new ArrayList<>(Rosa.inCampo(contesto.getAvversari()));
		candidati.addAll(Rosa.inCampo(contesto.getOspitiVulnerabili()));
		return Collections.unmodifiableList(candidati);
	}
}
