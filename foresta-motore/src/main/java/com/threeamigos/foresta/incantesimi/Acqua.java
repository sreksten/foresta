package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * L'incantesimo di Acqua: un incantesimo malefico di danno di Acqua (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_ACQUA_*}.
 */
public class Acqua extends IncantesimoMaleficoImpl implements Incantesimo {

	public Acqua(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.ACQUA;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ACQUA;
	}

	@Override
	public int getDanni() {
		return CostantiIncantesimi.INCANTESIMO_ACQUA_DANNI;
	}
}
