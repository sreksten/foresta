package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;

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
		return Costanti.INCANTESIMO_FULMINE_COSTO_ACQUISTO;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_FULMINE_COSTO_LANCIO;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_FULMINE_DANNI;
	}
}
