package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.TipoParametro;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Una condizione di una voce: il tipo, il parametro (del tipo che il tipo di predicato dichiara) e se è negata (<code>NON</code>). Immutabile.
 * Le condizioni di una voce sono in AND. Vedi tattiche_di_combattimento_automatiche.md §11.8.
 */
public final class Predicato {

	private final TipoPredicato tipo;
	private final boolean negato;
	private final Confronto confronto;
	private final int numero;
	private final TipoRosa rosa;
	private final List<Filtro> filtri;

	private Predicato(TipoPredicato tipo, boolean negato, Confronto confronto, int numero, TipoRosa rosa, List<Filtro> filtri) {
		this.tipo = tipo;
		this.negato = negato;
		this.confronto = confronto;
		this.numero = numero;
		this.rosa = rosa;
		this.filtri = Collections.unmodifiableList(new ArrayList<>(filtri));
	}

	/**
	 * Quanti candidati restano nella rosa dopo i filtri, confrontati con il numero
	 */
	public static Predicato quanti(TipoRosa rosa, List<Filtro> filtri, Confronto confronto, int numero) {
		return new Predicato(TipoPredicato.QUANTI, false, Objects.requireNonNull(confronto), numero, Objects.requireNonNull(rosa), filtri);
	}

	/**
	 * Un predicato con un confronto e un numero (ROUND, ASTUZIA, VOCE_USATA, ROUND_DALL_ULTIMO_USO)
	 */
	public static Predicato conConfrontoENumero(TipoPredicato tipo, Confronto confronto, int numero) {
		verifica(tipo, TipoParametro.CONFRONTO_E_NUMERO);
		return new Predicato(tipo, false, Objects.requireNonNull(confronto), numero, null, Collections.emptyList());
	}

	/**
	 * Vero nel n% dei casi
	 */
	public static Predicato probabilita(int percentuale) {
		return new Predicato(TipoPredicato.PROBABILITA, false, null, percentuale, null, Collections.emptyList());
	}

	private static void verifica(TipoPredicato tipo, TipoParametro atteso) {
		if (tipo.getParametro() != atteso) {
			throw new IllegalArgumentException("Il predicato " + tipo + " vuole un parametro " + tipo.getParametro() + ", non " + atteso);
		}
	}

	/**
	 * Lo stesso predicato con il risultato rovesciato (<code>NON</code>)
	 */
	public Predicato negato() {
		return new Predicato(tipo, !negato, confronto, numero, rosa, filtri);
	}

	public TipoPredicato getTipo() {
		return tipo;
	}

	public boolean isNegato() {
		return negato;
	}

	public Confronto getConfronto() {
		return confronto;
	}

	public int getNumero() {
		return numero;
	}

	/** Solo per QUANTI */
	public TipoRosa getRosa() {
		return rosa;
	}

	/** Solo per QUANTI */
	public List<Filtro> getFiltri() {
		return filtri;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (negato) {
			sb.append("NON ");
		}
		sb.append(tipo);
		if (rosa != null) {
			sb.append(' ').append(rosa);
			if (!filtri.isEmpty()) {
				sb.append(' ').append(filtri);
			}
		}
		if (confronto != null) {
			sb.append(' ').append(confronto);
		}
		sb.append(' ').append(numero);
		return sb.toString();
	}
}
