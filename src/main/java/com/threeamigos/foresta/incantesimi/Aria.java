package com.threeamigos.foresta.incantesimi;

public class Aria extends IncantesimoMaleficoImpl implements Incantesimo {

	public ClassiIncantesimo getClasse() {
		return ClassiIncantesimo.ARIA;
	}

	public String getNomeAbbreviato() {
		return "Aria";
	}

	public String getNomeSingolare() {
		return "incantesimo dell'Aria";
	}

	public String getNomePlurale() {
		return "incantesimi dell'Aria";
	}

	public PortataIncantesimo getPortata() {
		return PortataIncantesimo.GRUPPO;
	}

	public int getCostoAcquisto() {
		return 5;
	}

	public int getCostoLancio() {
		return 1;
	}

	public int getDanni() {
		return 35;
	}
}
