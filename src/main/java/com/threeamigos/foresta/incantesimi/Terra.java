package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

public class Terra extends IncantesimoMaleficoImpl implements Incantesimo {

	public Terra(int livello) {
		super(livello);
	}

	public ClasseIncantesimo getClasse() {
		return ClasseIncantesimo.TERRA;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_TERRA_COSTO_LANCIO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.TERRA;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_TERRA_DANNI;
	}
}
