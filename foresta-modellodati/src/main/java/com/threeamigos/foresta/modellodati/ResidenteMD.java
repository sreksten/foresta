package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.io.IOException;

/**
 * Chi ha un nome proprio nel mondo senza essere in un gruppo o in una locazione: il bardo e sua moglie, le persone di una
 * città, i campioni delle sfide... Vedi RegistroPersonaggiMD e nomi.md.
 */
public class ResidenteMD {

	private final String nome;
	// La città di cui è, oppure null: "Ruuna"
	private final String provenienza;
	// Null per chi non ha ancora una classe (il borgomastro, la levatrice)
	private final TipoPersonaggio classe;
	private boolean sconfitto;

	public ResidenteMD(String nome, String provenienza, TipoPersonaggio classe) {
		this.nome = Serializzabile.pulisciCampo(nome);
		this.provenienza = Serializzabile.pulisciCampo(provenienza);
		this.classe = classe;
	}

	public String getNome() {
		return nome;
	}

	public String getProvenienza() {
		return provenienza;
	}

	public TipoPersonaggio getClasse() {
		return classe;
	}

	/**
	 * Solo per i campioni delle sfide: se il gruppo li ha battuti.
	 */
	public boolean isSconfitto() {
		return sconfitto;
	}

	public void setSconfitto(boolean sconfitto) {
		this.sconfitto = sconfitto;
	}

	void salva(java.io.PrintWriter stream, String chiave) {
		stream.println(chiave + Serializzabile.PIPE + nome + Serializzabile.PIPE + Serializzabile.facoltativo(provenienza)
				+ Serializzabile.PIPE + Serializzabile.facoltativo(classe) + Serializzabile.PIPE + sconfitto);
	}

	/**
	 * Rilegge la riga scritta da {@link #salva}: la chiave è il primo campo, che legge chi chiama.
	 */
	static ResidenteMD leggi(LettoreCampi campi) throws IOException {
		String nome = campi.testo();
		String provenienza = campi.testoFacoltativo();
		TipoPersonaggio classe = campi.enumeratoFacoltativo(TipoPersonaggio.class);
		ResidenteMD residente = new ResidenteMD(nome, provenienza, classe);
		residente.sconfitto = campi.booleano();
		return residente;
	}
}
