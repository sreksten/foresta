package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.modellodati.LettoreCampi;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoParametro;

import java.util.Objects;

import static com.threeamigos.foresta.modellodati.Serializzabile.facoltativo;
import static com.threeamigos.foresta.modellodati.Serializzabile.stampaRiga;

/**
 * Un filtro di un selettore: il tipo, il parametro (del tipo che il tipo di filtro dichiara) e
 * se è negato (<code>NON</code>). È immutabile. Si costruisce con i metodi statici, che controllano che
 * il parametro sia quello giusto. Vedi tattiche_di_combattimento_automatiche.md §11.5.
 *
 * @author Stefano Reksten
 */
public final class FiltroMD {

	private final TipoFiltro tipo;
	private final boolean negato;
	private final Confronto confronto;
	private final int numero;
	private final TipoEffettoDiStato stato;
	private final TipoPersonaggio classe;
	private final CategoriaPersonaggio categoria;
	private final TipoDanno tipoDanno;

	private FiltroMD(TipoFiltro tipo, boolean negato, Confronto confronto, int numero, TipoEffettoDiStato stato, TipoPersonaggio classe,
				   CategoriaPersonaggio categoria, TipoDanno tipoDanno) {
		this.tipo = tipo;
		this.negato = negato;
		this.confronto = confronto;
		this.numero = numero;
		this.stato = stato;
		this.classe = classe;
		this.categoria = categoria;
		this.tipoDanno = tipoDanno;
	}

	private static FiltroMD crea(TipoFiltro tipo, TipoParametro atteso, Confronto confronto, int numero, TipoEffettoDiStato stato,
							   TipoPersonaggio classe, CategoriaPersonaggio categoria, TipoDanno tipoDanno) {
		if (tipo.getParametro() != atteso) {
			throw new IllegalArgumentException("Il filtro " + tipo + " vuole un parametro " + tipo.getParametro() + ", non " + atteso);
		}
		return new FiltroMD(tipo, false, confronto, numero, stato, classe, categoria, tipoDanno);
	}

	/**
	 * Un filtro che non vuole parametri.
	 *
	 * @throws IllegalArgumentException se il tipo di filtro ne vuole uno
	 */
	public static FiltroMD senzaParametro(TipoFiltro tipo) {
		return crea(tipo, TipoParametro.NESSUNO, null, 0, null, null, null, null);
	}

	/**
	 * Un filtro il cui parametro è un confronto con un numero.
	 *
	 * @throws IllegalArgumentException se il tipo di filtro vuole un altro parametro
	 */
	public static FiltroMD conConfrontoENumero(TipoFiltro tipo, Confronto confronto, int numero) {
		return crea(tipo, TipoParametro.CONFRONTO_E_NUMERO, Objects.requireNonNull(confronto), numero, null, null, null, null);
	}

	/**
	 * Un filtro il cui parametro è un confronto, senza numero.
	 *
	 * @throws IllegalArgumentException se il tipo di filtro vuole un altro parametro
	 */
	public static FiltroMD conConfronto(TipoFiltro tipo, Confronto confronto) {
		return crea(tipo, TipoParametro.CONFRONTO, Objects.requireNonNull(confronto), 0, null, null, null, null);
	}

	/**
	 * Un filtro il cui parametro è uno stato alterato.
	 *
	 * @throws IllegalArgumentException se il tipo di filtro vuole un altro parametro
	 */
	public static FiltroMD conStato(TipoFiltro tipo, TipoEffettoDiStato stato) {
		return crea(tipo, TipoParametro.STATO, null, 0, Objects.requireNonNull(stato), null, null, null);
	}

	/**
	 * Un filtro il cui parametro è una classe di personaggio.
	 *
	 * @throws IllegalArgumentException se il tipo di filtro vuole un altro parametro
	 */
	public static FiltroMD conClasse(TipoFiltro tipo, TipoPersonaggio classe) {
		return crea(tipo, TipoParametro.CLASSE, null, 0, null, Objects.requireNonNull(classe), null, null);
	}

	/**
	 * Un filtro il cui parametro è una categoria di personaggi.
	 *
	 * @throws IllegalArgumentException se il tipo di filtro vuole un altro parametro
	 */
	public static FiltroMD conCategoria(TipoFiltro tipo, CategoriaPersonaggio categoria) {
		return crea(tipo, TipoParametro.CATEGORIA, null, 0, null, null, Objects.requireNonNull(categoria), null);
	}

	/**
	 * Un filtro il cui parametro è un tipo di danno.
	 *
	 * @throws IllegalArgumentException se il tipo di filtro vuole un altro parametro
	 */
	public static FiltroMD conTipoDanno(TipoFiltro tipo, TipoDanno tipoDanno) {
		return crea(tipo, TipoParametro.TIPO_DANNO, null, 0, null, null, null, Objects.requireNonNull(tipoDanno));
	}

	/**
	 * Lo stesso filtro con il risultato rovesciato (<code>NON</code>)
	 */
	public FiltroMD negato() {
		return new FiltroMD(tipo, !negato, confronto, numero, stato, classe, categoria, tipoDanno);
	}

	public TipoFiltro getTipo() {
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

	public TipoEffettoDiStato getStato() {
		return stato;
	}

	public TipoPersonaggio getClasse() {
		return classe;
	}

	public CategoriaPersonaggio getCategoria() {
		return categoria;
	}

	public TipoDanno getTipoDanno() {
		return tipoDanno;
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
		sb.append(tipo);
		if (confronto != null) {
			sb.append(' ').append(confronto);
		}
		if (tipo.getParametro() == TipoParametro.CONFRONTO_E_NUMERO) {
			sb.append(' ').append(numero);
		}
		Object parametro = stato != null ? stato : classe != null ? classe : categoria != null ? categoria : tipoDanno;
		if (parametro != null) {
			sb.append(' ').append(parametro);
		}
		return sb.toString();
	}

	/**
	 * Scrive il filtro su una riga (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stampaRiga(stream,
				"FILTRO",
				tipo,
				negato,
				facoltativo(confronto),
				numero,
				facoltativo(stato),
				facoltativo(classe),
				facoltativo(categoria),
				facoltativo(tipoDanno));
	}

	/**
	 * Rilegge un filtro scritto da {@link #salva}; i parametri passano dalle fabbriche, che li controllano
	 */
	public static FiltroMD leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = FormatoTattiche.riga(stream, "FILTRO");
		TipoFiltro tipo = campi.enumerato(TipoFiltro.class);
		boolean negato = campi.booleano();
		Confronto confronto = campi.enumeratoFacoltativo(Confronto.class);
		int numero = campi.intero();
		TipoEffettoDiStato stato = campi.enumeratoFacoltativo(TipoEffettoDiStato.class);
		TipoPersonaggio classe = campi.enumeratoFacoltativo(TipoPersonaggio.class);
		CategoriaPersonaggio categoria = campi.enumeratoFacoltativo(CategoriaPersonaggio.class);
		TipoDanno tipoDanno = campi.enumeratoFacoltativo(TipoDanno.class);
		FiltroMD filtro;
		TipoParametro parametro = tipo.getParametro();
		try {
			// L'ultimo caso è il tipo di danno: un parametro che i filtri non usano finisce lì, e la fabbrica lo rifiuta
			if (parametro == TipoParametro.NESSUNO) {
				filtro = senzaParametro(tipo);
			} else if (parametro == TipoParametro.CONFRONTO_E_NUMERO) {
				filtro = conConfrontoENumero(tipo, confronto, numero);
			} else if (parametro == TipoParametro.CONFRONTO) {
				filtro = conConfronto(tipo, confronto);
			} else if (parametro == TipoParametro.STATO) {
				filtro = conStato(tipo, stato);
			} else if (parametro == TipoParametro.CLASSE) {
				filtro = conClasse(tipo, classe);
			} else if (parametro == TipoParametro.CATEGORIA) {
				filtro = conCategoria(tipo, categoria);
			} else {
				filtro = conTipoDanno(tipo, tipoDanno);
			}
		} catch (NullPointerException e) {
			throw new IOException("Filtro " + tipo + " senza il suo parametro", e);
		}
		return negato ? filtro.negato() : filtro;
	}

	/**
	 * Due filtri sono uguali se hanno gli stessi dati.
	 */
	@Override
	public boolean equals(Object altro) {
		if (this == altro) {
			return true;
		}
		if (!(altro instanceof FiltroMD)) {
			return false;
		}
		FiltroMD f = (FiltroMD) altro;
		return tipo == f.tipo && negato == f.negato && confronto == f.confronto && numero == f.numero && stato == f.stato && classe == f.classe
				&& categoria == f.categoria && tipoDanno == f.tipoDanno;
	}

	/**
	 * Coerente con {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(tipo, negato, confronto, numero, stato, classe, categoria, tipoDanno);
	}
}
