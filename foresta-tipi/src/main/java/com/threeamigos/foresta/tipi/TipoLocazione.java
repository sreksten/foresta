package com.threeamigos.foresta.tipi;

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
	CITTA_NYENA(CategoriaLocazione.CITTA),
	CITTA_MALGAARD(CategoriaLocazione.CITTA),
	CITTA_RUUNA(CategoriaLocazione.CITTA),
	CITTA_FLEENA(CategoriaLocazione.CITTA),
	/*
	 * Castelli
	 */
	CASTELLO_IDRA(CategoriaLocazione.CASTELLO),
	CASTELLO_MINOTAURO(CategoriaLocazione.CASTELLO),
	CASTELLO_LICH(CategoriaLocazione.CASTELLO),
	CASTELLO_STREGA(CategoriaLocazione.CASTELLO),
	CASTELLO_DRAGO(CategoriaLocazione.CASTELLO);

	private final CategoriaLocazione categoriaLocazione;

	TipoLocazione(CategoriaLocazione categoriaLocazione) {
		this.categoriaLocazione = categoriaLocazione;
	}

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

	public final boolean isLocazioneUnica() {
		return categoriaLocazione != CategoriaLocazione.STANDARD;
	}
}
