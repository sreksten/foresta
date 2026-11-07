package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.CostantiMotore;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * Quel che l'eremita regala al gruppo alla fine della prima locazione (vedi {@code IntermezzoFinePrimaLocazione}): una partita comincia
 * senza incantesimi né pozioni, così le icone dei primi turni sono poche. La consegna è un cambio di inventario, quindi la fa il motore
 * (gli intermezzi non conoscono gli oggetti) e la fa sempre, a tutorial acceso o spento.
 */
final class DotazioneDellEremita {

	private DotazioneDellEremita() {
	}

	static void consegna(GruppoGiocatore gruppo) {
		for (TipoIncantesimo tipo : new TipoIncantesimo[] {TipoIncantesimo.ARIA, TipoIncantesimo.ACQUA, TipoIncantesimo.TERRA}) {
			gruppo.addIncantesimi(tipo, CostantiMotore.DOTAZIONE_EREMITA_INCANTESIMI_PER_ELEMENTO);
		}
		gruppo.addPozioniSalute(CostantiMotore.DOTAZIONE_EREMITA_POZIONI_SALUTE);
	}
}
