package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * L'incantesimo del Fuoco: un incantesimo malefico di danno di Fuoco (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_FUOCO_*}.
 */
public class Fuoco extends IncantesimoMaleficoImpl implements Incantesimo {

	public Fuoco(int livello) {
		super(livello);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.FUOCO;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_FUOCO_COSTO_LANCIO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.FUOCO;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_FUOCO_DANNI;
	}
}
