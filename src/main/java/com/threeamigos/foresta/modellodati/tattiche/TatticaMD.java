package com.threeamigos.foresta.modellodati.tattiche;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * La tattica di un personaggio: una lista ordinata di voci (nessun tetto), di cui si esegue la prima eseguibile. Immutabile.
 * Vedi tattiche_di_combattimento_automatiche.md §11.
 */
public final class TatticaMD {

	/**
	 * Nessuna voce: si attacca sempre il primo avversario in campo (il ripiego). È la tattica dei miei personaggi finché il giocatore non ne scrive una.
	 */
	public static final TatticaMD VUOTA = new TatticaMD(Collections.emptyList());

	private final List<VoceMD> voci;

	public TatticaMD(List<VoceMD> voci) {
		this.voci = Collections.unmodifiableList(new ArrayList<>(voci));
	}

	public static TatticaMD di(VoceMD... voci) {
		return new TatticaMD(java.util.Arrays.asList(voci));
	}

	public List<VoceMD> getVoci() {
		return voci;
	}

	/**
	 * Gli errori di tutte le voci per quel lato, con il numero della voce (da 1); vuota se la tattica è a posto
	 */
	public List<String> problemi(boolean attoreDelGiocatore) {
		List<String> problemi = new ArrayList<>();
		for (int i = 0; i < voci.size(); i++) {
			for (String problema : voci.get(i).problemi(attoreDelGiocatore)) {
				problemi.add("voce " + (i + 1) + ": " + problema);
			}
		}
		return problemi;
	}

	@Override
	public String toString() {
		return voci.toString();
	}
}
