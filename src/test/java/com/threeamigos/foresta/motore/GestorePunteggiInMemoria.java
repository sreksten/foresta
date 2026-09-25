package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.InterfacciaGestorePunteggi;
import com.threeamigos.foresta.tools.Punteggio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * La classifica dei test, in memoria.
 */
final class GestorePunteggiInMemoria implements InterfacciaGestorePunteggi {

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
	public boolean isPunteggioInClassifica(int punteggio) {
		return punteggi.size() < POSTI || punteggio > punteggi.get(punteggi.size() - 1).getPunteggio();
	}

	@Override
	public void addPunteggio(String nome, int punteggio) {
		punteggi.add(new Punteggio() {
			@Override
			public String getNome() {
				return nome;
			}

			@Override
			public int getPunteggio() {
				return punteggio;
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
