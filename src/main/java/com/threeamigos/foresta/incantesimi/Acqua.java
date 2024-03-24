package com.threeamigos.foresta.incantesimi;

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
		return 5;
	}
	
	@Override
	public int getCostoLancio() {
		return 1;
	}
	
	@Override
	public int getDanni() {
		return 45;
	}
}
