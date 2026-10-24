package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
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

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ARIA;
	}

	public int getDanni() {
		return CostantiIncantesimi.INCANTESIMO_ARIA_DANNI;
	}
}
