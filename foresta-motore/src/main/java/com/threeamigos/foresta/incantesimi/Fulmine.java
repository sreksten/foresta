package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;

/**
 * L'incantesimo di Fulmine: un incantesimo malefico di danno di Fulmine (vedi {@link IncantesimoMaleficoImpl}). Costo e danni stanno nelle costanti {@code INCANTESIMO_FULMINE_*}.
 */
public class Fulmine extends IncantesimoMaleficoImpl implements Incantesimo {

	public Fulmine(int livello) {
		super(livello);
	}

	public ClasseIncantesimo getClasse() {
		return ClasseIncantesimo.FULMINE;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_FULMINE_COSTO_LANCIO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.FULMINE;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_FULMINE_DANNI;
	}
}
