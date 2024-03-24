package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Logger;

abstract class GestorePunteggiBase implements InterfacciaGestorePunteggi {

	private static final int NUMERO_MASSIMO = 10;

	private int[] punteggi = new int[NUMERO_MASSIMO];
	private String[] nomi = new String[NUMERO_MASSIMO];

	public GestorePunteggiBase() {
		if (!carica()) {
			Logger.log("Reinizializzazione punteggi");
			punteggi[0] = 10000;
			nomi[0]     = "Stefano";
			punteggi[1] =  9000;
			nomi[1]     = "Johan";
			punteggi[2] =  8000;
			nomi[2]     = "Alessandra";
			punteggi[3] =  7000;
			nomi[3]     = "BathMan";
			punteggi[4] =  6000;
			nomi[4]     = "Juda";
			punteggi[5] =  5000;
			nomi[5]     = "Jona";
			punteggi[6] =  2000;
			nomi[6]     = "Cosimo";
			punteggi[7] =  1000;
			nomi[7]     = "Rambo";
			punteggi[8] =   500;
			nomi[8]     = "Oreste";
			punteggi[9] =     0;
			nomi[9]     = "Axel e il sERCIO";
			salva();
		}
	}

	public int getCardinalita() {
		return NUMERO_MASSIMO;
	}
	
	public InterfacciaGestorePunteggi.Record getRecord(int posizione) {
		return new RecordImpl(nomi[posizione], punteggi[posizione]);
	}

	public boolean isPunteggioInClassifica(int punteggio) {
		return punteggio > punteggi[NUMERO_MASSIMO - 1];
	}

	public void addRecord(String nome, int punteggio) {
		int posizione = NUMERO_MASSIMO - 1;
		while (posizione > 0 && punteggio > punteggi[posizione - 1])
			posizione--;
		for (int i = NUMERO_MASSIMO - 1; i > posizione; i--) {
			punteggi[i] = punteggi[i - 1];
			nomi[i] = nomi[i - 1];
		}
		punteggi[posizione] = punteggio;
		nomi[posizione] = nome;
		salva();
	}
	
	protected void setRecord(int posizione, String nome, int punteggio) {
		punteggi[posizione] = punteggio;
		nomi[posizione] = nome;
	}
	
	private class RecordImpl implements InterfacciaGestorePunteggi.Record {
		
		private String nome;
		private int punteggio;
		
		RecordImpl(String nome, int punteggio) {
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
