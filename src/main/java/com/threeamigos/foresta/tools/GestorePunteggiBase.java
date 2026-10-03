package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.modellodati.Serializzabile;

abstract class GestorePunteggiBase extends GestoreSuFile implements InterfacciaGestorePunteggi {

	private static final int NUMERO_MASSIMO = 10;

	private final String[] nomi = new String[NUMERO_MASSIMO];
	private final int[] punteggi = new int[NUMERO_MASSIMO];
	// La partita di ogni punteggio, null per quelli predefiniti
	private final String[] idPartite = new String[NUMERO_MASSIMO];

	/**
	 * Se la classifica non si riesce a leggere (file assente, ma anche rovinato o troncato) si riparte da
	 * quella predefinita e la si salva subito, sovrascrivendo il file illeggibile: è voluto, perché da un
	 * file rovinato non c'è niente da recuperare e la classifica non è un dato prezioso come un salvataggio.
	 */
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
		return new PunteggioImpl(nomi[posizione], punteggi[posizione], idPartite[posizione]);
	}

	public boolean isPunteggioInClassifica(int punteggio, String idPartita) {
		int posizione = posizioneDella(idPartita);
		if (posizione >= 0) {
			return punteggio > punteggi[posizione];
		}
		return punteggio > punteggi[NUMERO_MASSIMO - 1];
	}

	public void addPunteggio(String nome, int punteggio, String idPartita) {
		if (!isPunteggioInClassifica(punteggio, idPartita)) {
			return;
		}
		// Il punteggio precedente della stessa partita se ne va: la classifica scorre in su da lì, e in fondo resta
		// un posto, che il nuovo punteggio occupa se non va più in alto
		int daTogliere = posizioneDella(idPartita);
		if (daTogliere < 0) {
			daTogliere = NUMERO_MASSIMO - 1;
		}
		for (int i = daTogliere; i < NUMERO_MASSIMO - 1; i++) {
			impostaPosizione(i, nomi[i + 1], punteggi[i + 1], idPartite[i + 1]);
		}
		int posizione = NUMERO_MASSIMO - 1;
		while (posizione > 0 && punteggio > punteggi[posizione - 1]) {
			posizione--;
		}
		for (int i = NUMERO_MASSIMO - 1; i > posizione; i--) {
			impostaPosizione(i, nomi[i - 1], punteggi[i - 1], idPartite[i - 1]);
		}
		impostaPosizione(posizione, pulisciNome(nome), punteggio, idPartita);
		salva();
	}

	private int posizioneDella(String idPartita) {
		if (idPartita == null) {
			return -1;
		}
		for (int i = 0; i < NUMERO_MASSIMO; i++) {
			if (idPartita.equals(idPartite[i])) {
				return i;
			}
		}
		return -1;
	}

	private void impostaPosizione(int posizione, String nome, int punteggio, String idPartita) {
		nomi[posizione] = nome;
		punteggi[posizione] = punteggio;
		idPartite[posizione] = idPartita;
	}

	protected void setPunteggio(int posizione, String nome, int punteggio, String idPartita) {
		impostaPosizione(posizione, pulisciNome(nome), punteggio, idPartita);
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
		private final String idPartita;
		
		PunteggioImpl(String nome, int punteggio, String idPartita) {
			this.nome = nome;
			this.punteggio = punteggio;
			this.idPartita = idPartita;
		}

		public String getIdPartita() {
			return idPartita;
		}
		
		public String getNome() {
			return nome;
		}
		
		public int getPunteggio() {
			return punteggio;
		}
	}
}
