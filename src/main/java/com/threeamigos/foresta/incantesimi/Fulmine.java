package com.threeamigos.foresta.incantesimi;

public class Fulmine extends IncantesimoMaleficoImpl implements Incantesimo {

	public ClassiIncantesimo getClasse() {
		return ClassiIncantesimo.FULMINE;
	}

	public String getNomeAbbreviato() {
		return "Fulmine";
	}

	public String getNomeSingolare() {
		return "incantesimo del Fulmine";
	}

	public String getNomePlurale() {
		return "incantesimi del Fulmine";
	}

	public PortataIncantesimo getPortata() {
		return PortataIncantesimo.GRUPPO;
	}

	public int getCostoAcquisto() {
		return 15;
	}

	public int getCostoLancio() {
		return 10;
	}

	public int getDanni() {
		return 100;
	}
}
