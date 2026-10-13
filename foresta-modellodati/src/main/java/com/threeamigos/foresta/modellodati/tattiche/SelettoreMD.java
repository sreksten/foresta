package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import java.util.Objects;
import com.threeamigos.foresta.modellodati.Serializzabile;
import com.threeamigos.foresta.modellodati.LettoreCampi;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
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

	/**
	 * Scrive il selettore e i suoi filtri (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stream.println(String.join(Serializzabile.PIPE, "SELETTORE", rosa.name(), ordinamento.getTipo().name(),
				Serializzabile.facoltativo(ordinamento.getTipoDanno()), String.valueOf(filtri.size())));
		for (FiltroMD filtro : filtri) {
			filtro.salva(stream);
		}
	}

	public static SelettoreMD leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = FormatoTattiche.riga(stream, "SELETTORE");
		TipoRosa rosa = campi.enumerato(TipoRosa.class);
		TipoOrdinamento tipoOrdinamento = campi.enumerato(TipoOrdinamento.class);
		TipoDanno tipoDanno = campi.enumeratoFacoltativo(TipoDanno.class);
		int numeroFiltri = campi.intero();
		List<FiltroMD> filtri = new ArrayList<>();
		for (int i = 0; i < numeroFiltri; i++) {
			filtri.add(FiltroMD.leggi(stream));
		}
		try {
			return new SelettoreMD(rosa, filtri, tipoDanno == null ? OrdinamentoMD.di(tipoOrdinamento) : OrdinamentoMD.di(tipoOrdinamento, tipoDanno));
		} catch (IllegalArgumentException e) {
			throw new IOException("Ordinamento " + tipoOrdinamento + " incoerente nel salvataggio", e);
		}
	}

	@Override
	public boolean equals(Object altro) {
		if (!(altro instanceof SelettoreMD)) {
			return false;
		}
		SelettoreMD s = (SelettoreMD) altro;
		return rosa == s.rosa && filtri.equals(s.filtri) && ordinamento.equals(s.ordinamento);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rosa, filtri, ordinamento);
	}
}
