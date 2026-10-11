package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chi è il bersaglio di una voce: una rosa di candidati, i filtri che li restringono (in AND, l'ordine non conta) e l'ordinamento che sceglie
 * uno fra i rimasti. Se non ne resta nessuno la voce non è eseguibile; per le azioni su un gruppo intero serve solo come test di esistenza.
 * Immutabile. Vedi tattiche_di_combattimento_automatiche.md §11.2.
 */
public final class Selettore {

	private final TipoRosa rosa;
	private final List<Filtro> filtri;
	private final Ordinamento ordinamento;

	public Selettore(TipoRosa rosa, List<Filtro> filtri, Ordinamento ordinamento) {
		this.rosa = rosa;
		this.filtri = Collections.unmodifiableList(new ArrayList<>(filtri));
		this.ordinamento = ordinamento;
	}

	public TipoRosa getRosa() {
		return rosa;
	}

	public List<Filtro> getFiltri() {
		return filtri;
	}

	public Ordinamento getOrdinamento() {
		return ordinamento;
	}

	/**
	 * Quali azioni si possono fare su un candidato: per ogni candidato della rosa l'azione della voce che si farebbe su di lui (la prima
	 * applicabile), o null se nessuna si può fare e il candidato non entra nel selettore
	 */
	public interface Ammissione {
		TipoAzione azioneSu(Personaggio candidato);
	}

	/**
	 * Applica la rosa, i filtri e l'ordinamento. Non cambia niente; tira il dado solo con l'ordinamento A_CASO.
	 */
	public RisultatoSelezione seleziona(ContestoTattico contesto) {
		return seleziona(contesto, null);
	}

	/**
	 * Come {@link #seleziona(ContestoTattico)}, ma restano nella rosa solo i candidati su cui un'azione della voce si può fare
	 * ({@link Ammissione}), e i filtri che dipendono dall'azione (DEBOLE_ALL_AZIONE) la valutano per quella che si farebbe su ciascuno
	 */
	public RisultatoSelezione seleziona(ContestoTattico contesto, Ammissione ammissione) {
		List<String> passi = new ArrayList<>();
		List<Personaggio> candidati = Rose.di(rosa).candidati(contesto);
		passi.add(rosa + ": " + candidati.size());
		Map<Personaggio, TipoAzione> azioni = new IdentityHashMap<>();
		if (ammissione != null) {
			List<Personaggio> applicabili = new ArrayList<>();
			for (Personaggio candidato : candidati) {
				TipoAzione azione = ammissione.azioneSu(candidato);
				if (azione != null) {
					azioni.put(candidato, azione);
					applicabili.add(candidato);
				}
			}
			candidati = applicabili;
			passi.add("azione applicabile: " + candidati.size());
		}
		for (Filtro filtro : filtri) {
			List<Personaggio> rimasti = new ArrayList<>();
			for (Personaggio candidato : candidati) {
				ContestoTattico perIlCandidato = azioni.containsKey(candidato) ? contesto.conAzione(azioni.get(candidato)) : contesto;
				if (Filtri.ammette(filtro, candidato, perIlCandidato)) {
					rimasti.add(candidato);
				}
			}
			candidati = rimasti;
			passi.add(filtro + ": " + candidati.size());
		}
		Personaggio scelto = Ordinamenti.scegli(candidati, ordinamento);
		passi.add(ordinamento + ": " + (scelto == null ? "nessuno" : scelto.getNome()));
		return new RisultatoSelezione(scelto, candidati, passi);
	}

	@Override
	public String toString() {
		return "SU " + rosa + (filtri.isEmpty() ? "" : " " + filtri) + " ordine " + ordinamento;
	}
}
