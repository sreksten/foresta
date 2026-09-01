package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;

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
		return Costanti.INCANTESIMO_FUOCO_COSTO_ACQUISTO;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_FUOCO_COSTO_LANCIO;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_FUOCO_DANNI;
	}
}
