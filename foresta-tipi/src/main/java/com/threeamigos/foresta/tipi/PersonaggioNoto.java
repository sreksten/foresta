package com.threeamigos.foresta.tipi;

/**
 * I personaggi non combattenti del mondo che hanno un nome proprio e che il registro dei personaggi tiene in conto
 * (vedi RegistroPersonaggi.getResidente): ognuno dice di che classe è, a quale ambito appartiene e da quale produzione di
 * nomi.txt prende il nome. Il piano è in nomi.md.
 * <p>
 * Il locandiere è qui per completezza, ma il suo nome non sta nel registro: lo decide la locanda (vedi
 * Locanda.LOCANDA_NOME_LOCANDIERE). Il campione di una sfida ha un nome ed epiteto scelti dalla missione e la classe
 * che gli ha dato.
 */
public enum PersonaggioNoto {

	BARDO_DELLE_LOCANDE(TipoPersonaggio.BARDO_LOCANDA, Ambito.GLOBALE, "NOME_MASCHILE", false),
	MOGLIE_DEL_BARDO(TipoPersonaggio.MOGLIE_DEL_BARDO, Ambito.GLOBALE, "NOME_MOGLIE", false),

	BORGOMASTRO(TipoPersonaggio.UOMO, Ambito.PER_CITTA, "NOME_ALTISONANTE", true),
	CAPITANO(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, Ambito.PER_CITTA, "NOME_CAPITANO", true),
	ALCHIMISTA(TipoPersonaggio.ALCHIMISTA, Ambito.PER_CITTA, "NOME_MASTRO", true),
	ARMAIOLO(TipoPersonaggio.ARMAIOLO, Ambito.PER_CITTA, "NOME_MASTRO", true),
	VENDITORE_DI_PERGAMENE(TipoPersonaggio.VENDITORE_DI_PERGAMENE, Ambito.PER_CITTA, "NOME_MASTRO", true),
	INCANTATORE(TipoPersonaggio.INCANTATORE, Ambito.PER_CITTA, "NOME_MASTRO", true),

	// Chi apre una bottega nella Foresta (vedi missioni.incarichi.IlNegozio): uno per mestiere, con la provenienza della città da cui parte la missione
	ALCHIMISTA_DELLA_FORESTA(TipoPersonaggio.ALCHIMISTA, Ambito.PER_NEGOZIO, "NOME_MASTRO", true),
	ARMAIOLO_DELLA_FORESTA(TipoPersonaggio.ARMAIOLO, Ambito.PER_NEGOZIO, "NOME_MASTRO", true),
	VENDITORE_DI_PERGAMENE_DELLA_FORESTA(TipoPersonaggio.VENDITORE_DI_PERGAMENE, Ambito.PER_NEGOZIO, "NOME_MASTRO", true),
	INCANTATORE_DELLA_FORESTA(TipoPersonaggio.INCANTATORE, Ambito.PER_NEGOZIO, "NOME_MASTRO", true),

	LOCANDIERE(TipoPersonaggio.LOCANDIERE, Ambito.PER_LOCANDA, null, false),

	LEVATRICE(TipoPersonaggio.DONNA, Ambito.COMPARSA, "NOME_COMPARSA", false),

	CAMPIONE(null, Ambito.PER_SFIDA, null, false);

	/**
	 * Quanti ce ne sono nel mondo e da dove prende il nome un personaggio noto.
	 */
	public enum Ambito {
		/** Uno solo in tutto il mondo (il bardo e sua moglie). */
		GLOBALE,
		/** Uno per città: la chiave è il personaggio più la città. */
		PER_CITTA,
		/** Uno per bottega della Foresta, quindi per missione: la chiave è la città da cui parte, e dà la provenienza. */
		PER_NEGOZIO,
		/** Uno per locanda: il nome sta nella locanda. */
		PER_LOCANDA,
		/** Compare al volo, alla prima richiesta, con un nome da un gruppo che si ripete. */
		COMPARSA,
		/** Uno per sfida: nome e classe li dà la missione. */
		PER_SFIDA
	}

	private final TipoPersonaggio tipo;
	private final Ambito ambito;
	private final String produzioneNomi;
	private final boolean conProvenienza;

	PersonaggioNoto(TipoPersonaggio tipo, Ambito ambito, String produzioneNomi, boolean conProvenienza) {
		this.tipo = tipo;
		this.ambito = ambito;
		this.produzioneNomi = produzioneNomi;
		this.conProvenienza = conProvenienza;
	}

	/**
	 * La classe del personaggio (il borgomastro è un Uomo e la levatrice una Donna, finché non avranno una classe loro), o
	 * null se la dà la missione (il campione).
	 */
	public TipoPersonaggio getTipo() {
		return tipo;
	}

	public Ambito getAmbito() {
		return ambito;
	}

	/**
	 * La produzione di nomi.txt da cui prende il nome, o null se il nome lo decide un altro.
	 */
	public String getProduzioneNomi() {
		return produzioneNomi;
	}

	/**
	 * Se il suo nome completo porta la provenienza: "Mastro Ferruccio da Ruuna".
	 */
	public boolean isConProvenienza() {
		return conProvenienza;
	}

	/**
	 * La chiave con cui il registro lo ricorda: la voce, e per chi ha un ambito più stretto il luogo o la sfida.
	 */
	public String chiave(String dettaglio) {
		return dettaglio == null ? name() : name() + ":" + dettaglio;
	}
}
