package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Una capacità innata del Bardo e della Cantastorie: costa MAGIA, non consuma pergamene. Non è fra le
 * {@link com.threeamigos.foresta.tipi.ClasseIncantesimo} perché non è una pergamena: non si compra, non si trova e non
 * si conta nell'inventario del gruppo. Chi può cantare e quando si decide in {@link CantoDelBardo}.
 */
public interface Canto {

	TipoCanto getTipo();

	/**
	 * La MAGIA che costa
	 */
	int getCostoLancio();

	/**
	 * Canta: fa l'effetto e scala la MAGIA al cantore. Il turno e la risposta avversaria li gestisce chi lo chiama.
	 *
	 * @param cantore il Bardo o la Cantastorie
	 * @param gruppo il gruppo del cantore
	 * @param gruppoAvversario gli avversari
	 * @return true se il canto ha avuto luogo (e ha quindi consumato MAGIA), false se non c'era nessuno su cui farlo
	 */
	boolean canta(Personaggio cantore, Gruppo gruppo, Gruppo gruppoAvversario);
}
