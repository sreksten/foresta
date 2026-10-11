package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoInizioRound;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Logger;

import java.util.Collections;
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
 * personaggio e non solo la sua posizione, perché la lista di un gruppo cambia durante lo scontro.</li>
 * </ul>
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

	private StatoDelCombattimento() {
	}

	/**
	 * Comincia una locazione: il round torna a 1 (e lo si annuncia), e si dimentica tutto il resto.
	 */
	public void reimposta() {
		round = 1;
		hannoAgitoNelRound.clear();
		puntatori.clear();
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
}
