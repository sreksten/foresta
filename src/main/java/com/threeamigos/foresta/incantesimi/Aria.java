package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;

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
		return Costanti.INCANTESIMO_ARIA_COSTO_ACQUISTO;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_ARIA_COSTO_LANCIO;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_ARIA_DANNI;
	}
}
