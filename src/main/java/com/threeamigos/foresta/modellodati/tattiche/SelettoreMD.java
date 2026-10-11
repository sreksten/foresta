package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chi è il bersaglio di una voce: una rosa di candidati, i filtri che li restringono (in AND, l'ordine non conta) e l'ordinamento che sceglie
 * uno fra i rimasti. Se non ne resta nessuno la voce non è eseguibile; per le azioni su un gruppo intero serve solo come test di esistenza.
 * Immutabile. Sono solo dati: chi li applica è <code>tattiche.Selettori</code>. Vedi tattiche_di_combattimento_automatiche.md §11.2.
 */
public final class SelettoreMD {

	private final TipoRosa rosa;
	private final List<FiltroMD> filtri;
	private final OrdinamentoMD ordinamento;

	public SelettoreMD(TipoRosa rosa, List<FiltroMD> filtri, OrdinamentoMD ordinamento) {
		this.rosa = rosa;
		this.filtri = Collections.unmodifiableList(new ArrayList<>(filtri));
		this.ordinamento = ordinamento;
	}

	public TipoRosa getRosa() {
		return rosa;
	}

	public List<FiltroMD> getFiltri() {
		return filtri;
	}

	public OrdinamentoMD getOrdinamento() {
		return ordinamento;
	}

	@Override
	public String toString() {
		return "SU " + rosa + (filtri.isEmpty() ? "" : " " + filtri) + " ordine " + ordinamento;
	}
}
