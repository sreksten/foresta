package com.threeamigos.foresta.tattiche;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * La tattica di un personaggio: una lista ordinata di voci (nessun tetto), di cui si esegue la prima eseguibile. Immutabile.
 * Vedi tattiche_di_combattimento_automatiche.md §11.
 */
public final class Tattica {

	/**
	 * Nessuna voce: si attacca sempre il primo avversario in campo (il ripiego). È la tattica dei miei personaggi finché il giocatore non ne scrive una.
	 */
	public static final Tattica VUOTA = new Tattica(Collections.emptyList());

	private final List<Voce> voci;

	public Tattica(List<Voce> voci) {
		this.voci = Collections.unmodifiableList(new ArrayList<>(voci));
	}

	public static Tattica di(Voce... voci) {
		return new Tattica(java.util.Arrays.asList(voci));
	}

	public List<Voce> getVoci() {
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
