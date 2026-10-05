package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.tools.Punteggio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * La classifica dei test, in memoria. Pubblica perché la usano anche i test della UI.
 */
public final class GestorePunteggiInMemoria implements GestorePunteggi {

	private static final int POSTI = 10;

	private final List<Punteggio> punteggi = new ArrayList<>();

	@Override
	public boolean carica() {
		return true;
	}

	@Override
	public int getConteggio() {
		return punteggi.size();
	}

	@Override
	public Punteggio getPunteggio(int posizione) {
		return punteggi.get(posizione);
	}

	@Override
	public boolean isPunteggioInClassifica(int punteggio, String idPartita) {
		for (Punteggio esistente : punteggi) {
			if (idPartita != null && idPartita.equals(esistente.getIdPartita())) {
				return punteggio > esistente.getPunteggio();
			}
		}
		return punteggi.size() < POSTI || punteggio > punteggi.get(punteggi.size() - 1).getPunteggio();
	}

	@Override
	public void addPunteggio(String nome, int punteggio, String idPartita) {
		punteggi.removeIf(esistente -> idPartita != null && idPartita.equals(esistente.getIdPartita()));
		punteggi.add(new Punteggio() {
			@Override
			public String getNome() {
				return nome;
			}

			@Override
			public int getPunteggio() {
				return punteggio;
			}

			@Override
			public String getIdPartita() {
				return idPartita;
			}
		});
		punteggi.sort(Comparator.comparingInt(Punteggio::getPunteggio).reversed());
		if (punteggi.size() > POSTI) {
			punteggi.remove(POSTI);
		}
	}

	@Override
	public boolean salva() {
		return true;
	}
}
