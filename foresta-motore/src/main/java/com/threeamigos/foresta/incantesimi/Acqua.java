package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;

/**
 * L'incantesimo di Acqua: un incantesimo malefico di danno di Acqua (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_ACQUA_*}.
 */
public class Acqua extends IncantesimoMaleficoImpl implements Incantesimo {

	public Acqua(int livello) {
		super(livello);
	}

	public ClasseIncantesimo getClasse() {
		return ClasseIncantesimo.ACQUA;
	}

	@Override
	public int getCostoLancio() {
		return Costanti.INCANTESIMO_ACQUA_COSTO_LANCIO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ACQUA;
	}

	@Override
	public int getDanni() {
		return Costanti.INCANTESIMO_ACQUA_DANNI;
	}
}
