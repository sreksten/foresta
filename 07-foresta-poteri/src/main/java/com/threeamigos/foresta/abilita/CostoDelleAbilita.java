package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;

/**
 * Quanto costa per ogni bersaglio un'abilità innata a un dato personaggio: per quasi tutte lo dice il tipo, ma il dardo arcano costa quanto
 * rende, e il suo danno dipende dalla classe e (per l'Elfo) dal livello di chi lo lancia. Per questo il costo lo dà l'abilità costruita
 * per lui (vedi {@link AbilitaDiDanno#getCostoPerBersaglio()}).
 */
public final class CostoDelleAbilita {

	private CostoDelleAbilita() {
	}

	/**
	 * I punti di Magia che costa a quel personaggio usare quell'abilità su un bersaglio.
	 */
	public static int di(TipoAbilita tipo, Personaggio personaggio) {
		return FabbricaAbilita.crea(tipo, personaggio).getCostoPerBersaglio();
	}
}
