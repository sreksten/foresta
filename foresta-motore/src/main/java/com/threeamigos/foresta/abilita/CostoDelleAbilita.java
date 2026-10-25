package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Quanto costa un'abilità innata a una data classe: uguale per tutti, tranne il dardo arcano, che costa meno al Mago (e alla Maga) che
 * all'Elfo (e all'Elfa). Il costo base, quello del Mago, lo dà {@link TipoAbilita#getCostoLancio()}.
 */
public final class CostoDelleAbilita {

	private CostoDelleAbilita() {
	}

	/**
	 * I punti di MAGIA che costa usare quell'abilità a quella classe.
	 */
	public static int di(TipoAbilita tipo, TipoPersonaggio classe) {
		if (tipo == TipoAbilita.DARDO_ARCANO && classe != TipoPersonaggio.MAGO && classe != TipoPersonaggio.MAGA) {
			return CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_ELFO;
		}
		return tipo.getCostoLancio();
	}
}
