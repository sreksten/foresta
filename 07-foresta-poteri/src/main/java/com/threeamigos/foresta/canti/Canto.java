package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.interfacce.OggettoConCostoDiLancio;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * Una capacità innata del Bardo e della Cantastorie: costa Magia, non consuma pergamene. Non è fra le
 * {@link TipoIncantesimo} perché non è una pergamena: non si compra, non si trova e non
 * si conta nell'inventario del gruppo. Chi può cantare e quando si decide in {@link CantoDelBardo}.
 */
public interface Canto extends OggettoConCostoDiLancio {

	TipoCanto getTipo();

	/**
	 * La Magia che costa: la dice il suo tipo.
	 */
	@Override
	default int getCostoLancio() {
		return getTipo().getCostoLancio();
	}

	/**
	 * Canta: fa l'effetto e scala la Magia al cantore. Il turno e la risposta avversaria li gestisce chi lo chiama.
	 *
	 * @param cantore il Bardo o la Cantastorie
	 * @param gruppo il gruppo del cantore
	 * @param gruppoAvversario gli avversari
	 * @return true se il canto ha avuto luogo (e ha quindi consumato Magia), false se non c'era nessuno su cui farlo
	 */
	boolean canta(Personaggio cantore, Gruppo gruppo, Gruppo gruppoAvversario);

	/**
	 * Come {@link #canta}, ma per un canto che ha un bersaglio (il curativo) lo sceglie chi canta, per esempio una tattica; gli altri canti
	 * agiscono su un gruppo e lo ignorano
	 */
	default boolean cantaSu(Personaggio cantore, Personaggio bersaglio, Gruppo gruppo, Gruppo gruppoAvversario) {
		return canta(cantore, gruppo, gruppoAvversario);
	}
}
