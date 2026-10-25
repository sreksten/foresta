package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.MappaProprieta;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Il bestiario: quali voci si sono sbloccate e quali caratteristiche si sono scoperte, per ogni voce. Una voce è un ruolo
 * (vedi {@link TipoPersonaggio#getRuolo()}): Mago e Maga sono la stessa voce, e le chiedono tutte e due i metodi con la classe che si vuole.
 * Non fa parte di {@link ModelloDati}: non appartiene a una partita ma passa dall'una all'altra, quindi non viene mai reimpostato e si
 * salva in un file a parte, come {@link TrofeiMD}.
 * <p>
 * Le caratteristiche scoperte sono le chiavi di {@code CaratteristicaDiClasse} ("DANNO:FUOCO", "INCANTESIMO:MORTE"), non il loro contenuto: quello
 * lo sa {@link TipoPersonaggio}, e la chiave dice solo che il giocatore l'ha vista.
 *
 * @author Stefano Reksten
 */
public class BestiarioMD implements Serializzabile {

	private final Set<TipoPersonaggio> sbloccate = EnumSet.noneOf(TipoPersonaggio.class);
	private final Map<TipoPersonaggio, Set<String>> scoperte = new EnumMap<>(TipoPersonaggio.class);

	/**
	 * Dice se la voce di questa classe (o di quella che ne condivide il ruolo) è sbloccata.
	 */
	public boolean isSbloccata(TipoPersonaggio classe) {
		return sbloccate.contains(classe.getRuolo());
	}

	/**
	 * Sblocca la voce di questa classe e di quella che ne condivide il ruolo.
	 *
	 * @return true se non lo era già
	 */
	public boolean sblocca(TipoPersonaggio classe) {
		return sbloccate.add(classe.getRuolo());
	}

	/**
	 * Le voci sbloccate, per ruolo, nell'ordine dell'enum.
	 */
	public Set<TipoPersonaggio> getSbloccate() {
		return Collections.unmodifiableSet(sbloccate);
	}

	/**
	 * Le chiavi delle caratteristiche scoperte per questa voce.
	 */
	public Set<String> getScoperte(TipoPersonaggio classe) {
		Set<String> chiavi = scoperte.get(classe.getRuolo());
		return chiavi == null ? Collections.emptySet() : Collections.unmodifiableSet(chiavi);
	}

	public boolean isScoperta(TipoPersonaggio classe, String chiave) {
		return getScoperte(classe).contains(chiave);
	}

	/**
	 * Segna come scoperta una caratteristica della voce di questa classe.
	 *
	 * @return true se non lo era già
	 */
	public boolean scopri(TipoPersonaggio classe, String chiave) {
		return scoperte.computeIfAbsent(classe.getRuolo(), r -> new LinkedHashSet<>()).add(chiave);
	}

	/**
	 * Una riga per voce che ha qualcosa da dire (sbloccata o con caratteristiche scoperte): name() del ruolo, se è sbloccata e le chiavi delle
	 * caratteristiche scoperte, separate da {@value MappaProprieta#SEPARATORE}.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		for (TipoPersonaggio ruolo : TipoPersonaggio.values()) {
			Set<String> chiavi = scoperte.get(ruolo);
			boolean conCaratteristiche = chiavi != null && !chiavi.isEmpty();
			if (sbloccate.contains(ruolo) || conCaratteristiche) {
				stampaRiga(stream,
						ruolo,
						sbloccate.contains(ruolo),
						conCaratteristiche ? String.join(MappaProprieta.SEPARATORE, chiavi) : "");
			}
		}
	}

	/**
	 * Una voce che manca dal file non è sbloccata e non ha scoperte, e una riga di una classe che non esiste più (una costante rinominata o tolta) si
	 * salta: non deve impedire di leggere le altre.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		sbloccate.clear();
		scoperte.clear();
		String riga;
		while ((riga = stream.readLine()) != null) {
			if (riga.isEmpty()) {
				continue;
			}
			LettoreCampi campi = new LettoreCampi(riga);
			TipoPersonaggio ruolo;
			try {
				ruolo = TipoPersonaggio.valueOf(campi.testo());
			} catch (IllegalArgumentException e) {
				// Una classe che il gioco non ha più non rovina il caricamento
				continue;
			}
			if (campi.booleano()) {
				sbloccate.add(ruolo.getRuolo());
			}
			String chiavi = campi.testo();
			if (!chiavi.isEmpty()) {
				for (String chiave : chiavi.split(MappaProprieta.SEPARATORE)) {
					scopri(ruolo, chiave);
				}
			}
		}
	}

}
