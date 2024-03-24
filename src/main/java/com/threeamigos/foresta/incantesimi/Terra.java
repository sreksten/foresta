package com.threeamigos.foresta.incantesimi;

public class Terra extends IncantesimoMaleficoImpl implements Incantesimo {

	public ClassiIncantesimo getClasse() {
		return ClassiIncantesimo.TERRA;
	}

	public String getNomeAbbreviato() {
		return "Terra";
	}

	public String getNomeSingolare() {
		return "incantesimo di Terra";
	}

	public String getNomePlurale() {
		return "incantesimi di Terra";
	}

	public PortataIncantesimo getPortata() {
		return PortataIncantesimo.GRUPPO;
	}

	public int getCostoAcquisto() {
		return 5;
	}

	public int getCostoLancio() {
		return 2;
	}

	public int getDanni() {
		return 50;
	}
}
