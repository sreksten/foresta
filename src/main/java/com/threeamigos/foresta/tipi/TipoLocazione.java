package com.threeamigos.foresta.tipi;

/**
 * Le locazioni della Foresta: l'identificativo che salva il modello dati (LocazioneMD, ForestaMD...) e che leggono
 * motore e UI. La locazione vera la costruisce locazioni.FabbricaLocazioni.
 */
public enum TipoLocazione {

	/*
	 * Locazioni standard della Foresta
	 */
	RADURA(CategoriaLocazione.STANDARD),
	BOSCO(CategoriaLocazione.STANDARD),
	PALUDE(CategoriaLocazione.STANDARD),
	LOCANDA(CategoriaLocazione.STANDARD),
	ROVINE(CategoriaLocazione.STANDARD),
	TEMPIO(CategoriaLocazione.STANDARD),
	GROTTA(CategoriaLocazione.STANDARD),
	/*
	 * Locazioni per le missioni secondarie
	 */
	GROTTA_RECUPERA_IL_MEDAGLIONE(CategoriaLocazione.MISSIONE_SECONDARIA, "la grotta dei ladri del Medaglione"),
	ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI(CategoriaLocazione.MISSIONE_SECONDARIA, "il covo dei Troll ladri di derrate"),
	/*
	 * Città
	 */
	CITTA_NYENA(CategoriaLocazione.CITTA, "la città di Nyena"),
	CITTA_MALGAARD(CategoriaLocazione.CITTA, "la città di Malgaard"),
	CITTA_RUUNA(CategoriaLocazione.CITTA, "la città di Ruuna"),
	CITTA_FLEENA(CategoriaLocazione.CITTA, "la città di Fleena"),
	/*
	 * Castelli
	 */
	CASTELLO_IDRA(CategoriaLocazione.CASTELLO, "la Rocca del Sangue"),
	CASTELLO_MINOTAURO(CategoriaLocazione.CASTELLO, "la Torre della Paura"),
	CASTELLO_LICH(CategoriaLocazione.CASTELLO, "il Castello dell'Ombra"),
	CASTELLO_STREGA(CategoriaLocazione.CASTELLO, "il Maniero del Malefizio"),
	CASTELLO_DRAGO(CategoriaLocazione.CASTELLO, "il Castello della Morte Alata");

	private final CategoriaLocazione categoria;
	private final String nomeProprio;

	TipoLocazione(CategoriaLocazione categoria) {
		this(categoria, null);
	}

	TipoLocazione(CategoriaLocazione categoria, String nomeProprio) {
		this.categoria = categoria;
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

	public CategoriaLocazione getCategoria() {
		return categoria;
	}

	public final boolean isLocazioneUnica() {
		return categoria != CategoriaLocazione.STANDARD;
	}
}
