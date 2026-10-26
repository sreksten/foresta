package com.threeamigos.foresta.tattiche.modifica;

import com.threeamigos.foresta.tattiche.*;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * I testi della schermata delle tattiche: le etichette dei menu e la frase che spiega una riga della tattica (il riquadro
 * destro), costruiti solo dai dati <code>*MD</code> e dagli enumerati di <code>tipi</code>, mai dal motore. Le parole stanno in
 * <code>testo_tattica_&lt;lingua&gt;.properties</code> (in italiano se la lingua scelta non ha la voce), con {0}, {1}... al posto dei valori
 * che il codice inserisce; i nomi e le descrizioni degli enumerati vengono dai file di ciascun enumerato.
 */
public final class TestoTattica {

	private static final String FILE = "testo_tattica";

	private TestoTattica() {
	}

	/**
	 * Il testo con quella chiave, con i valori al posto di {0}, {1}...; se la chiave manca, la chiave stessa, così l'errore si nota subito.
	 */
	public static String testo(String chiave, Object... valori) {
		String testo = Testi.get(FILE, chiave).orElse(chiave);
		for (int i = 0; i < valori.length; i++) {
			testo = testo.replace("{" + i + "}", String.valueOf(valori[i]));
		}
		return testo;
	}

	// Il testo di un valore di un enumerato: la chiave è il gruppo e il nome della costante (spiega.rosa.SE_STESSO)
	private static String testoDelValore(String gruppo, Enum<?> costante) {
		return testo(gruppo + '.' + costante.name());
	}

	/**
	 * Il confronto a parole: minore di, uguale a, maggiore di
	 */
	public static String confronto(TipoConfronto tipoConfronto) {
		return testoDelValore("confronto", tipoConfronto);
	}

	/**
	 * Il confronto di un filtro o di una condizione, che nei dati può essere rovesciato ("NON MINORE"): la schermata non ne crea più, ma una tattica letta
	 * o scritta nel codice (quelle dei mostri) ne ha, e si leggono con un "non" davanti.
	 */
	public static String relazione(TipoConfronto tipoConfronto, boolean negato) {
		return negato ? testo("relazione.NON", confronto(tipoConfronto)) : confronto(tipoConfronto);
	}

	/**
	 * Il criterio di scelta dell'incantesimo, a parole
	 */
	public static String criterio(CriterioIncantesimo criterio) {
		return testoDelValore("criterio", criterio);
	}

	/** Un nome di enumerato per i testi: MINOTAURO_GIGANTE diventa "minotauro gigante" */
	public static String nome(Enum<?> valore) {
		return valore.name().toLowerCase(Locale.ROOT).replace('_', ' ');
	}

	/** Il nome di una classe, dai testi della lingua: "minotauro gigante" */
	public static String nome(TipoPersonaggio classe) {
		return classe.getNome().toLowerCase(Locale.ROOT);
	}

	/**
	 * Un filtro a parole. I filtri con un confronto portano il rovescio dentro il confronto ("non minore di"), gli altri davanti ("senza")
	 */
	public static String filtro(FiltroMD filtro) {
		boolean conConfronto = filtro.getTipoFiltro().getParametro() == TipoParametro.CONFRONTO_E_NUMERO
				|| filtro.getTipoFiltro().getParametro() == TipoParametro.CONFRONTO;
		String descrizione = filtro.getTipoFiltro().getDescrizione().toLowerCase(Locale.ROOT);
		StringBuilder sb = new StringBuilder(filtro.isNegato() && !conConfronto ? testo("filtro.SENZA", descrizione) : descrizione);
		if (conConfronto) {
			sb.append(' ').append(relazione(filtro.getConfronto(), filtro.isNegato()));
			if (filtro.getTipoFiltro().getParametro() == TipoParametro.CONFRONTO_E_NUMERO) {
				sb.append(' ').append(filtro.getNumero());
			}
		}
		if (filtro.getTipoEffettoDiStato() != null) {
			sb.append(' ').append(filtro.getTipoEffettoDiStato().getDescrizione().toLowerCase(Locale.ROOT));
		} else if (filtro.getTipoPersonaggio() != null) {
			sb.append(' ').append(nome(filtro.getTipoPersonaggio()));
		} else if (filtro.getCategoriaPersonaggio() != null) {
			sb.append(' ').append(filtro.getCategoriaPersonaggio().getNome().toLowerCase(Locale.ROOT));
		} else if (filtro.getTipoDanno() != null) {
			sb.append(' ').append(filtro.getTipoDanno().getNome().toLowerCase(Locale.ROOT));
		}
		return sb.toString();
	}

	/**
	 * I filtri separati da virgole
	 */
	public static String filtri(List<FiltroMD> filtri) {
		List<String> testi = new ArrayList<>();
		for (FiltroMD filtro : filtri) {
			testi.add(filtro(filtro));
		}
		return String.join(", ", testi);
	}

	/**
	 * L'ordinamento a parole, con il tipo di danno se ne ha uno
	 */
	public static String ordinamento(OrdinamentoMD ordinamento) {
		String testo = ordinamento.getTipoOrdinamento().getDescrizione().toLowerCase(Locale.ROOT);
		return ordinamento.getTipoDanno() == null ? testo : testo + " " + ordinamento.getTipoDanno().getNome().toLowerCase(Locale.ROOT);
	}

	/**
	 * Il selettore a parole: la rosa, fra parentesi i filtri e dopo i due punti l'ordinamento
	 */
	public static String selettore(SelettoreMD selettore) {
		String testo = selettore.getTipoRosa().getDescrizione();
		if (!selettore.getFiltri().isEmpty()) {
			testo = testo("filtri.CON_FILTRI", testo, filtri(selettore.getFiltri()));
		}
		if (selettore.getOrdinamento() != null) {
			testo = testo("selettore.CON_ORDINAMENTO", testo, ordinamento(selettore.getOrdinamento()));
		}
		return testo;
	}

	/**
	 * Una condizione a parole; PROBABILITA e QUANTI hanno una forma loro
	 */
	public static String predicato(PredicatoMD predicato) {
		StringBuilder sb = new StringBuilder();
		switch (predicato.getTipoPredicato()) {
			case PROBABILITA:
				return testo(predicato.isNegato() ? "predicato.PROBABILITA_NEGATA" : "predicato.PROBABILITA", predicato.getNumero());
			case QUANTI:
				String rosa = predicato.getTipoRosa().getDescrizione().toLowerCase(Locale.ROOT);
				sb.append(testo("predicato.QUANTI", predicato.getFiltri().isEmpty() ? rosa : testo("filtri.CON_FILTRI", rosa, filtri(predicato.getFiltri()))));
				break;
			default:
				sb.append(predicato.getTipoPredicato().getDescrizione().toLowerCase(Locale.ROOT));
		}
		sb.append(' ').append(relazione(predicato.getTipoConfronto(), predicato.isNegato())).append(' ').append(predicato.getNumero());
		return sb.toString();
	}

	/**
	 * Un'azione a parole, con il criterio, il tipo di danno dell'arma e il "solo se più forte" se li ha
	 */
	public static String azione(AzioneMD azione) {
		String testo = azione.getTipo().getDescrizione();
		if (azione.getCriterio() != null) {
			testo = testo("azione.CON_CRITERIO", testo, criterio(azione.getCriterio()));
		}
		if (azione.getArmaDi() != null) {
			testo = testo("azione.ARMA_DI", testo, azione.getArmaDi().getNome().toLowerCase(Locale.ROOT));
		}
		if (azione.isSoloSePiuForteDellAttacco()) {
			testo = testo("azione.SOLO_SE_PIU_FORTE", testo);
		}
		return testo;
	}

	/**
	 * La riga corta di una voce, come sta nell'elenco: la rosa e le azioni
	 */
	public static String riga(VoceMD voce) {
		List<String> azioni = new ArrayList<>();
		for (AzioneMD azione : voce.getAzioni()) {
			azioni.add(azione(azione));
		}
		String testo = String.join(" " + testo("riga.OPPURE") + " ", azioni);
		return voce.getSelettore() == null ? testo : testo("riga.SU", testo, selettore(voce.getSelettore()));
	}

	/**
	 * La spiegazione per esteso di una voce, righe di testo per il riquadro destro
	 */
	public static List<String> spiegazione(VoceMD voce) {
		List<String> righe = new ArrayList<>();
		if (!voce.isAttiva()) {
			righe.add(testo("spiegazione.DISATTIVATA"));
		}
		if (voce.getCondizioni().isEmpty()) {
			righe.add(testo("spiegazione.SEMPRE"));
		} else {
			for (PredicatoMD condizione : voce.getCondizioni()) {
				righe.add(testo("spiegazione.SE", predicato(condizione)));
			}
		}
		if (voce.getSelettore() != null) {
			righe.add(testo("spiegazione.SU", selettore(voce.getSelettore())));
		}
		for (AzioneMD azione : voce.getAzioni()) {
			righe.add(testo("spiegazione.FA", azione(azione)));
		}
		righe.add(testo("spiegazione.REGOLA"));
		return righe;
	}

	// ================================================================ le spiegazioni degli elementi (riquadro destro della schermata)

	/**
	 * Il testo del riquadro destro per un elemento: una spiegazione per ogni valore degli enumerati, nel rispetto di quello che fa il motore
	 * (tattiche.Rose, Filtri, Predicati, Ordinamenti, Requisiti). Se cambia il comportamento di uno di questi va cambiato anche il testo.
	 */
	public static String spiega(TipoRosa rosa) {
		return testoDelValore("spiega.rosa", rosa);
	}

	public static String spiega(TipoFiltro filtro) {
		return testoDelValore("spiega.filtro", filtro);
	}

	public static String spiega(TipoPredicato predicato) {
		return testoDelValore("spiega.predicato", predicato);
	}

	public static String spiega(TipoOrdinamento ordinamento) {
		return testoDelValore("spiega.ordinamento", ordinamento);
	}

	public static String spiega(TipoConfronto tipoConfronto) {
		return testoDelValore("spiega.confronto", tipoConfronto);
	}

	public static String spiega(TipoConfronto tipoConfronto, boolean negato) {
		return negato ? testo("spiega.confronto.NEGATO", confronto(tipoConfronto)) : spiega(tipoConfronto);
	}

	public static String spiega(CriterioIncantesimo criterio) {
		return testoDelValore("spiega.criterio", criterio);
	}

	public static String spiega(TipoAzione azione) {
		return testoDelValore("spiega.azione", azione);
	}
}
