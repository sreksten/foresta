package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.incantesimi.TipoIncantesimo;

/**
 * Le pergamene di incantesimi che il PG porta in uno scontro del simulatore (vedi piano_montecarlo_matrix.md, §13).
 * Nel gioco chiunque può lanciare un incantesimo consumando la sua pergamena, se ha abbastanza MAGIA: il danno
 * lo fanno poi l'INTELLIGENZA e il moltiplicatore di danno magico della classe.
 */
public final class ScortaDiPergamene {

	public static final ScortaDiPergamene NESSUNA = new ScortaDiPergamene(null, 0);

	private final ClasseIncantesimo incantesimo;
	private final int quantita;

	private ScortaDiPergamene(ClasseIncantesimo incantesimo, int quantita) {
		this.incantesimo = incantesimo;
		this.quantita = quantita;
	}

	/**
	 * @param incantesimo un incantesimo malefico, cioè che fa danno
	 */
	public static ScortaDiPergamene di(ClasseIncantesimo incantesimo, int quantita) {
		if (incantesimo.getTipo() != TipoIncantesimo.MALEFICO) {
			throw new IllegalArgumentException("Nel simulatore si lanciano solo incantesimi malefici, non " + incantesimo);
		}
		return new ScortaDiPergamene(incantesimo, quantita);
	}

	public ClasseIncantesimo getIncantesimo() {
		return incantesimo;
	}

	public int getQuantita() {
		return quantita;
	}

	@Override
	public String toString() {
		return quantita == 0 ? "NESSUNA" : quantita + "x" + incantesimo;
	}
}
