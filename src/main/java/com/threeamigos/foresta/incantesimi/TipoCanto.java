package com.threeamigos.foresta.incantesimi;

/**
 * I canti innati del Bardo e della Cantastorie (vedi CantoDelBardo e bardo.md)
 */
public enum TipoCanto {

	CURATIVO("Canto curativo", "Cura il personaggio più ferito del gruppo."),
	IPNOTICO("Canto ipnotico", "Spaventa o confonde gli avversari."),
	DI_VALORE("Canto di valore", "Rafforza il coraggio e la precisione di tutto il gruppo.");

	private final String nome;
	private final String descrizione;

	TipoCanto(String nome, String descrizione) {
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
