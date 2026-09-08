package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

public class Aria extends IncantesimoMaleficoImpl implements Incantesimo {

	public Aria(int livello) {
		super(livello);
	}

	public ClasseIncantesimo getClasse() {
		return ClasseIncantesimo.ARIA;
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
