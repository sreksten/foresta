package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAzione;

/**
 * Dice alla UI che un personaggio sta per agire in combattimento: il riquadro del combattimento evidenzia chi agisce e, se c'è, il
 * bersaglio, e scrive sotto il suo nome che cosa fa. Va pubblicato prima dell'effetto, quando si sa che l'azione ci sarà.
 */
public final class AnnuncioDellAzione {

	private AnnuncioDellAzione() {
	}

	/**
	 * @param attore    chi agisce
	 * @param bersaglio chi subisce l'azione (o su cui si compie), o null se i bersagli sono più di uno o non ce ne sono
	 * @param azione    che cosa fa, o null per un attacco generico
	 */
	public static void azione(Personaggio attore, Personaggio bersaglio, TipoAzione azione) {
		BusEventi.pubblica(new InternoAzioneInCombattimento(attore, bersaglio, azione));
	}
}
