package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.modellodati.Serializzabile;

abstract class GestorePunteggiBase extends GestoreSuFile implements InterfacciaGestorePunteggi {

	private static final int NUMERO_MASSIMO = 10;

	private final String[] nomi = new String[NUMERO_MASSIMO];
	private final int[] punteggi = new int[NUMERO_MASSIMO];

	public GestorePunteggiBase() {
		if (!carica()) {
			punteggi[0] = 10000;
			nomi[0]     = "Stefano";
			punteggi[1] =  9000;
			nomi[1]     = "Johan";
			punteggi[2] =  8000;
			nomi[2]     = "Alessandra";
			punteggi[3] =  7000;
			nomi[3]     = "Peter Porker";
			punteggi[4] =  6000;
			nomi[4]     = "Judah";
			punteggi[5] =  5000;
			nomi[5]     = "Jona";
			punteggi[6] =  2000;
			nomi[6]     = "Jamaikan";
			punteggi[7] =  1000;
			nomi[7]     = "Cosimo";
			punteggi[8] =   500;
			nomi[8]     = "Oreste";
			punteggi[9] =     0;
			nomi[9]     = "Axel e il sERCIO";
			salva();
		}
	}

	public int getConteggio() {
		return NUMERO_MASSIMO;
	}
	
	public Punteggio getPunteggio(int posizione) {
		return new PunteggioImpl(nomi[posizione], punteggi[posizione]);
	}

	public boolean isPunteggioInClassifica(int punteggio) {
		return punteggio > punteggi[NUMERO_MASSIMO - 1];
	}

	public void addPunteggio(String nome, int punteggio) {
		int posizione = NUMERO_MASSIMO - 1;
		while (posizione > 0 && punteggio > punteggi[posizione - 1])
			posizione--;
		for (int i = NUMERO_MASSIMO - 1; i > posizione; i--) {
			punteggi[i] = punteggi[i - 1];
			nomi[i] = nomi[i - 1];
		}
		punteggi[posizione] = punteggio;
		nomi[posizione] = pulisciNome(nome);
		salva();
	}
	
	protected void setPunteggio(int posizione, String nome, int punteggio) {
		punteggi[posizione] = punteggio;
		nomi[posizione] = pulisciNome(nome);
	}

	/**
	 * Toglie in silenzio il separatore "|", come per i salvataggi; un nome vuoto diventa "nessun nome".
	 */
	static String pulisciNome(String nome) {
		String pulito = nome == null ? "" : Serializzabile.senzaPipe(nome);
		return pulito.trim().isEmpty() ? Serializzabile.NESSUN_NOME : pulito;
	}
	
	private static class PunteggioImpl implements Punteggio {
		
		private final String nome;
		private final int punteggio;
		
		PunteggioImpl(String nome, int punteggio) {
			this.nome = nome;
			this.punteggio = punteggio;
		}
		
		public String getNome() {
			return nome;
		}
		
		public int getPunteggio() {
			return punteggio;
		}
	}
}
