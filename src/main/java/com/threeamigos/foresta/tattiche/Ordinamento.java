package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoParametro;

import java.util.Objects;

/**
 * Come un selettore sceglie uno fra i candidati rimasti: il tipo e, per quelli che lo vogliono, un tipo di danno. Immutabile.
 */
public final class Ordinamento {

	private final TipoOrdinamento tipo;
	private final TipoDanno tipoDanno;

	private Ordinamento(TipoOrdinamento tipo, TipoDanno tipoDanno) {
		this.tipo = tipo;
		this.tipoDanno = tipoDanno;
	}

	public static Ordinamento di(TipoOrdinamento tipo) {
		if (tipo.getParametro() != TipoParametro.NESSUNO) {
			throw new IllegalArgumentException("L'ordinamento " + tipo + " vuole un parametro " + tipo.getParametro());
		}
		return new Ordinamento(tipo, null);
	}

	public static Ordinamento di(TipoOrdinamento tipo, TipoDanno tipoDanno) {
		if (tipo.getParametro() != TipoParametro.TIPO_DANNO) {
			throw new IllegalArgumentException("L'ordinamento " + tipo + " non vuole un tipo di danno");
		}
		return new Ordinamento(tipo, Objects.requireNonNull(tipoDanno));
	}

	public TipoOrdinamento getTipo() {
		return tipo;
	}

	public TipoDanno getTipoDanno() {
		return tipoDanno;
	}

	@Override
	public String toString() {
		return tipoDanno == null ? tipo.name() : tipo + " " + tipoDanno;
	}
}
