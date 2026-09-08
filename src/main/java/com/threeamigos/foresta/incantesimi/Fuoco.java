package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

public class Fuoco extends IncantesimoMaleficoImpl implements Incantesimo {

	public Fuoco(int livello) {
		super(livello);
	}

	public ClasseIncantesimo getClasse() {
		return ClasseIncantesimo.FUOCO;
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
