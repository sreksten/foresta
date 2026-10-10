package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.tipi.Comando;

/**
 * I canti innati del Bardo e della Cantastorie (vedi CantoDelBardo e bardo.md)
 */
public enum TipoCanto {

	CURATIVO(Comando.CANTO_CURATIVO, "Canto curativo", "Cura il personaggio più ferito del gruppo."),
	IPNOTICO(Comando.CANTO_IPNOTICO, "Canto ipnotico", "Spaventa o confonde gli avversari."),
	DI_VALORE(Comando.CANTO_DI_VALORE, "Canto di valore", "Rafforza il coraggio e la precisione di tutto il gruppo.");

	private final Comando comando;
	private final String nome;
	private final String descrizione;

	TipoCanto(Comando comando, String nome, String descrizione) {
		this.comando = comando;
		this.nome = nome;
		this.descrizione = descrizione;
	}

	/**
	 * Il comando con cui il giocatore sceglie questo canto nel menu degli incantesimi
	 */
	public Comando getComando() {
		return comando;
	}

	/**
	 * @return il canto scelto con quel comando, o null se il comando non è di un canto
	 */
	public static TipoCanto ofComando(Comando comando) {
		for (TipoCanto tipo : values()) {
			if (tipo.comando == comando) {
				return tipo;
			}
		}
		return null;
	}

	public String getNome() {
		return nome;
	}

	public String getDescrizione() {
		return descrizione;
	}
}
