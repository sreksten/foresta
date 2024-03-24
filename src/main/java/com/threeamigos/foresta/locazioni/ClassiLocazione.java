package com.threeamigos.foresta.locazioni;

import java.util.function.Supplier;

public enum ClassiLocazione {
	
	/*
	 * Locazioni standard della Foresta
	 */
	RADURA(Radura::getIstanza, TipoLocazione.STANDARD),
	BOSCO(Bosco::getIstanza, TipoLocazione.STANDARD),
	PALUDE(Palude::getIstanza, TipoLocazione.STANDARD),
	LOCANDA(Locanda::getIstanza, TipoLocazione.STANDARD),
	ROVINE(Rovine::getIstanza, TipoLocazione.STANDARD),
	TEMPIO(Tempio::getIstanza, TipoLocazione.STANDARD),
	GROTTA(Grotta::getIstanza, TipoLocazione.STANDARD),
	/*
	 * Locazioni per le missioni secondarie
	 */
	GROTTA_RECUPERA_IL_MEDAGLIONE(GrottaRecuperaIlMedaglione::getIstanza, TipoLocazione.MISSIONE_SECONDARIA),
	ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI(RovineRecuperaLeDerrateAlimentari::getIstanza, TipoLocazione.MISSIONE_SECONDARIA),
	/*
	 * Città
	 */
	CITTA_NYENA(CittaNyena::getIstanza, TipoLocazione.CITTA),
	CITTA_MALGAARD(CittaMalgaard::getIstanza, TipoLocazione.CITTA),
	CITTA_RUUNA(CittaRuuna::getIstanza, TipoLocazione.CITTA),
	CITTA_FLEENA(CittaFleena::getIstanza, TipoLocazione.CITTA),
	/*
	 * L'Alchimista si trova unicamente in città
	 */
	ALCHIMISTA(Alchimista::getIstanza, TipoLocazione.STANDARD),
	/*
	 * Castelli
	 */
	CASTELLO_IDRA(CastelloIdra::getIstanza, TipoLocazione.CASTELLO),
	CASTELLO_MINOTAURO(CastelloMinotauro::getIstanza, TipoLocazione.CASTELLO),
	CASTELLO_LICH(CastelloLich::getIstanza, TipoLocazione.CASTELLO),
	CASTELLO_STREGA(CastelloStrega::getIstanza, TipoLocazione.CASTELLO),
	CASTELLO_DRAGO(CastelloDrago::getIstanza, TipoLocazione.CASTELLO);

	private Supplier<Locazione> supplier;
	private TipoLocazione tipoLocazione;
	
	private ClassiLocazione(Supplier<Locazione> supplier, TipoLocazione tipoLocazione) {
		this.supplier = supplier;
		this.tipoLocazione = tipoLocazione;
	}
	
	public enum TipoLocazione {
		STANDARD,
		CITTA,
		CASTELLO,
		MISSIONE_SECONDARIA
	}

	public final Locazione getIstanza() {
		return supplier.get();
	}

	public TipoLocazione getTipoLocazione() {
		return tipoLocazione;
	}
	
	public final boolean isLocazioneUnica() {
		return tipoLocazione != TipoLocazione.STANDARD;
	}
}
