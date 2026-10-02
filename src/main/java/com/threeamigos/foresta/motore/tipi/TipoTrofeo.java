package com.threeamigos.foresta.motore.tipi;

/**
 * I trofei che il giocatore può vincere. A differenza delle missioni non scadono mai e
 * valgono da una partita all'altra: una volta vinto, un trofeo non si perde più.
 */
public enum TipoTrofeo {

	SBEVAZZONE("Sbevazzone", "Visita 100 locande nella foresta o in città"),
	AMMAZZAGOBLIN("Ammazzagoblin", "Uccidi 100 goblin"),
	AMICO_DI_TUTTI("Amico di tutti", "Fai amicizia 100 volte"),
	CORRUTTORE("Corruttore", "Porta a termine 100 tentativi di corruzione"),
	UCCIDI_IL_DRAGO("Uccidi il Drago", "Sconfiggi il Drago"),
	UCCIDI_LA_STREGA("Uccidi la Strega", "Sconfiggi la Strega"),
	UCCIDI_IL_LICH("Uccidi il Lich", "Sconfiggi il Lich"),
	UCCIDI_L_IDRA("Uccidi l'Idra", "Sconfiggi l'Idra"),
	UCCIDI_IL_MINOTAURO_GIGANTE("Uccidi il Minotauro Gigante", "Sconfiggi il Minotauro Gigante");

	private final String nome;
	private final String descrizione;

	TipoTrofeo(String nome, String descrizione) {
		this.nome = nome;
		this.descrizione = descrizione;
	}

	public String getNome() {
		return nome;
	}

	public String getDescrizione() {
		return descrizione;
	}
}
