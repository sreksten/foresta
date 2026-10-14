package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoInizioRound;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.tattiche.EsitoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.strumenti.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;

/**
 * La memoria di un combattimento, che le tattiche leggono (vedi tattiche_di_combattimento_automatiche.md §3.3): non fa
 * parte del modello dati, non si salva (un combattimento non si interrompe mai per salvare) e si azzera a ogni inizio
 * locazione. Tiene:
 * <ul>
 * <li>la <b>lista dei turni</b>: a inizio combattimento (e a ogni ondata) si fissa la lista di tutti i personaggi dei due gruppi, anche gli
 * ospiti vulnerabili, i caduti e chi è in panchina. I miei e gli avversari si alternano distribuendo i posti in proporzione: il
 * personaggio <i>i</i> di un gruppo di <i>n</i> sta alla posizione <i>(i + 1/2)/n</i>, e a parità di posizione viene prima il mio gruppo; la
 * lista, circolare, parte dal mio primo personaggio. Con gruppi pari sono le coppie (un mio, un avversario...); con 4 contro 1 esce
 * M M T M M, con 2 contro 5 M T T T M T T, così gli ultimi non sono tutti dello stesso gruppo. Un <b>cursore</b> dice a chi tocca: chi non può agire (morto, in panchina, un ospite che non combatte, chi ha già agito fuori
 * turno) è saltato, e questo vale anche se cade durante il giro. Il giro completo della lista è un round: finito, ne comincia un altro (con
 * un {@link InternoInizioRound}); il suo numero riparte da 1 a ogni inizio locazione e continua con le ondate. Chi arriva a
 * combattimento in corso (lo Scheletro della Mietitura) va in fondo alla lista;</li>
 * <li>chi ha già agito nel round (di entrambi i gruppi);</li>
 * <li>per ogni personaggio una <b>scheda</b> (indicizzata per identità, e non dimenticata quando cade: un compagno potrebbe
 * farlo risorgere): l'ultimo avversario e l'ultimo alleato scelti, chi lo ha attaccato per ultimo, se ha già lanciato un
 * incantesimo, i cambi d'arma, le voci della tattica già usate (e in quale round) e le armi già valutate;</li>
 * <li>la <b>conoscenza sulle reazioni</b>, che invece è di <b>pubblico dominio</b> e non è legata a nessuno: in combattimento
 * tutti vedono che cosa succede, nel bene o nel male, anche di sfuggita. Per ogni coppia (personaggio colpito, tipo di danno)
 * si sa di quanto l'equipaggiamento difensivo ha cambiato il danno (il resto, classe e stati, lo si prevede già con
 * {@code CalcolatoreCombattimento.moltiplicatoreAtteso}).</li>
 * </ul>
 * Chi lo aggiorna è il motore (i colpi li registra {@code PersonaggioBase.applicaRisultatoCombattimento}); le tattiche lo leggono e basta.
 */
public class StatoDelCombattimento {

	private static StatoDelCombattimento istanza;

	public static StatoDelCombattimento getIstanza() {
		if (istanza == null) {
			istanza = new StatoDelCombattimento();
		}
		return istanza;
	}

	/**
	 * Dimentica lo stato corrente. Serve ai test (vedi GruppoGiocatore.azzeraIstanza).
	 */
	public static void azzeraIstanza() {
		istanza = null;
	}

	/** I due lati del combattimento: il gruppo del giocatore e quello degli avversari */
	public enum Lato {
		GIOCATORE, AVVERSARI
	}

	private int round = 1;
	// La lista dei turni, che si costruisce quando serve (null: da costruire), e il posto a cui tocca (vedi getOrdineDelRound)
	private List<Personaggio> lista;
	private int cursore;
	private final Set<Personaggio> hannoAgitoNelRound = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<Personaggio, Scheda> schede = new IdentityHashMap<>();
	private final Map<Personaggio, Map<TipoDanno, Double>> reazioniNote = new IdentityHashMap<>();
	private final Map<Personaggio, Set<TipoAzione>> immunitaScoperte = new IdentityHashMap<>();

	/** Che cosa si ricorda di un personaggio */
	private static final class Scheda {
		private Personaggio ultimoAvversario;
		private Personaggio ultimoAlleato;
		private Personaggio ultimoAttaccante;
		private boolean haLanciato;
		private int cambiDArma;
		private final Map<String, Integer> vociUsate = new HashMap<>();
		private final Map<String, Integer> usiDelleVoci = new HashMap<>();
		private final Map<Personaggio, Map<TipoAzione, Integer>> tentativiFalliti = new IdentityHashMap<>();
		private final Map<Artefatto, Boolean> armiValutate = new IdentityHashMap<>();
	}

	private StatoDelCombattimento() {
	}

	/**
	 * Comincia una locazione: il round torna a 1 (e lo si annuncia), e si dimentica tutto il resto.
	 */
	public void reimposta() {
		round = 1;
		lista = null;
		cursore = 0;
		hannoAgitoNelRound.clear();
		schede.clear();
		reazioniNote.clear();
		immunitaScoperte.clear();
		BusEventi.pubblica(new InternoInizioRound(round));
	}

	public int getRound() {
		return round;
	}

	/**
	 * Dice se il personaggio ha già agito in questo round.
	 */
	public boolean haAgitoNelRound(Personaggio personaggio) {
		return hannoAgitoNelRound.contains(personaggio);
	}

	/** Un posto della lista mentre la si distribuisce: il personaggio e la sua posizione, come frazione (indice + 1/2)/dimensione del suo gruppo */
	private static final class Posto {
		private final Personaggio personaggio;
		private final int indice;
		private final int dimensione;
		private final int lato;

		private Posto(Personaggio personaggio, int indice, int dimensione, int lato) {
			this.personaggio = personaggio;
			this.indice = indice;
			this.dimensione = dimensione;
			this.lato = lato;
		}
	}

	/**
	 * I personaggi del gruppo che vanno in lista: quelli del gruppo e gli ospiti vulnerabili vivi (gli ospiti che non si possono colpire
	 * non compaiono).
	 */
	private static List<Personaggio> daMettereInLista(Gruppo gruppo) {
		List<Personaggio> elenco = new ArrayList<>(gruppo.getPersonaggi());
		for (Personaggio ospite : gruppo.getOspitiBersaglio()) {
			if (!elenco.contains(ospite)) {
				elenco.add(ospite);
			}
		}
		return elenco;
	}

	/**
	 * Quelli che fanno ancora parte di un gruppo: anche gli ospiti caduti, che restano in lista (grigi) finché il combattimento dura.
	 */
	private static List<Personaggio> chiFaParte(Gruppo gruppo) {
		List<Personaggio> elenco = new ArrayList<>(gruppo.getPersonaggi());
		elenco.addAll(gruppo instanceof GruppoGiocatore ? ((GruppoGiocatore) gruppo).getOspiti() : gruppo.getOspitiBersaglio());
		return elenco;
	}

	/**
	 * La lista dei turni: i miei e gli avversari distribuiti in proporzione (vedi la descrizione della classe). A parità di posizione
	 * viene prima il mio gruppo, e chi è in un gruppo conserva l'ordine che ha lì. La lista è circolare e parte dal mio primo personaggio.
	 */
	private static List<Personaggio> distribuisci(List<Personaggio> miei, List<Personaggio> loro) {
		List<Posto> posti = new ArrayList<>();
		for (int i = 0; i < miei.size(); i++) {
			posti.add(new Posto(miei.get(i), i, miei.size(), 0));
		}
		for (int i = 0; i < loro.size(); i++) {
			posti.add(new Posto(loro.get(i), i, loro.size(), 1));
		}
		// (indice + 1/2)/dimensione, cioè (2 * indice + 1)/(2 * dimensione), si confronta con il prodotto in croce, senza arrotondamenti
		posti.sort((a, b) -> {
			int confronto = Long.compare((2L * a.indice + 1) * b.dimensione, (2L * b.indice + 1) * a.dimensione);
			return confronto != 0 ? confronto : Integer.compare(a.lato, b.lato);
		});
		List<Personaggio> risultato = new ArrayList<>();
		for (Posto posto : posti) {
			risultato.add(posto.personaggio);
		}
		// Si parte dal mio primo personaggio (con le posizioni centrate qualche avversario può stare prima di lui)
		if (!miei.isEmpty()) {
			Collections.rotate(risultato, -risultato.indexOf(miei.get(0)));
		}
		return risultato;
	}

	/**
	 * Costruisce la lista se manca; se c'è, toglie chi ha lasciato i gruppi e mette in fondo chi è arrivato.
	 */
	private void assicuraLaLista() {
		Gruppo miei = GruppoGiocatore.getIstanza();
		Gruppo loro = GruppoAvversario.getIstanza();
		if (lista == null) {
			lista = distribuisci(daMettereInLista(miei), daMettereInLista(loro));
			cursore = 0;
			return;
		}
		List<Personaggio> presenti = chiFaParte(miei);
		presenti.addAll(chiFaParte(loro));
		for (int i = lista.size() - 1; i >= 0; i--) {
			if (!presenti.contains(lista.get(i))) {
				lista.remove(i);
				if (i < cursore) {
					cursore--;
				}
			}
		}
		for (Personaggio personaggio : daMettereInLista(miei)) {
			if (!lista.contains(personaggio)) {
				lista.add(personaggio);
			}
		}
		for (Personaggio personaggio : daMettereInLista(loro)) {
			if (!lista.contains(personaggio)) {
				lista.add(personaggio);
			}
		}
	}

	/**
	 * Se il personaggio, adesso, può agire: combatte (è del gruppo, non un ospite), è vivo e non in panchina, e nel giro non ha già agito.
	 */
	private boolean puoAgire(Personaggio personaggio) {
		return !personaggio.isFuoriCombattimento() && !hannoAgitoNelRound.contains(personaggio) && combatte(personaggio);
	}

	private static boolean combatte(Personaggio personaggio) {
		return GruppoAvversario.getIstanza().getPersonaggi().contains(personaggio)
				|| GruppoGiocatore.getIstanza().getPersonaggi().contains(personaggio);
	}

	private static Lato latoDi(Personaggio personaggio) {
		return GruppoAvversario.getIstanza().getPersonaggi().contains(personaggio) ? Lato.AVVERSARI : Lato.GIOCATORE;
	}

	/**
	 * Porta il cursore sul primo posto di chi può agire: i posti degli altri sono consumati senza che succeda niente.
	 *
	 * @return false se nel giro non resta nessuno che possa agire
	 */
	private boolean saltaChiNonPuoAgire() {
		while (cursore < lista.size() && !puoAgire(lista.get(cursore))) {
			cursore++;
		}
		return cursore < lista.size();
	}

	/**
	 * Quanti personaggi di un gruppo sono in grado di agire (vivi e non in panchina)
	 */
	private static int quantiAgiscono(Gruppo gruppo) {
		int quanti = 0;
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (!personaggio.isFuoriCombattimento()) {
				quanti++;
			}
		}
		return quanti;
	}

	/**
	 * Tutti i personaggi dei due gruppi nell'ordine dei turni (vedi la descrizione della classe), che vale per tutto il combattimento o
	 * fino alla prossima ondata: chi cade o va in panchina resta al suo posto, e semplicemente non agisce. È l'ordine che mostra anche
	 * il riquadro del combattimento.
	 */
	public List<Personaggio> getOrdineDelRound() {
		assicuraLaLista();
		return Collections.unmodifiableList(new ArrayList<>(lista));
	}

	/**
	 * Un'altra ondata di avversari: la lista dei turni si rifà con i nuovi arrivati e un nuovo giro comincia dal primo. Il numero del round
	 * non cambia.
	 */
	public void nuovaOndata() {
		lista = null;
		cursore = 0;
		hannoAgitoNelRound.clear();
	}

	/**
	 * A chi tocca adesso: il lato del prossimo personaggio che può agire (i posti di chi non può sono consumati).
	 *
	 * @return il lato che deve agire, oppure null se nel giro non resta nessuno in grado di farlo
	 */
	public Lato getProssimoLato() {
		assicuraLaLista();
		return saltaChiNonPuoAgire() ? latoDi(lista.get(cursore)) : null;
	}

	/**
	 * Il personaggio a cui tocca, se è di quel gruppo; altrimenti null (anche se nel giro non resta nessuno in grado di agire). Non
	 * consuma il posto: lo fa {@link #registraAzione}.
	 */
	public Personaggio getProssimoAttaccante(Gruppo gruppo) {
		assicuraLaLista();
		if (!saltaChiNonPuoAgire()) {
			return null;
		}
		Personaggio personaggio = lista.get(cursore);
		return gruppo.getPersonaggi().contains(personaggio) ? personaggio : null;
	}

	/**
	 * Il primo avversario in grado di agire a partire dal cursore, senza consumare niente: serve a chi attacca per primo fuori dal giro
	 * (un passaggio inosservato fallito).
	 */
	public Personaggio getAvversarioCheAttaccaPerPrimo() {
		assicuraLaLista();
		for (int k = 0; k < lista.size(); k++) {
			Personaggio candidato = lista.get((cursore + k) % lista.size());
			if (latoDi(candidato) == Lato.AVVERSARI && !candidato.isFuoriCombattimento() && combatte(candidato)) {
				return candidato;
			}
		}
		return null;
	}

	/**
	 * La lista dei lati, un posto per personaggio, per i test e il log.
	 */
	public List<Lato> getPiano() {
		assicuraLaLista();
		List<Lato> lati = new ArrayList<>();
		for (Personaggio personaggio : lista) {
			lati.add(latoDi(personaggio));
		}
		return Collections.unmodifiableList(lati);
	}

	/**
	 * Registra che il personaggio ha agito. Se toccava a lui il cursore avanza. Se agisce fuori turno (un'azione scelta dal giocatore) per
	 * la prima volta nel giro, il cursore resta dov'è e il suo posto, quando arriverà, sarà saltato: nessuno perde il turno. Se agisce
	 * fuori turno una seconda volta nel giro, l'azione costa il posto a cui tocca, che passa. Se con questa azione il giro è finito, ne
	 * comincia un altro.
	 *
	 * @return true se con questa azione il round è finito
	 */
	public boolean registraAzione(Personaggio personaggio) {
		if (personaggio == null) {
			return false;
		}
		boolean giaAgito = !hannoAgitoNelRound.add(personaggio);
		if (quantiAgiscono(GruppoAvversario.getIstanza()) == 0) {
			// Senza avversari il combattimento è finito: il round non avanza
			return false;
		}
		assicuraLaLista();
		if (cursore < lista.size() && (lista.get(cursore).equals(personaggio) || giaAgito)) {
			// Toccava a lui; oppure agisce di nuovo nello stesso giro (un'azione del giocatore): costa il posto a cui tocca, altrimenti
			// il giocatore potrebbe agire all'infinito senza che gli avversari rispondano
			cursore++;
		}
		return chiudeIlGiroSeFinito();
	}

	/**
	 * Registra che a quel lato toccava ma nessuno era in grado di agire: il posto è consumato lo stesso.
	 *
	 * @return true se con questo il round è finito
	 */
	public boolean registraAzioneSaltata(Lato lato) {
		if (quantiAgiscono(GruppoAvversario.getIstanza()) == 0) {
			return false;
		}
		assicuraLaLista();
		if (cursore < lista.size()) {
			cursore++;
		}
		return chiudeIlGiroSeFinito();
	}

	/**
	 * Consuma i posti di chi non può agire che seguono il cursore, e se la lista è finita comincia un altro round.
	 */
	private boolean chiudeIlGiroSeFinito() {
		if (saltaChiNonPuoAgire()) {
			return false;
		}
		round++;
		hannoAgitoNelRound.clear();
		cursore = 0;
		BusEventi.pubblica(new InternoInizioRound(round));
		return true;
	}

	/**
	 * Il giocatore sceglie chi dei suoi agisce per primo. A giro non cominciato la lista riparte da lui (e il primo avversario che segue
	 * è quello che viene dopo di lui nella lista); a giro avviato scambia il suo posto con quello del prossimo dei miei che deve agire.
	 */
	public void faAgirePerPrimo(Gruppo gruppo, Personaggio personaggio) {
		assicuraLaLista();
		int posizione = lista.indexOf(personaggio);
		if (posizione < 0) {
			return;
		}
		if (cursore == 0 && hannoAgitoNelRound.isEmpty()) {
			Collections.rotate(lista, -posizione);
			return;
		}
		if (!puoAgire(personaggio)) {
			return;
		}
		for (int i = cursore; i < lista.size(); i++) {
			Personaggio candidato = lista.get(i);
			if (puoAgire(candidato) && latoDi(candidato) == Lato.GIOCATORE) {
				Collections.swap(lista, i, posizione);
				return;
			}
		}
	}

	private Scheda scheda(Personaggio personaggio) {
		return schede.computeIfAbsent(personaggio, p -> new Scheda());
	}

	/**
	 * Registra un colpo andato a segno: ricorda chi ha colpito chi (l'ultimo avversario di chi attacca e l'ultimo attaccante di chi
	 * è colpito) e che cosa si è visto della sua reazione al tipo di danno.
	 */
	public void registraColpo(DannoRisultante risultato) {
		Personaggio attaccante = risultato.getAttaccante();
		Personaggio difensore = risultato.getDifensore();
		if (difensore == null) {
			return;
		}
		if (attaccante != null && attaccante != difensore) {
			scheda(attaccante).ultimoAvversario = difensore;
			scheda(difensore).ultimoAttaccante = attaccante;
		}
		if (risultato.getTipoDanno() != null) {
			registraReazione(difensore, risultato.getTipoDanno(), risultato.getFattoreEquipaggiamento());
		}
	}

	/** L'ultimo avversario scelto (colpito) da questo personaggio, o null; se è ancora valido lo decide chi lo legge */
	public Personaggio getUltimoAvversario(Personaggio personaggio) {
		Scheda scheda = schede.get(personaggio);
		return scheda == null ? null : scheda.ultimoAvversario;
	}

	/** Registra l'alleato scelto da questo personaggio (per una cura, una resurrezione, una pozione) */
	public void registraAlleato(Personaggio personaggio, Personaggio alleato) {
		scheda(personaggio).ultimoAlleato = alleato;
	}

	public Personaggio getUltimoAlleato(Personaggio personaggio) {
		Scheda scheda = schede.get(personaggio);
		return scheda == null ? null : scheda.ultimoAlleato;
	}

	/** Chi ha agito contro questo personaggio per ultimo, o null */
	public Personaggio getUltimoAttaccante(Personaggio personaggio) {
		Scheda scheda = schede.get(personaggio);
		return scheda == null ? null : scheda.ultimoAttaccante;
	}

	/** Registra che il personaggio ha lanciato un incantesimo o cantato (la regola del "magico") */
	public void registraIncantesimoLanciato(Personaggio personaggio) {
		scheda(personaggio).haLanciato = true;
	}

	/**
	 * Dice se il personaggio ha già lanciato un incantesimo in questo combattimento.
	 */
	public boolean haLanciatoUnIncantesimo(Personaggio personaggio) {
		Scheda scheda = schede.get(personaggio);
		return scheda != null && scheda.haLanciato;
	}

	/** Registra un cambio d'arma; le valutazioni delle armi del personaggio non valgono più (carico e mani sono cambiati) e si scartano */
	public void registraCambioArma(Personaggio personaggio) {
		Scheda scheda = scheda(personaggio);
		scheda.cambiDArma++;
		scheda.armiValutate.clear();
	}

	public int getCambiDArma(Personaggio personaggio) {
		Scheda scheda = schede.get(personaggio);
		return scheda == null ? 0 : scheda.cambiDArma;
	}

	/** Registra che il personaggio ha usato una voce della sua tattica, nel round corrente */
	public void registraUsoVoce(Personaggio personaggio, String voce) {
		Scheda scheda = scheda(personaggio);
		scheda.vociUsate.put(voce, round);
		scheda.usiDelleVoci.merge(voce, 1, Integer::sum);
	}

	/** Quante volte il personaggio ha usato la voce in questo combattimento */
	public int getUsiDellaVoce(Personaggio personaggio, String voce) {
		Scheda scheda = schede.get(personaggio);
		Integer usi = scheda == null ? null : scheda.usiDelleVoci.get(voce);
		return usi == null ? 0 : usi;
	}

	/**
	 * Dice se il personaggio ha già usato quella voce della sua tattica.
	 */
	public boolean haUsatoLaVoce(Personaggio personaggio, String voce) {
		return getRoundUltimoUsoVoce(personaggio, voce) > 0;
	}

	/** Il round in cui il personaggio ha usato la voce per l'ultima volta, o 0 se non l'ha ancora usata */
	public int getRoundUltimoUsoVoce(Personaggio personaggio, String voce) {
		Scheda scheda = schede.get(personaggio);
		Integer usata = scheda == null ? null : scheda.vociUsate.get(voce);
		return usata == null ? 0 : usata;
	}

	/**
	 * Registra com'è andata un'azione di un personaggio contro un bersaglio (vedi EsitoAzione): una riuscita azzera i tentativi falliti, una
	 * mancata li aumenta, un'immunità o resistenza scoperta si ricorda per tutti (conoscenza pubblica, come quella sulle reazioni).
	 */
	public void registraEsitoAzione(Personaggio attore, Personaggio bersaglio, TipoAzione azione, EsitoAzione esito) {
		Map<TipoAzione, Integer> falliti = scheda(attore).tentativiFalliti.computeIfAbsent(bersaglio, b -> new EnumMap<>(TipoAzione.class));
		switch (esito) {
			case RIUSCITA:
				falliti.remove(azione);
				break;
			case MANCATA:
				falliti.merge(azione, 1, Integer::sum);
				break;
			default:
				immunitaScoperte.computeIfAbsent(bersaglio, b -> EnumSet.noneOf(TipoAzione.class)).add(azione);
				break;
		}
	}

	/** Quante volte di fila l'attore ha fallito per caso quell'azione contro quel bersaglio */
	public int getTentativiFalliti(Personaggio attore, Personaggio bersaglio, TipoAzione azione) {
		Scheda scheda = schede.get(attore);
		Map<TipoAzione, Integer> falliti = scheda == null ? null : scheda.tentativiFalliti.get(bersaglio);
		Integer volte = falliti == null ? null : falliti.get(azione);
		return volte == null ? 0 : volte;
	}

	/** Se si è scoperto che il bersaglio è immune a quell'azione o le resiste (lo sa chiunque) */
	public boolean isImmunitaScoperta(Personaggio bersaglio, TipoAzione azione) {
		Set<TipoAzione> scoperte = immunitaScoperte.get(bersaglio);
		return scoperte != null && scoperte.contains(azione);
	}

	/** Registra che il personaggio ha già valutato quell'arma, e se può usarla */
	public void registraArmaValutata(Personaggio personaggio, Artefatto arma, boolean utilizzabile) {
		scheda(personaggio).armiValutate.put(arma, utilizzabile);
	}

	/** Se il personaggio ha valutato l'arma, se è utilizzabile; null se non l'ha ancora valutata */
	public Boolean getValutazioneArma(Personaggio personaggio, Artefatto arma) {
		Scheda scheda = schede.get(personaggio);
		return scheda == null ? null : scheda.armiValutate.get(arma);
	}

	/**
	 * Registra che si è visto un colpo di quel tipo di danno sul personaggio: <code>fattoreEquipaggiamento</code> è il rapporto fra il danno
	 * subito e quello che avrebbe subito senza l'equipaggiamento difensivo (1 se non conta, meno di 1 se lo protegge). È un dato
	 * pubblico: non dipende da chi ha colpito né da chi guarda. L'ultima osservazione sostituisce la precedente.
	 */
	public void registraReazione(Personaggio colpito, TipoDanno tipoDanno, double fattoreEquipaggiamento) {
		reazioniNote.computeIfAbsent(colpito, p -> new EnumMap<>(TipoDanno.class)).put(tipoDanno, fattoreEquipaggiamento);
	}

	/** Se si è già visto che cosa fa quel tipo di danno a quel personaggio */
	public boolean isReazioneNota(Personaggio colpito, TipoDanno tipoDanno) {
		Map<TipoDanno, Double> reazioni = reazioniNote.get(colpito);
		return reazioni != null && reazioni.containsKey(tipoDanno);
	}

	/** Il fattore dell'equipaggiamento visto per quel tipo di danno; 1 se non si è ancora visto niente */
	public double getFattoreEquipaggiamentoNoto(Personaggio colpito, TipoDanno tipoDanno) {
		Map<TipoDanno, Double> reazioni = reazioniNote.get(colpito);
		Double fattore = reazioni == null ? null : reazioni.get(tipoDanno);
		return fattore == null ? 1.0d : fattore;
	}
}
