package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.missioni.SetLeggendario;
import com.threeamigos.foresta.motore.tipi.TipoSlotArtefatto;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tutti i leggendari e i set leggendari di leggendari.txt, per chiave. I pezzi di un set sono i leggendari che lo
 * nominano in SET=, quanti che siano, anche quelli che nessuna leggenda ha ancora pescato. Si legge una volta sola
 * (la grammatica non cambia durante il gioco) e si controlla che le chiavi dei leggendari siano tutte diverse e che
 * ogni set si possa completare: almeno due pezzi, da indossare tutti insieme.
 */
public final class CatalogoLeggendari {

	/**
	 * Gli slot in cui sta un solo pezzo per personaggio.
	 */
	private static final Set<TipoSlotArtefatto> SLOT_SINGOLI = EnumSet.of(TipoSlotArtefatto.TESTA, TipoSlotArtefatto.VOLTO,
			TipoSlotArtefatto.CORPO, TipoSlotArtefatto.GAMBE, TipoSlotArtefatto.MANO_PRINCIPALE, TipoSlotArtefatto.MANO_SECONDARIA);

	private static Map<String, OggettoLeggendario> leggendari;
	private static Map<String, SetLeggendario> set;
	private static Map<String, Set<String>> pezzi;

	private CatalogoLeggendari() {
	}

	/**
	 * Il leggendario con quella chiave.
	 */
	public static Optional<OggettoLeggendario> getLeggendario(String chiave) {
		carica();
		return Optional.ofNullable(leggendari.get(chiave));
	}

	/**
	 * Tutti i leggendari, per chiave.
	 */
	public static Map<String, OggettoLeggendario> getTuttiILeggendari() {
		carica();
		return leggendari;
	}

	/**
	 * Il set con quella chiave.
	 */
	public static Optional<SetLeggendario> getSet(String chiave) {
		carica();
		return Optional.ofNullable(set.get(chiave));
	}

	/**
	 * Tutti i set, per chiave.
	 */
	public static Map<String, SetLeggendario> getTuttiISet() {
		carica();
		return set;
	}

	/**
	 * Le chiavi dei pezzi del set, in ordine di tipo (le armi, poi lo scudo, l'elmo...); vuoto per un set che non c'è.
	 */
	public static Set<String> getPezzi(String chiave) {
		carica();
		return pezzi.getOrDefault(chiave, Collections.emptySet());
	}

	private static synchronized void carica() {
		if (set != null) {
			return;
		}
		Map<String, SetLeggendario> tuttiISet = new HashMap<>();
		for (String riga : ProduttoreDiTestiCasuale.tuttiISetLeggendari()) {
			SetLeggendario letto = SetLeggendario.da(riga);
			if (tuttiISet.put(letto.getChiave(), letto) != null) {
				throw new IllegalStateException("Set leggendario ripetuto: " + letto.getChiave());
			}
		}
		Map<String, OggettoLeggendario> tuttiILeggendari = new HashMap<>();
		Map<String, Set<String>> pezziDeiSet = new HashMap<>();
		Map<String, Set<TipoSlotArtefatto>> slotDeiSet = new HashMap<>();
		for (String riga : ProduttoreDiTestiCasuale.tuttiGliOggettiLeggendari()) {
			OggettoLeggendario leggendario = OggettoLeggendario.da(riga);
			if (tuttiILeggendari.put(leggendario.getChiave(), leggendario) != null) {
				throw new IllegalStateException("Leggendario ripetuto: " + leggendario.getChiave());
			}
			String chiaveSet = leggendario.getSet();
			if (chiaveSet == null) {
				continue;
			}
			if (!tuttiISet.containsKey(chiaveSet)) {
				throw new IllegalStateException(leggendario.getChiave() + " è di un set che non c'è: " + chiaveSet);
			}
			pezziDeiSet.computeIfAbsent(chiaveSet, k -> new LinkedHashSet<>()).add(leggendario.getChiave());
			aggiungiSlot(slotDeiSet.computeIfAbsent(chiaveSet, k -> EnumSet.noneOf(TipoSlotArtefatto.class)), leggendario);
		}
		for (String chiaveSet : tuttiISet.keySet()) {
			if (pezziDeiSet.getOrDefault(chiaveSet, Collections.emptySet()).size() < 2) {
				throw new IllegalStateException("Il set " + chiaveSet + " ha meno di due pezzi");
			}
			// In ordine di tipo, così i testi elencano i pezzi sempre allo stesso modo
			Set<String> inOrdine = pezziDeiSet.get(chiaveSet).stream()
					.sorted(Comparator.comparing((String pezzo) -> tuttiILeggendari.get(pezzo).getTipo().ordinal())
							.thenComparing(pezzo -> pezzo))
					.collect(Collectors.toCollection(LinkedHashSet::new));
			pezziDeiSet.put(chiaveSet, Collections.unmodifiableSet(inOrdine));
		}
		leggendari = Collections.unmodifiableMap(tuttiILeggendari);
		pezzi = Collections.unmodifiableMap(pezziDeiSet);
		set = Collections.unmodifiableMap(tuttiISet);
	}

	/**
	 * Aggiunge lo slot del leggendario a quelli già occupati dal suo set, controllando che si possano indossare
	 * insieme: niente pergamene e simili, che non si indossano, e un solo pezzo per slot (l'arma a due mani le
	 * occupa entrambe); gli accessori sono quanti si vuole.
	 */
	private static void aggiungiSlot(Set<TipoSlotArtefatto> occupati, OggettoLeggendario leggendario) {
		TipoSlotArtefatto slot = leggendario.getTipo().getSlotArtefatto();
		String errore = leggendario.getChiave() + " non si può indossare con gli altri pezzi del set " + leggendario.getSet();
		if (slot == TipoSlotArtefatto.NUCLEO) {
			throw new IllegalStateException(leggendario.getChiave() + " non si indossa: non può far parte del set " + leggendario.getSet());
		}
		if (slot == TipoSlotArtefatto.ENTRAMBE_LE_MANI) {
			if (occupati.contains(TipoSlotArtefatto.MANO_PRINCIPALE) || occupati.contains(TipoSlotArtefatto.MANO_SECONDARIA)
					|| occupati.contains(TipoSlotArtefatto.ENTRAMBE_LE_MANI)) {
				throw new IllegalStateException(errore);
			}
			occupati.add(slot);
			return;
		}
		if ((slot == TipoSlotArtefatto.MANO_PRINCIPALE || slot == TipoSlotArtefatto.MANO_SECONDARIA)
				&& occupati.contains(TipoSlotArtefatto.ENTRAMBE_LE_MANI)) {
			throw new IllegalStateException(errore);
		}
		if (SLOT_SINGOLI.contains(slot) && !occupati.add(slot)) {
			throw new IllegalStateException(errore);
		}
	}
}
