package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;

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
		return Costanti.INCANTESIMO_TERRA_COSTO_ACQUISTO;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_TERRA_COSTO_LANCIO;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_TERRA_DANNI;
	}
}
