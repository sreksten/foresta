package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.testi.Testi;

/**
 * Le locazioni della Foresta.
 * <p>
 * IL nome è anche l'identificativo che salva il modello dati (LocazioneMD, ForestaMD...) e che leggono
 * motore e UI.
 * <p>
 * La locazione vera la costruisce locazioni.FabbricaLocazioni. I nomi delle locazioni uniche non stanno
 * qui ma nel file {@code tipo_locazione_<lingua>.properties}.
 * <p>
 * I testi di ogni costante (NOME_PROPRIO, NOME_DELLA_CITTA) non stanno qui ma nel file {@code tipo_locazione_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoLocazione {

	/*
	 * Locazioni standard della Foresta
	 */
	/**
	 * Uno spiazzo senza alberi, dove crescono alcuni ingredienti per l'alchimista.
	 */
	RADURA(CategoriaLocazione.STANDARD),
	/**
	 * Il bosco: la maggior parte della Foresta.
	 */
	BOSCO(CategoriaLocazione.STANDARD),
	/**
	 * Una palude.
	 */
	PALUDE(CategoriaLocazione.STANDARD),
	/**
	 * Una locanda nel bosco, con il suo locandiere.
	 */
	LOCANDA(CategoriaLocazione.STANDARD),
	/**
	 * Rovine di un castello o di una città, o di una bottega non ancora riaperta.
	 */
	ROVINE(CategoriaLocazione.STANDARD),
	/**
	 * Un tempio, che a volte custodisce un artefatto.
	 */
	TEMPIO(CategoriaLocazione.STANDARD),
	/**
	 * Una grotta.
	 */
	GROTTA(CategoriaLocazione.STANDARD),
	/*
	 * Le botteghe nella Foresta: nascono dalle rovine, alla fine di una missione (vedi missioni.incarichi.IlNegozio)
	 */
	/**
	 * La bottega di un alchimista nella Foresta.
	 */
	ALCHIMISTA(CategoriaLocazione.STANDARD),
	/**
	 * La bottega di un armaiolo nella Foresta.
	 */
	ARMAIOLO(CategoriaLocazione.STANDARD),
	/**
	 * La bottega di un venditore di pergamene nella Foresta.
	 */
	VENDITORE_DI_PERGAMENE(CategoriaLocazione.STANDARD),
	/**
	 * La bottega di un incantatore nella Foresta.
	 */
	INCANTATORE(CategoriaLocazione.STANDARD),
	/*
	 * Città
	 */
	/**
	 * La città di Nyena.
	 */
	CITTA_NYENA(CategoriaLocazione.CITTA),
	/**
	 * La città di Malgaard.
	 */
	CITTA_MALGAARD(CategoriaLocazione.CITTA),
	/**
	 * La città di Ruuna.
	 */
	CITTA_RUUNA(CategoriaLocazione.CITTA),
	/**
	 * La città di Fleena.
	 */
	CITTA_FLEENA(CategoriaLocazione.CITTA),
	/*
	 * Castelli
	 */
	/**
	 * Il castello dell'Idra.
	 */
	CASTELLO_IDRA(CategoriaLocazione.CASTELLO),
	/**
	 * Il castello del Minotauro.
	 */
	CASTELLO_MINOTAURO(CategoriaLocazione.CASTELLO),
	/**
	 * Il castello del Lich.
	 */
	CASTELLO_LICH(CategoriaLocazione.CASTELLO),
	/**
	 * Il castello della Strega.
	 */
	CASTELLO_STREGA(CategoriaLocazione.CASTELLO),
	/**
	 * Il castello del Drago.
	 */
	CASTELLO_DRAGO(CategoriaLocazione.CASTELLO);

	private final CategoriaLocazione categoriaLocazione;

	TipoLocazione(CategoriaLocazione categoriaLocazione) {
		this.categoriaLocazione = categoriaLocazione;
	}

	/**
	 * La categoria di questa locazione: standard, città o castello.
	 */
	public CategoriaLocazione getCategoriaLocazione() {
		return categoriaLocazione;
	}

	/**
	 * Il nome, con l'articolo, di una locazione unica ("la città di Ruuna", "il Maniero del Malefizio"); null per le
	 * altre. Quando la si costruisce finisce nel nome della casella (LocazioneMD.NOME), come quello delle locande e
	 * dei templi. Sta nel file {@code tipo_locazione_<lingua>.properties}, alla voce {@code NOME_PROPRIO}, solo per le
	 * locazioni uniche.
	 */
	public String getNomeProprio() {
		return Testi.testo(this, "NOME_PROPRIO").orElse(null);
	}

	/**
	 * Il nome di una città senza articolo né "la città di": "Ruuna"; null per le altre locazioni. È la provenienza dei
	 * suoi abitanti ("Mastro Ferruccio da Ruuna").
	 */
	public String getNomeDellaCitta() {
		if (categoriaLocazione != CategoriaLocazione.CITTA) {
			return null;
		}
		// Una voce a sé e non il nome proprio senza l'articolo ("la città di "): quel prefisso è italiano, e in un'altra
		// lingua l'articolo e il "di" stanno altrove
		return Testi.testo(this, "NOME_DELLA_CITTA").orElse(null);
	}

	/**
	 * Se è la bottega di un alchimista, di un armaiolo, di un venditore di pergamene o di un incantatore, nella Foresta: vi si
	 * entra come nel negozio di una città, e non vi si combatte.
	 */
	public boolean isBottega() {
		return this == ALCHIMISTA || this == ARMAIOLO || this == VENDITORE_DI_PERGAMENE || this == INCANTATORE;
	}

	/**
	 * Se ce n'è una sola nella Foresta (le città e i castelli).
	 */
	public final boolean isLocazioneUnica() {
		return categoriaLocazione != CategoriaLocazione.STANDARD;
	}
}
