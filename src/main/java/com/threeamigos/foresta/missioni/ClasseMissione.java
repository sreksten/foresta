package com.threeamigos.foresta.missioni;

import java.util.function.Supplier;

/**
 * Elenca le classi concrete di missione, cosi' che il modello dati possa
 * dichiarare il tipo di ogni nodo dell'albero e la ricostruzione dopo un
 * caricamento sia polimorfica.
 */
public enum ClasseMissione {

	SCONFIGGI_IL_DRAGO(SconfiggiIlDrago::new),
	SCONFIGGI_IL_MINOTAURO_GIGANTE(SconfiggiIlMinotauroGigante::new),
	SCONFIGGI_L_IDRA(SconfiggiLIdra::new),
	SCONFIGGI_IL_LICH(SconfiggiIlLich::new),
	SCONFIGGI_LA_STREGA(SconfiggiLaStrega::new),
	MISSIONE_DI_PROVA(MissioneDIProva::new),
	RECUPERA_IL_MEDAGLIONE(RecuperaIlMedaglione::new),
	RECUPERA_LE_DERRATE_ALIMENTARI(RecuperaLeDerrateAlimentari::new),

	MISSIONE_DI_PROVA_SECONDARIA_UNO(MissioneDiProvaSecondariaUno::new),
	MISSIONE_DI_PROVA_SECONDARIA_DUE(MissioneDiProvaSecondariaDue::new),
	MISSIONE_DI_PROVA_TERZIARIA_UNO(MissioneDiProvaTerziariaUno::new),

	MUOVI_A_LOCAZIONE(MuoviALocazione::new),
	MISSIONE_SECONDARIA(MissioneSecondaria::new);

	private final Supplier<Missione> supplier;

	ClasseMissione(Supplier<Missione> supplier) {
		this.supplier = supplier;
	}

	public Missione getIstanza() {
		return supplier.get();
	}
}
