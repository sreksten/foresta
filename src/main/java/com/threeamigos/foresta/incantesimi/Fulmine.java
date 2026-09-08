package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

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
