package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoInizioRound;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.strumenti.Logger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * La memoria di un combattimento, che le tattiche leggono (vedi tattiche_di_combattimento_automatiche.md §3.3): non fa
 * parte del modello dati, non si salva (un combattimento non si interrompe mai per salvare) e si azzera a ogni inizio
 * locazione. Per ora tiene:
 * <ul>
 * <li>il <b>numero del round</b>: finisce quando tutti gli avversari vivi hanno agito, così arriva comunque a una fine anche se il
 * giocatore continua con azioni manuali (e fa agire sempre lo stesso personaggio). Riparte da 1 a ogni inizio locazione e
 * continua con le ondate. Ogni round che comincia pubblica un {@link InternoInizioRound};</li>
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
	static void azzeraIstanza() {
		istanza = null;
	}

	/** L'ultimo personaggio che ha agito in ogni gruppo, e dov'era nella lista */
	private static final class Puntatore {
		private Personaggio personaggio;
		private int posizione = -1;
	}

	private int round = 1;
	private final Set<Personaggio> hannoAgitoNelRound = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<Gruppo, Puntatore> puntatori = new IdentityHashMap<>();
	private final Map<Personaggio, Scheda> schede = new IdentityHashMap<>();
	private final Map<Personaggio, Map<TipoDanno, Double>> reazioniNote = new IdentityHashMap<>();

	/** Che cosa si ricorda di un personaggio */
	private static final class Scheda {
		private Personaggio ultimoAvversario;
		private Personaggio ultimoAlleato;
		private Personaggio ultimoAttaccante;
		private boolean haLanciato;
		private int cambiDArma;
		private final Map<String, Integer> vociUsate = new HashMap<>();
		private final Map<Artefatto, Boolean> armiValutate = new IdentityHashMap<>();
	}

	private StatoDelCombattimento() {
	}

	/**
	 * Comincia una locazione: il round torna a 1 (e lo si annuncia), e si dimentica tutto il resto.
	 */
	public void reimposta() {
		round = 1;
		hannoAgitoNelRound.clear();
		puntatori.clear();
		schede.clear();
		reazioniNote.clear();
		BusEventi.pubblica(new InternoInizioRound(round));
	}

	public int getRound() {
		return round;
	}

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
	 * Registra che il personaggio ha agito. Quando tutti gli avversari in grado di agire hanno agito il round è finito e ne
	 * comincia un altro (se non ce ne sono più, il combattimento è finito e il round non avanza).
	 */
	public void registraAzione(Personaggio personaggio) {
		if (personaggio == null) {
			return;
		}
		hannoAgitoNelRound.add(personaggio);
		if (roundCompleto()) {
			round++;
			hannoAgitoNelRound.clear();
			BusEventi.pubblica(new InternoInizioRound(round));
		}
	}

	private boolean roundCompleto() {
		Gruppo avversari = GruppoAvversario.getIstanza();
		return !avversari.getPersonaggiVivi().isEmpty() && tuttiHannoAgito(avversari);
	}

	private boolean tuttiHannoAgito(Gruppo gruppo) {
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (!personaggio.isFuoriCombattimento() && !hannoAgitoNelRound.contains(personaggio)) {
				return false;
			}
		}
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
		scheda(personaggio).vociUsate.put(voce, round);
	}

	public boolean haUsatoLaVoce(Personaggio personaggio, String voce) {
		return getRoundUltimoUsoVoce(personaggio, voce) > 0;
	}

	/** Il round in cui il personaggio ha usato la voce per l'ultima volta, o 0 se non l'ha ancora usata */
	public int getRoundUltimoUsoVoce(Personaggio personaggio, String voce) {
		Scheda scheda = schede.get(personaggio);
		Integer usata = scheda == null ? null : scheda.vociUsate.get(voce);
		return usata == null ? 0 : usata;
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
