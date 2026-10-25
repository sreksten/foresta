package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.modellodati.CantiImparatiMD;
import com.threeamigos.foresta.tipi.TipoCanto;

import java.util.function.Supplier;

/**
 * I canti che il gruppo della partita in corso ha imparato strada facendo. Il gruppo non sa di averli imparati: lo sanno i canti. Dove sono
 * i dati lo dice chi li custodisce (vedi {@link #setFornitoreDiDati}).
 *
 * @author Stefano Reksten
 */
public final class CantiImparati {

	// Da dove prendere i dati della partita in corso: i canti non sanno chi li custodisce
	private static volatile Supplier<CantiImparatiMD> fornitoreDiDati;

	private CantiImparati() {
	}

	/**
	 * Dice da dove prendere i canti imparati nella partita in corso (lo fa chi custodisce i dati: ogni volta si prendono da quelli in corso).
	 */
	public static void setFornitoreDiDati(Supplier<CantiImparatiMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static CantiImparatiMD dati() {
		Supplier<CantiImparatiMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto ai canti dove sono i loro dati (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
	}

	/**
	 * Se il gruppo ha imparato quel canto strada facendo (l'Urlo della Banshee non lo sa nessuno all'inizio).
	 */
	public static boolean haImparato(TipoCanto canto) {
		return dati().getCanti().contains(canto);
	}

	/**
	 * Il gruppo impara quel canto: da ora in poi chi sa cantare lo può cantare.
	 */
	public static void impara(TipoCanto canto) {
		dati().aggiungi(canto);
	}
}
