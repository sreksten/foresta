package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.modellodati.Serializzabile;
import com.threeamigos.foresta.modellodati.LettoreCampi;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoParametro;

import java.util.Objects;

/**
 * Come un selettore sceglie uno fra i candidati rimasti: il tipo e, per quelli che lo vogliono, un tipo di danno. Immutabile.
 */
public final class OrdinamentoMD {

	private final TipoOrdinamento tipo;
	private final TipoDanno tipoDanno;

	private OrdinamentoMD(TipoOrdinamento tipo, TipoDanno tipoDanno) {
		this.tipo = tipo;
		this.tipoDanno = tipoDanno;
	}

	/**
	 * Un ordinamento che non vuole parametri.
	 *
	 * @throws IllegalArgumentException se il tipo di ordinamento ne vuole uno
	 */
	public static OrdinamentoMD di(TipoOrdinamento tipo) {
		if (tipo.getParametro() != TipoParametro.NESSUNO) {
			throw new IllegalArgumentException("L'ordinamento " + tipo + " vuole un parametro " + tipo.getParametro());
		}
		return new OrdinamentoMD(tipo, null);
	}

	/**
	 * Un ordinamento che vuole un tipo di danno.
	 *
	 * @throws IllegalArgumentException se il tipo di ordinamento non vuole un tipo di danno
	 */
	public static OrdinamentoMD di(TipoOrdinamento tipo, TipoDanno tipoDanno) {
		if (tipo.getParametro() != TipoParametro.TIPO_DANNO) {
			throw new IllegalArgumentException("L'ordinamento " + tipo + " non vuole un tipo di danno");
		}
		return new OrdinamentoMD(tipo, Objects.requireNonNull(tipoDanno));
	}

	public TipoOrdinamento getTipo() {
		return tipo;
	}

	public TipoDanno getTipoDanno() {
		return tipoDanno;
	}

	/**
	 * Una descrizione leggibile, per i messaggi di log e di prova.
	 */
	@Override
	public String toString() {
		return tipoDanno == null ? tipo.name() : tipo + " " + tipoDanno;
	}

	/**
	 * Due ordinamenti sono uguali se hanno gli stessi dati.
	 */
	@Override
	public boolean equals(Object altro) {
		return altro instanceof OrdinamentoMD && tipo == ((OrdinamentoMD) altro).tipo && tipoDanno == ((OrdinamentoMD) altro).tipoDanno;
	}

	/**
	 * Coerente con {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(tipo, tipoDanno);
	}
}
