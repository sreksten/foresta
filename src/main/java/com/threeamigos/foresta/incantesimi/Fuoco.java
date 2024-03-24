package com.threeamigos.foresta.incantesimi;

public class Fuoco extends IncantesimoMaleficoImpl implements Incantesimo {

	public ClassiIncantesimo getClasse() {
		return ClassiIncantesimo.FUOCO;
	}

	public String getNomeAbbreviato() {
		return "Fuoco";
	}

	public String getNomeSingolare() {
		return "incantesimo del Fuoco";
	}

	public String getNomePlurale() {
		return "incantesimi del Fuoco";
	}

	public PortataIncantesimo getPortata() {
		return PortataIncantesimo.GRUPPO;
	}

	public int getCostoAcquisto() {
		return 5;
	}

	public int getCostoLancio() {
		return 3;
	}

	public int getDanni() {
		return 60;
	}
}
