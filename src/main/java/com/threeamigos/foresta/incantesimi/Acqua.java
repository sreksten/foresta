package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;

public class Acqua extends IncantesimoMaleficoImpl implements Incantesimo {

	public ClassiIncantesimo getClasse() {
		return ClassiIncantesimo.ACQUA;
	}

	public String getNomeAbbreviato() {
		return "Acqua";
	}

	public String getNomeSingolare() {
		return "incantesimo dell'Acqua";
	}

	public String getNomePlurale() {
		return "incantesimi dell'Acqua";
	}

	@Override
	public PortataIncantesimo getPortata() {
		return PortataIncantesimo.GRUPPO;
	}

	@Override
	public int getCostoAcquisto() {
		return Costanti.INCANTESIMO_ACQUA_COSTO_ACQUISTO;
	}
	
	@Override
	public int getCostoLancio() {
		return Costanti.INCANTESIMO_ACQUA_COSTO_LANCIO;
	}
	
	@Override
	public int getDanni() {
		return Costanti.INCANTESIMO_ACQUA_DANNI;
	}
}
