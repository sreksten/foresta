package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.missioni.SetLeggendario;
import com.threeamigos.foresta.motore.tipi.TipoSlotArtefatto;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Tutti i set leggendari di leggendari.txt e i loro pezzi: i pezzi di un set sono le righe degli oggetti che lo
 * nominano in SET=, quanti che siano, anche quelli che nessuna leggenda ha ancora pescato. Si legge una volta sola
 * (la grammatica non cambia durante il gioco) e si controlla che ogni set si possa completare: almeno due pezzi, da
 * indossare tutti insieme.
 */
public final class CatalogoLeggendari {

	/**
	 * Gli slot in cui sta un solo pezzo per personaggio.
	 */
	private static final Set<TipoSlotArtefatto> SLOT_SINGOLI = EnumSet.of(TipoSlotArtefatto.TESTA, TipoSlotArtefatto.VOLTO,
			TipoSlotArtefatto.CORPO, TipoSlotArtefatto.GAMBE, TipoSlotArtefatto.MANO_PRINCIPALE, TipoSlotArtefatto.MANO_SECONDARIA);

	private static Map<String, SetLeggendario> set;
	private static Map<String, Set<String>> pezzi;

	private CatalogoLeggendari() {
	}

	/**
	 * Il set con quella chiave.
	 */
	public static Optional<SetLeggendario> getSet(String chiave) {
		carica();
		return Optional.ofNullable(set.get(chiave));
	}

	/**
	 * I pezzi del set, con il loro nome breve; vuoto per un set che non c'è.
	 */
	public static Set<String> getPezzi(String chiave) {
		carica();
		return pezzi.getOrDefault(chiave, Collections.emptySet());
	}

	/**
	 * Tutti i set, per chiave.
	 */
	public static Map<String, SetLeggendario> getTuttiISet() {
		carica();
		return set;
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
		Map<String, Set<String>> pezziDeiSet = new HashMap<>();
		Map<String, Set<TipoSlotArtefatto>> slotDeiSet = new HashMap<>();
		for (String riga : ProduttoreDiTestiCasuale.tuttiGliOggettiLeggendari()) {
			OggettoLeggendario leggendario = OggettoLeggendario.da(riga);
			String chiave = leggendario.getSet();
			if (chiave == null) {
				continue;
			}
			if (!tuttiISet.containsKey(chiave)) {
				throw new IllegalStateException(leggendario.getNomeBreve() + " è di un set che non c'è: " + chiave);
			}
			pezziDeiSet.computeIfAbsent(chiave, k -> new LinkedHashSet<>()).add(leggendario.getNomeBreve());
			aggiungiSlot(slotDeiSet.computeIfAbsent(chiave, k -> EnumSet.noneOf(TipoSlotArtefatto.class)), leggendario);
		}
		for (String chiave : tuttiISet.keySet()) {
			if (pezziDeiSet.getOrDefault(chiave, Collections.emptySet()).size() < 2) {
				throw new IllegalStateException("Il set " + chiave + " ha meno di due pezzi");
			}
			pezziDeiSet.put(chiave, Collections.unmodifiableSet(pezziDeiSet.get(chiave)));
		}
		set = Collections.unmodifiableMap(tuttiISet);
		pezzi = Collections.unmodifiableMap(pezziDeiSet);
	}

	/**
	 * Aggiunge lo slot del leggendario a quelli già occupati dal suo set, controllando che si possano indossare
	 * insieme: niente pergamene e simili, che non si indossano, e un solo pezzo per slot (l'arma a due mani le
	 * occupa entrambe); gli accessori sono quanti si vuole.
	 */
	private static void aggiungiSlot(Set<TipoSlotArtefatto> occupati, OggettoLeggendario leggendario) {
		TipoSlotArtefatto slot = leggendario.getTipo().getSlotArtefatto();
		String errore = leggendario.getNomeBreve() + " non si può indossare con gli altri pezzi del set " + leggendario.getSet();
		if (slot == TipoSlotArtefatto.NUCLEO) {
			throw new IllegalStateException(leggendario.getNomeBreve() + " non si indossa: non può far parte del set " + leggendario.getSet());
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
