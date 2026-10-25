package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.threeamigos.foresta.serializzazione.Serializzabile.facoltativo;
import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Chi è il bersaglio di una voce: una rosa di candidati, i filtri che li restringono (in AND, l'ordine non conta) e
 * l'ordinamento che sceglie uno fra i rimasti. Se non ne resta nessuno la voce non è eseguibile.
 * Per le azioni su un gruppo intero serve solo come test di esistenza. È immutabile.
 * Sono solo dati: chi li applica è <code>tattiche.Selettori</code>.
 * Vedi tattiche_di_combattimento_automatiche.md §11.2.
 *
 * @author Stefano Reksten
 */
public final class SelettoreMD {

	private final TipoRosa tipoRosa;
	private final List<FiltroMD> filtri;
	private final OrdinamentoMD ordinamento;

	public SelettoreMD(TipoRosa tipoRosa, List<FiltroMD> filtri, OrdinamentoMD ordinamento) {
		this.tipoRosa = tipoRosa;
		this.filtri = Collections.unmodifiableList(new ArrayList<>(filtri));
		this.ordinamento = ordinamento;
	}

	public TipoRosa getTipoRosa() {
		return tipoRosa;
	}

	public List<FiltroMD> getFiltri() {
		return filtri;
	}

	public OrdinamentoMD getOrdinamento() {
		return ordinamento;
	}

	/**
	 * Scrive il selettore e i suoi filtri (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stampaRiga(stream,
				"SELETTORE",
				tipoRosa,
				ordinamento.getTipoOrdinamento(),
				facoltativo(ordinamento.getTipoDanno()),
				filtri.size());
		for (FiltroMD filtro : filtri) {
			filtro.salva(stream);
		}
	}

	/**
	 * Rilegge un selettore scritto da {@link #salva(PrintWriter)}.
	 */
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

	/**
	 * Una descrizione leggibile, per i messaggi di log e di prova.
	 */
	@Override
	public String toString() {
		return "SU " + tipoRosa + (filtri.isEmpty() ? "" : " " + filtri) + " ordine " + ordinamento;
	}

	/**
	 * Due selettori sono uguali se hanno gli stessi dati.
	 */
	@Override
	public boolean equals(Object altro) {
		if (!(altro instanceof SelettoreMD)) {
			return false;
		}
		SelettoreMD s = (SelettoreMD) altro;
		return tipoRosa == s.tipoRosa && filtri.equals(s.filtri) && ordinamento.equals(s.ordinamento);
	}

	/**
	 * Coerente con {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(tipoRosa, filtri, ordinamento);
	}

}
