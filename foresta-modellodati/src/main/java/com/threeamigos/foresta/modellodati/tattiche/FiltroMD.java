package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoConfronto;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoParametro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

import static com.threeamigos.foresta.serializzazione.Serializzabile.facoltativo;
import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Un filtro di un selettore: il tipo, il parametro (del tipo che il tipo di filtro dichiara) e
 * se è negato (<code>NON</code>). È immutabile. Si costruisce con i metodi statici, che controllano che
 * il parametro sia quello giusto. Vedi tattiche_di_combattimento_automatiche.md §11.5.
 *
 * @author Stefano Reksten
 */
public final class FiltroMD {

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
	public static FiltroMD conConfrontoENumero(TipoFiltro tipo, TipoConfronto tipoConfronto, int numero) {
		return crea(tipo, TipoParametro.CONFRONTO_E_NUMERO, Objects.requireNonNull(tipoConfronto), numero, null, null, null, null);
	}

	/**
	 * Un filtro il cui parametro è un confronto, senza numero.
	 *
	 * @throws IllegalArgumentException se il tipo di filtro vuole un altro parametro
	 */
	public static FiltroMD conConfronto(TipoFiltro tipo, TipoConfronto tipoConfronto) {
		return crea(tipo, TipoParametro.CONFRONTO, Objects.requireNonNull(tipoConfronto), 0, null, null, null, null);
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

	private static FiltroMD crea(TipoFiltro tipo, TipoParametro atteso, TipoConfronto tipoConfronto, int numero, TipoEffettoDiStato stato,
								 TipoPersonaggio classe, CategoriaPersonaggio categoria, TipoDanno tipoDanno) {
		if (tipo.getParametro() != atteso) {
			throw new IllegalArgumentException("Il filtro " + tipo + " vuole un parametro " + tipo.getParametro() + ", non " + atteso);
		}
		return new FiltroMD(tipo, false, tipoConfronto, numero, stato, classe, categoria, tipoDanno);
	}

	private final TipoFiltro tipoFiltro;
	private final boolean negato;
	private final TipoConfronto tipoConfronto;
	private final int numero;
	private final TipoEffettoDiStato tipoEffettoDiStato;
	private final TipoPersonaggio tipoPersonaggio;
	private final CategoriaPersonaggio categoriaPersonaggio;
	private final TipoDanno tipoDanno;

	private FiltroMD(TipoFiltro tipoFiltro, boolean negato, TipoConfronto tipoConfronto, int numero, TipoEffettoDiStato tipoEffettoDiStato, TipoPersonaggio tipoPersonaggio,
					 CategoriaPersonaggio categoriaPersonaggio, TipoDanno tipoDanno) {
		this.tipoFiltro = tipoFiltro;
		this.negato = negato;
		this.tipoConfronto = tipoConfronto;
		this.numero = numero;
		this.tipoEffettoDiStato = tipoEffettoDiStato;
		this.tipoPersonaggio = tipoPersonaggio;
		this.categoriaPersonaggio = categoriaPersonaggio;
		this.tipoDanno = tipoDanno;
	}

	public TipoFiltro getTipoFiltro() {
		return tipoFiltro;
	}

	public boolean isNegato() {
		return negato;
	}

	public TipoConfronto getConfronto() {
		return tipoConfronto;
	}

	public int getNumero() {
		return numero;
	}

	public TipoEffettoDiStato getTipoEffettoDiStato() {
		return tipoEffettoDiStato;
	}

	public TipoPersonaggio getTipoPersonaggio() {
		return tipoPersonaggio;
	}

	public CategoriaPersonaggio getCategoriaPersonaggio() {
		return categoriaPersonaggio;
	}

	public TipoDanno getTipoDanno() {
		return tipoDanno;
	}

	/**
	 * Lo stesso filtro con il risultato rovesciato (<code>NON</code>)
	 */
	public FiltroMD negato() {
		return new FiltroMD(tipoFiltro, !negato, tipoConfronto, numero, tipoEffettoDiStato, tipoPersonaggio, categoriaPersonaggio, tipoDanno);
	}

	/**
	 * Scrive il filtro su una riga (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stampaRiga(stream,
				"FILTRO",
				tipoFiltro,
				negato,
				facoltativo(tipoConfronto),
				numero,
				facoltativo(tipoEffettoDiStato),
				facoltativo(tipoPersonaggio),
				facoltativo(categoriaPersonaggio),
				facoltativo(tipoDanno));
	}

	/**
	 * Rilegge un filtro scritto da {@link #salva}; i parametri passano dalle fabbriche, che li controllano
	 */
	public static FiltroMD leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = FormatoTattiche.riga(stream, "FILTRO");
		TipoFiltro tipo = campi.enumerato(TipoFiltro.class);
		boolean negato = campi.booleano();
		TipoConfronto tipoConfronto = campi.enumeratoFacoltativo(TipoConfronto.class);
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
				filtro = conConfrontoENumero(tipo, tipoConfronto, numero);
			} else if (parametro == TipoParametro.CONFRONTO) {
				filtro = conConfronto(tipo, tipoConfronto);
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
	 * Una descrizione leggibile, per i messaggi di log e di prova.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (negato) {
			sb.append("NON ");
		}
		sb.append(tipoFiltro);
		if (tipoConfronto != null) {
			sb.append(' ').append(tipoConfronto);
		}
		if (tipoFiltro.getParametro() == TipoParametro.CONFRONTO_E_NUMERO) {
			sb.append(' ').append(numero);
		}
		Object parametro = tipoEffettoDiStato != null ? tipoEffettoDiStato : tipoPersonaggio != null ? tipoPersonaggio : categoriaPersonaggio != null ? categoriaPersonaggio : tipoDanno;
		if (parametro != null) {
			sb.append(' ').append(parametro);
		}
		return sb.toString();
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
		return tipoFiltro == f.tipoFiltro && negato == f.negato && tipoConfronto == f.tipoConfronto && numero == f.numero && tipoEffettoDiStato == f.tipoEffettoDiStato && tipoPersonaggio == f.tipoPersonaggio
				&& categoriaPersonaggio == f.categoriaPersonaggio && tipoDanno == f.tipoDanno;
	}

	/**
	 * Coerente con {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(tipoFiltro, negato, tipoConfronto, numero, tipoEffettoDiStato, tipoPersonaggio, categoriaPersonaggio, tipoDanno);
	}

}
