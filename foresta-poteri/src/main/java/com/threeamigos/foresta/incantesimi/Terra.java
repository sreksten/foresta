package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * L'incantesimo di Terra: un incantesimo malefico di danno di Terra (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_TERRA_*}.
 */
public class Terra extends IncantesimoMaleficoImpl implements Incantesimo {

	public Terra(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.TERRA;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.TERRA;
	}

	public int getDanni() {
		return CostantiIncantesimi.INCANTESIMO_TERRA_DANNI;
	}
}
