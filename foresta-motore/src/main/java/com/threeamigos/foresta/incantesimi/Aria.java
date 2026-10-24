package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * L'incantesimo di Aria: un incantesimo malefico di danno di Aria (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_ARIA_*}.
 */
public class Aria extends IncantesimoMaleficoImpl implements Incantesimo {

	public Aria(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.ARIA;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_ARIA_COSTO_LANCIO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ARIA;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_ARIA_DANNI;
	}
}
