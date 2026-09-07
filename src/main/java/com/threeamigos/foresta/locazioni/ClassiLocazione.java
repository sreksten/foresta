package com.threeamigos.foresta.locazioni;

import java.util.function.Supplier;

public enum ClassiLocazione {
	
	/*
	 * Locazioni standard della Foresta
	 */
	RADURA(Radura::new, TipoLocazione.STANDARD),
	BOSCO(Bosco::new, TipoLocazione.STANDARD),
	PALUDE(Palude::new, TipoLocazione.STANDARD),
	LOCANDA(Locanda::new, TipoLocazione.STANDARD),
	ROVINE(Rovine::new, TipoLocazione.STANDARD),
	TEMPIO(Tempio::new, TipoLocazione.STANDARD),
	GROTTA(Grotta::new, TipoLocazione.STANDARD),
	/*
	 * Locazioni per le missioni secondarie
	 */
	GROTTA_RECUPERA_IL_MEDAGLIONE(GrottaRecuperaIlMedaglione::new, TipoLocazione.MISSIONE_SECONDARIA),
	ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI(RovineRecuperaLeDerrateAlimentari::new, TipoLocazione.MISSIONE_SECONDARIA),
	/*
	 * Città
	 */
	CITTA_NYENA(CittaNyena::new, TipoLocazione.CITTA),
	CITTA_MALGAARD(CittaMalgaard::new, TipoLocazione.CITTA),
	CITTA_RUUNA(CittaRuuna::new, TipoLocazione.CITTA),
	CITTA_FLEENA(CittaFleena::new, TipoLocazione.CITTA),
	/*
	 * L'Alchimista si trova unicamente in città
	 */
	ALCHIMISTA(Alchimista::new, TipoLocazione.STANDARD),
	/*
	 * Castelli
	 */
	CASTELLO_IDRA(CastelloIdra::new, TipoLocazione.CASTELLO),
	CASTELLO_MINOTAURO(CastelloMinotauro::new, TipoLocazione.CASTELLO),
	CASTELLO_LICH(CastelloLich::new, TipoLocazione.CASTELLO),
	CASTELLO_STREGA(CastelloStrega::new, TipoLocazione.CASTELLO),
	CASTELLO_DRAGO(CastelloDrago::new, TipoLocazione.CASTELLO);

	private final Supplier<Locazione> supplier;
	private final TipoLocazione tipoLocazione;
	
	ClassiLocazione(Supplier<Locazione> supplier, TipoLocazione tipoLocazione) {
		this.supplier = supplier;
		this.tipoLocazione = tipoLocazione;
	}
	
	public enum TipoLocazione {
		STANDARD,
		CITTA,
		CASTELLO,
		MISSIONE_SECONDARIA
	}

	/**
	 * Costruisce una nuova istanza della locazione, come fa
	 * {@code ClassePersonaggio.getIstanza(livello)}. Chi la usa per una casella
	 * della Foresta deve poi passarle il modello dati di quella casella.
	 */
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
