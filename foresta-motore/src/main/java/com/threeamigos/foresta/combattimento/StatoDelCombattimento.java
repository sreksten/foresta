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
 * <li>il <b>piano del round</b>: a inizio round si contano i personaggi in grado di agire dei due gruppi (<i>n</i> i miei, <i>m</i> gli
 * avversari) e si fanno max(<i>n</i>, <i>m</i>) coppie, alla coppia <i>k</i> agisce il mio <i>k</i>-esimo e poi l'avversario <i>k</i>-esimo;
 * chi non ha un compagno nell'altro gruppo agisce da solo, di seguito. Il piano è la sequenza dei lati ({@link Lato}), non dei
 * personaggi: chi agisce davvero lo decide il round-robin di ogni gruppo al momento. Il round finisce quando il piano è finito
 * (e ne comincia un altro, con un {@link InternoInizioRound}); il suo numero riparte da 1 a ogni inizio locazione e continua con le ondate;</li>
 * <li>chi ha già agito nel round (di entrambi i gruppi);</li>
 * <li>per ogni gruppo, il <b>puntatore</b> al personaggio che ha agito per ultimo, per il round-robin. Si ricorda il
 * personaggio e non solo la sua posizione, perché la lista di un gruppo cambia durante lo scontro;</li>
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

	/** L'ultimo personaggio che ha agito in ogni gruppo, e dov'era nella lista */
	private static final class Puntatore {
		private Personaggio personaggio;
		private int posizione = -1;
	}

	/** I due lati del combattimento: il gruppo del giocatore e quello degli avversari */
	public enum Lato {
		GIOCATORE, AVVERSARI
	}

	private int round = 1;
	// Il piano del round, che si costruisce quando serve (null: da costruire) e quanti posti ne sono già stati usati
	private List<Lato> piano;
	private int postiUsati;
	private final Set<Personaggio> hannoAgitoNelRound = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<Gruppo, Puntatore> puntatori = new IdentityHashMap<>();
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
		piano = null;
		postiUsati = 0;
		hannoAgitoNelRound.clear();
		puntatori.clear();
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

	/**
	 * Il prossimo personaggio del gruppo che deve agire, in round-robin: dopo l'ultimo che ha agito (la prima volta, il
	 * primo), saltando chi è caduto o in panchina. Il puntatore del gruppo avanza a ogni chiamata.
	 *
	 * @return il personaggio, oppure null se nel gruppo non ce n'è nessuno in grado di agire
	 */
	public Personaggio getProssimoAttaccante(Gruppo gruppo) {
		List<Personaggio> personaggi = gruppo.getPersonaggi();
		int dimensione = personaggi.size();
		if (gruppo.getPersonaggiVivi().isEmpty()) {
			Logger.log("[ROUND-ROBIN] Tutti i personaggi del gruppo sono stati sconfitti.");
			return null;
		}
		Puntatore puntatore = puntatori.computeIfAbsent(gruppo, g -> new Puntatore());
		int partenza = posizioneDiPartenza(puntatore, personaggi);
		for (int i = 1; i <= dimensione; i++) {
			int posizione = (partenza + i) % dimensione;
			Personaggio candidato = personaggi.get(posizione);
			if (candidato.isFuoriCombattimento()) {
				continue;
			}
			puntatore.personaggio = candidato;
			puntatore.posizione = posizione;
			return candidato;
		}
		return null;
	}

	/**
	 * Da dove riparte il giro: dal personaggio che ha agito per ultimo; se non è più nella lista (è uscito dal gruppo) dalla
	 * posizione in cui era, che ora è occupata dal suo successore.
	 */
	private static int posizioneDiPartenza(Puntatore puntatore, List<Personaggio> personaggi) {
		if (puntatore.personaggio == null) {
			return -1;
		}
		for (int i = 0; i < personaggi.size(); i++) {
			if (personaggi.get(i) == puntatore.personaggio) {
				return i;
			}
		}
		return Math.min(puntatore.posizione, personaggi.size()) - 1;
	}

	/**
	 * Quanti personaggi di un gruppo sono in grado di agire in un round (vivi e non in panchina)
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

	private void assicuraIlPiano() {
		if (piano != null) {
			return;
		}
		int mieiPersonaggi = quantiAgiscono(GruppoGiocatore.getIstanza());
		int avversariPersonaggi = quantiAgiscono(GruppoAvversario.getIstanza());
		piano = new ArrayList<>();
		postiUsati = 0;
		for (int k = 0; k < Math.max(mieiPersonaggi, avversariPersonaggi); k++) {
			if (k < mieiPersonaggi) {
				piano.add(Lato.GIOCATORE);
			}
			if (k < avversariPersonaggi) {
				piano.add(Lato.AVVERSARI);
			}
		}
	}

	/**
	 * Un'altra ondata di avversari: il piano del round in corso si rifà con i nuovi arrivati. Il numero del round non cambia.
	 */
	public void nuovaOndata() {
		piano = null;
	}

	/**
	 * A chi tocca adesso nel piano del round.
	 *
	 * @return il lato che deve agire, oppure null se nessuno può farlo (un gruppo senza nessuno e l'altro nemmeno)
	 */
	public Lato getProssimoLato() {
		assicuraIlPiano();
		return postiUsati < piano.size() ? piano.get(postiUsati) : null;
	}

	/**
	 * Il piano del round, per i test e il log: i lati nell'ordine in cui agiscono (anche quelli già passati)
	 */
	public List<Lato> getPiano() {
		assicuraIlPiano();
		return Collections.unmodifiableList(piano);
	}

	/**
	 * Registra che il personaggio ha agito, nel gruppo in cui sta. Se è del giocatore il suo gruppo riparte dal successivo
	 * (le azioni manuali scelgono chi agisce). Consuma il prossimo posto del suo lato nel piano; se il piano è finito il round è
	 * finito e ne comincia un altro.
	 *
	 * @return true se con questa azione il round è finito
	 */
	public boolean registraAzione(Personaggio personaggio) {
		if (personaggio == null) {
			return false;
		}
		hannoAgitoNelRound.add(personaggio);
		Lato lato = GruppoAvversario.getIstanza().contiene(personaggio) ? Lato.AVVERSARI : Lato.GIOCATORE;
		if (lato == Lato.GIOCATORE) {
			ricordaChiHaAgito(GruppoGiocatore.getIstanza(), personaggio);
		}
		return consuma(lato);
	}

	/**
	 * Registra che il lato doveva agire ma nessuno ne era in grado: il posto nel piano è usato lo stesso
	 *
	 * @return true se con questo il round è finito
	 */
	public boolean registraAzioneSaltata(Lato lato) {
		return consuma(lato);
	}

	/**
	 * Fa in modo che il prossimo a agire in quel gruppo, nel suo round-robin, sia questo personaggio (il giocatore ha scelto chi comincia).
	 */
	public void faAgirePerPrimo(Gruppo gruppo, Personaggio personaggio) {
		List<Personaggio> personaggi = gruppo.getPersonaggi();
		int posizione = personaggi.indexOf(personaggio);
		if (posizione < 0) {
			return;
		}
		int precedente = (posizione - 1 + personaggi.size()) % personaggi.size();
		Puntatore puntatore = puntatori.computeIfAbsent(gruppo, g -> new Puntatore());
		puntatore.personaggio = personaggi.get(precedente);
		puntatore.posizione = precedente;
	}

	private void ricordaChiHaAgito(Gruppo gruppo, Personaggio personaggio) {
		int posizione = gruppo.getPersonaggi().indexOf(personaggio);
		if (posizione < 0) {
			return;
		}
		Puntatore puntatore = puntatori.computeIfAbsent(gruppo, g -> new Puntatore());
		puntatore.personaggio = personaggio;
		puntatore.posizione = posizione;
	}

	private boolean consuma(Lato lato) {
		if (quantiAgiscono(GruppoAvversario.getIstanza()) == 0) {
			// Senza avversari il combattimento è finito: il round non avanza
			return false;
		}
		assicuraIlPiano();
		int posto = postiUsati;
		while (posto < piano.size() && piano.get(posto) != lato) {
			posto++;
		}
		if (posto >= piano.size()) {
			// Un'azione fuori dal piano (per esempio il lato non ha più posti): non consuma niente
			return false;
		}
		postiUsati = posto + 1;
		if (postiUsati < piano.size()) {
			return false;
		}
		round++;
		hannoAgitoNelRound.clear();
		piano = null;
		BusEventi.pubblica(new InternoInizioRound(round));
		return true;
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
