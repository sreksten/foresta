package com.threeamigos.foresta.tattiche;

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
public final class Filtro {

	private final TipoFiltro tipo;
	private final boolean negato;
	private final Confronto confronto;
	private final int numero;
	private final TipoEffettoDiStato stato;
	private final TipoPersonaggio classe;
	private final CategoriaPersonaggio categoria;
	private final TipoDanno tipoDanno;

	private Filtro(TipoFiltro tipo, boolean negato, Confronto confronto, int numero, TipoEffettoDiStato stato, TipoPersonaggio classe,
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

	private static Filtro crea(TipoFiltro tipo, TipoParametro atteso, Confronto confronto, int numero, TipoEffettoDiStato stato,
							   TipoPersonaggio classe, CategoriaPersonaggio categoria, TipoDanno tipoDanno) {
		if (tipo.getParametro() != atteso) {
			throw new IllegalArgumentException("Il filtro " + tipo + " vuole un parametro " + tipo.getParametro() + ", non " + atteso);
		}
		return new Filtro(tipo, false, confronto, numero, stato, classe, categoria, tipoDanno);
	}

	public static Filtro senzaParametro(TipoFiltro tipo) {
		return crea(tipo, TipoParametro.NESSUNO, null, 0, null, null, null, null);
	}

	public static Filtro conConfrontoENumero(TipoFiltro tipo, Confronto confronto, int numero) {
		return crea(tipo, TipoParametro.CONFRONTO_E_NUMERO, Objects.requireNonNull(confronto), numero, null, null, null, null);
	}

	public static Filtro conConfronto(TipoFiltro tipo, Confronto confronto) {
		return crea(tipo, TipoParametro.CONFRONTO, Objects.requireNonNull(confronto), 0, null, null, null, null);
	}

	public static Filtro conStato(TipoFiltro tipo, TipoEffettoDiStato stato) {
		return crea(tipo, TipoParametro.STATO, null, 0, Objects.requireNonNull(stato), null, null, null);
	}

	public static Filtro conClasse(TipoFiltro tipo, TipoPersonaggio classe) {
		return crea(tipo, TipoParametro.CLASSE, null, 0, null, Objects.requireNonNull(classe), null, null);
	}

	public static Filtro conCategoria(TipoFiltro tipo, CategoriaPersonaggio categoria) {
		return crea(tipo, TipoParametro.CATEGORIA, null, 0, null, null, Objects.requireNonNull(categoria), null);
	}

	public static Filtro conTipoDanno(TipoFiltro tipo, TipoDanno tipoDanno) {
		return crea(tipo, TipoParametro.TIPO_DANNO, null, 0, null, null, null, Objects.requireNonNull(tipoDanno));
	}

	/**
	 * Lo stesso filtro con il risultato rovesciato (<code>NON</code>)
	 */
	public Filtro negato() {
		return new Filtro(tipo, !negato, confronto, numero, stato, classe, categoria, tipoDanno);
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
}
