package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.modellodati.tattiche.FiltroMD;
import com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.tattiche.PredicatoMD;
import com.threeamigos.foresta.modellodati.tattiche.SelettoreMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.Confronto;
import com.threeamigos.foresta.tipi.tattiche.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoParametro;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * La tattica di un personaggio mentre il giocatore la modifica nella schermata delle tattiche (tattiche_di_combattimento_automatiche.md §11).
 * I dati <code>*MD</code> sono immutabili: la bozza li tiene in liste che la schermata cambia, e solo quando il giocatore conferma si
 * costruisce la {@link TatticaMD} da mandare al motore. A differenza di una voce <code>VoceMD</code>, una voce della bozza può non avere
 * ancora azioni (è un lavoro a metà): la bozza dice allora che non si può confermare ({@link #problemi}).
 * <p>
 * L'ultima riga della tattica, l'attacco fisico sul primo avversario, non è nella bozza: c'è sempre, non si sposta e non si toglie (è il
 * ripiego del valutatore, che attacca quando nessuna voce si può eseguire). Una voce uguale a lei in coda alla tattica letta non entra nella
 * bozza.
 */
final class BozzaTattica {

	/**
	 * Una voce in lavorazione
	 */
	static final class Voce {
		TipoRosa rosa;
		final List<FiltroMD> filtri = new ArrayList<>();
		OrdinamentoMD ordinamento;
		final List<PredicatoMD> condizioni = new ArrayList<>();
		final List<AzioneMD> azioni = new ArrayList<>();
		boolean attiva = true;

		Voce(TipoRosa rosa, OrdinamentoMD ordinamento) {
			this.rosa = rosa;
			this.ordinamento = ordinamento;
		}
	}

	private static final int MASSIMO_NUMERO = 999;

	private final List<Voce> voci = new ArrayList<>();

	private BozzaTattica() {
	}

	/**
	 * La bozza di una tattica, per un personaggio del giocatore
	 */
	static BozzaTattica da(TatticaMD tattica) {
		BozzaTattica bozza = new BozzaTattica();
		List<VoceMD> daLeggere = new ArrayList<>(tattica.getVoci());
		if (!daLeggere.isEmpty() && isAttaccoDiRiserva(daLeggere.get(daLeggere.size() - 1))) {
			daLeggere.remove(daLeggere.size() - 1);
		}
		for (VoceMD voceMD : daLeggere) {
			SelettoreMD selettore = voceMD.selettoreEffettivo();
			Voce voce = new Voce(selettore.getRosa(), selettore.getOrdinamento());
			for (FiltroMD filtro : selettore.getFiltri()) {
				voce.filtri.add(normalizza(filtro));
			}
			for (PredicatoMD condizione : voceMD.getCondizioni()) {
				voce.condizioni.add(normalizza(condizione));
			}
			voce.azioni.addAll(voceMD.getAzioni());
			voce.attiva = voceMD.isAttiva();
			bozza.voci.add(voce);
		}
		return bozza;
	}

	/**
	 * Un confronto rovesciato su un numero diventa quello equivalente senza rovescio (con numeri interi): "almeno 50" è "maggiore di 49", "al massimo 50" è
	 * "minore di 51". Se il numero uscirebbe dai limiti, o per "diverso da", resta com'è.
	 */
	static FiltroMD normalizza(FiltroMD filtro) {
		if (filtro.getTipo().getParametro() != TipoParametro.CONFRONTO_E_NUMERO || !filtro.isNegato()) {
			return filtro;
		}
		if (filtro.getConfronto() == Confronto.MINORE && filtro.getNumero() - 1 >= 0) {
			return filtro(filtro.getTipo(), false, Confronto.MAGGIORE, filtro.getNumero() - 1, null);
		}
		if (filtro.getConfronto() == Confronto.MAGGIORE && filtro.getNumero() + 1 <= massimo(filtro.getTipo())) {
			return filtro(filtro.getTipo(), false, Confronto.MINORE, filtro.getNumero() + 1, null);
		}
		return filtro;
	}

	static PredicatoMD normalizza(PredicatoMD predicato) {
		List<FiltroMD> filtri = new ArrayList<>();
		for (FiltroMD filtro : predicato.getFiltri()) {
			filtri.add(normalizza(filtro));
		}
		PredicatoMD con = predicato.getTipo() == TipoPredicato.QUANTI ? conFiltri(predicato, filtri) : predicato;
		if (con.getConfronto() == null || !con.isNegato()) {
			return con;
		}
		if (con.getConfronto() == Confronto.MINORE && con.getNumero() - 1 >= 0) {
			return predicato(con.getTipo(), false, Confronto.MAGGIORE, con.getNumero() - 1, con.getRosa(), con.getFiltri());
		}
		if (con.getConfronto() == Confronto.MAGGIORE && con.getNumero() + 1 <= massimo(con.getTipo())) {
			return predicato(con.getTipo(), false, Confronto.MINORE, con.getNumero() + 1, con.getRosa(), con.getFiltri());
		}
		return con;
	}

	private static boolean isAttaccoDiRiserva(VoceMD voce) {
		SelettoreMD selettore = voce.selettoreEffettivo();
		return voce.isAttiva() && voce.getCondizioni().isEmpty() && voce.getAzioni().size() == 1
				&& voce.getAzioni().get(0).equals(AzioneMD.di(TipoAzione.ATTACCO_FISICO))
				&& selettore.getRosa() == TipoRosa.AVVERSARI && selettore.getFiltri().isEmpty()
				&& selettore.getOrdinamento() != null && selettore.getOrdinamento().getTipo() == TipoOrdinamento.PRIMO;
	}

	List<Voce> getVoci() {
		return Collections.unmodifiableList(voci);
	}

	/**
	 * La tattica da mandare al motore: le voci con almeno un'azione (le altre, a metà, non si possono costruire)
	 */
	TatticaMD toTattica() {
		List<VoceMD> costruite = new ArrayList<>();
		for (Voce voce : voci) {
			if (!voce.azioni.isEmpty()) {
				SelettoreMD selettore = new SelettoreMD(voce.rosa, voce.filtri, voce.ordinamento);
				costruite.add(new VoceMD(selettore, voce.condizioni, voce.azioni, voce.attiva));
			}
		}
		return new TatticaMD(costruite);
	}

	/**
	 * La voce come dato (deve avere almeno un'azione), per spiegarla
	 */
	VoceMD toVoceMD(Voce voce) {
		return new VoceMD(new SelettoreMD(voce.rosa, voce.filtri, voce.ordinamento), voce.condizioni, voce.azioni, voce.attiva);
	}

	/**
	 * Perché la bozza non si può ancora confermare: le voci senza azioni e gli errori che la tattica trova (rosa non ammessa...). Vuota se va bene.
	 */
	List<String> problemi() {
		List<String> problemi = new ArrayList<>();
		for (int i = 0; i < voci.size(); i++) {
			if (voci.get(i).azioni.isEmpty()) {
				problemi.add("la voce " + (i + 1) + " non ha azioni");
			}
		}
		problemi.addAll(toTattica().problemi(true));
		return problemi;
	}

	/**
	 * Se la bozza descrive la stessa tattica (a parte l'attacco di riserva in coda)
	 */
	boolean descrive(TatticaMD tattica) {
		return toTattica().equals(da(tattica).toTattica());
	}

	// ------------------------------------------------------------------ le voci

	/**
	 * Una voce nuova in fondo (prima dell'attacco di riserva), completamente vuota: né rosa né azioni; la rosa e l'ordinamento si scelgono, o arrivano da
	 * soli con la prima azione. Finché non ha un'azione la bozza non si può confermare.
	 */
	Voce aggiungiVoce() {
		Voce voce = new Voce(null, null);
		voci.add(voce);
		return voce;
	}

	void rimuoviVoce(int indice) {
		voci.remove(indice);
	}

	/**
	 * @return false se la voce è già al margine
	 */
	boolean spostaVoce(int indice, int direzione) {
		int a = indice + direzione;
		if (indice < 0 || indice >= voci.size() || a < 0 || a >= voci.size()) {
			return false;
		}
		Collections.swap(voci, indice, a);
		return true;
	}

	/**
	 * Le rose che la voce può avere: quelle che valgono per i miei personaggi e che ogni sua azione accetta
	 */
	static List<TipoRosa> roseAmmesse(Voce voce) {
		Set<TipoRosa> ammesse = EnumSet.noneOf(TipoRosa.class);
		for (TipoRosa rosa : TipoRosa.values()) {
			if (rosa.valePer(true)) {
				ammesse.add(rosa);
			}
		}
		for (AzioneMD azione : voce.azioni) {
			ammesse.retainAll(azione.getTipo().roseAmmesse(true));
		}
		return new ArrayList<>(ammesse);
	}

	/**
	 * Le azioni che si possono aggiungere alla voce: della sua stessa portata, o qualunque se non ne ha ancora
	 */
	static List<TipoAzione> azioniAmmesse(Voce voce) {
		return azioniAmmesse(voce, null);
	}

	/**
	 * @param possibili le azioni che la classe del personaggio può avere (un elfo non canta); null: tutte
	 */
	static List<TipoAzione> azioniAmmesse(Voce voce, Set<TipoAzione> possibili) {
		List<TipoAzione> ammesse = new ArrayList<>();
		for (TipoAzione tipo : TipoAzione.values()) {
			if (tipo == TipoAzione.INCANTESIMO_DEL_REPERTORIO) {
				continue; // è per i mostri (la replica di come sceglievano), un giocatore usa le singole azioni
			}
			if (possibili != null && !possibili.contains(tipo)) {
				continue;
			}
			if (voce.azioni.isEmpty()) {
				// Senza azioni conta la rosa, se è già stata scelta
				if (voce.rosa == null || tipo.roseAmmesse(true).contains(voce.rosa)) {
					ammesse.add(tipo);
				}
			} else if (voce.azioni.get(0).getTipo().getPortata() == tipo.getPortata()) {
				ammesse.add(tipo);
			}
		}
		return ammesse;
	}

	/**
	 * Aggiunge un'azione; se la rosa della voce non la accetta passa alla prima che va bene (con l'ordinamento di base)
	 */
	void aggiungiAzione(Voce voce, TipoAzione tipo) {
		voce.azioni.add(azionePredefinita(tipo));
		correggiLaRosa(voce);
	}

	void sostituisciAzione(Voce voce, int indice, TipoAzione tipo) {
		voce.azioni.set(indice, azionePredefinita(tipo));
		correggiLaRosa(voce);
	}

	private static void correggiLaRosa(Voce voce) {
		List<TipoRosa> ammesse = roseAmmesse(voce);
		if (!ammesse.isEmpty() && (voce.rosa == null || !ammesse.contains(voce.rosa))) {
			voce.rosa = ammesse.get(0);
			voce.filtri.clear();
			voce.ordinamento = OrdinamentoMD.di(TipoOrdinamento.PRIMO);
		}
	}

	// ------------------------------------------------------------------ i valori predefiniti

	static AzioneMD azionePredefinita(TipoAzione tipo) {
		switch (tipo) {
			case INCANTESIMO_DI_DANNO:
				return AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_ADATTO, false);
			case INCANTESIMO_DEL_REPERTORIO:
				return AzioneMD.incantesimoDelRepertorio(CriterioIncantesimo.PIU_ADATTO, false);
			case CAMBIA_ARMA:
				return AzioneMD.cambiaArma(TipoDanno.values()[0]);
			default:
				return AzioneMD.di(tipo);
		}
	}

	static OrdinamentoMD ordinamentoPredefinito(TipoOrdinamento tipo) {
		return tipo.getParametro() == TipoParametro.TIPO_DANNO ? OrdinamentoMD.di(tipo, TipoDanno.values()[0]) : OrdinamentoMD.di(tipo);
	}

	static FiltroMD filtroPredefinito(TipoFiltro tipo) {
		return filtro(tipo, false, tipo.getParametro() == TipoParametro.CONFRONTO ? Confronto.MAGGIORE : Confronto.MINORE,
				numeroPredefinito(tipo), parametroPredefinito(tipo.getParametro()));
	}

	static PredicatoMD predicatoPredefinito(TipoPredicato tipo) {
		switch (tipo) {
			case QUANTI:
				return PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.emptyList(), Confronto.MAGGIORE, 0);
			case PROBABILITA:
				return PredicatoMD.probabilita(50);
			default:
				return PredicatoMD.conConfrontoENumero(tipo, tipo == TipoPredicato.VOCE_USATA ? Confronto.MINORE : Confronto.MAGGIORE,
						tipo == TipoPredicato.ASTUZIA || tipo == TipoPredicato.INTELLIGENZA ? 10 : 1);
		}
	}

	private static int numeroPredefinito(TipoFiltro tipo) {
		switch (tipo) {
			case SALUTE:
				return 50;
			case SALUTE_PERCENTUALE:
				return 50;
			case MAGIA:
				return 10;
			case LIVELLO:
				return 5;
			default:
				return 0;
		}
	}

	private static Object parametroPredefinito(TipoParametro parametro) {
		switch (parametro) {
			case STATO:
				return TipoEffettoDiStato.values()[0];
			case CLASSE:
				return TipoPersonaggio.values()[0];
			case CATEGORIA:
				return CategoriaPersonaggio.values()[0];
			case TIPO_DANNO:
				return TipoDanno.values()[0];
			default:
				return null;
		}
	}

	// ------------------------------------------------------------------ le modifiche a un filtro o a una condizione

	/**
	 * Le condizioni che un giocatore può scrivere: non l'astuzia né l'INTELLIGENZA, che servono a dividere i mostri in fasce di furbizia (le tattiche base dei
	 * mostri) e non hanno senso per i personaggi del gruppo.
	 */
	static List<TipoPredicato> predicatiAmmessi() {
		List<TipoPredicato> ammessi = new ArrayList<>();
		for (TipoPredicato tipo : TipoPredicato.values()) {
			if (tipo != TipoPredicato.ASTUZIA && tipo != TipoPredicato.INTELLIGENZA) {
				ammessi.add(tipo);
			}
		}
		return ammessi;
	}

	/**
	 * I filtri che hanno senso su una rosa:
	 * <ul>
	 * <li>mai quelli che confrontano il candidato con chi decide o con la sua storia (il capo, me stesso, scelto l'ultima volta, mi ha attaccato per ultimo):
	 * contando, o scegliendo fra gli alleati, non hanno senso, e per scegliere fra gli avversari ci sono gli ordinamenti "chi mi ha attaccato per ultimo" e
	 * "l'ultimo scelto", che si trovano meglio;</li>
	 * <li>su <b>se stessi</b> il candidato è già deciso: niente classe, categoria, livello rispetto al mio, né "debole all'azione" (su di sé si bevono pozioni
	 * o si cambia arma, azioni che non fanno danno).</li>
	 * </ul>
	 */
	static List<TipoFiltro> filtriAmmessi(TipoRosa rosa) {
		Set<TipoFiltro> esclusi = EnumSet.copyOf(SOLO_PER_SCEGLIERE);
		if (rosa == TipoRosa.SE_STESSO) {
			esclusi.addAll(SOLO_SU_ALTRI);
		}
		List<TipoFiltro> ammessi = new ArrayList<>();
		for (TipoFiltro tipo : TipoFiltro.values()) {
			if (!esclusi.contains(tipo)) {
				ammessi.add(tipo);
			}
		}
		return ammessi;
	}

	/**
	 * Gli ordinamenti di una rosa: quelli che guardano la storia di chi decide (chi mi ha attaccato per ultimo, l'ultimo scelto) solo fra gli avversari
	 */
	static List<TipoOrdinamento> ordinamentiAmmessi(TipoRosa rosa) {
		boolean traGliAvversari = rosa == null || rosa == TipoRosa.AVVERSARI || rosa == TipoRosa.AVVERSARI_E_OSPITI;
		List<TipoOrdinamento> ammessi = new ArrayList<>();
		for (TipoOrdinamento tipo : TipoOrdinamento.values()) {
			if (traGliAvversari || (tipo != TipoOrdinamento.ULTIMO_ATTACCANTE && tipo != TipoOrdinamento.ULTIMO_SCELTO)) {
				ammessi.add(tipo);
			}
		}
		return ammessi;
	}

	private static final Set<TipoFiltro> SOLO_PER_SCEGLIERE = EnumSet.of(TipoFiltro.IL_CAPO, TipoFiltro.ME_STESSO, TipoFiltro.SCELTO_L_ULTIMA_VOLTA,
			TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO);

	private static final Set<TipoFiltro> SOLO_SU_ALTRI = EnumSet.of(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoFiltro.CLASSE, TipoFiltro.CATEGORIA,
			TipoFiltro.IL_CAPO, TipoFiltro.ME_STESSO, TipoFiltro.SCELTO_L_ULTIMA_VOLTA, TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO,
			TipoFiltro.DEBOLE_ALL_AZIONE);

	/**
	 * Quanto vale un passo del numero di un filtro o di una condizione, e il massimo (le percentuali arrivano a 100)
	 */
	static int passo(TipoFiltro tipo) {
		return tipo == TipoFiltro.SALUTE || tipo == TipoFiltro.SALUTE_PERCENTUALE || tipo == TipoFiltro.MAGIA ? 5 : 1;
	}

	static int passo(TipoPredicato tipo) {
		return tipo == TipoPredicato.PROBABILITA ? 5 : 1;
	}

	static int massimo(TipoFiltro tipo) {
		return tipo == TipoFiltro.SALUTE_PERCENTUALE ? 100 : MASSIMO_NUMERO;
	}

	static int massimo(TipoPredicato tipo) {
		return tipo == TipoPredicato.PROBABILITA ? 100 : MASSIMO_NUMERO;
	}

	/**
	 * Ricostruisce un filtro dai suoi pezzi (i dati sono immutabili): quelli che il tipo non usa si ignorano
	 */
	static FiltroMD filtro(TipoFiltro tipo, boolean negato, Confronto confronto, int numero, Object parametro) {
		FiltroMD filtro;
		switch (tipo.getParametro()) {
			case CONFRONTO_E_NUMERO:
				filtro = FiltroMD.conConfrontoENumero(tipo, confronto, numero);
				break;
			case CONFRONTO:
				filtro = FiltroMD.conConfronto(tipo, confronto);
				break;
			case STATO:
				filtro = FiltroMD.conStato(tipo, (TipoEffettoDiStato) parametro);
				break;
			case CLASSE:
				filtro = FiltroMD.conClasse(tipo, (TipoPersonaggio) parametro);
				break;
			case CATEGORIA:
				filtro = FiltroMD.conCategoria(tipo, (CategoriaPersonaggio) parametro);
				break;
			case TIPO_DANNO:
				filtro = FiltroMD.conTipoDanno(tipo, (TipoDanno) parametro);
				break;
			default:
				filtro = FiltroMD.senzaParametro(tipo);
		}
		return negato ? filtro.negato() : filtro;
	}

	static Object parametroDi(FiltroMD filtro) {
		if (filtro.getStato() != null) {
			return filtro.getStato();
		}
		if (filtro.getClasse() != null) {
			return filtro.getClasse();
		}
		if (filtro.getCategoria() != null) {
			return filtro.getCategoria();
		}
		return filtro.getTipoDanno();
	}

	static FiltroMD conNumero(FiltroMD f, int numero) {
		int limitato = Math.max(0, Math.min(massimo(f.getTipo()), numero));
		return filtro(f.getTipo(), f.isNegato(), f.getConfronto(), limitato, parametroDi(f));
	}

	/**
	 * Se il filtro si può rovesciare ("senza"): quelli numerici no, perché il confronto dice già tutto (al posto di "non maggiore di" si sceglie "al massimo")
	 */
	static boolean isNegabile(TipoFiltro tipo) {
		return tipo.getParametro() != TipoParametro.CONFRONTO_E_NUMERO && tipo.getParametro() != TipoParametro.CONFRONTO;
	}

	/**
	 * Un confronto con il suo rovescio: "al massimo" è "non maggiore", "almeno" è "non minore"
	 */
	static FiltroMD conRelazione(FiltroMD f, Confronto confronto, boolean negato) {
		return filtro(f.getTipo(), negato, confronto, f.getNumero(), parametroDi(f));
	}

	static PredicatoMD conRelazione(PredicatoMD p, Confronto confronto, boolean negato) {
		return predicato(p.getTipo(), negato, confronto, p.getNumero(), p.getRosa(), p.getFiltri());
	}

	static FiltroMD conConfronto(FiltroMD f, Confronto confronto) {
		return filtro(f.getTipo(), f.isNegato(), confronto, f.getNumero(), parametroDi(f));
	}

	static FiltroMD conParametro(FiltroMD f, Object parametro) {
		return filtro(f.getTipo(), f.isNegato(), f.getConfronto(), f.getNumero(), parametro);
	}

	static PredicatoMD conNumero(PredicatoMD p, int numero) {
		int limitato = Math.max(0, Math.min(massimo(p.getTipo()), numero));
		return predicato(p.getTipo(), p.isNegato(), p.getConfronto(), limitato, p.getRosa(), p.getFiltri());
	}

	static PredicatoMD conConfronto(PredicatoMD p, Confronto confronto) {
		return predicato(p.getTipo(), p.isNegato(), confronto, p.getNumero(), p.getRosa(), p.getFiltri());
	}

	static PredicatoMD conRosa(PredicatoMD p, TipoRosa rosa) {
		// I filtri che non hanno senso sulla nuova rosa cadono
		List<FiltroMD> filtri = new ArrayList<>();
		for (FiltroMD filtro : p.getFiltri()) {
			if (filtriAmmessi(rosa).contains(filtro.getTipo())) {
				filtri.add(filtro);
			}
		}
		return predicato(p.getTipo(), p.isNegato(), p.getConfronto(), p.getNumero(), rosa, filtri);
	}

	static PredicatoMD conFiltri(PredicatoMD p, List<FiltroMD> filtri) {
		return predicato(p.getTipo(), p.isNegato(), p.getConfronto(), p.getNumero(), p.getRosa(), filtri);
	}

	static PredicatoMD predicato(TipoPredicato tipo, boolean negato, Confronto confronto, int numero, TipoRosa rosa, List<FiltroMD> filtri) {
		PredicatoMD predicato;
		switch (tipo) {
			case QUANTI:
				predicato = PredicatoMD.quanti(rosa, filtri, confronto, numero);
				break;
			case PROBABILITA:
				predicato = PredicatoMD.probabilita(numero);
				break;
			default:
				predicato = PredicatoMD.conConfrontoENumero(tipo, confronto, numero);
		}
		return negato ? predicato.negato() : predicato;
	}

	static AzioneMD conCriterio(AzioneMD a, CriterioIncantesimo criterio) {
		return a.getTipo() == TipoAzione.INCANTESIMO_DI_DANNO
				? AzioneMD.incantesimoDiDanno(criterio, a.isSoloSePiuForteDellAttacco())
				: AzioneMD.incantesimoDelRepertorio(criterio, a.isSoloSePiuForteDellAttacco());
	}

	/**
	 * Solo se più forte dell'attacco: per gli incantesimi che fanno danno; l'azione non cambia se non può averlo
	 */
	static AzioneMD commutaSoloSePiuForte(AzioneMD a) {
		if (a.getTipo() == TipoAzione.INCANTESIMO_DI_DANNO) {
			return AzioneMD.incantesimoDiDanno(a.getCriterio(), !a.isSoloSePiuForteDellAttacco());
		}
		if (a.getTipo() == TipoAzione.INCANTESIMO_DEL_REPERTORIO) {
			return AzioneMD.incantesimoDelRepertorio(a.getCriterio(), !a.isSoloSePiuForteDellAttacco());
		}
		if (a.isSoloSePiuForteDellAttacco()) {
			return AzioneMD.di(a.getTipo());
		}
		return a.soloSePiuForteDellAttacco();
	}

	/**
	 * Se l'azione ha un "solo se più forte dell'attacco" da accendere: gli incantesimi e il dardo che fanno danno
	 */
	static boolean haSoloSePiuForte(AzioneMD a) {
		if (a.getTipo() == TipoAzione.INCANTESIMO_DI_DANNO || a.getTipo() == TipoAzione.INCANTESIMO_DEL_REPERTORIO) {
			return true;
		}
		try {
			a.soloSePiuForteDellAttacco();
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
