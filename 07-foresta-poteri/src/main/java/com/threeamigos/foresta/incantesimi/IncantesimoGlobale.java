package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.List;

/**
 * Un incantesimo di portata GLOBALE: non ha un bersaglio scelto, agisce su tutta la locazione (il gruppo avversario, quello di chi lo formula o
 * tutti e due). Chi decide se lanciarlo (una tattica) chiede se in quel momento serve.
 */
public interface IncantesimoGlobale extends Incantesimo {

	/**
	 * Se lanciarlo adesso farebbe qualcosa: c'è nel campo almeno qualcuno su cui avrebbe effetto, e non l'ha già.
	 *
	 * @param alleati gli alleati di chi lo formula
	 * @param avversari i suoi avversari
	 */
	boolean serve(List<Personaggio> alleati, List<Personaggio> avversari);
}
