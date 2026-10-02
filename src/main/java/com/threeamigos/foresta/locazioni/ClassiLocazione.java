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
	GROTTA_RECUPERA_IL_MEDAGLIONE(GrottaRecuperaIlMedaglione::new, TipoLocazione.MISSIONE_SECONDARIA, "la grotta dei ladri del Medaglione"),
	ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI(RovineRecuperaLeDerrateAlimentari::new, TipoLocazione.MISSIONE_SECONDARIA, "il covo dei Troll che hanno rubato il carico di derrate alimentari"),
	/*
	 * Città
	 */
	CITTA_NYENA(CittaNyena::new, TipoLocazione.CITTA, "la città di Nyena"),
	CITTA_MALGAARD(CittaMalgaard::new, TipoLocazione.CITTA, "la città di Malgaard"),
	CITTA_RUUNA(CittaRuuna::new, TipoLocazione.CITTA, "la città di Ruuna"),
	CITTA_FLEENA(CittaFleena::new, TipoLocazione.CITTA, "la città di Fleena"),
	/*
	 * Castelli
	 */
	CASTELLO_IDRA(CastelloIdra::new, TipoLocazione.CASTELLO, "la Rocca del Sangue"),
	CASTELLO_MINOTAURO(CastelloMinotauro::new, TipoLocazione.CASTELLO, "la Torre della Paura"),
	CASTELLO_LICH(CastelloLich::new, TipoLocazione.CASTELLO, "il Castello dell'Ombra"),
	CASTELLO_STREGA(CastelloStrega::new, TipoLocazione.CASTELLO, "il Maniero del Malefizio"),
	CASTELLO_DRAGO(CastelloDrago::new, TipoLocazione.CASTELLO, "il Castello della Morte Alata");

	private final Supplier<Locazione> supplier;
	private final TipoLocazione tipoLocazione;
	private final String nomeProprio;

	ClassiLocazione(Supplier<Locazione> supplier, TipoLocazione tipoLocazione) {
		this(supplier, tipoLocazione, null);
	}

	ClassiLocazione(Supplier<Locazione> supplier, TipoLocazione tipoLocazione, String nomeProprio) {
		this.supplier = supplier;
		this.tipoLocazione = tipoLocazione;
		this.nomeProprio = nomeProprio;
	}

	/**
	 * Il nome, con l'articolo, di una locazione unica ("la città di Ruuna", "il Maniero del Malefizio"); null per le
	 * altre. Quando la si costruisce finisce nel nome della casella (LocazioneMD.NOME), come quello delle locande e
	 * dei templi.
	 */
	public String getNomeProprio() {
		return nomeProprio;
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
