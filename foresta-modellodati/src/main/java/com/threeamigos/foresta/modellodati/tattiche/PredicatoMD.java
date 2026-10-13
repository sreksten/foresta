package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.modellodati.Serializzabile;
import com.threeamigos.foresta.modellodati.LettoreCampi;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
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
public final class PredicatoMD {

	private final TipoPredicato tipo;
	private final boolean negato;
	private final Confronto confronto;
	private final int numero;
	private final TipoRosa rosa;
	private final List<FiltroMD> filtri;

	private PredicatoMD(TipoPredicato tipo, boolean negato, Confronto confronto, int numero, TipoRosa rosa, List<FiltroMD> filtri) {
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
	public static PredicatoMD quanti(TipoRosa rosa, List<FiltroMD> filtri, Confronto confronto, int numero) {
		return new PredicatoMD(TipoPredicato.QUANTI, false, Objects.requireNonNull(confronto), numero, Objects.requireNonNull(rosa), filtri);
	}

	/**
	 * Un predicato con un confronto e un numero (ROUND, ASTUZIA, VOCE_USATA, ROUND_DALL_ULTIMO_USO)
	 */
	public static PredicatoMD conConfrontoENumero(TipoPredicato tipo, Confronto confronto, int numero) {
		verifica(tipo, TipoParametro.CONFRONTO_E_NUMERO);
		return new PredicatoMD(tipo, false, Objects.requireNonNull(confronto), numero, null, Collections.emptyList());
	}

	/**
	 * Vero nel n% dei casi
	 */
	public static PredicatoMD probabilita(int percentuale) {
		return new PredicatoMD(TipoPredicato.PROBABILITA, false, null, percentuale, null, Collections.emptyList());
	}

	private static void verifica(TipoPredicato tipo, TipoParametro atteso) {
		if (tipo.getParametro() != atteso) {
			throw new IllegalArgumentException("Il predicato " + tipo + " vuole un parametro " + tipo.getParametro() + ", non " + atteso);
		}
	}

	/**
	 * Lo stesso predicato con il risultato rovesciato (<code>NON</code>)
	 */
	public PredicatoMD negato() {
		return new PredicatoMD(tipo, !negato, confronto, numero, rosa, filtri);
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
	public List<FiltroMD> getFiltri() {
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

	/**
	 * Scrive il predicato e, per QUANTI, i suoi filtri (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stream.println(String.join(Serializzabile.PIPE, "PREDICATO", tipo.name(), String.valueOf(negato), Serializzabile.facoltativo(confronto),
				String.valueOf(numero), Serializzabile.facoltativo(rosa), String.valueOf(filtri.size())));
		for (FiltroMD filtro : filtri) {
			filtro.salva(stream);
		}
	}

	public static PredicatoMD leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = FormatoTattiche.riga(stream, "PREDICATO");
		TipoPredicato tipo = campi.enumerato(TipoPredicato.class);
		boolean negato = campi.booleano();
		Confronto confronto = campi.enumeratoFacoltativo(Confronto.class);
		int numero = campi.intero();
		TipoRosa rosa = campi.enumeratoFacoltativo(TipoRosa.class);
		int numeroFiltri = campi.intero();
		List<FiltroMD> filtri = new ArrayList<>();
		for (int i = 0; i < numeroFiltri; i++) {
			filtri.add(FiltroMD.leggi(stream));
		}
		PredicatoMD predicato;
		try {
			switch (tipo.getParametro()) {
				case SELEZIONE_CONFRONTO_E_NUMERO:
					predicato = quanti(rosa, filtri, confronto, numero);
					break;
				case CONFRONTO_E_NUMERO:
					predicato = conConfrontoENumero(tipo, confronto, numero);
					break;
				case NUMERO:
					predicato = probabilita(numero);
					break;
				default:
					throw new IOException("Parametro di predicato sconosciuto: " + tipo.getParametro());
			}
		} catch (NullPointerException e) {
			throw new IOException("Predicato " + tipo + " senza il suo parametro", e);
		}
		return negato ? predicato.negato() : predicato;
	}

	@Override
	public boolean equals(Object altro) {
		if (!(altro instanceof PredicatoMD)) {
			return false;
		}
		PredicatoMD p = (PredicatoMD) altro;
		return tipo == p.tipo && negato == p.negato && confronto == p.confronto && numero == p.numero && rosa == p.rosa && filtri.equals(p.filtri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tipo, negato, confronto, numero, rosa, filtri);
	}
}
