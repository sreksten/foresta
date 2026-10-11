package com.threeamigos.foresta.modellodati.tattiche;

import com.threeamigos.foresta.modellodati.Serializzabile;
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

/**
 * Un filtro di un selettore: il tipo, il parametro (del tipo che il tipo di filtro dichiara) e se è negato (<code>NON</code>). Immutabile.
 * Si costruisce con i metodi statici, che controllano che il parametro sia quello giusto. Vedi tattiche_di_combattimento_automatiche.md §11.5.
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

	public static FiltroMD senzaParametro(TipoFiltro tipo) {
		return crea(tipo, TipoParametro.NESSUNO, null, 0, null, null, null, null);
	}

	public static FiltroMD conConfrontoENumero(TipoFiltro tipo, Confronto confronto, int numero) {
		return crea(tipo, TipoParametro.CONFRONTO_E_NUMERO, Objects.requireNonNull(confronto), numero, null, null, null, null);
	}

	public static FiltroMD conConfronto(TipoFiltro tipo, Confronto confronto) {
		return crea(tipo, TipoParametro.CONFRONTO, Objects.requireNonNull(confronto), 0, null, null, null, null);
	}

	public static FiltroMD conStato(TipoFiltro tipo, TipoEffettoDiStato stato) {
		return crea(tipo, TipoParametro.STATO, null, 0, Objects.requireNonNull(stato), null, null, null);
	}

	public static FiltroMD conClasse(TipoFiltro tipo, TipoPersonaggio classe) {
		return crea(tipo, TipoParametro.CLASSE, null, 0, null, Objects.requireNonNull(classe), null, null);
	}

	public static FiltroMD conCategoria(TipoFiltro tipo, CategoriaPersonaggio categoria) {
		return crea(tipo, TipoParametro.CATEGORIA, null, 0, null, null, Objects.requireNonNull(categoria), null);
	}

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
		stream.println(String.join(Serializzabile.PIPE, "FILTRO", tipo.name(), String.valueOf(negato), Serializzabile.facoltativo(confronto),
				String.valueOf(numero), Serializzabile.facoltativo(stato), Serializzabile.facoltativo(classe), Serializzabile.facoltativo(categoria),
				Serializzabile.facoltativo(tipoDanno)));
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
		try {
			switch (tipo.getParametro()) {
				case NESSUNO:
					filtro = senzaParametro(tipo);
					break;
				case CONFRONTO_E_NUMERO:
					filtro = conConfrontoENumero(tipo, confronto, numero);
					break;
				case CONFRONTO:
					filtro = conConfronto(tipo, confronto);
					break;
				case STATO:
					filtro = conStato(tipo, stato);
					break;
				case CLASSE:
					filtro = conClasse(tipo, classe);
					break;
				case CATEGORIA:
					filtro = conCategoria(tipo, categoria);
					break;
				case TIPO_DANNO:
					filtro = conTipoDanno(tipo, tipoDanno);
					break;
				default:
					throw new IOException("Parametro di filtro sconosciuto: " + tipo.getParametro());
			}
		} catch (NullPointerException e) {
			throw new IOException("Filtro " + tipo + " senza il suo parametro", e);
		}
		return negato ? filtro.negato() : filtro;
	}

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

	@Override
	public int hashCode() {
		return Objects.hash(tipo, negato, confronto, numero, stato, classe, categoria, tipoDanno);
	}
}
