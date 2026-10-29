package com.threeamigos.foresta.punteggi;

import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.strumenti.Punteggio;

/**
 * La classifica dei migliori dieci punteggi, indipendente da dove si conserva: tiene i nomi, i punteggi e la partita di
 * ciascuno, e sa dove inserire un nuovo punteggio. Dove si legge e si scrive lo decide chi la usa (vedi
 * {@link com.threeamigos.foresta.interfacce.GestorePunteggi}).
 */
public final class Classifica {

	public static final int NUMERO_MASSIMO = 10;

	private final String[] nomi = new String[NUMERO_MASSIMO];
	private final int[] punteggi = new int[NUMERO_MASSIMO];
	// La partita di ogni punteggio, null per quelli predefiniti
	private final String[] idPartite = new String[NUMERO_MASSIMO];

	/**
	 * Rimette la classifica predefinita, quella con cui si parte quando non ce n'è una da leggere.
	 */
	public void ripristinaLaPredefinita() {
		impostaPosizione(0, "Stefano", 10000, null);
		impostaPosizione(1, "Johan", 9000, null);
		impostaPosizione(2, "Alessandra", 8000, null);
		impostaPosizione(3, "Peter Porker", 7000, null);
		impostaPosizione(4, "Judah", 6000, null);
		impostaPosizione(5, "Jona", 5000, null);
		impostaPosizione(6, "Jamaikan", 2000, null);
		impostaPosizione(7, "Cosimo", 1000, null);
		impostaPosizione(8, "Oreste", 500, null);
		impostaPosizione(9, "Axel e il sERCIO", 0, null);
	}

	public int getConteggio() {
		return NUMERO_MASSIMO;
	}

	public Punteggio getPunteggio(int posizione) {
		return new PunteggioImpl(nomi[posizione], punteggi[posizione], idPartite[posizione]);
	}

	/**
	 * Se un punteggio può entrare in classifica. Se la partita c'è già, solo migliorando il suo punteggio: chi ricarica un
	 * salvataggio e rifinisce la partita non deve riempire la classifica di sé.
	 */
	public boolean isPunteggioInClassifica(int punteggio, String idPartita) {
		int posizione = posizioneDella(idPartita);
		if (posizione >= 0) {
			return punteggio > punteggi[posizione];
		}
		return punteggio > punteggi[NUMERO_MASSIMO - 1];
	}

	/**
	 * Inserisce il punteggio al suo posto in classifica, se ce n'è uno: un punteggio nuovo della stessa partita prende il posto
	 * di quello vecchio, gli altri scorrono e il più basso esce.
	 *
	 * @return true se il punteggio è entrato
	 */
	public boolean inserisci(String nome, int punteggio, String idPartita) {
		if (!isPunteggioInClassifica(punteggio, idPartita)) {
			return false;
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
		return true;
	}

	/**
	 * Mette un punteggio in una posizione, per chi lo legge da dove è conservato.
	 */
	public void setPunteggio(int posizione, String nome, int punteggio, String idPartita) {
		impostaPosizione(posizione, pulisciNome(nome), punteggio, idPartita);
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

	/**
	 * Toglie in silenzio il separatore "|" e gli a capo, come per i salvataggi; un nome vuoto diventa "nessun nome".
	 */
	public static String pulisciNome(String nome) {
		String pulito = nome == null ? "" : Serializzabile.pulisciCampo(nome);
		return pulito.trim().isEmpty() ? Serializzabile.NESSUN_NOME : pulito;
	}

	private static final class PunteggioImpl implements Punteggio {

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
