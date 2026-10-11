package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.FiltroMD;
import com.threeamigos.foresta.modellodati.tattiche.SelettoreMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applica un {@link SelettoreMD} (tattiche_di_combattimento_automatiche.md §11.2): la rosa, i filtri in AND e l'ordinamento che sceglie uno fra i
 * rimasti. Non cambia niente; tira il dado solo con l'ordinamento A_CASO.
 */
public final class Selettori {

	private Selettori() {
	}

	/**
	 * Quali azioni si possono fare su un candidato: per ogni candidato della rosa l'azione della voce che si farebbe su di lui (la prima
	 * applicabile), o null se nessuna si può fare e il candidato non entra nel selettore
	 */
	public interface Ammissione {
		TipoAzione azioneSu(Personaggio candidato);
	}

	/**
	 * Applica la rosa, i filtri e l'ordinamento.
	 */
	public static RisultatoSelezione seleziona(SelettoreMD selettore, ContestoTattico contesto) {
		return seleziona(selettore, contesto, null);
	}

	/**
	 * Come {@link #seleziona(SelettoreMD, ContestoTattico)}, ma restano nella rosa solo i candidati su cui un'azione della voce si può fare
	 * ({@link Ammissione}), e i filtri che dipendono dall'azione (DEBOLE_ALL_AZIONE) la valutano per quella che si farebbe su ciascuno
	 */
	public static RisultatoSelezione seleziona(SelettoreMD selettore, ContestoTattico contesto, Ammissione ammissione) {
		List<String> passi = new ArrayList<>();
		List<Personaggio> candidati = Rose.di(selettore.getRosa()).candidati(contesto);
		passi.add(selettore.getRosa() + ": " + candidati.size() + nomi(candidati));
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
			passi.add("azione applicabile: " + candidati.size() + nomi(candidati));
		}
		for (FiltroMD filtro : selettore.getFiltri()) {
			List<Personaggio> rimasti = new ArrayList<>();
			for (Personaggio candidato : candidati) {
				ContestoTattico perIlCandidato = azioni.containsKey(candidato) ? contesto.conAzione(azioni.get(candidato)) : contesto;
				if (Filtri.ammette(filtro, candidato, perIlCandidato)) {
					rimasti.add(candidato);
				}
			}
			candidati = rimasti;
			passi.add(filtro + ": " + candidati.size() + nomi(candidati));
		}
		Personaggio scelto = Ordinamenti.scegli(candidati, selettore.getOrdinamento(), contesto);
		String misure = Ordinamenti.descriviLeMisure(candidati, selettore.getOrdinamento());
		passi.add(selettore.getOrdinamento() + (misure.isEmpty() ? "" : " (" + misure + ")") + ": " + (scelto == null ? "nessuno" : scelto.getNome()));
		return new RisultatoSelezione(scelto, candidati, passi);
	}

	/**
	 * I nomi dei candidati rimasti, per il registro delle decisioni
	 */
	private static String nomi(List<Personaggio> candidati) {
		StringBuilder sb = new StringBuilder(" [");
		for (Personaggio candidato : candidati) {
			if (sb.length() > 2) {
				sb.append(", ");
			}
			sb.append(candidato.getNome());
		}
		return sb.append(']').toString();
	}
}
