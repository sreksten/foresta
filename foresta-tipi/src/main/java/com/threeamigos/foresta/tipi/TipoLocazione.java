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
	 * Le botteghe nella Foresta: nascono dalle rovine, alla fine di una missione (vedi missioni.incarichi.IlNegozio)
	 */
	ALCHIMISTA(CategoriaLocazione.STANDARD),
	ARMAIOLO(CategoriaLocazione.STANDARD),
	VENDITORE_DI_PERGAMENE(CategoriaLocazione.STANDARD),
	INCANTATORE(CategoriaLocazione.STANDARD),
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

	/**
	 * Il nome di una città senza articolo né "la città di": "Ruuna"; null per le altre locazioni. È la provenienza dei
	 * suoi abitanti ("Mastro Ferruccio da Ruuna").
	 */
	public String getNomeDellaCitta() {
		if (categoria != CategoriaLocazione.CITTA || nomeProprio == null) {
			return null;
		}
		String prefisso = "la città di ";
		return nomeProprio.startsWith(prefisso) ? nomeProprio.substring(prefisso.length()) : nomeProprio;
	}

	/**
	 * Se è la bottega di un alchimista, di un armaiolo, di un venditore di pergamene o di un incantatore, nella Foresta: vi si
	 * entra come nel negozio di una città, e non vi si combatte.
	 */
	public boolean isBottega() {
		return this == ALCHIMISTA || this == ARMAIOLO || this == VENDITORE_DI_PERGAMENE || this == INCANTATORE;
	}

	public CategoriaLocazione getCategoria() {
		return categoria;
	}

	public final boolean isLocazioneUnica() {
		return categoria != CategoriaLocazione.STANDARD;
	}
}
