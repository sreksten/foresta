package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;

/**
 * Una capacità innata del Chierico o del Druido: costa MAGIA, non consuma pergamene. Non è fra le
 * {@link com.threeamigos.foresta.tipi.TipoIncantesimo} perché non è una pergamena: non si compra, non si trova e non si conta nell'inventario del
 * gruppo. Chi può usarla e quando si decide in {@link AbilitaInnate}.
 */
public interface Abilita {

	TipoAbilita getTipo();

	/**
	 * La MAGIA che costa
	 */
	int getCostoLancio();

	/**
	 * Usa l'abilità: fa l'effetto e scala la MAGIA a chi la usa. Il turno e la risposta avversaria li gestisce chi la chiama.
	 *
	 * @param utilizzatore il Chierico o il Druido
	 * @param gruppo il gruppo di chi la usa
	 * @param gruppoAvversario gli avversari
	 * @return true se l'abilità ha avuto luogo (e ha quindi consumato MAGIA), false se non c'era nessuno su cui usarla
	 */
	boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario);

	/**
	 * Come {@link #usa}, ma per un'abilità che ha un bersaglio (l'imposizione delle mani, la rigenerazione) lo sceglie chi la usa, per esempio una
	 * tattica; le altre agiscono su un gruppo e lo ignorano
	 */
	default boolean usaSu(Personaggio utilizzatore, Personaggio bersaglio, Gruppo gruppo, Gruppo gruppoAvversario) {
		return usa(utilizzatore, gruppo, gruppoAvversario);
	}
}
