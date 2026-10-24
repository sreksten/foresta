package com.threeamigos.foresta.tattiche.rose;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gli alleati e poi gli ospiti vulnerabili, per le cure: vale solo per i personaggi del giocatore.
 */
final class RosaAlleatiEOspiti implements Rosa {

	@Override
	public TipoRosa getTipo() {
		return TipoRosa.ALLEATI_E_OSPITI;
	}

	@Override
	public List<Personaggio> candidati(ContestoTattico contesto) {
		verificaLato(contesto);
		List<Personaggio> candidati = new ArrayList<>(Rosa.inCampo(contesto.getAlleati()));
		candidati.addAll(Rosa.inCampo(contesto.getOspitiVulnerabili()));
		// Le cure non guariscono un non morto del gruppo: non è un candidato
		candidati.removeIf(p -> !p.isCurabile());
		return Collections.unmodifiableList(candidati);
	}
}
