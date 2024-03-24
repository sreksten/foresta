package com.threeamigos.foresta.offerte;

import java.util.function.Supplier;

public enum ClassiOfferta {

	AIUTO_GRATUITO(AiutoGratuito::new),
	AIUTO_MERCENARIO(AiutoMercenario::new),
	INCANTESIMI(Incantesimi::new),
	INFORMAZIONI(Informazioni::new),
	MAPPA_FORESTA(MappaForesta::new),
	MAPPA_ZONA(MappaZona::new),
	PASTO(Pasto::new);

	private Supplier<Offerta> supplier;
	
	private ClassiOfferta(Supplier<Offerta> supplier) {
		this.supplier = supplier;
	}

	public Offerta getIstanza() {
		return supplier.get();
	}

}