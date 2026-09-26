package com.threeamigos.foresta.intermezzi;

import java.util.function.Supplier;

/**
 * Elenca le classi concrete di intermezzo, come ClasseMissione per le missioni. L'ordine
 * conta: se più intermezzi scattano nello stesso momento vengono mostrati in quest'ordine.
 */
public enum ClasseIntermezzo {

	// Solo in modalità di prova (vedi ModalitaDiProva)
	INTERMEZZO_DI_PROVA(IntermezzoDiProva::new, true);

	private final Supplier<Intermezzo> supplier;
	private final boolean diProva;

	ClasseIntermezzo(Supplier<Intermezzo> supplier, boolean diProva) {
		this.supplier = supplier;
		this.diProva = diProva;
	}

	/**
	 * Se l'intermezzo esiste solo in modalità di prova
	 */
	public boolean isDiProva() {
		return diProva;
	}

	public Intermezzo getIstanza() {
		return supplier.get();
	}
}
