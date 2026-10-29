package com.threeamigos.foresta.tattiche.modifica;

import com.threeamigos.foresta.tipi.PortataAzione;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tattiche.*;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.*;

import java.util.*;

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
public final class BozzaTattica {

	/**
	 * Una voce in lavorazione
	 */
	public static final class Voce {
		public TipoRosa rosa;
		public final List<FiltroMD> filtri = new ArrayList<>();
		public OrdinamentoMD ordinamento;
		public final List<PredicatoMD> condizioni = new ArrayList<>();
		public final List<AzioneMD> azioni = new ArrayList<>();
		public boolean attiva = true;
		/** Il nome dato dal giocatore, o null: la schermata mostra allora "Tattica" e il numero della voce */
		public String nome;
		/** La voce è ripiegata nella schermata (solo la riga del nome): non si salva */
		public boolean chiusa;

		Voce(TipoRosa rosa, OrdinamentoMD ordinamento) {
			this.rosa = rosa;
			this.ordinamento = ordinamento;
		}
	}

	/** Il numero più alto che il giocatore può scrivere in un filtro o in una condizione (le percentuali si fermano a 100, vedi massimo) */
	private static final int MASSIMO_NUMERO = 999;

	// Le voci in lavorazione, nell'ordine in cui si valutano; non comprendono l'attacco di riserva, che la schermata disegna a parte
	private final List<Voce> voci = new ArrayList<>();

	private BozzaTattica() {
	}

	/**
	 * La bozza di una tattica, per un personaggio del giocatore
	 */
	public static BozzaTattica da(TatticaMD tattica) {
		// Si copia ogni voce nei suoi pezzi modificabili. La voce senza selettore diventa quella con il selettore di base (selettoreEffettivo), così
		// la schermata mostra sempre la rosa e l'ordinamento che il motore userebbe davvero
		BozzaTattica bozza = new BozzaTattica();
		List<VoceMD> daLeggere = new ArrayList<>(tattica.getVoci());
		if (!daLeggere.isEmpty() && isAttaccoDiRiserva(daLeggere.get(daLeggere.size() - 1))) {
			daLeggere.remove(daLeggere.size() - 1);
		}
		for (VoceMD voceMD : daLeggere) {
			SelettoreMD selettore = voceMD.selettoreEffettivo();
			Voce voce = new Voce(selettore.getTipoRosa(), selettore.getOrdinamento());
			for (FiltroMD filtro : selettore.getFiltri()) {
				voce.filtri.add(normalizza(filtro));
			}
			for (PredicatoMD condizione : voceMD.getCondizioni()) {
				voce.condizioni.add(normalizza(condizione));
			}
			voce.azioni.addAll(voceMD.getAzioni());
			voce.attiva = voceMD.isAttiva();
			// Una voce senza nome (una tattica scritta dal codice, o di prima) riceve subito quello predefinito: da qui il nome è suo e si salva
			voce.nome = voceMD.getNome() != null ? voceMD.getNome() : nomePredefinito(bozza.voci.size());
			bozza.voci.add(voce);
		}
		return bozza;
	}

	/**
	 * Un confronto rovesciato su un numero diventa quello equivalente senza rovescio (con numeri interi): "almeno 50" è "maggiore di 49", "al massimo 50" è
	 * "minore di 51". Se il numero uscirebbe dai limiti, o per "diverso da", resta com'è.
	 */
	public static FiltroMD normalizza(FiltroMD filtro) {
		if (filtro.getTipoFiltro().getParametro() != TipoParametro.CONFRONTO_E_NUMERO || !filtro.isNegato()) {
			return filtro;
		}
		if (filtro.getConfronto() == TipoConfronto.MINORE && filtro.getNumero() - 1 >= 0) {
			return filtro(filtro.getTipoFiltro(), false, TipoConfronto.MAGGIORE, filtro.getNumero() - 1, null);
		}
		if (filtro.getConfronto() == TipoConfronto.MAGGIORE && filtro.getNumero() + 1 <= massimo(filtro.getTipoFiltro())) {
			return filtro(filtro.getTipoFiltro(), false, TipoConfronto.MINORE, filtro.getNumero() + 1, null);
		}
		return filtro;
	}

	/**
	 * Lo stesso per una condizione: oltre al rovescio dei suoi confronti, normalizza anche i filtri che contiene (QUANTI)
	 */
	public static PredicatoMD normalizza(PredicatoMD predicato) {
		List<FiltroMD> filtri = new ArrayList<>();
		for (FiltroMD filtro : predicato.getFiltri()) {
			filtri.add(normalizza(filtro));
		}
		PredicatoMD con = predicato.getTipoPredicato() == TipoPredicato.QUANTI ? conFiltri(predicato, filtri) : predicato;
		if (con.getTipoConfronto() == null || !con.isNegato()) {
			return con;
		}
		if (con.getTipoConfronto() == TipoConfronto.MINORE && con.getNumero() - 1 >= 0) {
			return predicato(con.getTipoPredicato(), false, TipoConfronto.MAGGIORE, con.getNumero() - 1, con.getTipoRosa(), con.getFiltri());
		}
		if (con.getTipoConfronto() == TipoConfronto.MAGGIORE && con.getNumero() + 1 <= massimo(con.getTipoPredicato())) {
			return predicato(con.getTipoPredicato(), false, TipoConfronto.MINORE, con.getNumero() + 1, con.getTipoRosa(), con.getFiltri());
		}
		return con;
	}

	/**
	 * Se la voce è l'attacco fisico sul primo avversario senza filtri né condizioni: la stessa cosa che il Valutatore fa da solo quando nessuna voce è
	 * eseguibile. Scritta in coda a una tattica (le predefinite lo fanno) non entra nella bozza, per non mostrarla due volte.
	 */
	private static boolean isAttaccoDiRiserva(VoceMD voce) {
		SelettoreMD selettore = voce.selettoreEffettivo();
		return voce.isAttiva() && voce.getCondizioni().isEmpty() && voce.getAzioni().size() == 1
				&& voce.getAzioni().get(0).equals(AzioneMD.di(TipoAzione.ATTACCO_FISICO))
				&& selettore.getTipoRosa() == TipoRosa.AVVERSARI && selettore.getFiltri().isEmpty()
				&& selettore.getOrdinamento() != null && selettore.getOrdinamento().getTipoOrdinamento() == TipoOrdinamento.PRIMO;
	}

	/**
	 * Le voci della bozza, in sola lettura come lista (le singole voci si modificano)
	 */
	public List<Voce> getVoci() {
		return Collections.unmodifiableList(voci);
	}

	/**
	 * La tattica da mandare al motore: le voci con almeno un'azione (le altre, a metà, non si possono costruire)
	 */
	public TatticaMD toTattica() {
		List<VoceMD> costruite = new ArrayList<>();
		for (Voce voce : voci) {
			if (!voce.azioni.isEmpty()) {
				SelettoreMD selettore = new SelettoreMD(voce.rosa, voce.filtri, voce.ordinamento);
				costruite.add(new VoceMD(selettore, voce.condizioni, voce.azioni, voce.attiva, voce.nome));
			}
		}
		return new TatticaMD(costruite);
	}

	/**
	 * La voce come dato (deve avere almeno un'azione), per spiegarla
	 */
	public VoceMD toVoceMD(Voce voce) {
		return new VoceMD(new SelettoreMD(voce.rosa, voce.filtri, voce.ordinamento), voce.condizioni, voce.azioni, voce.attiva, voce.nome);
	}

	/**
	 * Perché la bozza non si può ancora confermare: le voci senza azioni e gli errori che la tattica trova (rosa non ammessa...). Vuota se va bene.
	 */
	public List<String> problemi() {
		List<String> problemi = new ArrayList<>();
		for (int i = 0; i < voci.size(); i++) {
			if (voci.get(i).azioni.isEmpty()) {
				problemi.add(TestoTattica.testo("problema.VOCE_SENZA_AZIONI", i + 1));
			}
		}
		problemi.addAll(toTattica().problemi(true));
		return problemi;
	}

	/**
	 * Se la bozza descrive la stessa tattica (a parte l'attacco di riserva in coda)
	 */
	public boolean descrive(TatticaMD tattica) {
		return toTattica().equals(da(tattica).toTattica());
	}

	/**
	 * Se c'è una voce ancora senza azioni (appena aggiunta): non entra nella tattica da mandare al motore, ma impedisce di confermare
	 */
	public boolean haVociSenzaAzioni() {
		for (Voce voce : voci) {
			if (voce.azioni.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------------ le voci

	/**
	 * Il nome di una voce che non ne ha uno suo: "Tattica" e il suo numero (da 1)
	 */
	public static String nomePredefinito(int indice) {
		return TestoTattica.testo("voce.NOME_PREDEFINITO", indice + 1);
	}

	/**
	 * Una voce nuova in fondo (prima dell'attacco di riserva), completamente vuota: né rosa né azioni; la rosa e l'ordinamento si scelgono, o arrivano da
	 * soli con la prima azione. Finché non ha un'azione la bozza non si può confermare.
	 */
	public Voce aggiungiVoce() {
		Voce voce = new Voce(null, null);
		voce.nome = nomePredefinito(voci.size());
		voci.add(voce);
		return voce;
	}

	/**
	 * Toglie la voce in quella posizione
	 */
	public void rimuoviVoce(int indice) {
		voci.remove(indice);
	}

	/**
	 * @return false se la voce è già al margine
	 */
	public boolean spostaVoce(int indice, int direzione) {
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
	public static List<TipoRosa> roseAmmesse(Voce voce) {
		Set<TipoRosa> ammesse = EnumSet.noneOf(TipoRosa.class);
		for (TipoRosa rosa : TipoRosa.values()) {
			if (rosa.valePer(true)) {
				ammesse.add(rosa);
			}
		}
		for (AzioneMD azione : voce.azioni) {
			ammesse.retainAll(PortataAzione.roseAmmesse(azione.getTipo(), true));
		}
		return new ArrayList<>(ammesse);
	}

	/**
	 * Le azioni che si possono aggiungere alla voce: della sua stessa portata, o qualunque se non ne ha ancora
	 */
	public static List<TipoAzione> azioniAmmesse(Voce voce) {
		return azioniAmmesse(voce, null);
	}

	/**
	 * @param possibili le azioni che la classe del personaggio può avere (un elfo non canta); null: tutte
	 */
	public static List<TipoAzione> azioniAmmesse(Voce voce, Set<TipoAzione> possibili) {
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
				if (voce.rosa == null || PortataAzione.roseAmmesse(tipo, true).contains(voce.rosa)) {
					ammesse.add(tipo);
				}
			} else if (PortataAzione.di(voce.azioni.get(0).getTipo()) == PortataAzione.di(tipo)) {
				ammesse.add(tipo);
			}
		}
		return ammesse;
	}

	/**
	 * Aggiunge un'azione; se la rosa della voce non la accetta passa alla prima che va bene (con l'ordinamento di base)
	 */
	public void aggiungiAzione(Voce voce, TipoAzione tipo) {
		voce.azioni.add(azionePredefinita(tipo));
		correggiLaRosa(voce);
	}

	/**
	 * Cambia il tipo di un'azione della voce, con i parametri predefiniti del nuovo tipo; anche qui la rosa si corregge se non va più bene
	 */
	public void sostituisciAzione(Voce voce, int indice, TipoAzione tipo) {
		voce.azioni.set(indice, azionePredefinita(tipo));
		correggiLaRosa(voce);
	}

	/**
	 * Se la rosa della voce non è più ammessa dalle sue azioni (o non è ancora stata scelta) passa alla prima che va bene, e filtri e ordinamento
	 * ripartono da zero: quelli della rosa di prima possono non avere senso sulla nuova
	 */
	private static void correggiLaRosa(Voce voce) {
		List<TipoRosa> ammesse = roseAmmesse(voce);
		if (!ammesse.isEmpty() && (voce.rosa == null || !ammesse.contains(voce.rosa))) {
			voce.rosa = ammesse.get(0);
			voce.filtri.clear();
			voce.ordinamento = OrdinamentoMD.di(TipoOrdinamento.PRIMO);
		}
	}

	// ------------------------------------------------------------------ i valori predefiniti

	/**
	 * L'azione di quel tipo con i parametri iniziali: il criterio più adatto per gli incantesimi, il primo tipo di danno per il cambio d'arma (il
	 * giocatore poi lo cambia)
	 */
	public static AzioneMD azionePredefinita(TipoAzione tipo) {
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

	/**
	 * L'ordinamento di quel tipo, con il primo tipo di danno se ne vuole uno
	 */
	public static OrdinamentoMD ordinamentoPredefinito(TipoOrdinamento tipo) {
		return tipo.getParametro() == TipoParametro.TIPO_DANNO ? OrdinamentoMD.di(tipo, TipoDanno.values()[0]) : OrdinamentoMD.di(tipo);
	}

	/**
	 * Un filtro nuovo di quel tipo, con un parametro qualsiasi ma valido (il confronto "minore di", tranne dove c'è solo il confronto): la bozza non
	 * può avere pezzi a metà, perché i dati sono immutabili e verificano i parametri alla costruzione
	 */
	public static FiltroMD filtroPredefinito(TipoFiltro tipo) {
		return filtro(tipo, false, tipo.getParametro() == TipoParametro.CONFRONTO ? TipoConfronto.MAGGIORE : TipoConfronto.MINORE,
				numeroPredefinito(tipo), parametroPredefinito(tipo.getParametro()));
	}

	/**
	 * Una condizione nuova di quel tipo con valori di partenza ragionevoli (50% di probabilità, "almeno un avversario" per QUANTI...)
	 */
	public static PredicatoMD predicatoPredefinito(TipoPredicato tipo) {
		switch (tipo) {
			case QUANTI:
				return PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.emptyList(), TipoConfronto.MAGGIORE, 0);
			case PROBABILITA:
				return PredicatoMD.probabilita(50);
			default:
				return PredicatoMD.conConfrontoENumero(tipo, tipo == TipoPredicato.VOCE_USATA ? TipoConfronto.MINORE : TipoConfronto.MAGGIORE,
						tipo == TipoPredicato.ASTUZIA || tipo == TipoPredicato.INTELLIGENZA ? 10 : 1);
		}
	}

	/**
	 * Il numero di partenza di un filtro numerico, nell'ordine di grandezza della sua misura (50 punti o percento di salute, 10 di MAGIA, livello 5)
	 */
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

	/**
	 * Il primo valore dell'enumerato che il parametro richiede, o null se non ne vuole uno
	 */
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
	public static List<TipoPredicato> predicatiAmmessi() {
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
	public static List<TipoFiltro> filtriAmmessi(TipoRosa rosa) {
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
	public static List<TipoOrdinamento> ordinamentiAmmessi(TipoRosa rosa) {
		boolean traGliAvversari = rosa == null || rosa == TipoRosa.AVVERSARI || rosa == TipoRosa.AVVERSARI_E_OSPITI;
		List<TipoOrdinamento> ammessi = new ArrayList<>();
		for (TipoOrdinamento tipo : TipoOrdinamento.values()) {
			if (traGliAvversari || (tipo != TipoOrdinamento.ULTIMO_ATTACCANTE && tipo != TipoOrdinamento.ULTIMO_SCELTO)) {
				ammessi.add(tipo);
			}
		}
		return ammessi;
	}

	// I filtri che confrontano il candidato con chi decide o con la sua storia: non si offrono mai nei filtri di un selettore o di un QUANTI
	private static final Set<TipoFiltro> SOLO_PER_SCEGLIERE = EnumSet.of(TipoFiltro.IL_CAPO, TipoFiltro.ME_STESSO, TipoFiltro.SCELTO_L_ULTIMA_VOLTA,
			TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO);

	// I filtri che non hanno senso quando la rosa è il solo attore (SE_STESSO)
	private static final Set<TipoFiltro> SOLO_SU_ALTRI = EnumSet.of(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoFiltro.CLASSE, TipoFiltro.CATEGORIA,
			TipoFiltro.IL_CAPO, TipoFiltro.ME_STESSO, TipoFiltro.SCELTO_L_ULTIMA_VOLTA, TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO,
			TipoFiltro.DEBOLE_ALL_AZIONE);

	/**
	 * Quanto vale un passo del numero di un filtro o di una condizione, e il massimo (le percentuali arrivano a 100)
	 */
	public static int passo(TipoFiltro tipo) {
		return tipo == TipoFiltro.SALUTE || tipo == TipoFiltro.SALUTE_PERCENTUALE || tipo == TipoFiltro.MAGIA ? 5 : 1;
	}

	/**
	 * Come passo(TipoFiltro), per una condizione: la probabilità va di 5 in 5
	 */
	public static int passo(TipoPredicato tipo) {
		return tipo == TipoPredicato.PROBABILITA ? 5 : 1;
	}

	public static int massimo(TipoFiltro tipo) {
		return tipo == TipoFiltro.SALUTE_PERCENTUALE ? 100 : MASSIMO_NUMERO;
	}

	/**
	 * Come massimo(TipoFiltro), per una condizione: la probabilità arriva a 100
	 */
	public static int massimo(TipoPredicato tipo) {
		return tipo == TipoPredicato.PROBABILITA ? 100 : MASSIMO_NUMERO;
	}

	/**
	 * Ricostruisce un filtro dai suoi pezzi (i dati sono immutabili): quelli che il tipo non usa si ignorano
	 */
	public static FiltroMD filtro(TipoFiltro tipo, boolean negato, TipoConfronto tipoConfronto, int numero, Object parametro) {
		FiltroMD filtro;
		switch (tipo.getParametro()) {
			case CONFRONTO_E_NUMERO:
				filtro = FiltroMD.conConfrontoENumero(tipo, tipoConfronto, numero);
				break;
			case CONFRONTO:
				filtro = FiltroMD.conConfronto(tipo, tipoConfronto);
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

	/**
	 * Il parametro enumerato del filtro (stato, classe, categoria o tipo di danno), o null se non ne ha: quello che serve per ricostruirlo con
	 * {@link #filtro}
	 */
	public static Object parametroDi(FiltroMD filtro) {
		if (filtro.getTipoEffettoDiStato() != null) {
			return filtro.getTipoEffettoDiStato();
		}
		if (filtro.getTipoPersonaggio() != null) {
			return filtro.getTipoPersonaggio();
		}
		if (filtro.getCategoriaPersonaggio() != null) {
			return filtro.getCategoriaPersonaggio();
		}
		return filtro.getTipoDanno();
	}

	/**
	 * Lo stesso filtro con un altro numero, tenuto fra 0 e il massimo del suo tipo
	 */
	public static FiltroMD conNumero(FiltroMD f, int numero) {
		int limitato = Math.max(0, Math.min(massimo(f.getTipoFiltro()), numero));
		return filtro(f.getTipoFiltro(), f.isNegato(), f.getConfronto(), limitato, parametroDi(f));
	}

	/**
	 * Se il filtro si può rovesciare ("senza"): quelli numerici no, perché il confronto dice già tutto (al posto di "non maggiore di" si sceglie "al massimo")
	 */
	public static boolean isNegabile(TipoFiltro tipo) {
		return tipo.getParametro() != TipoParametro.CONFRONTO_E_NUMERO && tipo.getParametro() != TipoParametro.CONFRONTO;
	}

	/**
	 * Un confronto con il suo rovescio: "al massimo" è "non maggiore", "almeno" è "non minore"
	 */
	public static FiltroMD conRelazione(FiltroMD f, TipoConfronto tipoConfronto, boolean negato) {
		return filtro(f.getTipoFiltro(), negato, tipoConfronto, f.getNumero(), parametroDi(f));
	}

	public static PredicatoMD conRelazione(PredicatoMD p, TipoConfronto tipoConfronto, boolean negato) {
		return predicato(p.getTipoPredicato(), negato, tipoConfronto, p.getNumero(), p.getTipoRosa(), p.getFiltri());
	}

	/**
	 * Lo stesso filtro con un altro confronto (i dati sono immutabili: si ricostruisce)
	 */
	public static FiltroMD conConfronto(FiltroMD f, TipoConfronto tipoConfronto) {
		return filtro(f.getTipoFiltro(), f.isNegato(), tipoConfronto, f.getNumero(), parametroDi(f));
	}

	/**
	 * Lo stesso filtro con un altro parametro enumerato
	 */
	public static FiltroMD conParametro(FiltroMD f, Object parametro) {
		return filtro(f.getTipoFiltro(), f.isNegato(), f.getConfronto(), f.getNumero(), parametro);
	}

	/**
	 * La stessa condizione con un altro numero, tenuto fra 0 e il massimo del suo tipo
	 */
	public static PredicatoMD conNumero(PredicatoMD p, int numero) {
		int limitato = Math.max(0, Math.min(massimo(p.getTipoPredicato()), numero));
		return predicato(p.getTipoPredicato(), p.isNegato(), p.getTipoConfronto(), limitato, p.getTipoRosa(), p.getFiltri());
	}

	/**
	 * La stessa condizione con un altro confronto
	 */
	public static PredicatoMD conConfronto(PredicatoMD p, TipoConfronto tipoConfronto) {
		return predicato(p.getTipoPredicato(), p.isNegato(), tipoConfronto, p.getNumero(), p.getTipoRosa(), p.getFiltri());
	}

	/**
	 * La stessa condizione QUANTI su un'altra rosa
	 */
	public static PredicatoMD conRosa(PredicatoMD p, TipoRosa rosa) {
		// I filtri che non hanno senso sulla nuova rosa cadono
		List<FiltroMD> filtri = new ArrayList<>();
		for (FiltroMD filtro : p.getFiltri()) {
			if (filtriAmmessi(rosa).contains(filtro.getTipoFiltro())) {
				filtri.add(filtro);
			}
		}
		return predicato(p.getTipoPredicato(), p.isNegato(), p.getTipoConfronto(), p.getNumero(), rosa, filtri);
	}

	/**
	 * La stessa condizione QUANTI con altri filtri
	 */
	public static PredicatoMD conFiltri(PredicatoMD p, List<FiltroMD> filtri) {
		return predicato(p.getTipoPredicato(), p.isNegato(), p.getTipoConfronto(), p.getNumero(), p.getTipoRosa(), filtri);
	}

	/**
	 * Ricostruisce una condizione dai suoi pezzi (i dati sono immutabili): quelli che il tipo non usa si ignorano
	 */
	public static PredicatoMD predicato(TipoPredicato tipo, boolean negato, TipoConfronto tipoConfronto, int numero, TipoRosa rosa, List<FiltroMD> filtri) {
		PredicatoMD predicato;
		switch (tipo) {
			case QUANTI:
				predicato = PredicatoMD.quanti(rosa, filtri, tipoConfronto, numero);
				break;
			case PROBABILITA:
				predicato = PredicatoMD.probabilita(numero);
				break;
			default:
				predicato = PredicatoMD.conConfrontoENumero(tipo, tipoConfronto, numero);
		}
		return negato ? predicato.negato() : predicato;
	}

	/**
	 * La stessa azione (INCANTESIMO_DI_DANNO o del repertorio) con un altro criterio di scelta
	 */
	public static AzioneMD conCriterio(AzioneMD a, CriterioIncantesimo criterio) {
		return a.getTipo() == TipoAzione.INCANTESIMO_DI_DANNO
				? AzioneMD.incantesimoDiDanno(criterio, a.isSoloSePiuForteDellAttacco())
				: AzioneMD.incantesimoDelRepertorio(criterio, a.isSoloSePiuForteDellAttacco());
	}

	/**
	 * Solo se più forte dell'attacco: per gli incantesimi che fanno danno; l'azione non cambia se non può averlo
	 */
	public static AzioneMD commutaSoloSePiuForte(AzioneMD a) {
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
	public static boolean haSoloSePiuForte(AzioneMD a) {
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
