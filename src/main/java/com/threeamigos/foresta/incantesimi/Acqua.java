package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

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
