package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.modellodati.LettoreCampi;
import com.threeamigos.foresta.tipi.tattiche.TipoConfronto;
import com.threeamigos.foresta.tipi.tattiche.TipoParametro;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.threeamigos.foresta.modellodati.Serializzabile.facoltativo;
import static com.threeamigos.foresta.modellodati.Serializzabile.stampaRiga;

/**
 * Una condizione di una voce: il tipo, il parametro (del tipo che il tipo di predicato dichiara) e se è negata
 * (<code>NON</code>). È immutabile. Le condizioni di una voce sono in AND.
 * Vedi tattiche_di_combattimento_automatiche.md §11.8.
 *
 * @author Stefano Reksten
 */
public final class PredicatoMD {

	/**
	 * Quanti candidati restano nella rosa dopo i filtri, confrontati con il numero
	 */
	public static PredicatoMD quanti(TipoRosa rosa, List<FiltroMD> filtri, TipoConfronto tipoConfronto, int numero) {
		return new PredicatoMD(TipoPredicato.QUANTI, false, Objects.requireNonNull(tipoConfronto), numero, Objects.requireNonNull(rosa), filtri);
	}

	/**
	 * Un predicato con un confronto e un numero (ROUND, ASTUZIA, VOCE_USATA, ROUND_DALL_ULTIMO_USO)
	 */
	public static PredicatoMD conConfrontoENumero(TipoPredicato tipo, TipoConfronto tipoConfronto, int numero) {
		verifica(tipo, TipoParametro.CONFRONTO_E_NUMERO);
		return new PredicatoMD(tipo, false, Objects.requireNonNull(tipoConfronto), numero, null, Collections.emptyList());
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

	private final TipoPredicato tipoPredicato;
	private final boolean negato;
	private final TipoConfronto tipoConfronto;
	private final int numero;
	private final TipoRosa tipoRosa;
	private final List<FiltroMD> filtri;

	private PredicatoMD(TipoPredicato tipoPredicato, boolean negato, TipoConfronto tipoConfronto, int numero, TipoRosa tipoRosa, List<FiltroMD> filtri) {
		this.tipoPredicato = tipoPredicato;
		this.negato = negato;
		this.tipoConfronto = tipoConfronto;
		this.numero = numero;
		this.tipoRosa = tipoRosa;
		this.filtri = Collections.unmodifiableList(new ArrayList<>(filtri));
	}

	public TipoPredicato getTipoPredicato() {
		return tipoPredicato;
	}

	public boolean isNegato() {
		return negato;
	}

	public TipoConfronto getTipoConfronto() {
		return tipoConfronto;
	}

	public int getNumero() {
		return numero;
	}

	/** Solo per QUANTI */
	public TipoRosa getTipoRosa() {
		return tipoRosa;
	}

	/** Solo per QUANTI */
	public List<FiltroMD> getFiltri() {
		return filtri;
	}

	/**
	 * Lo stesso predicato con il risultato rovesciato (<code>NON</code>)
	 */
	public PredicatoMD negato() {
		return new PredicatoMD(tipoPredicato, !negato, tipoConfronto, numero, tipoRosa, filtri);
	}

	/**
	 * Scrive il predicato e, per QUANTI, i suoi filtri (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stampaRiga(stream,
				"PREDICATO",
				tipoPredicato,
				negato,
				facoltativo(tipoConfronto),
				numero,
				facoltativo(tipoRosa),
				filtri.size());
		for (FiltroMD filtro : filtri) {
			filtro.salva(stream);
		}
	}

	/**
	 * Rilegge un predicato scritto da {@link #salva(PrintWriter)}, passando dalle fabbriche che controllano i parametri.
	 */
	public static PredicatoMD leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = FormatoTattiche.riga(stream, "PREDICATO");
		TipoPredicato tipo = campi.enumerato(TipoPredicato.class);
		boolean negato = campi.booleano();
		TipoConfronto tipoConfronto = campi.enumeratoFacoltativo(TipoConfronto.class);
		int numero = campi.intero();
		TipoRosa rosa = campi.enumeratoFacoltativo(TipoRosa.class);
		int numeroFiltri = campi.intero();
		List<FiltroMD> filtri = new ArrayList<>();
		for (int i = 0; i < numeroFiltri; i++) {
			filtri.add(FiltroMD.leggi(stream));
		}
		PredicatoMD predicato;
		TipoParametro parametro = tipo.getParametro();
		try {
			// L'ultimo caso è il numero (la probabilità): un parametro che i predicati non usano finisce lì, e verifica lo rifiuta
			if (parametro == TipoParametro.SELEZIONE_CONFRONTO_E_NUMERO) {
				predicato = quanti(rosa, filtri, tipoConfronto, numero);
			} else if (parametro == TipoParametro.CONFRONTO_E_NUMERO) {
				predicato = conConfrontoENumero(tipo, tipoConfronto, numero);
			} else {
				verifica(tipo, TipoParametro.NUMERO);
				predicato = probabilita(numero);
			}
		} catch (NullPointerException e) {
			throw new IOException("Predicato " + tipo + " senza il suo parametro", e);
		}
		return negato ? predicato.negato() : predicato;
	}

	/**
	 * Una descrizione leggibile, per i messaggi di log e di prova.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (negato) {
			sb.append("NON ");
		}
		sb.append(tipoPredicato);
		if (tipoRosa != null) {
			sb.append(' ').append(tipoRosa);
			if (!filtri.isEmpty()) {
				sb.append(' ').append(filtri);
			}
		}
		if (tipoConfronto != null) {
			sb.append(' ').append(tipoConfronto);
		}
		sb.append(' ').append(numero);
		return sb.toString();
	}

	/**
	 * Due predicati sono uguali se hanno gli stessi dati.
	 */
	@Override
	public boolean equals(Object altro) {
		if (!(altro instanceof PredicatoMD)) {
			return false;
		}
		PredicatoMD p = (PredicatoMD) altro;
		return tipoPredicato == p.tipoPredicato && negato == p.negato && tipoConfronto == p.tipoConfronto && numero == p.numero && tipoRosa == p.tipoRosa && filtri.equals(p.filtri);
	}

	/**
	 * Coerente con {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(tipoPredicato, negato, tipoConfronto, numero, tipoRosa, filtri);
	}

}
