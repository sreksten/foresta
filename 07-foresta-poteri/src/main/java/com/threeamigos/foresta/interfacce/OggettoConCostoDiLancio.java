package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Qualcosa che chi lo usa paga con la Magia: un incantesimo, un canto, un'abilità innata, il dardo arcano.
 * <p>
 * Si paga per ogni bersaglio che tocca davvero (vedi CostantiIncantesimi.costoPerBersaglio): se la Magia di chi lo usa non basta per
 * tutti, tocca solo i primi che si possono pagare, e se non basta neanche per uno non si può usare.
 */
public interface OggettoConCostoDiLancio {

	/**
	 * I punti di Magia che costa usarlo su un bersaglio.
	 */
	int getCostoPerBersaglio();

	/**
	 * I punti di Magia che costa usarlo su quel numero di bersagli.
	 */
	default int getCosto(int bersagli) {
		return getCostoPerBersaglio() * bersagli;
	}

	/**
	 * Se chi lo usa ha la Magia per toccare almeno un bersaglio.
	 */
	default boolean isPagabileDa(Personaggio chi) {
		return chi.getMagia() >= getCostoPerBersaglio();
	}

	/**
	 * I primi bersagli dell'elenco che chi lo usa può pagare con la sua Magia (tutti, se non costa niente); un elenco nuovo.
	 */
	default <T> List<T> soloIPagabili(Personaggio chi, List<T> bersagli) {
		int costo = getCostoPerBersaglio();
		int pagabili = costo <= 0 ? bersagli.size() : Math.min(bersagli.size(), Math.max(0, chi.getMagia()) / costo);
		return new ArrayList<>(bersagli.subList(0, pagabili));
	}

	/**
	 * Toglie a chi lo usa la Magia per quel numero di bersagli.
	 */
	default void paga(Personaggio chi, int bersagli) {
		chi.subMagia(getCosto(bersagli));
	}
}
